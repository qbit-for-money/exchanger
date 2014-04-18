package com.qbit.exchanger.auth;

/**
 * @author Alexander_Sergeev
 */
public class CaptchaAuthException extends Exception {
	/**
	 * Creates a new instance of <code>CaptchaAuthException</code> without
	 * detail message.
	 */
	public CaptchaAuthException() {
	}

	/**
	 * Constructs an instance of <code>CaptchaAuthException</code> with
	 * the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public CaptchaAuthException(String msg) {
		super(msg);
	}
}
