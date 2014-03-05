package com.qbit.exchanger.money.bitcoin;

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
	@Path("getNewAddress")
	@Produces(MediaType.APPLICATION_JSON)
	public WalletAddress getNewAddress() {
		return new WalletAddress(bitcoinMoneyService.getNewAddress());
	}

	@GET
	@Path("balance")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBalance() {
		return bitcoinMoneyService.getBalance();
	}
}
