package com.qbit.exchanger.order.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * @author Alexander_Sergeev
 */
@Entity
@IdClass(MailNotificationPK.class)
@Access(AccessType.FIELD)
public class MailNotification implements Serializable {
	
	@Id
	private String orderId;
	@Id
	private OrderStatus orderStatus;

	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String id) {
		this.orderId = id;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + Objects.hashCode(this.orderId);
		hash = 23 * hash + Objects.hashCode(this.orderStatus);
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
		final MailNotification other = (MailNotification) obj;
		if (!Objects.equals(this.orderId, other.orderId)) {
			return false;
		}
		if (this.orderStatus != other.orderStatus) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MailNotification{" + "orderId=" + orderId + ", orderStatus=" + orderStatus + '}';
	}
}
