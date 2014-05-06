package com.qbit.exchanger.external.exchange.core;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.external.exchange.cryptsy.CryptsyExchange;
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
	private Env env;

	@Inject
	private CryptsyExchange cryptsyExchange;

	@Override
	public Rate getRate(Currency from, Currency to) throws Exception {
		try {
			return cryptsyExchange.getRate(from, to).mul(env.getRateMultiplier());
		} catch (Exception ex) {
			return cryptsyExchange.getRate(to, from).mul(env.getRateMultiplier()).inv();
		}
	}
}
