package az.azal.libraff_book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.SalaryHistoryEntity;

@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistoryEntity, Integer> {

	boolean existsByEmployeeAndPayPeriod(EmployeeEntity employee, String currentPeriod);

}
