package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
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

	public List<OrderInfo> findActiveAndNotInProcess() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findActiveAndNotInProcess", OrderInfo.class);
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

	public List<OrderInfo> findByUserAndTimestamp(String userPublicKey, Date creationDate) {
		if ((userPublicKey == null) || (creationDate == null)) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findByUserAndTimestamp", OrderInfo.class);
			query.setParameter("userPublicKey", userPublicKey);
			query.setParameter("creationDate", creationDate);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}

	public void changeStatus(String id, OrderStatus orderStatus, boolean inProcess) {
		if ((id == null) || (orderStatus == null)) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
			orderInfo.setStatus(orderStatus);
			orderInfo.setInProcess(inProcess);
			entityManager.getTransaction().commit();
		} catch (Throwable ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Throwable doNothing) {
			}
			throw ex;
		} finally {
			entityManager.close();
		}
	}
	
	public void changeStatusAndInAmount(String id, OrderStatus orderStatus, boolean inProcess,
			Amount inAmount) {
		if ((id == null) || (orderStatus == null) || (inAmount == null) || !inAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
			orderInfo.setStatus(orderStatus);
			orderInfo.setInProcess(inProcess);
			orderInfo.getInTransfer().setAmount(inAmount);
			entityManager.getTransaction().commit();
		} catch (Throwable ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Throwable doNothing) {
			}
			throw ex;
		} finally {
			entityManager.close();
		}
	}
	
	public void changeStatusAndOutAmount(String id, OrderStatus orderStatus, boolean inProcess,
			Amount outAmount) {
		if ((id == null) || (orderStatus == null) || (outAmount == null) || !outAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
			orderInfo.setStatus(orderStatus);
			orderInfo.setInProcess(inProcess);
			orderInfo.getOutTransfer().setAmount(outAmount);
			entityManager.getTransaction().commit();
		} catch (Throwable ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Throwable doNothing) {
			}
			throw ex;
		} finally {
			entityManager.close();
		}
	}
	
	public void changeStatusAndAmounts(String id, OrderStatus orderStatus, boolean inProcess,
			Amount inAmount, Amount outAmount) {
		if ((id == null) || (orderStatus == null) || (inAmount == null) || !inAmount.isValid()
				|| (outAmount == null) || !outAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
			orderInfo.setStatus(orderStatus);
			orderInfo.setInProcess(inProcess);
			orderInfo.getInTransfer().setAmount(inAmount);
			orderInfo.getOutTransfer().setAmount(outAmount);
			entityManager.getTransaction().commit();
		} catch (Throwable ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Throwable doNothing) {
			}
			throw ex;
		} finally {
			entityManager.close();
		}
	}

	public OrderInfo create(OrderInfo orderInfo) {
		return create(orderInfo.getUserPublicKey(), orderInfo.getInTransfer(), orderInfo.getOutTransfer());
	}

	public OrderInfo create(String userPublicKey, Transfer inTransfer, Transfer outTransfer) {
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
			order.setStatus(OrderStatus.INITIAL);
			entityManager.persist(order);
			entityManager.getTransaction().commit();
			return order;
		} catch (Throwable ex) {
			try {
				entityManager.getTransaction().rollback();
			} catch (Throwable doNothing) {
			}
			throw ex;
		} finally {
			entityManager.close();
		}
	}
}
