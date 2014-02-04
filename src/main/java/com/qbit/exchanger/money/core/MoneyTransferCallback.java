package com.qbit.exchanger.money.core;

/**
 *
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyTransferCallback {

	void success();

	void error(String msg);
}
