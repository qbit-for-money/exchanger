package com.qbit.exchanger.buffer;

import static com.qbit.commons.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.commons.dao.util.TrCallable;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

@Singleton
public class BufferDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public boolean reserveAmount(final Currency currency, final Amount currentBalance, final Amount amount) {
		if ((currency == null) || (currentBalance == null) || !currentBalance.isPositive()
				|| (amount == null) || !amount.isValid() || (currentBalance.compareTo(amount) < 0)) {
			return false;
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<Boolean>() {

			@Override
			public Boolean call(EntityManager entityManager) {
				boolean result = false;
				BufferBalanceInfo balanceInfo = getBalanceInfo(entityManager, currency);
				if (balanceInfo != null) {
					BigDecimal reserved = balanceInfo.getAmount().toBigDecimal();
					BigDecimal freeAmount = currentBalance.toBigDecimal().subtract(reserved);
					if (freeAmount.compareTo(amount.toBigDecimal()) > 0) {
						BigDecimal newAmount = reserved.add(amount.toBigDecimal());
						balanceInfo.setAmount(new Amount(newAmount, currency.getCentsInCoin()));
						result = true;
					}
				} else {
					BigDecimal freeAmount = currentBalance.toBigDecimal();
					if (freeAmount.compareTo(amount.toBigDecimal()) > 0) {
						createBalanceInfo(entityManager, currency, amount);
						result = true;
					}
				}
				return result;
			}
		});
	}

	public void deleteReservation(final Currency currency, final Amount amount) {
		if ((currency == null) || (amount == null) || !amount.isValid()) {
			return;
		}
		invokeInTransaction(entityManagerFactory, new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				BufferBalanceInfo balanceInfo = getBalanceInfo(entityManager, currency);
				if (balanceInfo != null) {
					BigDecimal reserved = balanceInfo.getAmount().toBigDecimal();
					BigDecimal amountToSubtract = amount.toBigDecimal();
					if (reserved.compareTo(amountToSubtract) > 0) {
						BigDecimal newAmount = reserved.subtract(amountToSubtract);
						balanceInfo.setAmount(new Amount(newAmount, currency.getCentsInCoin()));
					}
				}
				return null;
			}
		});
	}

	private void createBalanceInfo(EntityManager em, Currency currency, Amount amount) {
		BufferBalanceInfo balanceInfo = new BufferBalanceInfo();
		balanceInfo.setCurrencyCode(currency.getCode());
		balanceInfo.setAmount(amount);
		em.persist(balanceInfo);
	}

	private BufferBalanceInfo getBalanceInfo(EntityManager em, Currency currency) {
		return em.find(BufferBalanceInfo.class, currency.getCode(), LockModeType.PESSIMISTIC_WRITE);
	}
}
