package com.qbit.exchanger.order.service;

import com.qbit.exchanger.admin.CryptoService;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.model.Amount;
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
			if (processInTransfer(orderId, inTransfer, rate)) {
				processOutTransfer(orderId, outTransfer);
			}
		} else {
			logger.error("Invalid rate: " + rate);
		}
	}

	private boolean processInTransfer(String orderId, Transfer inTransfer, Rate rate) throws Exception {
		boolean ok;
		if (inTransfer.isCrypto()) {
			ok = processCryptoInTransfer(orderId, inTransfer, rate);
		} else {
			ok = processDefaultInTransfer(orderId, inTransfer, rate);
		}
		return ok;
	}
	
	private boolean processCryptoInTransfer(String orderId, Transfer inTransfer, Rate rate) {
		boolean ok;
		CryptoService cryptoService = moneyServiceProvider.get(inTransfer, CryptoService.class);
		Amount amountReceived = cryptoService.getBalance(inTransfer.getAddress());
		if (isReceivedAmountValid(amountReceived, rate)) {
			processPayed(orderId, rate, amountReceived);
			ok = true;
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Too small amount received to address \"" + inTransfer.getAddress() + "\".");
			}
			ok = false;
		}
		return ok;
	}
	
	private boolean processDefaultInTransfer(String orderId, Transfer inTransfer, Rate rate) {
		boolean ok;
		MoneyService moneyService = moneyServiceProvider.get(inTransfer);
		try {
			Amount amountReceived = moneyService.receiveMoney(inTransfer.getAddress(), inTransfer.getAmount());
			if (isReceivedAmountValid(amountReceived, rate)) {
				processPayed(orderId, rate, amountReceived);
				ok = true;
			} else {
				logger.error("Too small amount received to address \"" + inTransfer.getAddress() + "\".");
				processInFailed(orderId);
				ok = false;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			processInFailed(orderId);
			ok = false;
		}
		return ok;
	}
	
	private boolean isReceivedAmountValid(Amount amountReceived, Rate rate) {
		return ((amountReceived != null) && amountReceived.isPositive()
				&& (rate != null) && rate.isValid() && rate.mul(amountReceived).isPositive());
	}

	private void processPayed(String orderId, Rate rate, Amount amountReceived) {
		orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, amountReceived, rate.mul(amountReceived));
	}

	private void processInFailed(String orderId) {
		orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED);
	}

	private void processOutTransfer(String orderId, Transfer outTransfer) {
		try {
			MoneyService moneyService = moneyServiceProvider.get(outTransfer);
			moneyService.sendMoney(outTransfer.getAddress(), outTransfer.getAmount());
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
