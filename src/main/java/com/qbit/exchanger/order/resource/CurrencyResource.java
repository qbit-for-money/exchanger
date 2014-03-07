package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.money.model.Currency;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.qbit.exchanger.money.model.serialization.CurrencyAdapter;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Path("currency")
public class CurrencyResource {

	@XmlRootElement
	public static class CurrencyWrapper implements Serializable {

		@XmlJavaTypeAdapter(CurrencyAdapter.class)
		private Currency currency;

		public CurrencyWrapper() {
		}

		public CurrencyWrapper(Currency currency) {
			this.currency = currency;
		}

		public Currency getCurrency() {
			return currency;
		}
	}

	@XmlRootElement
	public static class CurrencyListWrapper implements Serializable {

		@XmlJavaTypeAdapter(CurrencyAdapter.class)
		private List<Currency> currencies;

		public CurrencyListWrapper() {
		}

		public CurrencyListWrapper(List<Currency> currencies) {
			this.currencies = currencies;
		}

		public List<Currency> getCurrencies() {
			return currencies;
		}
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public CurrencyWrapper get(@PathParam("id") String id) {
		return new CurrencyWrapper(Currency.valueOf(id));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CurrencyListWrapper findAll() {
		return new CurrencyListWrapper(Arrays.asList(Currency.values()));
	}
}
