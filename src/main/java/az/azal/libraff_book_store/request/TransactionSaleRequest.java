package az.azal.libraff_book_store.request;

import java.util.Map;

import lombok.Data;

@Data
public class TransactionSaleRequest {

	private Integer storeId;

	private Integer employeeId;

	private Map<Integer, Integer> soldBooks;

}
