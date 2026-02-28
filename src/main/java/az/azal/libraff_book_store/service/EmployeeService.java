package az.azal.libraff_book_store.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
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
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import az.azal.libraff_book_store.response.EmployeeListResponse;
import az.azal.libraff_book_store.response.EmployeeSingleResponse;

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

		// 6. Construct and Map Response DTO

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

}
