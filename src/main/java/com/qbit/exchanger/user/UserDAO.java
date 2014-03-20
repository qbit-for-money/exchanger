package com.qbit.exchanger.user;

import com.qbit.exchanger.dao.util.DAOUtil;
import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.dao.util.TrCallable;
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

	public boolean isExists(String publicKey) {
		return (find(publicKey) != null);
	}

	public UserInfo find(String publicKey) {
		if ((publicKey == null) || publicKey.isEmpty()) {
			return null;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return DAOUtil.find(entityManagerFactory.createEntityManager(),
					UserInfo.class, publicKey, UserInfo.EMPTY);
		} finally {
			entityManager.close();
		}
	}

	public UserInfo create() {
		return invokeInTransaction(entityManagerFactory, new TrCallable<UserInfo>() {

			@Override
			public UserInfo call(EntityManager entityManager) {
				UserInfo user = new UserInfo();
				user.setPublicKey(UUID.randomUUID().toString());
				user.setRegistrationDate(new Date());
				entityManager.persist(user);
				return user;
			}
		});
	}

	public UserInfo edit(final UserInfo user) {
		if ((user == null) || !isExists(user.getPublicKey())) {
			return null;
		}
		return invokeInTransaction(entityManagerFactory, new TrCallable<UserInfo>() {

			@Override
			public UserInfo call(EntityManager entityManager) {
				return entityManager.merge(user);
			}
		});
	}
}
