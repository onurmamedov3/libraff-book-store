package az.azal.libraff_book_store.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.EmployeeAddRequest;
import az.azal.libraff_book_store.response.EmployeeAddResponse;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepository repository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private ModelMapper mapper;

	public EmployeeAddResponse add(EmployeeAddRequest request) {
		EmployeeEntity employee = new EmployeeEntity();
		mapper.map(request, employee);

		if (repository.existsByFIN(employee.getFIN())) {
			throw new MyException("Employee with this FIN already exists!", "DUPLICATE FIN", HttpStatus.CONFLICT);
		}

		if (repository.existsByEmail(employee.getEmail())) {
			throw new MyException("Employee with this Email already exists!", "DUPLICATE EMAIL", HttpStatus.CONFLICT);
		}

		if (repository.existsByPhone(employee.getPhone())) {
			throw new MyException("Employee with this Phone number already exists!", "DUPLICATE PHONE",
					HttpStatus.CONFLICT);
		}

		if (request.getStoreId() != null) {
			StoreEntity storeEntity = storeRepository.findById(request.getStoreId())
					.orElseThrow(() -> new EntityNotFoundException("Store Not Found!"));

			employee.setStore(storeEntity);
		}

		repository.save(employee);

		EmployeeAddResponse response = new EmployeeAddResponse();

		if (employee.getStore() != null) {
			response.setStoreName(employee.getStore().getName());
		}

		mapper.map(employee, response);
		return response;
	}

}
