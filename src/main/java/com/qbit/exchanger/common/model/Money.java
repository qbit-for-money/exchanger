package com.qbit.exchanger.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Embeddable;

/**
 *
 * @author Александр
 */
@Embeddable
public class Money implements Serializable {
	
	public static int CENTS_IN_COIN = 1000 * 1000 * 1000;
	
	public static Money ZERO = new Money(0, 0);
	
	public static Money ONE = new Money(1, 0);

	private int coins, cents;

	public Money() {
	}

	public Money(int coins, int cents) {
		this(coins, cents, CENTS_IN_COIN);
	}
	
	public Money(int coins, int cents, int centsInCoin) {
		this.coins = coins;
		this.cents = (int) (CENTS_IN_COIN * (long) cents / centsInCoin);
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

	public BigDecimal toBigDecimal() {
		return toBigDecimal(CENTS_IN_COIN);
	}
	
	public BigDecimal toBigDecimal(int centsInCoin) {
		return new BigDecimal(coins + "." + (cents * (long) centsInCoin / CENTS_IN_COIN));
	}

	@Override
	public String toString() {
		return "Money{" + "coins=" + coins + ", cents=" + cents + '}';
	}
}
