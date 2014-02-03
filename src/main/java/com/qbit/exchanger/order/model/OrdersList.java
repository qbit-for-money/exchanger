package com.qbit.exchanger.order.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@XmlRootElement
public class OrdersList implements Serializable {

	private List<OrderInfo> orders = Collections.emptyList();

	public OrdersList() {
	}
	
	public OrdersList(List<OrderInfo> orders) {
		this.orders = orders;
	}

	public List<OrderInfo> getOrders() {
		return orders;
	}

	public void setOrders(List<OrderInfo> orders) {
		this.orders = orders;
	}
}
