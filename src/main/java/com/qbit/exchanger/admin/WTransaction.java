package com.qbit.exchanger.admin;

import com.qbit.exchanger.money.model.Amount;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@XmlRootElement
public class WTransaction implements Serializable {
	
	private Amount amount;
	private Amount amountSentToMe;
	private Amount amountSentFromMe;
	private String address;
	private String trHash;
	private Date updateTime;

	public WTransaction() {
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public Amount getAmountSentToMe() {
		return amountSentToMe;
	}

	public void setAmountSentToMe(Amount amountSentToMe) {
		this.amountSentToMe = amountSentToMe;
	}

	public Amount getAmountSentFromMe() {
		return amountSentFromMe;
	}

	public void setAmountSentFromMe(Amount amountSentFromMe) {
		this.amountSentFromMe = amountSentFromMe;
	}

	public String getTrHash() {
		return trHash;
	}

	public void setTrHash(String trHash) {
		this.trHash = trHash;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.trHash);
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
		final WTransaction other = (WTransaction) obj;
		if (!Objects.equals(this.trHash, other.trHash)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "WTransaction{" + "amount=" + amount + ", amountSentToMe=" + amountSentToMe + ", amountSentFromMe=" + amountSentFromMe + ", address=" + address + ", trHash=" + trHash + ", updateTime=" + updateTime + '}';
	}
}
