package com.qbit.exchanger.order.model;

import com.qbit.exchanger.common.model.Identifiable;
import com.qbit.exchanger.common.model.Amount;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "OrderInfo.findByExternalId",
			query = "SELECT o FROM OrderInfo o WHERE o.externalId = :externalId"),
	@NamedQuery(name = "OrderInfo.findActiveByUser",
			query = "SELECT o FROM OrderInfo o WHERE o.status = com.qbit.exchanger.order.model.OrderStatus.ACTIVE"
			+ " and o.userPublicKey = :userPublicKey")})
@XmlRootElement
public class OrderInfo implements Identifiable<String>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	private String userPublicKey;

	@ManyToOne
	private Currency sourceCurrency;

	@ManyToOne
	private Currency targetCurrency;

	@Embedded
	private Amount amount;

	private OrderStatus status;

	private String externalId;

	private String additionalId;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getUserPublicKey() {
		return userPublicKey;
	}

	public void setUserPublicKey(String userPublicKey) {
		this.userPublicKey = userPublicKey;
	}

	public Currency getSourceCurrency() {
		return sourceCurrency;
	}

	public void setSourceCurrency(Currency sourceCurrency) {
		this.sourceCurrency = sourceCurrency;
	}

	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public void setTargetCurrency(Currency targetCurrency) {
		this.targetCurrency = targetCurrency;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getAdditionalId() {
		return additionalId;
	}

	public void setAdditionalId(String additionalId) {
		this.additionalId = additionalId;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.id);
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
		final OrderInfo other = (OrderInfo) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OrderInfo{" + "id=" + id + ", creationDate=" + creationDate + ", userPublicKey=" + userPublicKey + ", sourceCurrency=" + sourceCurrency + ", targetCurrency=" + targetCurrency + ", amount=" + amount + ", status=" + status + ", externalId=" + externalId + ", additionalId=" + additionalId + '}';
	}
}
