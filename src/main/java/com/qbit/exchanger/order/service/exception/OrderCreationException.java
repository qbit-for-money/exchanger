package com.qbit.exchanger.order.service.exception;

/**
 *
 * @author Александр
 */
public class OrderCreationException extends Exception {

	/**
	 * Creates a new instance of <code>OrderCreationException</code> without
	 * detail message.
	 */
	public OrderCreationException() {
	}

	/**
	 * Constructs an instance of <code>OrderCreationException</code> with
	 * the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public OrderCreationException(String msg) {
		super(msg);
	}
}
