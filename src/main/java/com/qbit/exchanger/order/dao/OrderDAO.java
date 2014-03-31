package com.qbit.exchanger.order.dao;

import com.qbit.exchanger.dao.util.DAOUtil;
import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.dao.util.TrCallable;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import com.qbit.exchanger.user.UserDAO;
import com.qbit.exchanger.user.UserInfo;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
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
	
	@Inject
	private UserDAO userDAO;

	public OrderInfo find(String id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtil.find(entityManagerFactory.createEntityManager(),
					OrderInfo.class, id, null);
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findByStatus(EnumSet<OrderStatus> statuses) {
		if ((statuses == null) || statuses.isEmpty()) {
			return Collections.emptyList();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findByStatus", OrderInfo.class);
			query.setParameter("statuses", statuses);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findByFullStatus(EnumSet<OrderStatus> statuses, boolean inProcess) {
		if ((statuses == null) || statuses.isEmpty()) {
			return Collections.emptyList();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findByFullStatus", OrderInfo.class);
			query.setParameter("statuses", statuses);
			query.setParameter("inProcess", inProcess);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}
	
	public List<OrderInfo> findByUserAndStatus(String userPublicKey, EnumSet<OrderStatus> statuses) {
		if ((userPublicKey == null) || userPublicKey.isEmpty() || (statuses == null) || statuses.isEmpty()) {
			return Collections.emptyList();
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<OrderInfo> query = entityManager.createNamedQuery("OrderInfo.findByUserAndStatus", OrderInfo.class);
			query.setParameter("userPublicKey", userPublicKey);
			query.setParameter("statuses", statuses);
			return query.getResultList();
		} finally {
			entityManager.close();
		}
	}

	public List<OrderInfo> findByUserAndTimestamp(String userPublicKey, Date creationDate) {
		if ((userPublicKey == null) || userPublicKey.isEmpty() || (creationDate == null)) {
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

	public void changeStatus(final String id, final OrderStatus orderStatus, final boolean inProcess) {
		if ((id == null) || (orderStatus == null)) {
			throw new IllegalArgumentException();
		}
		invokeInTransaction(entityManagerFactory, new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
				if (orderInfo == null) {
					return null;
				}
				orderInfo.setStatus(orderStatus);
				orderInfo.setInProcess(inProcess);
				return null;
			}
		});
	}

	public void changeStatusAndInAmount(final String id, final OrderStatus orderStatus, final boolean inProcess,
			final Amount inAmount) {
		if ((id == null) || (orderStatus == null) || (inAmount == null) || !inAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		invokeInTransaction(entityManagerFactory, new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
				if (orderInfo == null) {
					return null;
				}
				orderInfo.setStatus(orderStatus);
				orderInfo.setInProcess(inProcess);
				orderInfo.getInTransfer().setAmount(inAmount);
				return null;
			}
		});
	}

	public void changeStatusAndOutAmount(final String id, final OrderStatus orderStatus, final boolean inProcess,
			final Amount outAmount) {
		if ((id == null) || (orderStatus == null) || (outAmount == null) || !outAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		invokeInTransaction(entityManagerFactory, new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
				if (orderInfo == null) {
					return null;
				}
				orderInfo.setStatus(orderStatus);
				orderInfo.setInProcess(inProcess);
				orderInfo.getOutTransfer().setAmount(outAmount);
				return null;
			}
		});
	}

	public void changeStatusAndAmounts(final String id, final OrderStatus orderStatus, final boolean inProcess,
			final Amount inAmount, final Amount outAmount) {
		if ((id == null) || (orderStatus == null) || (inAmount == null) || !inAmount.isValid()
				|| (outAmount == null) || !outAmount.isValid()) {
			throw new IllegalArgumentException();
		}
		invokeInTransaction(entityManagerFactory, new TrCallable<Void>() {

			@Override
			public Void call(EntityManager entityManager) {
				OrderInfo orderInfo = entityManager.find(OrderInfo.class, id);
				if (orderInfo == null) {
					return null;
				}
				orderInfo.setStatus(orderStatus);
				orderInfo.setInProcess(inProcess);
				orderInfo.getInTransfer().setAmount(inAmount);
				orderInfo.getOutTransfer().setAmount(outAmount);
				return null;
			}
		});
	}

	public OrderInfo create(OrderInfo orderInfo) {
		if (orderInfo == null) {
			throw new IllegalArgumentException("Order is NULL.");
		}
		return create(orderInfo.getUserPublicKey(), orderInfo.getInTransfer(), orderInfo.getOutTransfer());
	}

	public OrderInfo create(final String userPublicKey, final Transfer inTransfer, final Transfer outTransfer) {
		if ((inTransfer == null) || (outTransfer == null)
				|| !inTransfer.isPositive() || !outTransfer.isPositive()
				|| !TransferType.IN.equals(inTransfer.getType())
				|| !TransferType.OUT.equals(outTransfer.getType())
				|| inTransfer.getCurrency().equals(outTransfer.getCurrency())) {
			throw new IllegalArgumentException("Order is inconsistent.");
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<OrderInfo>() {

			@Override
			public OrderInfo call(EntityManager entityManager) {
				UserInfo userInfo = UserDAO.findAndLock(entityManager, userPublicKey);
				if (userInfo == null) {
					return null;
				}
				OrderInfo order = new OrderInfo();
				order.setCreationDate(new Date());
				order.setUserPublicKey(userPublicKey);
				order.setInTransfer(inTransfer);
				order.setOutTransfer(outTransfer);
				order.setStatus(OrderStatus.INITIAL);
				entityManager.persist(order);
				return order;
			}
		});
	}
}
