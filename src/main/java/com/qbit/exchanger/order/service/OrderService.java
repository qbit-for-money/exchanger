package com.qbit.exchanger.order.service;

import com.qbit.exchanger.order.service.exception.OrderTestException;
import com.qbit.exchanger.order.service.exception.OrderServiceSecurityException;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
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
	@Named("moneyServiceFacade")
	private MoneyService moneyService;
	
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	
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
	
	public OrderInfo create(OrderInfo orderInfo) throws OrderServiceException {
		if ((orderInfo == null) || !orderInfo.isValid()) {
			throw new OrderServiceSecurityException("Can not create order. Order is inconsistent.");
		}
		
		OrderInfo activeOrder = getActiveOrder(orderInfo.getUserPublicKey());
		if (activeOrder != null) {
			throw new OrderServiceSecurityException("No more than one active order per user.");
		}
		
		if (moneyService.test(orderInfo.getOutTransfer())) {
			throw new OrderTestException();
		}
		
		OrderInfo result = orderDAO.create(orderInfo);
		if (result == null) {
			throw new OrderServiceSecurityException("Can not create order.");
		}
		return result;
	}
}
