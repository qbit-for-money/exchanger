package com.qbit.exchanger.money.dogecoin;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@XmlRootElement
public class DogeAddressInfo implements Serializable {

	private BigDecimal balance;
	private String address;

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "DogeAddressInfo{" + "balance=" + balance + ", address=" + address + '}';
	}
}
