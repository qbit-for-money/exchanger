package com.qbit.exchanger.dao.util;

import com.qbit.exchanger.dao.util.DAOExecutor;
import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.dao.util.TrCallable;
import com.qbit.exchanger.env.Env;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class DefaultDAOExecutor implements DAOExecutor {

	private static class FailSafeRunnable implements Runnable {
		
		private final Logger logger = LoggerFactory.getLogger(FailSafeRunnable.class);

		private final Runnable task;
		private final int maxFailCount;
		
		private volatile ScheduledFuture<?> future;
		
		private volatile int failCount;

		public FailSafeRunnable(Runnable task, int maxFailCount) {
			this.task = task;
			this.maxFailCount = maxFailCount;
		}
		
		public void linkToFuture(ScheduledFuture<?> future) {
			this.future = future;
		}

		@Override
		public void run() {
			if (failCount > maxFailCount) {
				if (future != null) {
					future.cancel(false);
				}
				return;
			}
			try {
				task.run();
			} catch (Throwable ex) {
				failCount++;
				if (failCount > maxFailCount) {
					logger.error(ex.getMessage(), ex);
				} else {
					logger.info(ex.getMessage(), ex);
				}
			}
		}
	}

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
	public ScheduledFuture<?> submit(final TrCallable<Void> callable, int maxFailCount) {
		FailSafeRunnable failSafeRunnable = new FailSafeRunnable(new Runnable() {

			@Override
			public void run() {
				invokeInTransaction(entityManagerFactory, callable);
			}
		}, maxFailCount);
		ScheduledFuture<?> future = executorService.scheduleWithFixedDelay(failSafeRunnable,
				0, env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
		failSafeRunnable.linkToFuture(future);
		return future;
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
