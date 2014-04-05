package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Embeddable
@Access(AccessType.FIELD)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Amount implements Serializable, Comparable<Amount> {
	
	public static Amount zero(long centsInCoin) {
		return new Amount(0, 0, centsInCoin);
	}

	private final long coins;
	private final long cents;

	private final long centsInCoin;
	
	/**
	 * Needed by JPA
	 */
	protected Amount() {
		coins = 0;
		cents = 0;
		centsInCoin = 0;
	}

	public Amount(BigDecimal amount, long centsInCoin) {
		if (amount.signum() < 0) {
			throw new IllegalArgumentException();
		}
		this.coins = amount.longValue();
		if (amount.scale() > 0) {
			this.cents = amount.subtract(BigDecimal.valueOf(coins))
					.multiply(BigDecimal.valueOf(centsInCoin)).longValue();
		} else {
			this.cents = 0;
		}
		this.centsInCoin = centsInCoin;
	}

	public Amount(long coins, long cents, long centsInCoin) {
		if ((coins < 0) || (cents < 0) || (centsInCoin <= 0)) {
			throw new IllegalArgumentException();
		}
		this.coins = coins;
		this.cents = cents;
		this.centsInCoin = centsInCoin;
	}

	public long getCoins() {
		return coins;
	}

	public long getCents() {
		return cents;
	}

	public long getCentsInCoin() {
		return centsInCoin;
	}

	public int scale() {
		return (int) Math.round(Math.log10(centsInCoin));
	}
	
	public Amount mul(BigDecimal k) {
		return new Amount(toBigDecimal().multiply(k), centsInCoin);
	}

	public boolean isValid() {
		return ((coins >= 0) && (cents >= 0) && (centsInCoin > 0));
	}

	public boolean isPositive() {
		long minCents = centsInCoin / 100;
		return (isValid() && ((coins > 0) || (cents >= minCents)));
	}

	public BigDecimal toBigDecimal() {
		return BigDecimal.valueOf(coins).add(BigDecimal.valueOf(cents)
				.divide(BigDecimal.valueOf(centsInCoin), scale(), RoundingMode.HALF_UP));
	}

	@Override
	public int compareTo(Amount o) {
		if ((o == null) || (centsInCoin != o.centsInCoin)) {
			throw new IllegalArgumentException();
		}
		if (coins < o.cents) {
			return -1;
		} else if (coins > o.coins) {
			return 1;
		} else {
			return Long.compare(cents, o.cents);
		}
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
