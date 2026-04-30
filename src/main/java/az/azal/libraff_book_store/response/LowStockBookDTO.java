package az.azal.libraff_book_store.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LowStockBookDTO {

	private Integer id;

	private String name;

	private Integer quantity;

	private String storeName;

}
