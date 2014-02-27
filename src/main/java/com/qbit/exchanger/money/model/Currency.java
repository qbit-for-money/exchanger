package com.qbit.exchanger.money.model;

/**
 *
 * @author Александр
 */
public enum Currency {

	YANDEX_RUB(new Amount(0, 1, 100), "RUR", true),
	BITCOIN(new Amount(0, 1, 100 * 1000 * 1000), "BTC", false),
	LITECOIN(new Amount(0, 1, 100 * 1000 * 1000), "LTC", false),
	PEERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "PPC", false),
	NAMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "NMC", false),
	QUARKCOIN(new Amount(0, 1, 100 * 1000 * 1000), "QRK", false),
	PRIMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "XPM", false),
	NOVACOIN(new Amount(0, 1, 100 * 1000 * 1000), "NVC", false),
	FEATHERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "FTC", false),
	ZETACOIN(new Amount(0, 1, 100 * 1000 * 1000), "ZET", false),
	DIGITALCOIN(new Amount(0, 1, 100 * 1000 * 1000), "DGC", false),
	STABLECOIN(new Amount(0, 1, 100 * 1000 * 1000), "SBC", false);

	private final Amount minSignificantAmount;
	private final String code;
	private final boolean supported;

	private Currency(Amount minSignificantAmount, String code, boolean supported) {
		this.minSignificantAmount = minSignificantAmount;
		this.code = code;
		this.supported = supported;
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

	public boolean isSupported() {
		return supported;
	}
}
