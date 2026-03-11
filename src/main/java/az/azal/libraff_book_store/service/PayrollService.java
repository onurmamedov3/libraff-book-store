package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.GradeStructureEntity;
import az.azal.libraff_book_store.entity.SalaryHistoryEntity;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.repository.GradePositionRepository;
import az.azal.libraff_book_store.repository.GradeStoreRepository;
import az.azal.libraff_book_store.repository.SalaryHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // for logging
@Service
@RequiredArgsConstructor
public class PayrollService {

	private final SalaryHistoryRepository salaryHistoryRepository;

	private final EmployeeRepository employeeRepository;

	private final GradeService gradeService;

	private final GradePositionRepository gradePositionRepository;

	private final GradeStoreRepository gradeStoreRepository;

	// @Scheduled(cron = "0 0 0 1 * *") // Real: 1st day of every month
	@Scheduled(cron = "*/15 * * * * *") // Testing: Every 15 seconds
	@Transactional
	public void payMonthlySalary() {
		log.info("Starting automated payroll processing...");

		// YearMonth.now().minusMonths(1);
		String currentPeriod = YearMonth.now().toString();

		List<EmployeeEntity> employees = employeeRepository.findAllByIsActiveTrue();

		for (EmployeeEntity employee : employees) {
			try {
				processEmployeeSalary(employee, currentPeriod);
			} catch (Exception e) {
				log.error("Failed to pay Employee {}: {}", employee.getName(), e.getMessage());
			}
		}

		log.info("Payroll processing completed for period: {}", currentPeriod);
	}

	private void processEmployeeSalary(EmployeeEntity employee, String currentPeriod) {

		if (salaryHistoryRepository.existsByEmployeeAndPayPeriod(employee, currentPeriod)) {
			log.warn("Skipping Employee ID {}: Already paid for {}", employee.getId(), currentPeriod);
			return;
		}

		YearMonth yearMonth = YearMonth.parse(currentPeriod);
		LocalDate periodStart = yearMonth.atDay(1);
		LocalDate periodEnd = yearMonth.atEndOfMonth();

		if (employee.getDateEmployed().isAfter(periodEnd)) {
			log.warn("Skipping Employee ID {}: Employeed after current month {}", employee.getId(), currentPeriod);
			return;
		}

		Double salaryAmount = calculateSalary(employee, periodStart, periodEnd);

		Double employeeBonus = 0.0;
		Double storeBonus = 0.0;

		List<GradeStructureEntity> employeeGrades = gradePositionRepository
				.findAllGradesByPositionId(employee.getPosition().getId());

		if (employeeGrades != null) {
			employeeBonus = gradeService.calculateTotalBonusForEmployee(employee, periodStart, periodEnd,
					employeeGrades);
		}
		;

		List<GradeStructureEntity> storeGrades = gradeStoreRepository
				.findAllGradesByStoreId(employee.getStore().getId());

		if (storeGrades != null) {
			storeBonus = gradeService.calculateTotalBonusForStore(employee, periodStart, periodEnd, storeGrades);
		}

		Double totalBonus = employeeBonus + storeBonus;

		saveSalaryHistory(employee, salaryAmount, totalBonus, currentPeriod);
		log.info("Successfully processed salary for Employee: {}", employee.getName());
	}

	private Double calculateSalary(EmployeeEntity employee, LocalDate startOfMonth, LocalDate periodEnd) {

		LocalDate hireDate = employee.getDateEmployed();

		// If hired before this month started, give full salary.
		if (!hireDate.isAfter(startOfMonth)) { // alternative: hireDate.getDayOfMonth() == 1
			return employee.getSalary();
		}

		// If the employee is hired after 1st day of month
		LocalDate endOfMonth = hireDate.with(TemporalAdjusters.lastDayOfMonth());
		long daysWorked = ChronoUnit.DAYS.between(hireDate, endOfMonth) + 1;
		int totalDaysInMonth = startOfMonth.lengthOfMonth();

		double proRatedSalary = (employee.getSalary() / totalDaysInMonth) * daysWorked;

		// Round to 2 decimal places
		return Math.round(proRatedSalary * 100.0) / 100.0;
	}

	private void saveSalaryHistory(EmployeeEntity employee, Double salaryAmount, Double totalBonus,
			String currentPeriod) {
		SalaryHistoryEntity history = new SalaryHistoryEntity();

		history.setEmployee(employee);
		history.setSalaryAmount(salaryAmount);

		history.setBonusAmount(totalBonus);
		history.setTotalAmount(salaryAmount + totalBonus);

		history.setStore(employee.getStore());
		history.setPayPeriod(currentPeriod);
		history.setSalaryGivenDate(LocalDate.now());

		salaryHistoryRepository.save(history);
	}
}