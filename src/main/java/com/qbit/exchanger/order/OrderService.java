package com.qbit.exchanger.order;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderService {
	
	@Inject
	private OrderDAO orderDAO;
}
