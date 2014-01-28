package com.qbit.exchanger.money.core;

/**
 * Core interface for payment methods
 *
 * @author Ivan_Rakitnyh
 */
public interface Money {

    void receiveMoney(String id, int coins, int cents);

    void sendMoney(String id, int coins, int cents);
}
