package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowWorker implements Runnable {

	@Inject
	private Env env;

	@Inject
	private OrderDAO orderDAO;

	@Inject
	private MoneyService moneyService;
	
	private ScheduledExecutorService executorService;

	@PostConstruct
	public void init() {
		executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "OrderFlowWorker");
				thread.setDaemon(true);
				return thread;
			}
		});
		executorService.scheduleWithFixedDelay(this, env.getOrderWorkerPeriodSecs(),
				env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		List<OrderInfo> activeOrders = orderDAO.findActiveAndNotInProcess();
		if (activeOrders != null) {
			for (OrderInfo activeOrder : activeOrders) {
				try {
					processActiveOrder(activeOrder);
				} catch (Exception ex) {
					Logger.getLogger(OrderFlowWorker.class.getName()).log(Level.SEVERE,
							ex.getMessage(), ex);
				}
			}
		}
	}

	private void processActiveOrder(OrderInfo activeOrder) {
		if (!activeOrder.isValid()) {
			throw new IllegalArgumentException("Order #" + activeOrder.getId() + " is inconsistent.");
		}
		if (activeOrder.isInProcess()) {
			return;
		}
		switch (activeOrder.getStatus()) {
			case INITIAL:
				processInTransfer(activeOrder);
				break;
			case PAYED:
				processOutTransfer(activeOrder);
				break;
		}
	}

	private void processInTransfer(final OrderInfo activeOrder) {
		final String orderId = activeOrder.getId();
		Transfer inTransfer = activeOrder.getInTransfer();
		orderDAO.changeStatus(orderId, OrderStatus.INITIAL, true);
		moneyService.process(inTransfer, new MoneyTransferCallback() {

			@Override
			public void success() {
				orderDAO.changeStatus(orderId, OrderStatus.PAYED, false);
			}

			@Override
			public void error(String msg) {
				orderDAO.changeStatus(orderId, OrderStatus.IN_FAILED, false);
			}
		});
	}

	private void processOutTransfer(final OrderInfo activeOrder) {
		final String orderId = activeOrder.getId();
		final Transfer outTransfer = activeOrder.getInTransfer();
		orderDAO.changeStatus(orderId, OrderStatus.PAYED, true);
		moneyService.process(outTransfer, new MoneyTransferCallback() {

			@Override
			public void success() {
				orderDAO.changeStatus(orderId, OrderStatus.SUCCESS, false);
			}

			@Override
			public void error(String msg) {
				orderDAO.changeStatus(orderId, OrderStatus.OUT_FAILED, false);
			}
		});
	}
}
