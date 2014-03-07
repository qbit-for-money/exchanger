package com.qbit.exchanger.external.exchange.core;

import com.qbit.exchanger.external.exchange.btce.BTCExchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class ExchangeFacade implements Exchange {

	@Inject
	private BTCExchange btcExchange;

	@Override
	public Rate getRate(Currency from, Currency to) throws Exception {
		try {
			return btcExchange.getRate(from, to);
		} catch (Exception ex) {
			Rate invRate = btcExchange.getRate(to, from);
			return invRate.inv();
		}
	}
}
