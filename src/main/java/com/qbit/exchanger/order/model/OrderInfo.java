package com.qbit.exchanger.order.model;

import com.qbit.exchanger.common.model.Identifiable;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Александр
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "OrderInfo.findByStatus",
			query = "SELECT o FROM OrderInfo o WHERE o.status IN :statuses"),
	@NamedQuery(name = "OrderInfo.findByUserAndStatus",
			query = "SELECT o FROM OrderInfo o WHERE o.userPublicKey = :userPublicKey"
			+ " AND o.status IN :statuses"),
	@NamedQuery(name = "OrderInfo.findByUserAndTimestamp",
			query = "SELECT o FROM OrderInfo o WHERE o.userPublicKey = :userPublicKey"
			+ " AND o.creationDate = :creationDate"),
	@NamedQuery(name = "OrderInfo.cleanUp",
			query = "DELETE FROM OrderInfo o WHERE o.creationDate < :deadline"
			+ " AND o.status = com.qbit.exchanger.order.model.OrderStatus.INITIAL")})
@Access(AccessType.FIELD)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderInfo implements Identifiable<String>, Serializable {

	private static final long serialVersionUID = 1L;
	
	public static OrderInfo clone(OrderInfo orderInfo) {
		OrderInfo result = new OrderInfo();
		result.setId(orderInfo.getId());
		result.setCreationDate(orderInfo.getCreationDate());
		result.setUserPublicKey(orderInfo.getUserPublicKey());
		result.setInTransfer(Transfer.clone(orderInfo.getInTransfer()));
		result.setOutTransfer(Transfer.clone(orderInfo.getOutTransfer()));
		result.setStatus(orderInfo.getStatus());
		return result;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@XmlTransient
	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	private String userPublicKey;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "IN_TRR_TYPE")),
		@AttributeOverride(name = "currency", column = @Column(name = "IN_TRR_CUR")),
		@AttributeOverride(name = "address", column = @Column(name = "IN_TRR_ADDR")),
		@AttributeOverride(name = "amount.coins", column = @Column(name = "IN_TRR_COINS")),
		@AttributeOverride(name = "amount.cents", column = @Column(name = "IN_TRR_CENTS")),
		@AttributeOverride(name = "amount.centsInCoin", column = @Column(name = "IN_TRR_CENTS_IN_COIN")),})
	private Transfer inTransfer;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "OUT_TRR_TYPE")),
		@AttributeOverride(name = "currency", column = @Column(name = "OUT_TRR_CUR")),
		@AttributeOverride(name = "address", column = @Column(name = "OUT_TRR_ADDR")),
		@AttributeOverride(name = "amount.coins", column = @Column(name = "OUT_TRR_COINS")),
		@AttributeOverride(name = "amount.cents", column = @Column(name = "OUT_TRR_CENTS")),
		@AttributeOverride(name = "amount.centsInCoin", column = @Column(name = "OUT_TRR_CENTS_IN_COIN")),})
	private Transfer outTransfer;

	private OrderStatus status;
	
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

	public Transfer getInTransfer() {
		return inTransfer;
	}

	public void setInTransfer(Transfer inTransfer) {
		this.inTransfer = inTransfer;
	}

	public Transfer getOutTransfer() {
		return outTransfer;
	}

	public void setOutTransfer(Transfer outTransfer) {
		this.outTransfer = outTransfer;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public boolean isValid() {
		return ((userPublicKey != null) && (inTransfer != null) && (outTransfer != null)
				&& inTransfer.isPositive() && outTransfer.isPositive()
				&& TransferType.IN.equals(inTransfer.getType())
				&& TransferType.OUT.equals(outTransfer.getType())
				&& !inTransfer.getCurrency().equals(outTransfer.getCurrency()));
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
		return "OrderInfo{" + "id=" + id + ", creationDate=" + creationDate + ", userPublicKey=" + userPublicKey + ", inTransfer=" + inTransfer + ", outTransfer=" + outTransfer + ", status=" + status + '}';
	}
}
