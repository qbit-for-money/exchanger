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

	public static long CENTS_IN_COIN = 1000 * 1000 * 1000;

	public static Amount ZERO = new Amount(0, 0);

	public static Amount ONE = new Amount(1, 0);

	private long coins, cents;

	private long centsInCoin;

	public Amount() {
	}

	public Amount(long coins, long cents) {
		this(coins, cents, CENTS_IN_COIN);
	}
	
	public Amount(BigDecimal amount, long centsInCoin) {
		if (amount.signum() < 0) {
			throw new IllegalArgumentException();
		}
		String amountStr = amount.toPlainString();
		int decimalPointIndex = amountStr.indexOf('.');
		if (decimalPointIndex > 0) {
			this.coins = Long.parseLong(amountStr.substring(0, decimalPointIndex));
			this.cents = Long.parseLong(amountStr.substring(decimalPointIndex + 1));
		} else {
			this.coins = Long.parseLong(amountStr);
			this.cents = 0;
		}
		this.centsInCoin = centsInCoin;
	}

	public Amount(long coins, long cents, long centsInCoin) {
		this.coins = coins;
		this.cents = (CENTS_IN_COIN * cents / centsInCoin);
		this.centsInCoin = centsInCoin;
	}
	
	public long getCoins() {
		return coins;
	}

	public void setCoins(long coins) {
		this.coins = coins;
	}

	public long getCents() {
		return cents;
	}

	public void setCents(long cents) {
		this.cents = cents;
	}

	public long getCentsInCoin() {
		return centsInCoin;
	}

	public void setCentsInCoin(long centsInCoin) {
		this.centsInCoin = centsInCoin;
	}

	public boolean isValid() {
		return (coins >= 0) && (cents >= 0) && (centsInCoin >= 0);
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(coins + "." + (cents * centsInCoin / CENTS_IN_COIN));
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + (int) (this.coins ^ (this.coins >>> 32));
		hash = 53 * hash + (int) (this.cents ^ (this.cents >>> 32));
		hash = 53 * hash + (int) (this.centsInCoin ^ (this.centsInCoin >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Amount other = (Amount) obj;
		if (this.coins != other.coins) {
			return false;
		}
		if (this.cents != other.cents) {
			return false;
		}
		if (this.centsInCoin != other.centsInCoin) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Amount{" + "coins=" + coins + ", cents=" + cents + ", centsInCoin=" + centsInCoin + '}';
	}
}
