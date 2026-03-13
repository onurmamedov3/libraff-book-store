package az.azal.libraff_book_store.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.DiscountEntity;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Integer> {

	@Query("SELECT d FROM DiscountEntity d " + "WHERE d.isActive = true "
			+ "AND :today BETWEEN d.discountStartDate AND d.discountEndDate " + "AND (" + "   d.book.id = :bookId OR "
			+ "   d.author.id IN :authorIds OR " + "   d.genre.id = :genreId OR "
			+ "   (d.store.id = :storeId OR d.store.id IS NULL)" + ") " + "ORDER BY d.discountPercentage DESC")
	List<DiscountEntity> findApplicableDiscounts(@Param("bookId") Integer bookId,
			@Param("authorIds") List<Integer> authorIds, @Param("genreId") Integer genreId,
			@Param("storeId") Integer storeId, @Param("today") LocalDate today);

}
