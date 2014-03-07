package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.model.Amount;

/**
 *
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyTransferCallback {

	void success(Amount amount);

	void error(String msg);
}
