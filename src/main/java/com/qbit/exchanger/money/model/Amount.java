package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Embeddable;

/**
 *
 * @author Александр
 */
@Embeddable
public class Amount implements Serializable {

	public static int CENTS_IN_COIN = 1000 * 1000 * 1000;

	public static Amount ZERO = new Amount(0, 0);

	public static Amount ONE = new Amount(1, 0);

	private int coins, cents;

	private int centsInCoin;

	public Amount() {
	}

	public Amount(int coins, int cents) {
		this(coins, cents, CENTS_IN_COIN);
	}

	public Amount(int coins, int cents, int centsInCoin) {
		this.coins = coins;
		this.cents = (int) (CENTS_IN_COIN * (long) cents / centsInCoin);
		this.centsInCoin = centsInCoin;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public int getCents() {
		return cents;
	}

	public void setCents(int cents) {
		this.cents = cents;
	}

	public int getCentsInCoin() {
		return centsInCoin;
	}

	public void setCentsInCoin(int centsInCoin) {
		this.centsInCoin = centsInCoin;
	}

	public boolean isValid() {
		return (coins >= 0) && (cents >= 0);
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(coins + "." + (cents * (long) centsInCoin / CENTS_IN_COIN));
	}

	@Override
	public String toString() {
		return "Money{" + "coins=" + coins + ", cents=" + cents + '}';
	}
}
