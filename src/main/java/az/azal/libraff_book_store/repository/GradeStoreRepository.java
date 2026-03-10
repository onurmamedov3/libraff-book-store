package az.azal.libraff_book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.GradeStoreEntity;
import az.azal.libraff_book_store.entity.GradeStructureEntity;

@Repository
public interface GradeStoreRepository extends JpaRepository<GradeStoreEntity, Integer> {

	@Query("SELECT gp.gradeStructure FROM GradeStoreEntity gp WHERE gp.store.id = :storeId")
	public GradeStructureEntity findByStoreId(Integer storeId);

}
