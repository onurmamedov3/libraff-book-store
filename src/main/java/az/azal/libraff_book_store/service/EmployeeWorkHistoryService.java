package az.azal.libraff_book_store.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.EmployeeWorkHistoryEntity;
import az.azal.libraff_book_store.repository.EmployeeWorkHistoryRepository;
import jakarta.transaction.Transactional;

@Service
public class EmployeeWorkHistoryService {

	@Autowired
	private EmployeeWorkHistoryRepository workHistoryRepository;

	@Transactional
	public void recordHistory(EmployeeEntity employee, boolean isStartingNewRole) {

		// 1. Close the current active chapter (if one exists)
		workHistoryRepository.findByEmployeeAndIsActiveTrue(employee).ifPresent(existing -> {
			existing.setEndDate(LocalDateTime.now());
			existing.setIsActive(false);
			workHistoryRepository.save(existing);
		});

		// 2. ONLY open a new timeline if they are actively working!
		if (isStartingNewRole) {
			EmployeeWorkHistoryEntity record = new EmployeeWorkHistoryEntity();
			record.setEmployee(employee);

			// This will capture the NEW store if you updated the employee before calling
			// this
			record.setStore(employee.getStore());
			record.setStartDate(LocalDateTime.now());
			record.setPosition(employee.getPosition());
			record.setSalary(employee.getSalary());

			record.setIsActive(true); // Always true if a new role is starting

			workHistoryRepository.save(record);
		}
	}

}
