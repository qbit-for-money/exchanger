package com.qbit.exchanger.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Entity
@XmlRootElement
public class OrderInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;
	
	private String userPublicKey;
	
	@ManyToOne
	private OrderBufferType sourceBufferType;
	
	@ManyToOne
	private OrderBufferType targetBufferType;
	
	private BigDecimal amount;
	
	private String externalId;
	
	private String additionalId;
	
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

	public OrderBufferType getSourceBufferType() {
		return sourceBufferType;
	}

	public void setSourceBufferType(OrderBufferType sourceBufferType) {
		this.sourceBufferType = sourceBufferType;
	}

	public OrderBufferType getTargetBufferType() {
		return targetBufferType;
	}

	public void setTargetBufferType(OrderBufferType targetBufferType) {
		this.targetBufferType = targetBufferType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
		return "Order{" + "id=" + id + '}';
	}
}
