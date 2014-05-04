package com.qbit.exchanger.money.dogecoin;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@XmlRootElement
public class DogeAddressInfo implements Serializable {
	
	public static class Data {

		private String balance;
		private String address;

		public String getBalance() {
			return balance;
		}

		public void setBalance(String balance) {
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

	@XmlElement
	@XmlList
	private List<Data> data;

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DogeAddressInfo{" + "address=" + data + '}';
	}
}
