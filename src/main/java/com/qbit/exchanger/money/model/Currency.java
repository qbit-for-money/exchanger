package com.qbit.exchanger.money.model;

/**
 *
 * @author Александр
 */
public enum Currency {
	
	YANDEX_RUB(new Amount(0, 1, 100), "RUR"), 
	BITCOIN(new Amount(0, 1, 100 * 1000 * 1000), "BTC");

	private final Amount minSignificantAmount;
	private final String code;
	
	private Currency(Amount minSignificantAmount, String code) {
		this.minSignificantAmount = minSignificantAmount;
		this.code = code;
	}

	public Amount getMinSignificantAmount() {
		return minSignificantAmount;
	}

	public String getCode() {
		return code;
	}
}
