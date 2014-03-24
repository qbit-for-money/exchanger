package com.qbit.exchanger.order.service;

import com.qbit.exchanger.dao.util.DAOExecutor;
import com.qbit.exchanger.dao.util.TrCallable;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Rate;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import java.util.Arrays;
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
		List<OrderInfo> ordersUnderWork = orderDAO.findByFullStatus(
				EnumSet.of(OrderStatus.INITIAL, OrderStatus.PAYED), false);
		if (ordersUnderWork != null) {
			for (OrderInfo orderUnderWork : ordersUnderWork) {
				try {
					processOrderUnderWork(orderUnderWork);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}
	}

	private void processOrderUnderWork(OrderInfo orderUnderWork) throws Exception {
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
		if (!orderUnderWork.isValid()) {
			throw new IllegalArgumentException("Order #" + orderUnderWork.getId() + " is inconsistent.");
		}
		final String orderId = orderUnderWork.getId();
		Transfer inTransfer = orderUnderWork.getInTransfer();
		final Rate rate = exchange.getRate(inTransfer.getCurrency(),
				orderUnderWork.getOutTransfer().getCurrency());
		if ((rate == null) || !rate.isValid()) {
			throw new IllegalStateException();
		}
		orderDAO.changeStatus(orderId, OrderStatus.INITIAL, true);
		MoneyService moneyService = moneyServiceProvider.get(inTransfer);
		moneyService.process(inTransfer, new MoneyTransferCallback() {

			@Override
			public void success(final Amount inAmount) {
				databaseExecutor.submit(new TrCallable<Void>() {

					@Override
					public Void call(EntityManager entityManager) {
						//try {
							orderDAO.changeStatusAndAmounts(orderId, OrderStatus.PAYED, false,
								inAmount, rate.mul(inAmount));
						//} catch(Exception ex) {
							//orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED, false);
						//}					
						return null;
					}
				}, MAX_STATUS_CHANGE_FAIL_COUNT);
			}

			@Override
			public void error(String msg) {
				databaseExecutor.submit(new TrCallable<Void>() {

					@Override
					public Void call(EntityManager entityManager) {
						orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED, false);
						return null;
					}
				}, MAX_STATUS_CHANGE_FAIL_COUNT);
			}
		});
	}

	private void processOutTransfer(final OrderInfo orderUnderWork) {
		final String orderId = orderUnderWork.getId();
		final Transfer outTransfer = orderUnderWork.getOutTransfer();
		orderDAO.changeStatus(orderId, OrderStatus.PAYED, true);
		MoneyService moneyService = moneyServiceProvider.get(outTransfer);
		moneyService.process(outTransfer, new MoneyTransferCallback() {

			@Override
			public void success(final Amount outAmount) {
				databaseExecutor.submit(new TrCallable<Void>() {

					@Override
					public Void call(EntityManager entityManager) {
						orderDAO.changeStatusAndOutAmount(orderId, OrderStatus.SUCCESS, false,
								outAmount);
						return null;
					}
				}, MAX_STATUS_CHANGE_FAIL_COUNT);

			}

			@Override
			public void error(String msg) {
				databaseExecutor.submit(new TrCallable<Void>() {

					@Override
					public Void call(EntityManager entityManager) {
						orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED, false);
						return null;
					}
				}, MAX_STATUS_CHANGE_FAIL_COUNT);
			}
		});
	}
}
