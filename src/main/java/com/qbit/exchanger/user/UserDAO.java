package com.qbit.exchanger.user;

import com.qbit.exchanger.utils.DAOUtils;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class UserDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;

	public UserInfo get(String publicKey) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtils.get(entityManagerFactory.createEntityManager(),
					UserInfo.class, publicKey, UserInfo.EMPTY);
		} finally {
			entityManager.close();
		}
	}

	public UserInfo create() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			UserInfo user = new UserInfo();
			user.setPublicKey(UUID.randomUUID().toString());
			user.setRegistrationDate(new Date());
			entityManager.persist(user);
			entityManager.getTransaction().commit();
			return user;
		} finally {
			entityManager.close();
		}
	}

	public UserInfo edit(UserInfo user) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			user = entityManager.merge(user);
			entityManager.getTransaction().commit();
			return user;
		} finally {
			entityManager.close();
		}
	}
}
