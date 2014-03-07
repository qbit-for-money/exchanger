package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.bitcoin.Bitcoin;
import com.qbit.exchanger.money.litecoin.Litecoin;
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
	private Bitcoin bitcoinService;
	
	@Inject
	private Litecoin litecoinService;

	@Inject
	private YandexMoneyService yandexMoneyService;

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
			servicesMap = new HashMap<>();
			servicesMap.put(Currency.BITCOIN, bitcoinService);
			servicesMap.put(Currency.YANDEX_RUB, yandexMoneyService);
			servicesMap.put(Currency.LITECOIN, litecoinService);
		}
		return servicesMap;
	}
}
