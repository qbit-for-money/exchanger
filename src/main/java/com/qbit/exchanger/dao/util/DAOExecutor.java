package com.qbit.exchanger.dao.util;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author Александр
 */
public interface DAOExecutor {
	
	<T> Future<T> submit(TrCallable<T> callable);
	
	ScheduledFuture<?> submit(TrCallable<Void> callable, int maxFailCount);
}
