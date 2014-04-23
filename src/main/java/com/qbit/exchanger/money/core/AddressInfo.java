package com.qbit.exchanger.money.core;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@XmlRootElement
public class AddressInfo implements Serializable {
	
	public static class Data implements Serializable {
		
		private String address;
		private String balance;
		private int confirmations;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getBalance() {
			return balance;
		}

		public void setBalance(String balance) {
			this.balance = balance;
		}

		public int getConfirmations() {
			return confirmations;
		}

		public void setConfirmations(int confirmations) {
			this.confirmations = confirmations;
		}

		@Override
		public String toString() {
			return "Data{" + "address=" + address + ", balance=" + balance + ", confirmations=" + confirmations + '}';
		}
	}
	
	private String status;
	private int code;
	private String message;
	private Data data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}	

	@Override
	public String toString() {
		return "Balance{" + "status=" + status + ", code=" + code + ", message=" + message + ", data=" + data + '}';
	}
}
