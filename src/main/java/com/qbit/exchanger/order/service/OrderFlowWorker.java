package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
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
		Logger.getLogger(OrderFlowWorker.class.getName()).log(Level.INFO, "Processing order #"
				+ activeOrder.getId() + " ...");
		// TODO
	}
}
