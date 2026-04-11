package az.azal.libraff_book_store.util;

import java.util.Map;

public class PositionConstants {

	public static final int CASHIER = 1;

	public static final int SALES_REPRESENTATIVE = 2;

	public static final int CHIEF_SALES_REPRESENTATIVE = 3;

	public static final int STORE_MANAGER = 4;

	public static final Map<Integer, Integer> LIMITS = Map.of(CASHIER, 2, SALES_REPRESENTATIVE, 3,
			CHIEF_SALES_REPRESENTATIVE, 2, STORE_MANAGER, 1);

}
