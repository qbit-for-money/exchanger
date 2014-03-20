package com.qbit.exchanger.dao.util;

import java.util.concurrent.Future;

/**
 *
 * @author Александр
 */
public interface DAOExecutor {
	
	<T> Future<T> submit(TrCallable<T> callable);
	
	void submit(TrCallable<Void> callable, int maxFailCount);
}
