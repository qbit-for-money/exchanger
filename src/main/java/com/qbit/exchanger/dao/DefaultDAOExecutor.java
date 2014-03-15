package com.qbit.exchanger.dao;

import com.qbit.exchanger.dao.util.DAOExecutor;
import com.qbit.exchanger.dao.util.TrCallable;
import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.env.Env;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class DefaultDAOExecutor implements DAOExecutor {
	
	@Inject
	private Env env;
	
	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	private ScheduledExecutorService executorService;
	
	@PostConstruct
	public void init() {
		executorService = Executors.newScheduledThreadPool(10);
	}

	@Override
	public <T> Future<T> submit(final TrCallable<T> callable) {
		return executorService.submit(new Callable<T>() {

			@Override
			public T call() throws Exception {
				return invokeInTransaction(entityManagerFactory, callable);
			}
		});
	}

	@Override
	public void submit(final TrCallable<Void> callable, final int maxFailCount) {
		executorService.scheduleWithFixedDelay(new Runnable() {
			
			private int failCount = 0;

			@Override
			public void run() {
				while (failCount <= maxFailCount) {
					try {
						invokeInTransaction(entityManagerFactory, callable);
						break;
					} catch (Throwable ex) {
						failCount++;
						if (failCount > maxFailCount) {
							Logger.getLogger(DefaultDAOExecutor.class.getName()).log(
									Level.SEVERE, ex.getMessage(), ex);
							throw ex;
						}
					}
				}
			}
		}, 0, env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
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
