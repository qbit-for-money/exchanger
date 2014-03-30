package com.qbit.exchanger.money.bitcoin;

import com.qbit.exchanger.money.model.Amount;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("bitcoin")
public class BitcoinResource {

	@Inject
	private BitcoinMoneyService bitcoinMoneyService;

	@GET
	@Path("balance")
	@Produces(MediaType.APPLICATION_JSON)
	public Amount getBalance() {
		return bitcoinMoneyService.getBalance();
	}
}
