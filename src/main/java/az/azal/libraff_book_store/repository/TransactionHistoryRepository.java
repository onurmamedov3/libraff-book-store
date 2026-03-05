package az.azal.libraff_book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, Integer> {

}
