package com.qbit.exchanger.admin;

import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@Path("admin")
public class AdminResource {

	@Inject
	private CryptoServiceProvider cryptoServiceProvider;
	
	@XmlRootElement
	public static class MoneyRequest implements Serializable {

		private Amount amount;
		private String address;
		private Currency currency;

		public MoneyRequest() {
		}

		public Amount getAmount() {
			return amount;
		}

		public void setAmount(Amount amount) {
			this.amount = amount;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Currency getCurrency() {
			return currency;
		}

		public void setCurrency(Currency currency) {
			this.currency = currency;
		}

		public boolean isValid() {
			return amount != null && address != null && currency != null && amount.isValid();
		}
	}

	@XmlRootElement
	public static class WTransactionWrapper implements Serializable {

		@XmlElement
		@XmlList
		private List<WTransaction> transactions;

		public WTransactionWrapper() {
		}

		public WTransactionWrapper(List<WTransaction> transactions) {
			this.transactions = transactions;
		}

		public List<WTransaction> getTransactions() {
			return transactions;
		}
	}

	@GET
	@Path("{currency}/balance")
	@Produces(MediaType.APPLICATION_JSON)
	public Amount getBalance(@PathParam("currency") Currency currency) {
		CryptoService moneyService = cryptoServiceProvider.get(currency);
		return moneyService.getBalance();
	}

	@GET
	@Path("{currency}/transactions")
	@Produces(MediaType.APPLICATION_JSON)
	public WTransactionWrapper getTransactionHisory(@PathParam("currency") Currency currency) {
		CryptoService moneyService = cryptoServiceProvider.get(currency);
		return new WTransactionWrapper(moneyService.getTransactionHistory());
	}

	@GET
	@Path("{currency}/transactionsByCoinsAndCents")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WTransaction> getTransactionHisoryByCoinsAndCents(@PathParam("currency") Currency currency, @QueryParam("coins") long coins, @QueryParam("cents") long cents) {
		CryptoService moneyService = cryptoServiceProvider.get(currency);
		Amount amount = new Amount(coins, cents, Currency.LITECOIN.getCentsInCoin());
		return moneyService.getTransactionHistoryByAmount(amount);
	}

	@GET
	@Path("{currency}/transactionsByAddress")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WTransaction> getTransactionHistoryByAddress(@PathParam("currency") Currency currency, @QueryParam("address") String address) {
		CryptoService moneyService = cryptoServiceProvider.get(currency);
		return moneyService.getTransactionHistoryByAddress(address);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMoney(MoneyRequest amountRequest) {
		if (!amountRequest.isValid()) {
			throw new IllegalArgumentException();
		}
		CryptoService moneyService = cryptoServiceProvider.get(amountRequest.getCurrency());

		Amount amount = amountRequest.getAmount();
		amount.setCentsInCoin(amountRequest.getCurrency().getCentsInCoin());
		moneyService.sendMoney(amount, amountRequest.getAddress());
	}
}
