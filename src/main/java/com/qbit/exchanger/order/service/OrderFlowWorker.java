package com.qbit.exchanger.order.service;

import com.qbit.exchanger.admin.CryptoService;
import com.qbit.exchanger.external.exchange.core.Exchange;
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
import java.util.List;
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

	@Override
	public void run() {
		try {
			List<OrderInfo> ordersUnderWork = orderDAO.findByStatus(EnumSet.of(OrderStatus.INITIAL));
			if (ordersUnderWork != null) {
				for (OrderInfo orderUnderWork : ordersUnderWork) {
					try {
						processOrderUnderWork(orderUnderWork);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
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
			mailService.send(orderUnderWork);
			OrderInfo payedOrder = processInTransfer(orderId, inTransfer.getCurrency(), inTransfer.getAddress(),
					inTransfer.getAmount(), rate);
			if ((payedOrder != null) && (OrderStatus.PAYED == payedOrder.getStatus())) {
				mailService.send(payedOrder);
				Transfer outTransfer = payedOrder.getOutTransfer();
				OrderInfo finalOrder = processOutTransfer(orderId, outTransfer.getCurrency(), outTransfer.getAddress(),
					outTransfer.getAmount());
				mailService.send(finalOrder);
			}
		} else {
			logger.error("Invalid rate: " + rate);
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
			return processPayed(orderId, rate, receivedAmount);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Too small amount received to address \"" + inAddress + "\".");
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
				logger.error("Too small amount received to address \"" + inAddress + "\".");
				return processInFailed(orderId);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
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
		return orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED);
	}

	private OrderInfo processOutTransfer(String orderId, Currency outCurrency, String outAddress,
			Amount outAmount) {
		try {
			MoneyService moneyService = moneyServiceProvider.get(outCurrency);
			moneyService.sendMoney(outAddress, outAmount);
			return processSuccess(orderId);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return processOutFailed(orderId);
		}
	}

	private OrderInfo processSuccess(String orderId) {
		return orderDAO.changeStatus(orderId, OrderStatus.SUCCESS);
	}

	private OrderInfo processOutFailed(String orderId) {
		return orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED);
	}
}
