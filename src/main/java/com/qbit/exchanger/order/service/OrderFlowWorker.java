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
import javax.inject.Inject;
import javax.inject.Named;
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
	@Named("moneyServiceFacade")
	private MoneyService moneyService;
	
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "OrderFlowWorker");
			thread.setDaemon(true);
			return thread;
		}
	});

	{
		executorService.scheduleWithFixedDelay(this, env.getOrderWorkerPeriodSecs(),
				env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		List<OrderInfo> activeOrders = orderDAO.findActive();
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
			throw new IllegalArgumentException("Order is inconsistent.");
		}
		switch (activeOrder.getStatus()) {
			case ACTIVE:
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
		moneyService.process(inTransfer, new MoneyTransferCallback() {

			@Override
			public void success() {
				orderDAO.changeOrderStatus(orderId, OrderStatus.PAYED);
			}

			@Override
			public void error(String msg) {
				orderDAO.changeOrderStatus(orderId, OrderStatus.IN_FAILED);
			}
		});
	}
	
	private void processOutTransfer(final OrderInfo activeOrder) {
		final String orderId = activeOrder.getId();
		final Transfer outTransfer = activeOrder.getInTransfer();
		moneyService.process(outTransfer, new MoneyTransferCallback() {

			@Override
			public void success() {
				orderDAO.changeOrderStatus(orderId, OrderStatus.SUCCESS);
			}

			@Override
			public void error(String msg) {
				orderDAO.changeOrderStatus(orderId, OrderStatus.OUT_FAILED);
			}
		});
	}
}
