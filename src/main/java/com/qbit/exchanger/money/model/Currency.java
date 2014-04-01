package com.qbit.exchanger.money.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author Александр
 */
public enum Currency {
	
	YANDEX_RUB(new Amount(0, 1, 100), "RUR", true, false), 
	BITCOIN(new Amount(0, 1, 100 * 1000 * 1000), "BTC", true),
	LITECOIN(new Amount(0, 1, 100 * 1000 * 1000), "LTC", true),
	
	PEERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "PPC", false),
	NAMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "NMC", false),
	QUARKCOIN(new Amount(0, 1, 100 * 1000 * 1000), "QRK", false),
	PRIMECOIN(new Amount(0, 1, 100 * 1000 * 1000), "XPM", false),
	NOVACOIN(new Amount(0, 1, 100 * 1000 * 1000), "NVC", false),
	FEATHERCOIN(new Amount(0, 1, 100 * 1000 * 1000), "FTC", false),
	ZETACOIN(new Amount(0, 1, 100 * 1000 * 1000), "ZET", false),
	DIGITALCOIN(new Amount(0, 1, 100 * 1000 * 1000), "DGC", false),
	STABLECOIN(new Amount(0, 1, 100 * 1000 * 1000), "SBC", false);
	
	private static final EnumSet<Currency> SUPPORTED_VALUES;
	private static final EnumSet<Currency> SUPPORTED_CRYPTO_VALUES;
	private static final EnumSet<Currency> SUPPORTED_NONCRYPTO_VALUES;
	
	static {
		Collection<Currency> supportedValues = new ArrayList<>();
		for (Currency currency : values()) {
			if (currency.isSupported()) {
				supportedValues.add(currency);
			}
		}
		SUPPORTED_VALUES = EnumSet.copyOf(supportedValues);
		Collection<Currency> supportedCryptoValues = new ArrayList<>();
		Collection<Currency> supportedNoncryptoValues = new ArrayList<>();
		for (Currency currency : SUPPORTED_VALUES) {
			if (currency.isCrypto()) {
				supportedCryptoValues.add(currency);
			} else {
				supportedNoncryptoValues.add(currency);
			}
		}
		SUPPORTED_CRYPTO_VALUES = EnumSet.copyOf(supportedCryptoValues);
		SUPPORTED_NONCRYPTO_VALUES = EnumSet.copyOf(supportedNoncryptoValues);
	}
	
	public static EnumSet<Currency> supportedValues() {
		return SUPPORTED_VALUES;
	}
	
	public static EnumSet<Currency> supportedCryptoValues() {
		return SUPPORTED_CRYPTO_VALUES;
	}
	
	public static EnumSet<Currency> supportedNoncryptoValues() {
		return SUPPORTED_NONCRYPTO_VALUES;
	}
	
	private final Amount minSignificantAmount;
	private final String code;
	private final boolean supported;
	private final boolean crypto;
	
	private Currency(Amount minSignificantAmount, String code, boolean supported) {
		this(minSignificantAmount, code, supported, true);
	}
	
	private Currency(Amount minSignificantAmount, String code, boolean supported, boolean crypto) {
		this.minSignificantAmount = minSignificantAmount;
		this.code = code;
		this.supported = supported;
		this.crypto = crypto;
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

	public boolean isCrypto() {
		return crypto;
	}
}
