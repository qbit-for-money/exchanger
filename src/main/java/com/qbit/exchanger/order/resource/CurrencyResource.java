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

/**
 *
 * @author Александр
 */
@Path("currency")
public class CurrencyResource {
	
	private static class CurrencyResponse {
		
		@XmlJavaTypeAdapter(CurrencyAdapter.class)
		private Currency currency;

		public Currency getCurrency() {
			return currency;
		}

		public void setCurrency(Currency currency) {
			this.currency = currency;
		}
	}
	
	private static class CurrenciesResponse {
		
		@XmlJavaTypeAdapter(CurrencyAdapter.class)
		private List<Currency> currencies;

		public List<Currency> getCurrencies() {
			return currencies;
		}

		public void setCurrencies(List<Currency> currencies) {
			this.currencies = currencies;
		}
	}
	
	@GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	public CurrencyResponse get(@PathParam("id") String id) {
		CurrencyResponse response = new CurrencyResponse();
		response.setCurrency(Currency.valueOf(id));
		return response;
	}
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public CurrenciesResponse findAll() {
		CurrenciesResponse response = new CurrenciesResponse();
		response.setCurrencies(Arrays.asList(Currency.values()));
		return response;
	}
}
