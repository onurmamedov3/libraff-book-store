package az.azal.libraff_book_store.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, Integer> {

	// 1. Get total sales for a specific STORE within a date range
	@Query("SELECT COALESCE(SUM(t.salesPrice), 0) FROM TransactionHistoryEntity t " + "WHERE t.store.id = :storeId "
			+ "AND t.transactionDate BETWEEN :startDate AND :endDate")
	Double getTotalSalesByStoreAndDateRange(@Param("storeId") Integer storeId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	// 2. Get total sales for a specific EMPLOYEE within a date range
	@Query("SELECT COALESCE(SUM(t.salesPrice), 0) FROM TransactionHistoryEntity t "
			+ "WHERE t.employee.id = :employeeId " + "AND t.transactionDate BETWEEN :startDate AND :endDate")
	Double getTotalSalesByEmployeeAndDateRange(@Param("employeeId") Integer employeeId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
