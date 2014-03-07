package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.bitcoin.BitcoinMoneyService;
import com.qbit.exchanger.money.litecoin.LitecoinMoneyService;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.yandex.YandexMoneyService;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class MoneyServiceProvider {

	@Inject
	private BitcoinMoneyService bitcoinService;
	
	@Inject
	private YandexMoneyService yandexMoneyService;
        
        @Inject
	private LitecoinMoneyService litecoinService;

	private Map<Currency, MoneyService> servicesMap;

	public MoneyService get(Transfer transfer) {
		if ((transfer == null) || !transfer.isValid()) {
			throw new IllegalArgumentException("Illegal transfer.");
		}
		return get(transfer.getCurrency());
	}

	public MoneyService get(Currency currency) {
		if (currency == null) {
			throw new IllegalArgumentException("Illegal transfer.");
		}
		MoneyService moneyService = getServicesMap().get(currency);
		if (moneyService == null) {
			throw new UnsupportedOperationException("Currency not supported.");
		}
		return moneyService;
	}

	private synchronized Map<Currency, MoneyService> getServicesMap() {
		if (servicesMap == null) {
			servicesMap = new HashMap<Currency, MoneyService>();
			servicesMap.put(Currency.BITCOIN, bitcoinService);
			servicesMap.put(Currency.YANDEX_RUB, yandexMoneyService);
			servicesMap.put(Currency.LITECOIN, litecoinService);
		}
		return servicesMap;
	}
}
