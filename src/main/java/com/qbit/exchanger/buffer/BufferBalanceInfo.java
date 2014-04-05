package com.qbit.exchanger.buffer;

import com.qbit.exchanger.money.model.Amount;
import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class BufferBalanceInfo implements Serializable {

	@Id
	private String currencyCode;

	@Embedded
	private Amount amount;

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}
}
