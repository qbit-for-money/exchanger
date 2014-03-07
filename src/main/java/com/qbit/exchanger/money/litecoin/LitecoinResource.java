package com.qbit.exchanger.money.litecoin;

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
<<<<<<< HEAD
=======
	
	@GET
	@Path("getNewAddress")
	@Produces(MediaType.APPLICATION_JSON)
	public WalletAddress getNewAddress() {
		return new WalletAddress(litecoinMoneyService.generateAddress());
	}
>>>>>>> 2300110edb008f43f4d019bdd0c2725b3fdaab8c

	@GET
	@Path("balance")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBalance() {
		return litecoinMoneyService.getBalance();
	}
}
