package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.order.dao.CurrencyDAO;
import com.qbit.exchanger.order.model.Currency;
import java.util.List;
import javax.inject.Inject;
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
	
	@Inject
	private CurrencyDAO currencyDAO;
	
	@GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	public Currency get(@PathParam("id") String id) {
		return currencyDAO.find(id);
	}
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public List<Currency> findAll() {
		return currencyDAO.findAll();
	}
}
