package com.qbit.exchanger.order.service;

/**
 *
 * @author Александр
 */
public class OrderServiceSecurityException extends Exception {

	/**
	 * Creates a new instance of <code>OrderServiceSecurityException</code>
	 * without detail message.
	 */
	public OrderServiceSecurityException() {
	}

	/**
	 * Constructs an instance of <code>OrderServiceSecurityException</code> with
	 * the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public OrderServiceSecurityException(String msg) {
		super(msg);
	}
}
