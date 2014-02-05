package com.qbit.exchanger.money.model;

/**
 *
 * @author Александр
 */
public enum Currency {
	
	YANDEX_RUB(new Amount(0, 1, 100)), BITCOIN(new Amount(0, 1, 100 * 1000 * 1000));

	private final Amount minSignificantAmount;

	private Currency(Amount minSignificantAmount) {
		this.minSignificantAmount = minSignificantAmount;
	}

	public Amount getMinSignificantAmount() {
		return minSignificantAmount;
	}
}
