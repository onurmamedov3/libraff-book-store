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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		log.info("Adding new employee with FIN: {}", request.getFIN());

		// 1. Uniqueness Validations

		if (repository.existsByFIN(request.getFIN())) {
			log.warn("Duplicate FIN rejected: {}", request.getFIN());
			throw new MyException(ErrorStatus.DUPLICATE_FIN);
		}

		if (repository.existsByEmail(request.getEmail())) {
			log.warn("Duplicate Email rejected: {}", request.getEmail());
			throw new MyException(ErrorStatus.DUPLICATE_EMAIL);
		}

		if (repository.existsByPhone(request.getPhone())) {
			log.warn("Duplicate Phone rejected: {}", request.getPhone());
			throw new MyException(ErrorStatus.DUPLICATE_PHONE);
		}

		// 2. Fetch Position and Validate Business Rules
		log.debug("Fetching position with id: {}", request.getPositionId());

		PositionEntity position = positionRepository.findById(request.getPositionId()).orElseThrow(() -> {
			log.warn("Position not found with id: {}", request.getPositionId());
			return new MyException(ErrorStatus.POSITION_NOT_FOUND);
		});

		if (request.getSalary() < position.getMinSalary() || request.getSalary() > position.getMaxSalary()) {
			log.warn("Invalid salary {} for position '{}' (allowed: {}-{})", request.getSalary(), position.getId(),
					position.getMinSalary(), position.getMaxSalary());
			throw new MyException(ErrorStatus.INVALID_SALARY);
		}

		// 3. Fetch Optional Store Relationship

		StoreEntity storeEntity = null;

		if (request.getStoreId() != null) {
			log.debug("Fetching store with id: {}", request.getStoreId());
			storeEntity = storeRepository.findById(request.getStoreId()).orElseThrow(() -> {
				log.warn("Store not found with id: {}", request.getStoreId());
				return new MyException(ErrorStatus.STORE_NOT_FOUND);
			});
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
		log.debug("Employee entity saved with id: {}", employee.getId());

		// 7. Record employee history

		employeeHistoryService.recordHistory(employee, true);

		// 8. Construct and Map Response DTO

		EmployeeAddResponse response = new EmployeeAddResponse();

		if (employee.getStore() != null) {
			response.setStoreName(employee.getStore().getName());
		}

		mapper.map(employee, response);

		log.info("Employee successfully added with id: {} and FIN: {}", employee.getId(), employee.getFIN());

		return response;
	}

	public EmployeeListResponse getAll() {

		log.info("Fetching all employees");

		List<EmployeeEntity> employees = repository.findAll();

		log.debug("Total employees fetched from DB: {}", employees.size());

		List<EmployeeSingleResponse> responseList = new ArrayList<EmployeeSingleResponse>();

		for (EmployeeEntity employee : employees) {
			EmployeeSingleResponse response = new EmployeeSingleResponse();
			mapper.map(employee, response);
			responseList.add(response);
		}

		EmployeeListResponse listResponse = new EmployeeListResponse();
		listResponse.setEmployees(responseList);

		log.info("Returning {} employees", responseList.size());

		return listResponse;
	}

	public EmployeeSingleResponse findEmployeeById(Integer id) {

		log.info("Finding employee with id: {}", id);

		Optional<EmployeeEntity> optional = repository.findById(id);
		EmployeeEntity employee = null;

		if (optional.isPresent()) {
			employee = optional.get();
		} else {
			throw new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND);
		}

		EmployeeSingleResponse response = new EmployeeSingleResponse();
		mapper.map(employee, response);

		log.debug("Employee found: id={}, FIN={}", employee.getId(), employee.getFIN());

		return response;

	}

	@Transactional
	public void deleteEmployeeById(Integer id) {

		log.info("Firing employee with id: {}", id);

		EmployeeEntity employee = repository.findById(id).orElseThrow(() -> {
			log.warn("Cannot fire — employee not found with id: {}", id);
			return new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND);
		});

		if (!employee.getIsActive()) {
			log.warn("Cannot fire — employee id: {} is already inactive", id);
			throw new MyException(ErrorStatus.ALREADY_INACTIVE);
		}

		employeeHistoryService.recordHistory(employee, false);

		employee.setIsActive(false);
		employee.setDateUnemployed(LocalDate.now());
		repository.save(employee);

		log.info("Employee successfully fired with id: {}", employee.getId());

	}

	@Transactional
	public void patchEmployee(EmployeeUpdateRequest updateRequest) {

		log.info("Patching employee with id: {}", updateRequest.getId());

		EmployeeEntity employee = repository.findById(updateRequest.getId()).orElseThrow(() -> {
			log.warn("Cannot patch — employee not found with id: {}", updateRequest.getId());
			return new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND);
		});

		if (employee.getIsActive() != true) {
			log.warn("Cannot patch — employee id: {} is inactive", updateRequest.getId());
			throw new MyException(ErrorStatus.EMPLOYEE_INACTIVE);
		}

		// 1. Check if the user is attempting to change the immutable FIN
		if (updateRequest.getFIN() != null && !updateRequest.getFIN().equals(employee.getFIN())) {
			log.warn("Attempt to change immutable FIN for employee id: {}", employee.getId());
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
			log.debug("Store or position changed for employee id: {} — checking position limits", employee.getId());
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

			log.debug("Position change detected for employee id: {} ({} → {})", employee.getId(), oldPositionId,
					updateRequest.getPositionId());

			PositionEntity newPosition = positionRepository.findById(updateRequest.getPositionId()).orElseThrow(() -> {
				log.warn("New position not found with id: {}", updateRequest.getPositionId());
				return new MyException(ErrorStatus.POSITION_NOT_FOUND);
			});

			Double salaryToCheck = (updateRequest.getSalary() != null) ? updateRequest.getSalary()
					: employee.getSalary();

			if (salaryToCheck != null) {
				if (salaryToCheck < newPosition.getMinSalary() || salaryToCheck > newPosition.getMaxSalary()) {

					log.warn("Salary {} out of range for new position '{}' (allowed: {}-{})", salaryToCheck,
							newPosition.getId(), newPosition.getMinSalary(), newPosition.getMaxSalary());

					throw new MyException(ErrorStatus.INVALID_SALARY);

				}
			}

			employee.setPosition(newPosition);
			isHistoryChanged = true;

		}

		// If the position has NOT CHANGED, but only the salary has CHANGED (separate
		// verification)
		if (updateRequest.getSalary() != null && !updateRequest.getSalary().equals(oldSalary)) {

			log.debug("Salary change detected for employee id: {} ({} → {})", employee.getId(), oldSalary,
					updateRequest.getSalary());
			// Check the limits of the current task
			PositionEntity currentPos = employee.getPosition();
			if (updateRequest.getSalary() < currentPos.getMinSalary()
					|| updateRequest.getSalary() > currentPos.getMaxSalary()) {
				log.warn("Salary {} exceeds position limits for employee id: {}", updateRequest.getSalary(),
						employee.getId());
				throw new MyException(ErrorStatus.INVALID_SALARY);
			}
			employee.setSalary(updateRequest.getSalary());
			isHistoryChanged = true;
		}

		if (updateRequest.getStoreId() != null && !updateRequest.getStoreId().equals(oldStoreId)) {

			log.debug("Store change detected for employee id: {} ({} → {})", employee.getId(), oldStoreId,
					updateRequest.getStoreId());

			StoreEntity newStore = storeRepository.findById(updateRequest.getStoreId()).orElseThrow(() -> {
				log.warn("New store not found with id: {}", updateRequest.getStoreId());
				return new MyException(ErrorStatus.STORE_NOT_FOUND);
			});

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
			log.debug("History-worthy changes detected — recording history for employee id: {}", employee.getId());
			employeeHistoryService.recordHistory(employee, employee.getIsActive());
		}

		log.info("Employee id: {} successfully patched", employee.getId());
	}

	@Transactional
	public void rehireEmployeeById(Integer id) {

		log.info("Rehiring employee with id: {}", id);

		EmployeeEntity employee = repository.findById(id).orElseThrow(() -> {
			log.warn("Cannot rehire — employee not found with id: {}", id);
			return new MyException(ErrorStatus.EMPLOYEE_NOT_FOUND);
		});

		if (employee.getIsActive()) {
			log.warn("Rehire rejected — employee id: {} is already active", id);
			throw new MyException(ErrorStatus.ALREADY_INACTIVE);
		}

		// Check if bringing this employee back exceeds the limit
		if (employee.getStore() != null && employee.getPosition() != null) {
			log.debug("Checking position limits before rehiring employee id: {}", id);
			checkPositionLimit(employee.getStore().getId(), employee.getPosition().getId());
		}

		employeeHistoryService.recordHistory(employee, true);

		employee.setIsActive(true);
		employee.setDateUnemployed(null);

		repository.save(employee);

		log.info("Employee rehired successfully with id: {}", employee.getId());
	}

	private void checkPositionLimit(Integer storeId, Integer positionId) {
		if (storeId == null || positionId == null) {
			log.debug("checkPositionLimit skipped — storeId or positionId is null");
			return;
		}
		Integer limit = PositionConstants.LIMITS.get(positionId);

		if (limit != null) {
			int currentActiveCount = repository.countByStoreIdAndPositionIdAndIsActiveTrue(storeId, positionId);

			log.debug("Position id: {} in store id: {} — active: {}/{}", positionId, storeId, currentActiveCount,
					limit);

			if (currentActiveCount >= limit) {

				log.warn("Position limit reached — positionId: {}, storeId: {}, limit: {}", positionId, storeId, limit);

				throw new MyException(ErrorStatus.POSITION_LIMIT_EXCEEDED);
			}
		}
	}

}
