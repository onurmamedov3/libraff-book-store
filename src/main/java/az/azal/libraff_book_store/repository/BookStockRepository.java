package az.azal.libraff_book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.BookStockEntity;

@Repository
public interface BookStockRepository extends JpaRepository<BookStockEntity, Integer> {

	Optional<BookStockEntity> findByBookIdAndStoreId(Integer bookId, Integer storeId);

}
