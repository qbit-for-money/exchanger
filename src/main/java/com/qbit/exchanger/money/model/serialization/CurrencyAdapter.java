package com.qbit.exchanger.money.model.serialization;

import com.qbit.exchanger.money.model.Currency;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author alisherfaz
 */
public class CurrencyAdapter extends XmlAdapter<CurrencyAdapter.AdaptedCurrency, Currency> {
	
	public static class  AdaptedCurrency {
	
		private String id;
		private String code;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}	
	}
	
	@Override
	public Currency unmarshal(AdaptedCurrency adaptedCurrency) throws Exception {
		Currency result = null;
		if (adaptedCurrency != null) {
			result = Currency.valueOf(adaptedCurrency.getId());
		}
		return result;
	}

	@Override
	public AdaptedCurrency marshal(Currency currency) throws Exception {
		AdaptedCurrency result = null;
		if (currency != null) {
			result = new AdaptedCurrency();
			result.setId(currency.name());
			result.setCode(currency.getCode());
		}
		return result;
	}
}
