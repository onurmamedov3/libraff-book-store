package az.azal.libraff_book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import az.azal.libraff_book_store.entity.EmployeeEntity;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Integer> {

	boolean existsByFIN(String fin);

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

}
