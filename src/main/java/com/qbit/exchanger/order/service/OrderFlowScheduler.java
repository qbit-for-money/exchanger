package com.qbit.exchanger.order.service;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.order.dao.OrderDAO;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderFlowScheduler {
	
	private final Logger logger = LoggerFactory.getLogger(OrderFlowScheduler.class);

	@Inject
	private Env env;

	@Inject
	private OrderDAO orderDAO;
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
				orderFlowWorker.run();
			}
		}, env.getOrderWorkerPeriodSecs(), env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
		executorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					orderDAO.cleanUp();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, env.getOrderCleanupPeriodHours(), env.getOrderCleanupPeriodHours(), TimeUnit.HOURS);
		executorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					orderDAO.cleanUpCancellationTokens();
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}, env.getOrderCancellationTokenLifetimeHours(), env.getOrderCancellationTokenLifetimeHours(), TimeUnit.HOURS);
	}

	@PreDestroy
	public void shutdown() {
		try {
			executorService.shutdown();
		} catch (Throwable ex) {
			// Do nothing
		}
	}
}
