package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.common.model.Money;
import com.qbit.exchanger.order.model.OrderStatus;
import com.qbit.exchanger.order.model.OrderBufferType;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.utils.DAOUtils;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

/**
 *
 * @author Александр
 */
@Singleton
public class OrderDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public OrderInfo find(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.find(entityManagerFactory.createEntityManager(),
					OrderInfo.class, id, null);
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findByExternalId(String externalId) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findByExternalId", OrderInfo.class);
			query.setParameter("externalId", externalId);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findActiveByUser(String userPublicKey) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findActiveByUser", OrderInfo.class);
			query.setParameter("userPublicKey", userPublicKey);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}

	public OrderInfo create(OrderInfo orderInfo) {
		return create(orderInfo.getUserPublicKey(), orderInfo.getSourceBufferType(), orderInfo.getTargetBufferType(),
				orderInfo.getAmount(), orderInfo.getExternalId(), orderInfo.getAdditionalId());
	}

	public OrderInfo create(String userPublicKey, OrderBufferType sourceBufferType,
			OrderBufferType targetBufferType, Money amount,
			String externalId, String additionalId) {
		if ((userPublicKey == null) || (sourceBufferType == null) || (targetBufferType == null)
				|| (amount == null)) {
			return null;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo order = new OrderInfo();
			order.setCreationDate(new Date());
			order.setUserPublicKey(userPublicKey);
			order.setSourceBufferType(sourceBufferType);
			order.setTargetBufferType(targetBufferType);
			order.setAmount(amount);
			order.setStatus(OrderStatus.ACTIVE);
			order.setExternalId(externalId);
			order.setAdditionalId(additionalId);
			entityManager.persist(order);
			entityManager.getTransaction().commit();
			entityManager.detach(order);
			return order;
		} finally {
			entityManager.close();
		}
	}
}
