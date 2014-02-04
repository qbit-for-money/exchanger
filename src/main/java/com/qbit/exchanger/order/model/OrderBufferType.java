package com.qbit.exchanger.order.model;

import com.qbit.exchanger.common.model.Money;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Entity
@XmlRootElement
public class OrderBufferType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	@Embedded
	@Column(name = "MIN_SIGNIFICANT_AMOUNT")
	private Money minSignificantAmount = new Money(0, 1);
	
	@Embedded
	@Column(name = "MAX_TRANSACTION_AMOUNT")
	private Money maxTransactionAmount = Money.ZERO;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Money getMinSignificantAmount() {
		return minSignificantAmount;
	}

	public void setMinSignificantAmount(Money minSignificantAmount) {
		this.minSignificantAmount = minSignificantAmount;
	}

	public Money getMaxTransactionAmount() {
		return maxTransactionAmount;
	}

	public void setMaxTransactionAmount(Money maxTransactionAmount) {
		this.maxTransactionAmount = maxTransactionAmount;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof OrderBufferType)) {
			return false;
		}
		OrderBufferType other = (OrderBufferType) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OrderBufferType{" + "id=" + id + ", minSignificantAmount=" + minSignificantAmount + ", maxTransactionAmount=" + maxTransactionAmount + '}';
	}
}
