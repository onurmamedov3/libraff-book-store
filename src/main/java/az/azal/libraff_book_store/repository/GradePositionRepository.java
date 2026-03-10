package az.azal.libraff_book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.GradePositionEntity;
import az.azal.libraff_book_store.entity.GradeStructureEntity;

@Repository
public interface GradePositionRepository extends JpaRepository<GradePositionEntity, Integer> {

	@Query("SELECT gp.gradeStructure FROM GradePositionEntity gp WHERE gp.position.id = :positionId")
	public GradeStructureEntity findByPositionId(Integer positionId);
}
