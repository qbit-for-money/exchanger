package com.qbit.exchanger.money.core;

/**
 * Core interface for payment methods
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyService {

    void receiveMoney(Transfer transfer, MoneyTransferCallback callback);

    void sendMoney(Transfer transfer, MoneyTransferCallback callback);

	void testSend(Transfer transfer, MoneyTransferCallback callback);

	void testReceive(Transfer transfer, MoneyTransferCallback callback);
}
