package com.qbit.exchanger.money.model.serialization;

import com.qbit.exchanger.money.model.Currency;
import java.io.Serializable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author alisherfaz
 */
public class CurrencyAdapter extends XmlAdapter<CurrencyAdapter.AdaptedCurrency, Currency> {
	
	public static class AdaptedCurrency implements Serializable {
	
		private String id;
		private String code;
		private long centsInCoin;
		private boolean supported;

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

		public long getCentsInCoin() {
			return centsInCoin;
		}

		public void setCentsInCoin(long centsInCoin) {
			this.centsInCoin = centsInCoin;
		}

		public boolean isSupported() {
			return supported;
		}

		public void setSupported(boolean supported) {
			this.supported = supported;
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
			result.setCentsInCoin(currency.getCentsInCoin());
			result.setSupported(currency.isSupported());
		}
		return result;
	}
}
