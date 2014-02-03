package com.qbit.exchanger.order;

import java.io.Serializable;
import java.math.BigDecimal;
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
	
	private BigDecimal minSignificantAmount = new BigDecimal("0.01");
	
	private BigDecimal maxTransactionAmount = BigDecimal.ZERO;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getMinSignificantAmount() {
		return minSignificantAmount;
	}

	public void setMinSignificantAmount(BigDecimal minSignificantAmount) {
		this.minSignificantAmount = minSignificantAmount;
	}

	public BigDecimal getMaxTransactionAmount() {
		return maxTransactionAmount;
	}

	public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) {
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
		return "com.qbit.exchanger.order.BufferType[ id=" + id + " ]";
	}
	
}
