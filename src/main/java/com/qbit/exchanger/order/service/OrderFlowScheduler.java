package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowScheduler {

	@Inject
	private Env env;

	@Inject
	private EntityManagerFactory entityManagerFactory;

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
		executorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				if (entityManagerFactory.isOpen()) {
					orderFlowWorker.run();
				}
			}
		}, env.getOrderWorkerPeriodSecs(), env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
	}

	@PreDestroy
	private void shutdown() {
		executorService.shutdown();
	}
}
