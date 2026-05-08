package az.azal.libraff_book_store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Integer> {

	boolean existsByFIN(String fin);

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

	List<EmployeeEntity> findAllByIsActiveTrue();

	int countByStoreIdAndPositionIdAndIsActiveTrue(Integer storeId, Integer positionId);

	Optional<EmployeeEntity> findByFIN(String fin);

	Optional<EmployeeEntity> findByStoreIdAndPositionIdAndIsActive(Integer storeId, Integer positionId,
			Boolean isActive);

	@Query("SELECT e.email FROM EmployeeEntity e WHERE e.FIN=:fin")
	String findEmailByFIN(@Param("fin") String fin);

	Optional<EmployeeEntity> findByFINAndEmail(String fin, String email);
}
