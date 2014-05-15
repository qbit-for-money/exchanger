package com.qbit.exchanger.external.exchange.btce;

import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import javax.inject.Singleton;
import static com.qbit.commons.rest.util.RESTClientUtil.*;

/**
 *
 * @author Александр
 */
@Singleton
public class BTCExchange implements Exchange {

	public final static String BTCE_API_BASE_URL = "https://btc-e.com/api/2/";

	@Override
	public Rate getRate(Currency from, Currency to) throws Exception {
		if ((from == null) || (to == null)) {
			throw new IllegalArgumentException();
		}
		String path = (from.getCode() + "_" + to.getCode() + "/ticker").toLowerCase();
		TickerResponse tickerResponse = get(BTCE_API_BASE_URL, path, TickerResponse.class, true);
		Ticker ticker = tickerResponse.getTicker();
		return new Rate(ticker.getBuy(), from, to);
	}
}
