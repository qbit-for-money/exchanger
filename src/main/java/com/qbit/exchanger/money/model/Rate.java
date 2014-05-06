package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Rate implements Serializable {

	private final Amount numerator;
	private final Amount denominator;
	
	protected Rate(){
		numerator = null;
		denominator = null;
	}

	public Rate(BigDecimal numerator, Currency fromCurrency, Currency toCurrency) {
		if ((numerator == null) || (fromCurrency == null)
				|| (toCurrency == null)) {
			throw new IllegalArgumentException();
		}
		this.numerator = new Amount(numerator, toCurrency.getCentsInCoin());
		this.denominator = new Amount(1, 0, fromCurrency.getCentsInCoin());
	}

	public Rate(BigDecimal numerator, BigDecimal denominator,
			Currency fromCurrency, Currency toCurrency) {
		if ((numerator == null) || (denominator == null)
				|| (fromCurrency == null) || (toCurrency == null)) {
			throw new IllegalArgumentException();
		}
		this.numerator = new Amount(numerator, toCurrency.getCentsInCoin());
		this.denominator = new Amount(denominator, fromCurrency.getCentsInCoin());
	}

	public Rate(Amount numerator, Amount denominator) {
		if ((numerator == null) || !numerator.isValid()
				|| (denominator == null) || !denominator.isValid()) {
			throw new IllegalArgumentException();
		}
		this.numerator = numerator;
		this.denominator = denominator;
	}

	public Amount getNumerator() {
		return numerator;
	}

	public Amount getDenominator() {
		return denominator;
	}

	public Rate inv() {
		return new Rate(denominator, numerator);
	}

	public Amount mul(Amount amount) {
		if ((amount == null) || !amount.isValid()) {
			throw new IllegalArgumentException();
		}
		BigDecimal result = numerator.toBigDecimal().multiply(
				amount.toBigDecimal()).divide(denominator.toBigDecimal(), numerator.scale(), RoundingMode.HALF_UP);
		return new Amount(result, numerator.getCentsInCoin());
	}

	public Rate mul(BigDecimal k) {
		return new Rate(numerator.mul(k), denominator);
	}

	public boolean isValid() {
		return ((numerator != null) && numerator.isPositive()
				&& (denominator != null) && denominator.isPositive());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + Objects.hashCode(this.numerator);
		hash = 53 * hash + Objects.hashCode(this.denominator);
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
		final Rate other = (Rate) obj;
		if (!Objects.equals(this.numerator, other.numerator)) {
			return false;
		}
		if (!Objects.equals(this.denominator, other.denominator)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Rate{" + "numerator=" + numerator + ", denominator=" + denominator + '}';
	}
}
