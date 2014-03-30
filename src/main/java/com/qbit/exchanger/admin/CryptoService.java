package com.qbit.exchanger.admin;

import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.model.Amount;
import java.util.List;

/**
 * @author Alexander_Sergeev
 */
public interface CryptoService extends MoneyService {
	
	Amount getBalance();
	
	Amount getBalance(String address);
	
	List<WTransaction> getTransactionHistory();
	
	List<WTransaction> getTransactionHistoryByAmount(Amount amount);
	
	List<WTransaction> getTransactionHistoryByAddress(String address);
	
	void sendMoney(Amount amount, String address);
}
