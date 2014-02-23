package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@XmlRootElement
public class Rate implements Serializable {
	
	private Amount numerator;
	
	private Amount denominator;

	public Rate() {
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

	public void setNumerator(Amount numerator) {
		this.numerator = numerator;
	}

	public Amount getDenominator() {
		return denominator;
	}

	public void setDenominator(Amount denominator) {
		this.denominator = denominator;
	}
	
	public Rate inv() {
		return new Rate(denominator, numerator);
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
