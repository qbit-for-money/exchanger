package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.model.Transfer;

/**
 * Core interface for payment methods
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyService {

    void process(Transfer transfer, MoneyTransferCallback callback);

	void test(Transfer transfer, MoneyTransferCallback callback);
}
