package com.qbit.exchanger.order.service.exception;

/**
 *
 * @author Александр
 */
public class OrderServiceException extends Exception {

	/**
	 * Creates a new instance of <code>OrderServiceException</code> without
	 * detail message.
	 */
	public OrderServiceException() {
	}

	/**
	 * Constructs an instance of <code>OrderServiceException</code> with the
	 * specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public OrderServiceException(String msg) {
		super(msg);
	}
}
