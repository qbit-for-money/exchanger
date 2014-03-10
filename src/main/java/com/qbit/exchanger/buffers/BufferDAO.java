package com.qbit.exchanger.buffers;

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

	public boolean reserveAmount(Currency currency, Amount currentBalance, Amount amount) {
		boolean result = false;
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			Amount reservedAmount = getReservedAmount(entityManager, currency);
			BigDecimal freeAmount;
			if (reservedAmount != null) {
				BigDecimal reserved = reservedAmount.toBigDecimal();
				freeAmount = currentBalance.toBigDecimal().subtract(reserved);
			} else {
				freeAmount = currentBalance.toBigDecimal();
			}
			if (freeAmount.compareTo(amount.toBigDecimal()) > 0) {
				updateAmount(entityManager, currency, amount);
			}
			entityManager.getTransaction().commit();
			result = true;
		} catch (Exception ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Exception e) {
				// do nothing
			}
			throw ex;
		} finally {
			entityManager.close();
		}
		return result;
	}

	private void updateAmount(EntityManager em, Currency currency, Amount amount) {
		BufferBalanceInfo balanceInfo = new BufferBalanceInfo();
		balanceInfo.setCurrencyCode(currency.getCode());
		balanceInfo.setAmount(amount);
		em.persist(balanceInfo);
	}

	private Amount getReservedAmount(EntityManager em, Currency currency) {
		BufferBalanceInfo balanceInfo = em.find(BufferBalanceInfo.class, currency.getCode(), LockModeType.PESSIMISTIC_WRITE);
		return balanceInfo != null ? balanceInfo.getAmount() : null;
	}

}
