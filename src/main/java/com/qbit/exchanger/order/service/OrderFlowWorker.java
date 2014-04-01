package com.qbit.exchanger.order.service;

import com.qbit.exchanger.admin.CryptoService;
import com.qbit.exchanger.dao.util.DAOExecutor;
import com.qbit.exchanger.dao.util.TrCallable;
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
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowWorker implements Runnable {

	private static final int MAX_STATUS_CHANGE_FAIL_COUNT = 2 * 60;

	private final Logger logger = LoggerFactory.getLogger(OrderFlowWorker.class);

	@Inject
	private OrderDAO orderDAO;

	@Inject
	private MoneyServiceProvider moneyServiceProvider;

	@Inject
	private Exchange exchange;

	@Inject
	private DAOExecutor databaseExecutor;

	@Override
	public void run() {
		try {
			List<OrderInfo> ordersUnderWork = orderDAO.findByFullStatus(EnumSet.of(OrderStatus.INITIAL, OrderStatus.PAYED), false);
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
			throw new IllegalArgumentException("Order is inconsistent.");
		}
		switch (orderUnderWork.getStatus()) {
			case INITIAL:
				processInTransfer(orderUnderWork);
				break;
			case PAYED:
				processOutTransfer(orderUnderWork);
				break;
		}
	}

	private void processInTransfer(final OrderInfo orderUnderWork) throws Exception {
		Transfer inTransfer = orderUnderWork.getInTransfer();
		Rate rate = exchange.getRate(inTransfer.getCurrency(), orderUnderWork.getOutTransfer().getCurrency());
		if ((rate == null) || !rate.isValid()) {
			throw new IllegalStateException("Invalid rate.");
		}
		String orderId = orderUnderWork.getId();
		orderDAO.changeStatus(orderId, OrderStatus.INITIAL, true);
		try {
			if (inTransfer.isCrypto()) {
				CryptoService cryptoService = moneyServiceProvider.get(inTransfer, CryptoService.class);
				Amount amountReceived = cryptoService.getBalance(inTransfer.getAddress());
				if ((amountReceived != null) && amountReceived.isPositive()) {
					processPayed(orderId, rate, amountReceived);
				}
			} else {
				MoneyService moneyService = moneyServiceProvider.get(inTransfer);
				Amount amountReceived = moneyService.receiveMoney(inTransfer.getAddress(), inTransfer.getAmount());
				if ((amountReceived != null) && amountReceived.isPositive()) {
					processPayed(orderId, rate, amountReceived);
				} else {
					processInFailed(orderId);
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			processInFailed(orderId);
		}
	}

	private void processPayed(final String orderId, final Rate rate, final Amount amountReceived) {
		final Amount inAmount = amountReceived;
		final Amount outAmount = rate.mul(amountReceived);
		databaseExecutor.submit(new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, false, inAmount, outAmount);
				return null;
			}
		}, MAX_STATUS_CHANGE_FAIL_COUNT);
	}

	private void processInFailed(final String orderId) {
		databaseExecutor.submit(new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED, false);
				return null;
			}
		}, MAX_STATUS_CHANGE_FAIL_COUNT);
	}

	private void processOutTransfer(final OrderInfo orderUnderWork) {
		Transfer outTransfer = orderUnderWork.getOutTransfer();
		String orderId = orderUnderWork.getId();
		orderDAO.changeStatus(orderId, OrderStatus.PAYED, true);
		try {
			MoneyService moneyService = moneyServiceProvider.get(outTransfer);
			moneyService.sendMoney(outTransfer.getAddress(), outTransfer.getAmount(), true);
			processSuccess(orderId, outTransfer.getAmount());
		} catch (Exception ex) {
			processOutFailed(orderId);
		}
	}

	private void processSuccess(final String orderId, final Amount outAmount) {
		databaseExecutor.submit(new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				orderDAO.changeStatusAndOutAmount(orderId, OrderStatus.SUCCESS, false, outAmount);
				return null;
			}
		}, MAX_STATUS_CHANGE_FAIL_COUNT);
	}

	private void processOutFailed(final String orderId) {
		databaseExecutor.submit(new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED, false);
				return null;
			}
		}, MAX_STATUS_CHANGE_FAIL_COUNT);
	}
}
