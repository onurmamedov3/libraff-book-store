package az.azal.libraff_book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.entity.EmployeeWorkHistoryEntity;

@Repository
public interface EmployeeWorkHistoryRepository extends JpaRepository<EmployeeWorkHistoryEntity, Integer> {

	Optional<EmployeeWorkHistoryEntity> findByEmployeeAndIsActiveTrue(EmployeeEntity employee);

}
