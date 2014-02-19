package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowScheduler {
	
	@Inject
	private Env env;
	
	@Inject
	private OrderFlowWorker orderFlowWorker;
	
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
		executorService.scheduleWithFixedDelay(orderFlowWorker, env.getOrderWorkerPeriodSecs(),
				env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
	}
}
