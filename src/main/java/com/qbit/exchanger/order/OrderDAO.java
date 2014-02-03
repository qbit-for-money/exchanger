package com.qbit.exchanger.order;

import com.qbit.exchanger.utils.DAOUtils;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public OrderInfo get(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.get(entityManagerFactory.createEntityManager(),
					OrderInfo.class, id, null);
		} finally {
			entityManager.close();
		}
	}
}
