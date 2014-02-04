package com.qbit.exchanger.order.model;

import com.qbit.exchanger.common.model.Amount;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
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
public class Currency implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	@Embedded
	@Column(name = "MIN_SIGNIFICANT_AMOUNT")
	private Amount minSignificantAmount;
	
	@Embedded
	@Column(name = "MAX_TRANSACTION_AMOUNT")
	private Amount maxTransactionAmount;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Amount getMinSignificantAmount() {
		return minSignificantAmount;
	}

	public void setMinSignificantAmount(Amount minSignificantAmount) {
		this.minSignificantAmount = minSignificantAmount;
	}

	public Amount getMaxTransactionAmount() {
		return maxTransactionAmount;
	}

	public void setMaxTransactionAmount(Amount maxTransactionAmount) {
		this.maxTransactionAmount = maxTransactionAmount;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + Objects.hashCode(this.id);
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
		final Currency other = (Currency) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Currency{" + "id=" + id + ", minSignificantAmount=" + minSignificantAmount + ", maxTransactionAmount=" + maxTransactionAmount + '}';
	}
}
