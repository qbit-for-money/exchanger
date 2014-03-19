package com.qbit.exchanger.external.exchange.resource;

import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("exchanges")
public class ExchangesResource {

	@Inject
	private Exchange exchange;

	@GET
	@Path("rate")
	@Produces(MediaType.APPLICATION_JSON)
	public Rate getRate(@QueryParam("from") Currency from, @QueryParam("to") Currency to) throws Exception {
		return exchange.getRate(from, to);
//		return new Rate(BigDecimal.ONE, from, to);
	}
}
