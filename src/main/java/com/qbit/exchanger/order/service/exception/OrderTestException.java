package com.qbit.exchanger.order.service.exception;

/**
 *
 * @author Александр
 */
public class OrderTestException extends OrderServiceException {

	/**
	 * Creates a new instance of <code>OrderTestFailedException</code>
	 * without detail message.
	 */
	public OrderTestException() {
	}

	/**
	 * Constructs an instance of <code>OrderTestFailedException</code> with
	 * the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public OrderTestException(String msg) {
		super(msg);
	}
}
