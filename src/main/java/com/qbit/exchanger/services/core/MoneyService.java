package com.qbit.exchanger.services.core;

/**
 * Core interface for payment methods
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyService {

    void receiveMoney(Money moneyToRecive, Money moneyToSend, MoneyService sendService);

    void sendMoney(Money moneyToSend) throws Exception;
}
