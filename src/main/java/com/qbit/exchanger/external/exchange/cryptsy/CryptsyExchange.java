package com.qbit.exchanger.external.exchange.cryptsy;

import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import static com.qbit.exchanger.external.exchange.cryptsy.CryptsyExchangeRestClient.*;

/**
 * @author Alexander_Sergeev
 */
@Singleton
public class CryptsyExchange implements Exchange {

	public final static String CRYPTSY_API_BASE_URL = "http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=";
	public Map<String, String> currensiesMap;

	public CryptsyExchange() {
		currensiesMap = new HashMap<>();
		currensiesMap.put("DOGE_BTC", "132");
		currensiesMap.put("DOGE_LTC", "135");
		currensiesMap.put("LTC_BTC", "3");
	}

	@Override
	public Rate getRate(Currency from, Currency to) throws Exception {
		if ((from == null) || (to == null)) {
			throw new IllegalArgumentException();
		}

		Rate rate = null;
		if (currensiesMap.containsKey(from.getCode() + "_" + to.getCode())) {
			String id = currensiesMap.get(from.getCode() + "_" + to.getCode());
			
			CryptsyRateResponse rateResponse = get(CRYPTSY_API_BASE_URL + id, "", CryptsyRateResponse.class, true);
			BigDecimal lastTradePrice = rateResponse.getReturnMarket().getMarkets().getCurrency().getLastTradePrice();
			rate = new Rate(lastTradePrice.multiply(new BigDecimal("100000")), new BigDecimal("100000"), from, to);
		}
		return rate;
	}
}
