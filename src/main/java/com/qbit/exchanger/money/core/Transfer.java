package com.qbit.exchanger.money.core;

import com.qbit.exchanger.common.model.Amount;
import java.io.Serializable;

/**
 * Transaction money bean
 *
 * @author Ivan_Rakitnyh
 */
public class Transfer implements Serializable {

    private String address;
	
    private Amount amount;

    public Transfer() {}

	public Transfer(String address, Amount amount) {
		this.address = address;
		this.amount = amount;
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
	
	public boolean isValid() {
		return ((address != null) && (amount != null) && amount.isValid());
	}
}
