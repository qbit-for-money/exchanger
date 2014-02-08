package com.qbit.exchanger.order.service;

import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderService {
	
	@Inject
	private OrderDAO orderDAO;
	
	public OrderInfo getActiveOrder(String userPublicKey) throws OrderServiceSecurityException {
		List<OrderInfo> activeOrders = orderDAO.findActiveByUser(userPublicKey);
		if ((activeOrders != null) && activeOrders.size() > 1) {
			throw new OrderServiceSecurityException("No more than one active order per user.");
		}
		if ((activeOrders != null) && !activeOrders.isEmpty()) {
			return activeOrders.get(0);
		} else {
			return null;
		}
	}
	
	public OrderInfo create(OrderInfo orderInfo) throws OrderServiceSecurityException {
		if (orderInfo.getUserPublicKey() == null) {
			throw new OrderServiceSecurityException("Can not create order. User can not be empty.");
		}
		OrderInfo activeOrder = getActiveOrder(orderInfo.getUserPublicKey());
		if (activeOrder != null) {
			throw new OrderServiceSecurityException("No more than one active order per user.");
		}
		OrderInfo result = orderDAO.create(orderInfo);
		if (result == null) {
			throw new OrderServiceSecurityException("Can not create order.");
		}
		return result;
	}
}
