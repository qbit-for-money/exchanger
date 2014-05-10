package com.qbit.exchanger.external.exchange.cryptsy;

import com.qbit.exchanger.common.model.Tuple2;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import static com.qbit.exchanger.rest.util.RESTClientUtil.*;

/**
 * @author Alexander_Sergeev
 */
@Singleton
public class CryptsyExchange implements Exchange {

	public static final String CRYPTSY_API_BASE_URL = "http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=";
	private static final Map<Tuple2<Currency, Currency>, String> CURRENCIES_MAP;

	static {
		CURRENCIES_MAP = new HashMap<>();
		CURRENCIES_MAP.put(new Tuple2(Currency.DOGECOIN, Currency.BITCOIN), "132");
		CURRENCIES_MAP.put(new Tuple2(Currency.DOGECOIN, Currency.LITECOIN), "135");
		CURRENCIES_MAP.put(new Tuple2(Currency.LITECOIN, Currency.BITCOIN), "3");
	}

	@Override
	public Rate getRate(Currency from, Currency to) throws Exception {
		if ((from == null) || (to == null)) {
			throw new IllegalArgumentException();
		}
		Rate rate = null;
		if (CURRENCIES_MAP.containsKey(new Tuple2<>(from, to))) {
			String id = CURRENCIES_MAP.get(new Tuple2<>(from, to));
			String lastTradePriceStr = getValue(CRYPTSY_API_BASE_URL + id, "", "lasttradeprice");
			if (lastTradePriceStr != null) {
				BigDecimal lastTradePrice = new BigDecimal(lastTradePriceStr);
				rate = new Rate(lastTradePrice, from, to);
			}
		}
		return rate;
	}
}
