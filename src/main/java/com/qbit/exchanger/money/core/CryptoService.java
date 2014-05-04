package com.qbit.exchanger.money.core;

import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.model.Amount;
import java.util.List;

/**
 * @author Alexander_Sergeev
 */
public interface CryptoService extends MoneyService {
	
	String generateAddress();
	
	Amount getBalance(String address);
	
	void addBalance(Amount amount);
	
	List<WTransaction> getWalletTransactions();
}
