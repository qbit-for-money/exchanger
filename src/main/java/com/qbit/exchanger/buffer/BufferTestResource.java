package com.qbit.exchanger.buffer;

import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("buffer")
public class BufferTestResource {

	@Inject
	private BufferDAO bufferDAO;

	@GET
	@Path("test")
	public void test() {
		Amount balance = new Amount(BigDecimal.valueOf(100), Currency.YANDEX_RUB.getCentsInCoin());
		Amount amountToReserve = new Amount(BigDecimal.valueOf(10), Currency.YANDEX_RUB.getCentsInCoin());
		System.out.println("locking amount...");
		boolean isReserved = bufferDAO.reserveAmount(Currency.YANDEX_RUB, balance, amountToReserve);
		System.out.println("amount locked: " + isReserved);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			System.out.println(ex.getMessage());
		}
		if (isReserved) {
			System.out.println("unlocking amount...");
			bufferDAO.deleteReservation(Currency.YANDEX_RUB, amountToReserve);
			System.out.println("amount unlocked");
		}
	}
}
