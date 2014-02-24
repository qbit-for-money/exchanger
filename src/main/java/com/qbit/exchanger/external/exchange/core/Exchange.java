package com.qbit.exchanger.external.exchange.core;

import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;

/**
 *
 * @author Александр
 */
public interface Exchange {
	
	Rate getRate(Currency from, Currency to) throws Exception;
}
