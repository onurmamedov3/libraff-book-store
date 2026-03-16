package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.PositionEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.PositionRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.EmployeeAddRequest;
import az.azal.libraff_book_store.request.EmployeeUpdateRequest;
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import az.azal.libraff_book_store.response.EmployeeListResponse;
import az.azal.libraff_book_store.response.EmployeeSingleResponse;
import jakarta.transaction.Transactional;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepository repository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private EmployeeWorkHistoryService employeeHistoryService;

	@Transactional
	public EmployeeAddResponse add(EmployeeAddRequest request) {

		// 1. Uniqueness Validations

		if (repository.existsByFIN(request.getFIN())) {
			throw new MyException("Employee with this FIN already exists!", "DUPLICATE_FIN", HttpStatus.CONFLICT);
		}

		if (repository.existsByEmail(request.getEmail())) {
			throw new MyException("Employee with this Email already exists!", "DUPLICATE_EMAIL", HttpStatus.CONFLICT);
		}

		if (repository.existsByPhone(request.getPhone())) {
			throw new MyException("Employee with this Phone number already exists!", "DUPLICATE_PHONE",
					HttpStatus.CONFLICT);
		}

		// 2. Fetch Position and Validate Business Rules

		PositionEntity position = positionRepository.findById(request.getPositionId()).orElseThrow(
				() -> new MyException("Position not found!", "POSITION_NOT_FOUND", HttpStatus.BAD_REQUEST));

		if (request.getSalary() < position.getMinSalary() || request.getSalary() > position.getMaxSalary()) {
			throw new MyException("This salary is not valid for the current position!", "INVALID_SALARY",
					HttpStatus.BAD_REQUEST);
		}

		// 3. Fetch Optional Store Relationship

		StoreEntity storeEntity = null;

		if (request.getStoreId() != null) {
			storeEntity = storeRepository.findById(request.getStoreId())
					.orElseThrow(() -> new MyException("Store Not Found!", "STORE_NOT_FOUND", HttpStatus.NOT_FOUND));
		}

		// 4. Map Request DTO to Entity and Set Relationships

		EmployeeEntity employee = new EmployeeEntity();
		mapper.map(request, employee);
		employee.setStore(storeEntity);
		employee.setPosition(position);

		// 5. Save to Database

		repository.save(employee);

		// 6. Record employee history

		employeeHistoryService.recordHistory(employee, true);

		// 7. Construct and Map Response DTO

		EmployeeAddResponse response = new EmployeeAddResponse();

		if (employee.getStore() != null) {
			response.setStoreName(employee.getStore().getName());
		}

		mapper.map(employee, response);
		return response;
	}

	public EmployeeListResponse getAll() {

		List<EmployeeEntity> employees = repository.findAll();
		List<EmployeeSingleResponse> responseList = new ArrayList<EmployeeSingleResponse>();

		for (EmployeeEntity employee : employees) {
			EmployeeSingleResponse response = new EmployeeSingleResponse();
			mapper.map(employee, response);
			responseList.add(response);
		}

		EmployeeListResponse listResponse = new EmployeeListResponse();
		listResponse.setEmployees(responseList);
		return listResponse;
	}

	public EmployeeSingleResponse findEmployeeById(Integer id) {

		Optional<EmployeeEntity> optional = repository.findById(id);
		EmployeeEntity employee = null;

		if (optional.isPresent()) {
			employee = optional.get();
		} else {
			throw new MyException("Employee not found!", "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND);
		}

		EmployeeSingleResponse response = new EmployeeSingleResponse();
		mapper.map(employee, response);
		return response;

	}

	@Transactional
	public void deleteEmployeeById(Integer id) {

		EmployeeEntity employee = repository.findById(id)
				.orElseThrow(() -> new MyException("Employee not found!", "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND));

		if (!employee.getIsActive()) {
			throw new MyException("Employee is already inactive!", "ALREADY_INACTIVE", HttpStatus.CONFLICT);
		}

		employeeHistoryService.recordHistory(employee, false);

		employee.setIsActive(false);
		employee.setDateUnemployed(LocalDate.now());
		repository.save(employee);

	}

	@Transactional
	public void patchEmployee(EmployeeUpdateRequest updateRequest) {

		EmployeeEntity employee = repository.findById(updateRequest.getId())
				.orElseThrow(() -> new MyException("Employee not found", "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND));

		if (employee.getIsActive() != true) {
			throw new MyException("Employee is inactive. Cannot perform the operation", "INACTIVE_EMPLOYEE",
					HttpStatus.BAD_REQUEST);
		}

		// 1. Check if the user is attempting to change the immutable FIN
		if (updateRequest.getFIN() != null && !updateRequest.getFIN().equals(employee.getFIN())) {
			throw new MyException("FIN is immutable and cannot be changed!", "IMMUTABLE_FIELD", HttpStatus.BAD_REQUEST);
		}

		// Dəyişiklikləri izləmək üçün köhnə dəyərləri yadda saxlayırıq (Tarixçə üçün)
		Integer oldPositionId = employee.getPosition() != null ? employee.getPosition().getId() : null;
		Double oldSalary = employee.getSalary();
		Integer oldStoreId = employee.getStore() != null ? employee.getStore().getId() : null;

		// 2. TƏHLÜKƏSİZLİK ÜÇÜN LOKAL MAPPER YARADIRIQ
		// (Beləliklə digər API-lərin qlobal mapper konfiqurasiyası pozulmur)
		ModelMapper patchMapper = new ModelMapper();
		patchMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);

		// Yalnız null olmayan field-ləri köçürürük
		patchMapper.map(updateRequest, employee);

		boolean isHistoryChanged = false;

		// 3. Əlaqəli cədvəlləri (Position, Store) əllə yeniləyirik (Əgər request-də
		// göndərilibsə)
		if (updateRequest.getPositionId() != null && !updateRequest.getPositionId().equals(oldPositionId)) {
			PositionEntity newPosition = positionRepository.findById(updateRequest.getPositionId())
					.orElseThrow(() -> new MyException("Position not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

			Double salaryToCheck = (updateRequest.getSalary() != null) ? updateRequest.getSalary()
					: employee.getSalary();

			if (salaryToCheck != null) {
				if (salaryToCheck < newPosition.getMinSalary() || salaryToCheck > newPosition.getMaxSalary()) {
					throw new MyException("Salary is not valid for the new position range!", "INVALID_SALARY",
							HttpStatus.BAD_REQUEST);

				}
			}

			employee.setPosition(newPosition);
			isHistoryChanged = true;

		}

		// Əgər vəzifə DEYİŞMƏYİB, amma yalnız maaş DEYİŞİBSƏ (ayrıca yoxlama)
		if (updateRequest.getSalary() != null && !updateRequest.getSalary().equals(oldSalary)) {
			// Hazırkı vəzifənin limitlərini yoxla
			PositionEntity currentPos = employee.getPosition();
			if (updateRequest.getSalary() < currentPos.getMinSalary()
					|| updateRequest.getSalary() > currentPos.getMaxSalary()) {
				throw new MyException("New salary out of range for current position", "INVALID_SALARY",
						HttpStatus.BAD_REQUEST);
			}
			employee.setSalary(updateRequest.getSalary());
			isHistoryChanged = true;
		}

		if (updateRequest.getStoreId() != null && !updateRequest.getStoreId().equals(oldStoreId)) {
			StoreEntity newStore = storeRepository.findById(updateRequest.getStoreId())
					.orElseThrow(() -> new MyException("Store not found", "NOT_FOUND", HttpStatus.NOT_FOUND));
			employee.setStore(newStore);
		}

		// 4. Bazaya yadda saxlayırıq
		repository.save(employee);

		// 5. Tarixçəni (History) yoxlayıb yazırıq
		// Əgər maaş, vəzifə və ya mağaza dəyişibsə, yeni tarixçə yaradırıq

		if (updateRequest.getSalary() != null && !updateRequest.getSalary().equals(oldSalary))
			isHistoryChanged = true;

		if (updateRequest.getPositionId() != null && !updateRequest.getPositionId().equals(oldPositionId))
			isHistoryChanged = true;

		if (updateRequest.getStoreId() != null && !updateRequest.getStoreId().equals(oldStoreId))
			isHistoryChanged = true;

		if (isHistoryChanged) {
			employeeHistoryService.recordHistory(employee, employee.getIsActive());
		}
	}

	@Transactional
	public void rehireEmployeeById(Integer id) {

		EmployeeEntity employee = repository.findById(id)
				.orElseThrow(() -> new MyException("Employee not found!", "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND));

		if (employee.getIsActive()) {
			throw new MyException("Employee is already active!", "ALREADY_ACTIVE", HttpStatus.CONFLICT);
		}

		employeeHistoryService.recordHistory(employee, true);

		employee.setIsActive(true);
		employee.setDateUnemployed(null);

		repository.save(employee);
	}

}
