package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.order.model.Currency;
import com.qbit.exchanger.utils.DAOUtils;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class CurrencyDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public Currency find(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.find(entityManagerFactory.createEntityManager(),
					Currency.class, id, null);
		} finally {
			entityManager.close();
		}
	}

	public List<Currency> findAll() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.findAll(entityManagerFactory.createEntityManager(),
					Currency.class);
		} finally {
			entityManager.close();
		}
	}
}
