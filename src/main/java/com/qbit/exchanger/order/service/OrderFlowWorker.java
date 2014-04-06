package com.qbit.exchanger.order.service;

import com.qbit.exchanger.admin.CryptoService;
import com.qbit.exchanger.external.exchange.core.Exchange;
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

	private void processOrderUnderWork(OrderInfo orderUnderWork) throws Exception {
		if (!orderUnderWork.isValid()) {
			throw new IllegalArgumentException("Order #" + orderUnderWork.getId() + " is inconsistent.");
		}
		String orderId = orderUnderWork.getId();
		Transfer inTransfer = orderUnderWork.getInTransfer();
		Transfer outTransfer = orderUnderWork.getOutTransfer();
		Rate rate = exchange.getRate(inTransfer.getCurrency(), outTransfer.getCurrency());
		if ((rate != null) && rate.isValid()) {
			Amount receivedAmount = processInTransfer(orderId, inTransfer.getCurrency(), inTransfer.getAddress(),
					inTransfer.getAmount(), rate);
			if (isReceivedAmountValid(receivedAmount, rate)) {
				processOutTransfer(orderId, outTransfer.getCurrency(), outTransfer.getAddress(),
						rate.mul(receivedAmount));
			}
		} else {
			logger.error("Invalid rate: " + rate);
		}
	}

	private Amount processInTransfer(String orderId, Currency inCurrency, String inAddress,
			Amount inAmount, Rate rate) throws Exception {
		Amount receivedAmount;
		if (inCurrency.isCrypto()) {
			receivedAmount = processCryptoInTransfer(orderId, inCurrency, inAddress, rate);
		} else {
			receivedAmount = processDefaultInTransfer(orderId, inCurrency, inAddress, inAmount, rate);
		}
		return receivedAmount;
	}
	
	private Amount processCryptoInTransfer(String orderId, Currency inCurrency, String inAddress, Rate rate) {
		CryptoService cryptoService = moneyServiceProvider.get(inCurrency, CryptoService.class);
		Amount receivedAmount = cryptoService.getBalance(inAddress);
		if (isReceivedAmountValid(receivedAmount, rate)) {
			processPayed(orderId, rate, receivedAmount);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Too small amount received to address \"" + inAddress + "\".");
			}
		}
		return receivedAmount;
	}
	
	private Amount processDefaultInTransfer(String orderId, Currency inCurrency, String inAddress,
			Amount inAmount, Rate rate) {
		MoneyService moneyService = moneyServiceProvider.get(inCurrency);
		Amount receivedAmount;
		try {
			receivedAmount = moneyService.receiveMoney(inAddress, inAmount);
			if (isReceivedAmountValid(receivedAmount, rate)) {
				processPayed(orderId, rate, receivedAmount);
			} else {
				logger.error("Too small amount received to address \"" + inAddress + "\".");
				processInFailed(orderId);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			processInFailed(orderId);
			receivedAmount = null;
		}
		return receivedAmount;
	}
	
	private boolean isReceivedAmountValid(Amount receivedAmount, Rate rate) {
		return ((receivedAmount != null) && receivedAmount.isPositive()
				&& (rate != null) && rate.isValid() && rate.mul(receivedAmount).isPositive());
	}

	private void processPayed(String orderId, Rate rate, Amount receivedAmount) {
		orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, receivedAmount, rate.mul(receivedAmount));
	}

	private void processInFailed(String orderId) {
		orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED);
	}

	private void processOutTransfer(String orderId, Currency outCurrency, String outAddress,
			Amount outAmount) {
		try {
			MoneyService moneyService = moneyServiceProvider.get(outCurrency);
			moneyService.sendMoney(outAddress, outAmount);
			processSuccess(orderId);
		} catch (Exception ex) {
			processOutFailed(orderId);
		}
	}

	private void processSuccess(String orderId) {
		orderDAO.changeStatus(orderId, OrderStatus.SUCCESS);
	}

	private void processOutFailed(String orderId) {
		orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED);
	}
}
