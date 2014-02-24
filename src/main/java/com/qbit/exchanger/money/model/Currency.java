package com.qbit.exchanger.money.model;

/**
 *
 * @author Александр
 */
public enum Currency {
	
	YANDEX_RUB(new Amount(0, 1, 100), "RUR"), 
	BITCOIN(new Amount(0, 1, 100 * 1000 * 1000), "BTC"),
	LITECOIN(new Amount(0, 1, 100 * 1000 * 1000), "LTC"),
	PEERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "PPC"),
	NAMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "NMC"),
	QUARKCOIN(new Amount(0, 1, 100 * 1000 * 1000), "QRK"),
	PRIMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "XPM"),
	NOVACOIN(new Amount(0, 1, 100 * 1000 * 1000), "NVC"),
	FEATHERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "FTC"),
	ZETACOIN(new Amount(0, 1, 100 * 1000 * 1000), "ZET"),
	DIGITALCOIN(new Amount(0, 1, 100 * 1000 * 1000), "DGC"),
	STABLECOIN(new Amount(0, 1, 100 * 1000 * 1000), "SBC");
	
	private final Amount minSignificantAmount;
	private final String code;
	
	private Currency(Amount minSignificantAmount, String code) {
		this.minSignificantAmount = minSignificantAmount;
		this.code = code;
	}
	
	public long getCentsInCoin() {
		return minSignificantAmount.getCentsInCoin();
	}

	public Amount getMinSignificantAmount() {
		return minSignificantAmount;
	}

	public String getCode() {
		return code;
	}
}
