package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 *
 * @author Александр
 */
@Embeddable
public class Transfer implements Serializable {
	
	private TransferType type;
	
	private Currency currency;
	
	private String address;
	
	@Embedded
	private Amount amount;

	public TransferType getType() {
		return type;
	}

	public void setType(TransferType type) {
		this.type = type;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public int getCoins() {
		return amount.getCoins();
	}

	public int getCents() {
		return amount.getCents();
	}

	public BigDecimal toBigDecimal() {
		return amount.toBigDecimal();
	}
	
	public boolean isValid() {
		return ((type != null) && (currency != null) && (address != null) && (amount != null) && amount.isValid());
	}

	@Override
	public String toString() {
		return "Transaction{" + "type=" + type + ", currency=" + currency + ", address=" + address + ", amount=" + amount + '}';
	}
}