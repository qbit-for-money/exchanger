package com.qbit.exchanger.buffer;

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
			BufferBalanceInfo balanceInfo = getBalanceInfo(entityManager, currency);
			if (balanceInfo != null) {
				BigDecimal reserved = balanceInfo.getAmount().toBigDecimal();
				BigDecimal freeAmount = currentBalance.toBigDecimal().subtract(reserved);
				if (freeAmount.compareTo(amount.toBigDecimal()) > 0) {
					BigDecimal newAmount = reserved.add(amount.toBigDecimal());
					balanceInfo.setAmount(new Amount(newAmount, currency.getCentsInCoin()));
				}
			} else {
				BigDecimal freeAmount = currentBalance.toBigDecimal();
				if (freeAmount.compareTo(amount.toBigDecimal()) > 0) {
					createBalanceInfo(entityManager, currency, amount);
				}

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

	public void deleteReservation(Currency currency, Amount amount) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			BufferBalanceInfo balanceInfo = getBalanceInfo(entityManager, currency);
			if (balanceInfo != null) {
				BigDecimal reserved = balanceInfo.getAmount().toBigDecimal();
				BigDecimal amountToSubtract = amount.toBigDecimal();
				if (reserved.compareTo(amountToSubtract) > 0) {
					BigDecimal newAmount = reserved.subtract(amountToSubtract);
					balanceInfo.setAmount(new Amount(newAmount, currency.getCentsInCoin()));
				}
			}
			entityManager.getTransaction().commit();
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
	}

	private void createBalanceInfo(EntityManager em, Currency currency, Amount amount) {
		BufferBalanceInfo balanceInfo = new BufferBalanceInfo();
		balanceInfo.setCurrencyCode(currency.getCode());
		balanceInfo.setAmount(amount);
		em.persist(balanceInfo);
	}

	private BufferBalanceInfo getBalanceInfo(EntityManager em, Currency currency) {
		BufferBalanceInfo balanceInfo = em.find(BufferBalanceInfo.class, currency.getCode(), LockModeType.PESSIMISTIC_WRITE);
		return balanceInfo;
	}

}
