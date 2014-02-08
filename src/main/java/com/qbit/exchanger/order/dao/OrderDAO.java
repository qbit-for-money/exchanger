package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import com.qbit.exchanger.order.model.OrderStatus;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.util.DAOUtil;
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
			return DAOUtil.find(entityManagerFactory.createEntityManager(),
					OrderInfo.class, id, null);
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findActive() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findActive", OrderInfo.class);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findActiveByUser(String userPublicKey) {
		if (userPublicKey == null) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findActiveByUser", OrderInfo.class);
			query.setParameter("userPublicKey", userPublicKey);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}
	
	public void changeOrderStatus(String id, OrderStatus orderStatus) {
		if ((id == null) || (orderStatus == null)) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
			orderInfo.setStatus(orderStatus);
			entityManager.getTransaction().commit();
		} finally {
			entityManager.close();
		}
	}

	public OrderInfo create(OrderInfo orderInfo) {
		return create(orderInfo.getUserPublicKey(), orderInfo.getInTransfer(), orderInfo.getOutTransfer(),
				orderInfo.getExternalId(), orderInfo.getAdditionalId());
	}

	public OrderInfo create(String userPublicKey, Transfer inTransfer, Transfer outTransfer,
			String externalId, String additionalId) {
		if ((userPublicKey == null) || (inTransfer == null) || (outTransfer == null)
				|| !inTransfer.isValid() || !outTransfer.isValid()
				|| !TransferType.IN.equals(inTransfer.getType())
				|| !TransferType.OUT.equals(outTransfer.getType())) {
			throw new IllegalArgumentException("Order is inconsistent.");
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo order = new OrderInfo();
			order.setCreationDate(new Date());
			order.setUserPublicKey(userPublicKey);
			order.setInTransfer(inTransfer);
			order.setOutTransfer(outTransfer);
			order.setStatus(OrderStatus.ACTIVE);
			order.setExternalId(externalId);
			order.setAdditionalId(additionalId);
			entityManager.persist(order);
			entityManager.getTransaction().commit();
			return order;
		} finally {
			entityManager.close();
		}
	}
}
