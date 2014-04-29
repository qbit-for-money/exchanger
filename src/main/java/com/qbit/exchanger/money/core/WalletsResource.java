package com.qbit.exchanger.money.core;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.WalletAddress;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("wallets")
@Singleton
public class WalletsResource {

	@Inject
	private MoneyServiceProvider moneyServiceProvider;
	
	@Inject
	private Env env;

	@GET
	@Path("{currency}/generated-address")
	@Produces(MediaType.APPLICATION_JSON)
	public WalletAddress generateAddress(@PathParam("currency") Currency currency) {
		CryptoService moneyService = moneyServiceProvider.get(currency, CryptoService.class);
		return new WalletAddress(moneyService.generateAddress());
	}
	
	@GET
	@Path("{currency}/balance")
	@Produces(MediaType.APPLICATION_JSON)
	public Amount getBalance(@PathParam("currency") Currency currency) {
		CryptoService moneyService = moneyServiceProvider.get(currency, CryptoService.class);
		Amount balance = moneyService.getBalance();
		Amount maxTransactionAmount = balance.mul(env.getMaxTransactionAmountToBalanceCoef());
		return maxTransactionAmount;
	}
}
