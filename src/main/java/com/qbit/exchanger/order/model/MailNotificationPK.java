package com.qbit.exchanger.order.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Alexander_Sergeev
 */
public class MailNotificationPK implements Serializable {
	
	private String orderId;
	private OrderStatus orderStatus;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.orderId);
		hash = 17 * hash + Objects.hashCode(this.orderStatus);
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
		final MailNotificationPK other = (MailNotificationPK) obj;
		if (!Objects.equals(this.orderId, other.orderId)) {
			return false;
		}
		if (this.orderStatus != other.orderStatus) {
			return false;
		}
		return true;
	}
}
