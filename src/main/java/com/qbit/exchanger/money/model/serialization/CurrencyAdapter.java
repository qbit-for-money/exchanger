package com.qbit.exchanger.money.model.serialization;

import com.qbit.exchanger.money.model.Currency;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author alisherfaz
 */
public class CurrencyAdapter extends XmlAdapter<CurrencyAdapter.AdaptedCurrency, Currency>{
	
	@XmlRootElement
	public static class AdaptedCurrency {
		@XmlElement public String id;
		@XmlElement public String code;
	}

	@Override
	public Currency unmarshal(AdaptedCurrency adaptedCurrency) throws Exception {
		Currency result = null;
		if (adaptedCurrency != null) {
			result = Currency.valueOf(adaptedCurrency.id);
		}
		return result;
	}

	@Override
	public AdaptedCurrency marshal(Currency currency) throws Exception {
		AdaptedCurrency result = null;
		if (currency != null) {
			result.id = currency.name();
			result.code = currency.getCode();
		}
		return result;
	}
}
