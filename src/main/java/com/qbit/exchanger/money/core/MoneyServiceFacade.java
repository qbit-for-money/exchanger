package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.bitcoin.Bitcoin;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.yandex.YandexMoneyService;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
@Named("moneyServiceFacade")
public class MoneyServiceFacade implements MoneyService {

	@Inject
	private Bitcoin bitcoinService;
	
	@Inject
	private YandexMoneyService yandexMoneyService;

	private Map<Currency, MoneyService> servicesMap;

	@Override
	public void process(Transfer transfer, MoneyTransferCallback callback) {
		MoneyService moneyService = getMoneyService(transfer);
		moneyService.process(transfer, callback);
	}

	@Override
	public void test(Transfer transfer, MoneyTransferCallback callback) {
		MoneyService moneyService = getMoneyService(transfer);
		moneyService.process(transfer, callback);
	}

	private MoneyService getMoneyService(Transfer transfer) {
		if ((transfer == null) || !transfer.isValid()) {
			throw new IllegalArgumentException("Illegal transfer.");
		}
		MoneyService moneyService = getServicesMap().get(transfer.getCurrency());
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
		}
		return servicesMap;
	}
}
