package com.qbit.exchanger.order.service;

import com.qbit.exchanger.money.core.CryptoService;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.order.dao.MailNotificationDAO;
import com.qbit.exchanger.mail.MailService;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowWorker implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(OrderFlowWorker.class);

	@Inject
	private OrderDAO orderDAO;

	@Inject
	private MoneyServiceProvider moneyServiceProvider;

	@Inject
	private Exchange exchange;

	@Inject
	private MailService mailService;
	@Inject
	private MailNotificationDAO mailNotificationDAO;

	private final Executor executor = Executors.newCachedThreadPool();

	@Override
	public void run() {
		try {
			List<OrderInfo> ordersUnderWork = orderDAO.findByStatus(EnumSet.of(OrderStatus.INITIAL));
			if (ordersUnderWork != null) {
				for (OrderInfo orderUnderWork : ordersUnderWork) {
					try {
						processOrderUnderWork(orderUnderWork);
					} catch (Exception ex) {
						if (logger.isErrorEnabled()) {
							logger.error("[{}] " + ex.getMessage(), orderUnderWork.getId(), ex);
						}
					}
				}
			}
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private void processOrderUnderWork(final OrderInfo orderUnderWork) throws Exception {
		if (!orderUnderWork.isValid()) {
			throw new IllegalArgumentException("Order #" + orderUnderWork.getId() + " is inconsistent.");
		}
		String orderId = orderUnderWork.getId();
		Transfer inTransfer = orderUnderWork.getInTransfer();
		Rate rate = exchange.getRate(inTransfer.getCurrency(), orderUnderWork.getOutTransfer().getCurrency());
		if ((rate != null) && rate.isValid()) {
			sendOrderFlowNotification(orderUnderWork);
			OrderInfo payedOrder = processInTransfer(orderId, inTransfer.getCurrency(), inTransfer.getAddress(),
					inTransfer.getAmount(), rate);
			if ((payedOrder != null) && (OrderStatus.PAYED == payedOrder.getStatus())) {
				sendOrderFlowNotification(payedOrder);
				Transfer outTransfer = payedOrder.getOutTransfer();
				OrderInfo finalOrder = processOutTransfer(orderId, outTransfer.getCurrency(), outTransfer.getAddress(),
						outTransfer.getAmount());
				sendOrderFlowNotification(finalOrder);
			}
		} else {
			if (logger.isErrorEnabled()) {
				logger.error("[{}] Invalid rate: {}", orderId, rate);
			}
		}
	}

	private OrderInfo processInTransfer(String orderId, Currency inCurrency, String inAddress,
			Amount inAmount, Rate rate) throws Exception {
		OrderInfo orderInfo;
		if (inCurrency.isCrypto()) {
			orderInfo = processCryptoInTransfer(orderId, inCurrency, inAddress, rate);
		} else {
			orderInfo = processDefaultInTransfer(orderId, inCurrency, inAddress, inAmount, rate);
		}
		return orderInfo;
	}

	private OrderInfo processCryptoInTransfer(String orderId, Currency inCurrency, String inAddress, Rate rate) {
		CryptoService cryptoService = moneyServiceProvider.get(inCurrency, CryptoService.class);
		Amount receivedAmount = cryptoService.getBalance(inAddress);
		if (isReceivedAmountValid(receivedAmount, rate)) {
			cryptoService.addBalance(receivedAmount);
			return processPayed(orderId, rate, receivedAmount);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("[{}] Too small amount received to address \"{}\".", orderId, inAddress);
			}
			return null;
		}
	}

	private OrderInfo processDefaultInTransfer(String orderId, Currency inCurrency, String inAddress,
			Amount inAmount, Rate rate) {
		MoneyService moneyService = moneyServiceProvider.get(inCurrency);
		try {
			Amount receivedAmount = moneyService.receiveMoney(inAddress, inAmount);
			if (isReceivedAmountValid(receivedAmount, rate)) {
				return processPayed(orderId, rate, receivedAmount);
			} else {
				if (logger.isErrorEnabled()) {
					logger.error("[{}] Too small amount received to address \"{}\".", orderId, inAddress);
				}
				return processInFailed(orderId);
			}
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error("[{}] " + ex.getMessage(), orderId, ex);
			}
			return processInFailed(orderId);
		}
	}

	private boolean isReceivedAmountValid(Amount receivedAmount, Rate rate) {
		return ((receivedAmount != null) && receivedAmount.isPositive()
				&& (rate != null) && rate.isValid() && rate.mul(receivedAmount).isPositive());
	}

	private OrderInfo processPayed(String orderId, Rate rate, Amount receivedAmount) {
		return orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, receivedAmount, rate.mul(receivedAmount));
	}

	private OrderInfo processInFailed(String orderId) {
		if (logger.isErrorEnabled()) {
			logger.error("[{}] Process in failed", orderId);
		}
		return orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED);
	}

	private OrderInfo processOutTransfer(String orderId, Currency outCurrency, String outAddress,
			Amount outAmount) {
		try {
			MoneyService moneyService = moneyServiceProvider.get(outCurrency);
			moneyService.sendMoney(outAddress, outAmount);
			if (logger.isInfoEnabled()) {
				logger.info("[{}] Sent money to \"{}\".", orderId, outAddress);
			}
			return processSuccess(orderId);
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error("[{}] " + ex.getMessage(), orderId, ex);
			}
			return processOutFailed(orderId);
		}
	}

	private OrderInfo processSuccess(String orderId) {
		return orderDAO.changeStatus(orderId, OrderStatus.SUCCESS);
	}

	private OrderInfo processOutFailed(String orderId) {
		return orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED);
	}
	
	private void sendOrderFlowNotification(OrderInfo orderInfo) {
		if ((orderInfo == null) || (orderInfo.getId() == null) || orderInfo.getId().isEmpty()
				|| (orderInfo.getUserPublicKey() == null) || orderInfo.getUserPublicKey().isEmpty()
				|| !orderInfo.getUserPublicKey().contains("@")) {
			return;
		}
		final OrderInfo safeOrderInfo = OrderInfo.clone(orderInfo);
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (mailNotificationDAO.isNotificationSent(safeOrderInfo.getId(), safeOrderInfo.getStatus())) {
						return;
					}
				} catch (Exception ex) {
					if (logger.isErrorEnabled()) {
						logger.error("[{}]" + ex.getMessage(), safeOrderInfo.getId(), ex);
					}
				}
				try {
					String tmplPrefix;
					if (safeOrderInfo.isValid()) {
						tmplPrefix = safeOrderInfo.getStatus().name().toLowerCase();
					} else {
						tmplPrefix = "invalid";
					}
					Map<String, Object> templateInput = new HashMap<>();
					templateInput.put("order", safeOrderInfo);
					if ((safeOrderInfo.getInTransfer() != null) && (safeOrderInfo.getInTransfer().getAmount() != null)) {
						templateInput.put("inAmount", safeOrderInfo.getInTransfer().toBigDecimal());
					}
					if ((safeOrderInfo.getOutTransfer() != null) && (safeOrderInfo.getOutTransfer().getAmount() != null)) {
						templateInput.put("outAmount", safeOrderInfo.getOutTransfer().toBigDecimal());
					}
					mailService.send(safeOrderInfo.getUserPublicKey(), "[INFO] Order #" + safeOrderInfo.getId(),
							tmplPrefix + "-order.tmpl", templateInput);
				} catch (Exception ex) {
					if (logger.isErrorEnabled()) {
						logger.error("[{}]" + ex.getMessage(), safeOrderInfo.getId(), ex);
					}
				} finally {
					try {
						mailNotificationDAO.registerNotification(safeOrderInfo.getId(), safeOrderInfo.getStatus());
					} catch (Exception ex) {
						//
					}
				}
			}
		});
	}
}
