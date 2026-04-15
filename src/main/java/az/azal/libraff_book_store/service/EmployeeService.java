package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.PositionEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.PositionRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.EmployeeAddRequest;
import az.azal.libraff_book_store.request.EmployeeUpdateRequest;
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import az.azal.libraff_book_store.response.EmployeeListResponse;
import az.azal.libraff_book_store.response.EmployeeSingleResponse;
import az.azal.libraff_book_store.util.PositionConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

	private final EmployeeRepository repository;

	private final StoreRepository storeRepository;

	private final PositionRepository positionRepository;

	private final ModelMapper mapper;

	private final EmployeeWorkHistoryService employeeHistoryService;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public EmployeeAddResponse add(EmployeeAddRequest request) {

		// 1. Uniqueness Validations

		if (repository.existsByFIN(request.getFIN())) {
			throw new MyException(ErrorStatus.DUPLICATE_FIN);
		}

		if (repository.existsByEmail(request.getEmail())) {
			throw new MyException(ErrorStatus.DUPLICATE_EMAIL);
		}

		if (repository.existsByPhone(request.getPhone())) {
			throw new MyException(ErrorStatus.DUPLICATE_PHONE);
		}

		// 2. Fetch Position and Validate Business Rules

		PositionEntity position = positionRepository.findById(request.getPositionId())
				.orElseThrow(() -> new MyException(ErrorStatus.POSITION_NOT_FOUND));

		if (request.getSalary() < position.getMinSalary() || request.getSalary() > position.getMaxSalary()) {
			throw new MyException(ErrorStatus.INVALID_SALARY);
		}

		// 3. Fetch Optional Store Relationship

		StoreEntity storeEntity = null;

		if (request.getStoreId() != null) {
			storeEntity = storeRepository.findById(request.getStoreId())
					.orElseThrow(() -> new MyException(ErrorStatus.STORE_NOT_FOUND));
		}

		// 4. Check position count limits

		checkPositionLimit(storeEntity.getId(), position.getId());

		// 5. Map Request DTO to Entity and Set Relationships

		EmployeeEntity employee = new EmployeeEntity();
		mapper.map(request, employee);
		employee.setPassword(passwordEncoder.encode(request.getPassword()));
		employee.setStore(storeEntity);
		employee.setPosition(position);

		// 6. Save to Database

		repository.save(employee);

		// 7. Record employee history

		employeeHistoryService.recordHistory(employee, true);

		// 8. Construct and Map Response DTO

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
			throw new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND);
		}

		EmployeeSingleResponse response = new EmployeeSingleResponse();
		mapper.map(employee, response);
		return response;

	}

	@Transactional
	public void deleteEmployeeById(Integer id) {

		EmployeeEntity employee = repository.findById(id)
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (!employee.getIsActive()) {
			throw new MyException(ErrorStatus.ALREADY_INACTIVE);
		}

		employeeHistoryService.recordHistory(employee, false);

		employee.setIsActive(false);
		employee.setDateUnemployed(LocalDate.now());
		repository.save(employee);

	}

	@Transactional
	public void patchEmployee(EmployeeUpdateRequest updateRequest) {

		EmployeeEntity employee = repository.findById(updateRequest.getId())
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (employee.getIsActive() != true) {
			throw new MyException(ErrorStatus.EMPLOYEE_INACTIVE);
		}

		// 1. Check if the user is attempting to change the immutable FIN
		if (updateRequest.getFIN() != null && !updateRequest.getFIN().equals(employee.getFIN())) {
			throw new MyException(ErrorStatus.IMMUTABLE_FIELD);
		}

		// We save the old values to track changes (for the history)
		Integer oldPositionId = employee.getPosition() != null ? employee.getPosition().getId() : null;
		Double oldSalary = employee.getSalary();
		Integer oldStoreId = employee.getStore() != null ? employee.getStore().getId() : null;

		// Determine the effective store and position after the patch
		Integer newStoreId = updateRequest.getStoreId() != null ? updateRequest.getStoreId() : oldStoreId;
		Integer newPositionId = updateRequest.getPositionId() != null ? updateRequest.getPositionId() : oldPositionId;

		// Only check limits if the employee is moving to a NEW store or getting a
		// NEW position
		if (!newStoreId.equals(oldStoreId) || !newPositionId.equals(oldPositionId)) {
			checkPositionLimit(newStoreId, newPositionId);
		}

		// 2. CREATE A LOCAL MAPPER FOR SECURITY
		ModelMapper patchMapper = new ModelMapper();
		patchMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);

		// We only map non-null fields
		patchMapper.map(updateRequest, employee);

		boolean isHistoryChanged = false;

		// 3. Manually update the related tables (Position, Store) (if sent in the
		// request)
		if (updateRequest.getPositionId() != null && !updateRequest.getPositionId().equals(oldPositionId)) {
			PositionEntity newPosition = positionRepository.findById(updateRequest.getPositionId())
					.orElseThrow(() -> new MyException(ErrorStatus.POSITION_NOT_FOUND));

			Double salaryToCheck = (updateRequest.getSalary() != null) ? updateRequest.getSalary()
					: employee.getSalary();

			if (salaryToCheck != null) {
				if (salaryToCheck < newPosition.getMinSalary() || salaryToCheck > newPosition.getMaxSalary()) {
					throw new MyException(ErrorStatus.INVALID_SALARY);

				}
			}

			employee.setPosition(newPosition);
			isHistoryChanged = true;

		}

		// If the position has NOT CHANGED, but only the salary has CHANGED (separate
		// verification)
		if (updateRequest.getSalary() != null && !updateRequest.getSalary().equals(oldSalary)) {
			// Check the limits of the current task
			PositionEntity currentPos = employee.getPosition();
			if (updateRequest.getSalary() < currentPos.getMinSalary()
					|| updateRequest.getSalary() > currentPos.getMaxSalary()) {
				throw new MyException(ErrorStatus.INVALID_SALARY);
			}
			employee.setSalary(updateRequest.getSalary());
			isHistoryChanged = true;
		}

		if (updateRequest.getStoreId() != null && !updateRequest.getStoreId().equals(oldStoreId)) {
			StoreEntity newStore = storeRepository.findById(updateRequest.getStoreId())
					.orElseThrow(() -> new MyException(ErrorStatus.STORE_NOT_FOUND));
			employee.setStore(newStore);
		}

		// 4. Save employee to
		repository.save(employee);

		// 5. Check and write the history
		// If the salary, position, or store has changed, create a new history

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
				.orElseThrow(() -> new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (employee.getIsActive()) {
			throw new MyException(ErrorStatus.ALREADY_INACTIVE);
		}

		// Check if bringing this employee back exceeds the limit
		if (employee.getStore() != null && employee.getPosition() != null) {
			checkPositionLimit(employee.getStore().getId(), employee.getPosition().getId());
		}

		employeeHistoryService.recordHistory(employee, true);

		employee.setIsActive(true);
		employee.setDateUnemployed(null);

		repository.save(employee);
	}

	private void checkPositionLimit(Integer storeId, Integer positionId) {
		if (storeId == null || positionId == null)
			return;

		Integer limit = PositionConstants.LIMITS.get(positionId);

		if (limit != null) {
			int currentActiveCount = repository.countByStoreIdAndPositionIdAndIsActiveTrue(storeId, positionId);

			if (currentActiveCount >= limit) {
				throw new MyException(ErrorStatus.POSITION_LIMIT_EXCEEDED);
			}
		}
	}

}
