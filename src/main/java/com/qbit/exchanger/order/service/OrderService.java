package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import com.qbit.commons.mail.MailService;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderCancellationToken;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import com.qbit.exchanger.order.service.exception.OrderServiceSecurityException;
import com.qbit.exchanger.order.service.exception.OrderTestException;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderService {

	@Inject
	private Env env;

	@Inject
	private OrderDAO orderDAO;

	@Inject
	private MoneyServiceProvider moneyServiceProvider;
	
	@Inject
	private MailService mailService;

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

	public OrderInfo create(String userPublicKey, OrderInfo orderInfo) throws OrderServiceException {
		if ((userPublicKey == null) || (orderInfo == null) || !orderInfo.isValid()
				|| !orderInfo.getUserPublicKey().equals(userPublicKey)) {
			throw new OrderServiceSecurityException("Can not create order. Order is inconsistent.");
		}

		OrderInfo activeOrder = getActiveByUser(orderInfo.getUserPublicKey());
		if (activeOrder != null) {
			throw new OrderServiceSecurityException("Can not create order. No more than one active order per user.");
		}

		Transfer outTransfer = orderInfo.getOutTransfer();
		MoneyService moneyService = moneyServiceProvider.get(outTransfer.getCurrency());
		if (!testBalanceAgainstAmount(moneyService, outTransfer.getAmount())) {
			throw new OrderTestException();
		}

		OrderInfo result = orderDAO.create(orderInfo);
		if (result == null) {
			throw new OrderServiceSecurityException("Can not create order.");
		}
		return result;
	}
	
	public OrderInfo sendCancellationToken(String userPublicKey) throws OrderServiceException {
		OrderInfo activeOrder = getActiveByUser(userPublicKey);
		if ((activeOrder == null) || !activeOrder.isValid() || !activeOrder.getUserPublicKey().contains("@")) {
			throw new OrderServiceSecurityException("Can not cancel order. Order is inconsistent.");
		}
		
		List<OrderCancellationToken> tokens = orderDAO.findCancellationTokens(activeOrder.getId());
		OrderCancellationToken orderCancellationToken;
		if ((tokens != null) && !tokens.isEmpty()) {
			orderCancellationToken = tokens.get(0);
		} else {
			orderCancellationToken = orderDAO.createCancellationToken(activeOrder.getId());
		}
		
		Map<String, Object> templateInput = new HashMap<>();
		templateInput.put("order", activeOrder);
		if ((activeOrder.getInTransfer() != null) && (activeOrder.getInTransfer().getAmount() != null)) {
			templateInput.put("inAmount", activeOrder.getInTransfer().toBigDecimal());
		}
		if ((activeOrder.getOutTransfer() != null) && (activeOrder.getOutTransfer().getAmount() != null)) {
			templateInput.put("outAmount", activeOrder.getOutTransfer().toBigDecimal());
		}
		templateInput.put("url", env.getOrderCancellationURL() + orderCancellationToken.getToken());
		
		mailService.send(activeOrder.getUserPublicKey(), "[CANCELLATION] Order #" + activeOrder.getId(),
				"confirm-order-cancellation", templateInput);
		
		return activeOrder;
	}
	
	public OrderInfo cancel(String userPublicKey, String token, String address) throws Exception {
		OrderInfo activeOrder = getActiveByUser(userPublicKey);
		if ((activeOrder == null) || !activeOrder.isValid() || !activeOrder.getUserPublicKey().contains("@")
				|| !EnumSet.of(OrderStatus.INITIAL, OrderStatus.OUT_FAILED).contains(activeOrder.getStatus())
				|| ((OrderStatus.OUT_FAILED == activeOrder.getStatus()) && (address == null))) {
			throw new OrderServiceSecurityException("Can not cancel order. Order is inconsistent or invalid token.");
		}
		
		OrderInfo canceledOrder;
		if (OrderStatus.OUT_FAILED == activeOrder.getStatus()) {
			Transfer inTransfer = activeOrder.getInTransfer();
			MoneyService moneyService = moneyServiceProvider.get(inTransfer.getCurrency());
			if (!testBalanceAgainstAmount(moneyService, inTransfer.getAmount())) {
				throw new OrderTestException();
			}
			canceledOrder = orderDAO.changeStatus(activeOrder.getId(), OrderStatus.CANCELED);
			moneyService.sendMoney(address, inTransfer.getAmount());
		} else { // INITIAL
			canceledOrder = orderDAO.changeStatus(activeOrder.getId(), OrderStatus.CANCELED);
		}
		
		return canceledOrder;
	}

	private boolean testBalanceAgainstAmount(MoneyService moneyService, Amount amount) {
		try {
			Amount balance = moneyService.getBalance();
			Amount maxTransactionAmount = balance.mul(env.getMaxTransactionAmountToBalanceCoef());
			return (amount.compareTo(maxTransactionAmount) < 0);
		} catch (Exception ex) {
			return false;
		}
	}
}
