package com.qbit.exchanger.dao;

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

	private final Logger logger = LoggerFactory.getLogger(DefaultDAOExecutor.class);

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
		
		new FixedExecutionRunnable(new Runnable() {

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
							logger.error(ex.getMessage(), ex);
							throw ex;
						} else {
							logger.info(ex.getMessage(), ex);
						}
					}
				}
			}
		}).scheduleWithFixedDelay(executorService, env.getOrderWorkerPeriodSecs(), TimeUnit.SECONDS);
	}

	@PreDestroy
	public void shutdown() {
		try {
			executorService.shutdown();
		} catch (Throwable ex) {
			// Do nothing
		}
	}

	class FixedExecutionRunnable implements Runnable {

		private volatile ScheduledFuture<?> self;
		private final Runnable task;

		public FixedExecutionRunnable(Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			task.run();
			self.cancel(false);
		}

		public void scheduleWithFixedDelay(ScheduledExecutorService executor, long delay, TimeUnit unit) {
			self = executor.scheduleWithFixedDelay(this, 0, delay, unit);
		}
	}
}
