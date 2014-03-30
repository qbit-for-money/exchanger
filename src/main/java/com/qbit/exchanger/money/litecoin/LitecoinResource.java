package com.qbit.exchanger.money.litecoin;

import com.qbit.exchanger.money.model.Amount;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Alexander_Sergeev
 */
@Path("litecoin")
public class LitecoinResource {
	
	@Inject
	private LitecoinMoneyService litecoinMoneyService;

	@GET
	@Path("balance")
	@Produces(MediaType.APPLICATION_JSON)
	public Amount getBalance() {
		return litecoinMoneyService.getBalance();
	}
}
