package az.azal.libraff_book_store.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;
import az.azal.libraff_book_store.enums.TransactionType;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, Integer> {

	// 1. Get total sales for a specific STORE within a date range
	@Query("SELECT COALESCE(SUM(t.salesPrice), 0) FROM TransactionHistoryEntity t " + "WHERE t.store.id = :storeId "
			+ "AND t.transactionType = az.azal.libraff_book_store.enums.TransactionType.SALE "
			+ "AND t.transactionDate BETWEEN :startDate AND :endDate")
	Double getTotalSalesByStoreAndDateRange(@Param("storeId") Integer storeId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	// 2. Get total sales for a specific EMPLOYEE within a date range
	@Query("SELECT COALESCE(SUM(t.salesPrice), 0) FROM TransactionHistoryEntity t "
			+ "WHERE t.employee.id = :employeeId "
			+ "AND t.transactionType = az.azal.libraff_book_store.enums.TransactionType.SALE "
			+ "AND t.transactionDate BETWEEN :startDate AND :endDate")
	Double getTotalSalesByEmployeeAndDateRange(@Param("employeeId") Integer employeeId,
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// Filtered by type AND date range
	List<TransactionHistoryEntity> findByTransactionTypeAndTransactionDateBetween(TransactionType type,
			LocalDateTime start, LocalDateTime end);

	// For combined report — date range only
	List<TransactionHistoryEntity> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

}
