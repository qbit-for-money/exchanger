package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.model.Amount;

/**
 * Core interface for payment methods
 *
 * @author Ivan_Rakitnyh
 */
public interface MoneyService {
	
	Amount getBalance();
	
	void sendMoney(String address, Amount amount) throws Exception;
	
	void sendMoney(String address, Amount amount, boolean unreserve) throws Exception;
	
	/**
	 * Optional
	 * @param address
	 * @param amount
	 * @return
	 * @throws Exception 
	 */
	Amount receiveMoney(String address, Amount amount) throws Exception;

	boolean reserve(String address, Amount amount);
}
