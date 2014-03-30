package com.qbit.exchanger.admin;

import com.qbit.exchanger.money.model.Amount;
import java.util.List;

/**
 * @author Alexander_Sergeev
 */
public interface CryptoService {
	Amount getBalance();
	
	List<WTransaction> getTransactionHistory();
	
	List<WTransaction> getTransactionHistoryByAmount(Amount amount);
	
	List<WTransaction> getTransactionHistoryByAddress(String address);
	
	void sendMoney(Amount amount, String address);
}
