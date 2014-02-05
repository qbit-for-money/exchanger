package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.money.model.Currency;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("currency")
public class CurrencyResource {
	
	@GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	public Currency get(@PathParam("id") String id) {
		return Currency.valueOf(id);
	}
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public Currency[] findAll() {
		return Currency.values();
	}
}
