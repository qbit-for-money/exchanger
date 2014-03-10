package com.qbit.exchanger.buffers;

import com.qbit.exchanger.money.model.Amount;
import java.io.Serializable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Buffers")
public class BufferBalanceInfo implements Serializable {

	@Id
	private String currencyCode;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "coins", column = @Column(name = "COINS")),
		@AttributeOverride(name = "cents", column = @Column(name = "CENTS")),
		@AttributeOverride(name = "cents", column = @Column(name = "CENTS_IN_COIN"))})
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
