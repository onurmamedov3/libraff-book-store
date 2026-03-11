package az.azal.libraff_book_store.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.GradeStructureEntity;
import az.azal.libraff_book_store.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeService {

	private final TransactionHistoryRepository transactionHistoryRepository;

	// private final EmployeeRepository employeeRepository;

	// For employee
	public Double calculateTotalBonusForEmployee(EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd,
			List<GradeStructureEntity> gradeStructures) {

		LocalDate startDate;
		LocalDate endDate;

		switch (gradeStructures.get(0).getBonusFrequency()) {

		case MONTHLY -> {
			startDate = periodEnd.with(TemporalAdjusters.firstDayOfMonth());
			endDate = periodEnd.with(TemporalAdjusters.lastDayOfMonth());
		}
		case SEASONLY -> {
			int month = periodEnd.getMonthValue(); // return month in integer value: e.g. April -> 4 etc.
			int seasonStartMonth = ((month - 1) / 3) * 3 + 1; // formula to find start month of the season
			startDate = LocalDate.of(periodEnd.getYear(), seasonStartMonth, 1); // year, month, day
			endDate = startDate.plusMonths(3).minusDays(1); // 10.03.2026 - > 31.03.2026

		}
		case ANNUAL -> {
			startDate = periodEnd.with(TemporalAdjusters.firstDayOfYear());
			endDate = periodEnd.with(TemporalAdjusters.lastDayOfYear());
		}
		default -> {
			return 0.0;
		}
		}
		;

		return calculateBonus(employee.getId(), employee.getSalary(), true, startDate, endDate, gradeStructures);

	}

	// For store
	public Double calculateTotalBonusForStore(EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd,
			List<GradeStructureEntity> gradeStructures) {
		LocalDate startDate;
		LocalDate endDate;

		switch (gradeStructures.get(0).getBonusFrequency()) {

		case MONTHLY -> {
			startDate = periodEnd.with(TemporalAdjusters.firstDayOfMonth());
			endDate = periodEnd.with(TemporalAdjusters.lastDayOfMonth());
		}
		case SEASONLY -> {
			int month = periodEnd.getMonthValue(); // return month in integer value: e.g. April -> 4 etc.
			int seasonStartMonth = ((month - 1) / 3) * 3 + 1; // formula to find start month of the season
			startDate = LocalDate.of(periodEnd.getYear(), seasonStartMonth, 1); // year, month, day
			endDate = startDate.plusMonths(3).minusDays(1); // 10.03.2026 - > 31.05.2026

		}
		case ANNUAL -> {
			startDate = periodEnd.with(TemporalAdjusters.firstDayOfYear());
			endDate = periodEnd.with(TemporalAdjusters.lastDayOfYear());
		}
		default -> {
			return 0.0;
		}
		}
		;
		return calculateBonus(employee.getStore().getId(), employee.getSalary(), false, startDate, endDate,
				gradeStructures);
	}

	private Double calculateBonus(Integer targetId, Double employeeSalary, boolean isEmployee, LocalDate startDate,
			LocalDate endDate, List<GradeStructureEntity> gradeStructures) {

		if (gradeStructures == null || gradeStructures.isEmpty()) {
			return 0.0;
		}

		Double totalSales;

		if (isEmployee) {
			totalSales = transactionHistoryRepository.getTotalSalesByEmployeeAndDateRange(targetId, startDate, endDate);
		} else {
			totalSales = transactionHistoryRepository.getTotalSalesByStoreAndDateRange(targetId, startDate, endDate);
		}

		if (totalSales == null) {
			totalSales = 0.0;
		}

		final Double finalSales = totalSales;
		// if the statament reached here, it means no bonus is calculated
		// for the employee
		return gradeStructures.stream().filter(g -> finalSales >= g.getMinSalesThreshold())
				.max(Comparator.comparing(GradeStructureEntity::getMinSalesThreshold)).map(g -> {
					if (g.getBonusAmount() != null && g.getBonusAmount() > 0) {
						if (g.getBonusPercentage() != null && g.getBonusPercentage() > 0) {
							return g.getBonusAmount() + employeeSalary * (g.getBonusPercentage() / 100);
						}
						return g.getBonusAmount();
					}
					if (g.getBonusPercentage() != null && g.getBonusPercentage() > 0) {
						return employeeSalary * (g.getBonusPercentage() / 100);
					}

					return 0.0;
				}).orElse(0.0);

	}

}
