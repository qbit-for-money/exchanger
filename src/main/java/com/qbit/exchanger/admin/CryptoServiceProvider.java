package com.qbit.exchanger.admin;

import com.qbit.exchanger.money.bitcoin.BitcoinMoneyService;
import com.qbit.exchanger.money.litecoin.LitecoinMoneyService;
import com.qbit.exchanger.money.model.Currency;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Alexander_Sergeev
 */
@Singleton
public class CryptoServiceProvider {
	@Inject
	private BitcoinMoneyService bitcoinService;

	@Inject
	private LitecoinMoneyService litecoinService;

	private Map<Currency, CryptoService> servicesMap;
	
	public CryptoService get(Currency currency) {
		if (currency == null) {
			throw new IllegalArgumentException("Illegal query.");
		}
		CryptoService moneyService = getServicesMap().get(currency);
		if (moneyService == null) {
			throw new UnsupportedOperationException("Currency not supported.");
		}
		return moneyService;
	}
	
	private synchronized Map<Currency, CryptoService> getServicesMap() {
		if (servicesMap == null) {
			servicesMap = new EnumMap<>(Currency.class);
			servicesMap.put(Currency.BITCOIN, bitcoinService);
			servicesMap.put(Currency.LITECOIN, litecoinService);
		}
		return servicesMap;
	}
}
