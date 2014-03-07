package com.qbit.exchanger.money.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WalletAddress implements Serializable {

	private String address;

	public WalletAddress() {
	}

	public WalletAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
