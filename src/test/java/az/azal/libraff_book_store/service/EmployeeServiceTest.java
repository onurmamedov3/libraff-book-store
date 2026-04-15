package az.azal.libraff_book_store.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

	@Mock
	private EmployeeRepository repository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private PositionRepository positionRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmployeeWorkHistoryService employeeHistoryService;

	private EmployeeAddRequest addRequest;

	private StoreEntity fakeStore;

	private PositionEntity fakePosition;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private EmployeeService employeeService;

	@BeforeEach
	void addEmployee() {
		addRequest = new EmployeeAddRequest();
		addRequest.setName("Aslan");
		addRequest.setSurname("Mamedov");
		addRequest.setFIN("AZ12345");
		addRequest.setEmail("aslan.123@gmail.com");
		addRequest.setDateEmployed(LocalDate.of(2026, 2, 16));

		addRequest.setPositionId(1);
		addRequest.setStoreId(1);
		addRequest.setSalary(3000.0);
		addRequest.setPassword("password123");

		fakeStore = new StoreEntity();
		fakeStore.setId(1);
		fakeStore.setName("Main Store");

		fakePosition = new PositionEntity();
		fakePosition.setId(1);
		fakePosition.setMinSalary(2500.0);
		fakePosition.setMaxSalary(7500.0);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// findEmployeeById()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void findEmployeeById_Success_WhenEmployeeExists() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setName("Huseyn");

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		doAnswer(invocation -> {
			EmployeeEntity source = invocation.getArgument(0);
			EmployeeSingleResponse target = invocation.getArgument(1);
			target.setName(source.getName());
			return null;
		}).when(mapper).map(any(EmployeeEntity.class), any(EmployeeSingleResponse.class));

		EmployeeSingleResponse response = employeeService.findEmployeeById(1);

		assertNotNull(response);
		assertEquals("Huseyn", response.getName());

	}

	@Test
	void findEmployeeById_ShouldThrow_WhenEmployeeNotFound() {
		when(repository.findById(55)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.findEmployeeById(55));
		assertEquals(ErrorStatus.EMPLOYEE_NOT_FOUND.getMessage(), exception.getMessage());
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// getAllEmployees()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void getAllEmployees_ReturnEmployees_WhenEmployeesExist() {

		EmployeeEntity employee1 = new EmployeeEntity();
		employee1.setId(1);
		employee1.setName("Huseyn");

		EmployeeEntity employee2 = new EmployeeEntity();
		employee2.setId(2);
		employee2.setName("Mirtohid");

		when(repository.findAll()).thenReturn(List.of(employee1, employee2));

		doAnswer(invocation -> {
			EmployeeEntity source = invocation.getArgument(0);
			EmployeeSingleResponse target = invocation.getArgument(1);
			target.setName(source.getName());
			return null;
		}).when(mapper).map(any(EmployeeEntity.class), any(EmployeeSingleResponse.class));

		EmployeeListResponse result = employeeService.getAll();

		assertNotNull(result);
		assertEquals(2, result.getEmployees().size());
		assertEquals("Huseyn", result.getEmployees().get(0).getName());
		assertEquals("Mirtohid", result.getEmployees().get(1).getName());

		verify(repository, times(1)).findAll();

	}

	@Test
	void getAllBooks_ReturnsEmptyList_WhenNoEmployeesExist() {

		when(repository.findAll()).thenReturn(Collections.emptyList());

		EmployeeListResponse result = employeeService.getAll();

		assertNotNull(result);
		assertEquals(0, result.getEmployees().size());

		verify(repository, times(1)).findAll();
		verify(mapper, never()).map(any(), any());

	}

	// ═══════════════════════════════════════════════════════════════════════════
	// addEmployee()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void addEmployee_Success_WhenAllDataIsValid() {

		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.of(fakePosition));
		when(storeRepository.findById(1)).thenReturn(Optional.of(fakeStore));
		when(passwordEncoder.encode("password123"))
				.thenReturn("$2a$12$BQy9.JDLz41ygEqBPv6mOeY7N9yd66Y6vo53ypVljoBGWUUSIwEX6");

		doAnswer(invocation -> {
			EmployeeEntity employee = invocation.getArgument(1);
			employee.setName(addRequest.getName());
			employee.setPhone(addRequest.getPhone());
			return null;
		}).when(mapper).map(eq(addRequest), any(EmployeeEntity.class));

		doAnswer(invocation -> {
			EmployeeEntity savedEmployee = invocation.getArgument(0);
			savedEmployee.setId(100);
			return savedEmployee;
		}).when(repository).save(any(EmployeeEntity.class));

		doAnswer(invocation -> {
			EmployeeEntity source = invocation.getArgument(0);
			EmployeeAddResponse target = invocation.getArgument(1);
			target.setId(source.getId());
			target.setName(source.getName());
			return null;
		}).when(mapper).map(any(EmployeeEntity.class), any(EmployeeAddResponse.class));

		EmployeeAddResponse response = employeeService.add(addRequest);

		assertNotNull(response);
		assertEquals(100, response.getId());
		assertEquals("Aslan", response.getName());

		verify(repository, times(1)).save(any(EmployeeEntity.class));

		verify(passwordEncoder, times(1)).encode("password123");
	}

	@Test
	void addEmployee_ShouldThrow_WhenFINAlreadyExists() {

		when(repository.existsByFIN(addRequest.getFIN())).thenReturn(true);

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.DUPLICATE_FIN.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.DUPLICATE_FIN.getErrorCode(), exception.getErrorCode());
		assertEquals(ErrorStatus.DUPLICATE_FIN.getHttpStatus(), exception.getHttpStatus());

		verify(repository, never()).save(any());

	}

	@Test
	void addEmployee_ShouldThrow_WhenEmailAlreadyExists() {
		when(repository.existsByEmail(addRequest.getEmail())).thenReturn(true);

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.DUPLICATE_EMAIL.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.DUPLICATE_EMAIL.getErrorCode(), exception.getErrorCode());
		assertEquals(ErrorStatus.DUPLICATE_EMAIL.getHttpStatus(), exception.getHttpStatus());

		verify(repository, never()).save(any());
	}

	@Test
	void addEmployee_ShouldThrow_WhenPhoneAlreadyExists() {
		when(repository.existsByPhone(addRequest.getPhone())).thenReturn(true);

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.DUPLICATE_PHONE.getMessage(), exception.getMessage());
		assertEquals(ErrorStatus.DUPLICATE_PHONE.getErrorCode(), exception.getErrorCode());
		assertEquals(ErrorStatus.DUPLICATE_PHONE.getHttpStatus(), exception.getHttpStatus());

		verify(repository, never()).save(any());
	}

	@Test
	void addEmployee_ThrowsPositionNotFound() {

		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.POSITION_NOT_FOUND.getMessage(), exception.getMessage());

	}

	@Test
	void addEmployee_ThrowsStoreNotFound() {

		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.of(fakePosition));
		when(storeRepository.findById(1)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.STORE_NOT_FOUND.getMessage(), exception.getMessage());

	}

	@Test
	void addEmployee_WhenSalaryBelowMinimum() {
		addRequest.setSalary(100.0);

		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.of(fakePosition));

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.INVALID_SALARY.getMessage(), exception.getMessage());

	}

	@Test
	void addEmployee_WhenSalaryAboveMaximum() {
		addRequest.setSalary(10000.0);

		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.of(fakePosition));

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.INVALID_SALARY.getMessage(), exception.getMessage());

	}

	@Test
	void addEmployee_WhenPositionLimitExceeded() {
		checkDuplicates();

		when(positionRepository.findById(1)).thenReturn(Optional.of(fakePosition));
		when(storeRepository.findById(1)).thenReturn(Optional.of(fakeStore));

		when(repository.countByStoreIdAndPositionIdAndIsActiveTrue(1, 1)).thenReturn(99);

		MyException exception = assertThrows(MyException.class, () -> employeeService.add(addRequest));

		assertEquals(ErrorStatus.POSITION_LIMIT_EXCEEDED.getMessage(), exception.getMessage());

	}

	// ═══════════════════════════════════════════════════════════════════════════
	// deleteEmployeeById()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void deleteEmployee_Success_WhenEmployeeIsActive() {

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		employeeService.deleteEmployeeById(employee.getId());

		assertFalse(employee.getIsActive());
		verify(repository, times(1)).save(employee);
		verify(employeeHistoryService, times(1)).recordHistory(employee, false);

	}

	@Test
	void deleteEmployee_ShouldThrow_WhenEmployeeNotFound() {

		when(repository.findById(1)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.deleteEmployeeById(1));

		assertEquals(ErrorStatus.EMPLOYEE_NOT_FOUND.getMessage(), exception.getMessage());
	}

	@Test
	void deleteEmployee_ShouldThrow_WhenEmployeeAlreadyInactive() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setIsActive(false);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.deleteEmployeeById(1));

		assertEquals(ErrorStatus.ALREADY_INACTIVE.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());

	}

	// ═══════════════════════════════════════════════════════════════════════════
	// rehireEmployeeById()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void rehireEmployee_Success_WhenEmployeeIsInactive() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(false);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		employeeService.rehireEmployeeById(1);

		assertEquals(true, employee.getIsActive());
		verify(repository, times(1)).save(any());

	}

	@Test
	void rehireEmployee_ShouldThrow_WhenEmployeeNotFound() {

		when(repository.findById(1)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.rehireEmployeeById(1));

		assertEquals(ErrorStatus.EMPLOYEE_NOT_FOUND.getMessage(), exception.getMessage());
	}

	@Test
	void rehireEmployee_ShouldThrow_WhenEmployeeAlreadyActive() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.rehireEmployeeById(1));

		assertEquals(ErrorStatus.ALREADY_INACTIVE.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());
	}

	@Test
	void rehireEmployee_ShouldThrow_WhenPositionLimitExceeded() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setPosition(fakePosition);
		employee.setStore(fakeStore);
		employee.setIsActive(false);

		when(repository.findById(1)).thenReturn(Optional.of(employee));
		when(repository.countByStoreIdAndPositionIdAndIsActiveTrue(1, 1)).thenReturn(55);

		MyException exception = assertThrows(MyException.class, () -> employeeService.rehireEmployeeById(1));

		assertEquals(ErrorStatus.POSITION_LIMIT_EXCEEDED.getMessage(), exception.getMessage());
		verify(repository, never()).save(any());

	}

	// ═══════════════════════════════════════════════════════════════════════════
	// patchEmployee()
	// ═══════════════════════════════════════════════════════════════════════════

	@Test
	void patchEmployee_Success_WhenAllDataIsValid() {
		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setName("Razil");

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setName("Aslan");
		employee.setIsActive(true);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		assertDoesNotThrow(() -> employeeService.patchEmployee(updateRequest));

		verify(repository, times(1)).save(any(EmployeeEntity.class));

	}

	@Test
	void patchEmployee_ShouldThrow_WhenEmployeeNotFound() {
		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);

		when(repository.findById(1)).thenReturn(Optional.empty());

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.EMPLOYEE_NOT_FOUND.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());
	}

	@Test
	void patchEmployee_ShouldThrow_WhenEmployeeIsInactive() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setName("Zeynalabdin");

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setName("Firudin");
		employee.setIsActive(false);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.EMPLOYEE_INACTIVE.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());
	}

	@Test
	void patchEmployee_ShouldThrow_WhenFINIsChanged() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setFIN("AZ88888");

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setFIN("AZ00000");
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.IMMUTABLE_FIELD.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());
	}

	@Test
	void patchEmployee_ShouldThrow_WhenNewSalaryBelowPositionMinimum() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setSalary(100.0);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setSalary(4500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.INVALID_SALARY.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());

	}

	@Test
	void patchEmployee_ShouldThrow_WhenNewSalaryAbovePositionMaximum() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setSalary(10000.0);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		// employee.setSalary(4500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.INVALID_SALARY.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());

	}

	@Test
	void patchEmployee_ShouldThrow_WhenNewPositionSalaryIsInvalid() {

		PositionEntity position = new PositionEntity();
		position.setId(2);
		position.setMinSalary(950.0);
		position.setMaxSalary(6500.0);

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setPositionId(2);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setSalary(114500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));
		when(repository.countByStoreIdAndPositionIdAndIsActiveTrue(1, 2)).thenReturn(0);
		when(positionRepository.findById(2)).thenReturn(Optional.of(position));

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.INVALID_SALARY.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());

	}

	@Test
	void patchEmployee_ShouldThrow_WhenPositionLimitExceededAfterMove() {

		PositionEntity position = new PositionEntity();
		position.setId(2);
		position.setMinSalary(950.0);
		position.setMaxSalary(6500.0);

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setPositionId(2);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setSalary(4500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));
		when(repository.countByStoreIdAndPositionIdAndIsActiveTrue(1, 2)).thenReturn(55);

		MyException exception = assertThrows(MyException.class, () -> employeeService.patchEmployee(updateRequest));

		assertEquals(ErrorStatus.POSITION_LIMIT_EXCEEDED.getMessage(), exception.getMessage());

		verify(repository, never()).save(any());

	}

	@Test
	void patchEmployee_ShouldRecordHistory_WhenSalaryChanges() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setSalary(3000.0);

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setSalary(2500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		employeeService.patchEmployee(updateRequest);

		verify(employeeHistoryService, times(1)).recordHistory(any(EmployeeEntity.class), anyBoolean());

	}

	@Test
	void patchEmployee_ShouldNotRecordHistory_WhenNothingChanges() {

		EmployeeUpdateRequest updateRequest = new EmployeeUpdateRequest();
		updateRequest.setId(1);
		updateRequest.setName("Mirtohid");
		;

		EmployeeEntity employee = new EmployeeEntity();
		employee.setId(1);
		employee.setSalary(2500.0);
		employee.setStore(fakeStore);
		employee.setPosition(fakePosition);
		employee.setIsActive(true);

		when(repository.findById(1)).thenReturn(Optional.of(employee));

		employeeService.patchEmployee(updateRequest);

		verify(employeeHistoryService, never()).recordHistory(any(EmployeeEntity.class), anyBoolean());

	}

	private void checkDuplicates() {
		when(repository.existsByFIN(any())).thenReturn(false);
		when(repository.existsByEmail(any())).thenReturn(false);
		when(repository.existsByPhone(any())).thenReturn(false);
	}

}
