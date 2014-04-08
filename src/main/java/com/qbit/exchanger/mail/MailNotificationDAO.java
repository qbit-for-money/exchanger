package com.qbit.exchanger.mail;

import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.dao.util.TrCallable;
import com.qbit.exchanger.order.model.OrderStatus;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Alexander_Sergeev
 */
@Singleton
public class MailNotificationDAO {
	
	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	public boolean isNotificationSent(String orderId, OrderStatus orderStatus) {
		if ((orderId == null) || (orderStatus == null)) {
			return false;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			MailNotificationPK notificationPK = new MailNotificationPK();
			notificationPK.setOrderId(orderId);
			notificationPK.setOrderStatus(orderStatus);
			MailNotification notification = entityManager.find(MailNotification.class, notificationPK);
			return (notification != null);
		} finally {
			entityManager.close();
		}
	}
	
	public MailNotification registerNotification(final String orderId, final OrderStatus orderStatus) {
		if ((orderId == null) || (orderStatus == null)) {
			throw new IllegalArgumentException("OrderId or OrderStatus is NULL.");
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<MailNotification>() {
			@Override
			public MailNotification call(EntityManager entityManager) {
				MailNotification notification = new MailNotification();
				notification.setOrderId(orderId);
				notification.setOrderStatus(orderStatus);
				entityManager.persist(notification);
				return notification;
			}
		});
	}
}
