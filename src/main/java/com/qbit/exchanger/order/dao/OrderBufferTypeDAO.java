package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.order.model.OrderBufferType;
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
public class OrderBufferTypeDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	public OrderBufferType find(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.find(entityManagerFactory.createEntityManager(),
					OrderBufferType.class, id, null);
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderBufferType> findAll() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.findAll(entityManagerFactory.createEntityManager(),
					OrderBufferType.class);
		} finally {
			entityManager.close();
		}
	}
}
