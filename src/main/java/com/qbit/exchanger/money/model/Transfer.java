package com.qbit.exchanger.money.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
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
public class Transfer implements Serializable {
	
	public static Transfer clone(Transfer transfer) {
		Transfer result = new Transfer();
		result.setType(transfer.getType());
		result.setCurrency(transfer.getCurrency());
		result.setAddress(transfer.getAddress());
		result.setAmount(Amount.clone(transfer.getAmount()));
		return result;
	}

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

	public long getCoins() {
		return amount.getCoins();
	}

	public long getCents() {
		return amount.getCents();
	}

	public BigDecimal toBigDecimal() {
		return amount.toBigDecimal();
	}
	
	public boolean isPositive() {
		return (isValid() && amount.isPositive());
	}

	public boolean isValid() {
		return ((type != null) && (currency != null) && (address != null)
				&& (amount != null) && amount.isValid());
	}
	
	public boolean isCrypto() {
		return ((currency != null) && currency.isCrypto());
	}

	@Override
	public String toString() {
		return "Transaction{" + "type=" + type + ", currency=" + currency + ", address=" + address + ", amount=" + amount + '}';
	}
}
