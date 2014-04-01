package com.qbit.exchanger.order.service;

import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import com.qbit.exchanger.order.service.exception.OrderServiceSecurityException;
import com.qbit.exchanger.order.service.exception.OrderTestException;
import java.util.Date;
import java.util.EnumSet;
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

	@Inject
	private MoneyServiceProvider moneyServiceProvider;

	public OrderInfo getActiveByUser(String userPublicKey) throws OrderServiceException {
		List<OrderInfo> activeOrders = orderDAO.findByUserAndStatus(userPublicKey,
			EnumSet.of(OrderStatus.INITIAL, OrderStatus.PAYED, OrderStatus.IN_FAILED, OrderStatus.OUT_FAILED));
		if ((activeOrders != null) && activeOrders.size() > 1) {
			throw new OrderServiceSecurityException("No more than one active order per user.");
		}
		if ((activeOrders != null) && !activeOrders.isEmpty()) {
			return activeOrders.get(0);
		} else {
			return null;
		}
	}

	public OrderInfo getByUserAndTimestamp(String userPublicKey, Date creationDate) throws OrderServiceException {
		List<OrderInfo> orders = orderDAO.findByUserAndTimestamp(userPublicKey, creationDate);
		if ((orders != null) && orders.size() > 1) {
			throw new OrderServiceSecurityException("No more than one order per user per timestamp.");
		}
		if ((orders != null) && !orders.isEmpty()) {
			return orders.get(0);
		} else {
			return null;
		}
	}

	public OrderInfo create(OrderInfo orderInfo) throws OrderServiceException {
		if ((orderInfo == null) || !orderInfo.isValid()) {
			throw new OrderServiceSecurityException("Can not create order. Order is inconsistent.");
		}

		OrderInfo activeOrder = getActiveByUser(orderInfo.getUserPublicKey());
		if (activeOrder != null) {
			throw new OrderServiceSecurityException("Can not create order. No more than one active order per user.");
		}

		Transfer outTransfer = orderInfo.getOutTransfer();
		MoneyService moneyService = moneyServiceProvider.get(outTransfer);
		if (!moneyService.reserve(outTransfer.getAddress(), outTransfer.getAmount())) {
			throw new OrderTestException();
		}

		OrderInfo result = orderDAO.create(orderInfo);
		if (result == null) {
			throw new OrderServiceSecurityException("Can not create order.");
		}
		return result;
	}
}
