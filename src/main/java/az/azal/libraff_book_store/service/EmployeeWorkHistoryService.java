package az.azal.libraff_book_store.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.EmployeeWorkHistoryEntity;
import az.azal.libraff_book_store.repository.EmployeeWorkHistoryRepository;

@Service
public class EmployeeWorkHistoryService {

	@Autowired
	private EmployeeWorkHistoryRepository workHistoryRepository;

	public void recordHistory(EmployeeEntity employee, boolean isActive) {

		workHistoryRepository.findByEmployeeAndIsActiveTrue(employee).ifPresent(existing -> {
			existing.setEndDate(LocalDateTime.now());
			existing.setIsActive(false);
			workHistoryRepository.save(existing);
		});

		EmployeeWorkHistoryEntity record = new EmployeeWorkHistoryEntity();
		record.setEmployee(employee);
		record.setStore(employee.getStore());
		record.setStartDate(LocalDateTime.now());
		record.setPosition(employee.getPosition());
		record.setSalary(employee.getSalary());
		record.setIsActive(isActive);

		workHistoryRepository.save(record);
	}

}
