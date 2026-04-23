package az.azal.libraff_book_store.util;

import java.util.List;

import org.springframework.stereotype.Component;

import az.azal.libraff_book_store.entity.TransactionHistoryEntity;

@Component
public class BillPrinter {

	public String printBill(List<TransactionHistoryEntity> transactions) {

		StringBuilder sb = new StringBuilder();

		TransactionHistoryEntity first = transactions.get(0);

		sb.append("""

				============================================
				         LIBRAFF BOOK STORE
				============================================
				 Store     : %s
				 Employee  : %s
				 Date      : %s
				 Type      : %s
				--------------------------------------------
				""".formatted(first.getStore().getName(), first.getEmployee().getName(), first.getTransactionDate(),
				first.getTransactionType().name()));

		double grandTotal = 0.0;

		for (TransactionHistoryEntity t : transactions) {
			double lineTotal = t.getSalesPrice() * t.getQuantity();
			grandTotal += lineTotal;

			sb.append("""
					 Book      : %s
					 Quantity  : %d
					 Unit Price: $%.2f
					 Line Total: $%.2f
					--------------------------------------------
					""".formatted(t.getBook().getName(), t.getQuantity(), t.getSalesPrice(), lineTotal));
		}

		sb.append("""
				 GRAND TOTAL: $%.2f
				============================================
				   Thanks for shopping with us!
				============================================
				""".formatted(grandTotal));

		String bill = sb.toString();

		System.out.println(bill);

		return bill;
	}
}