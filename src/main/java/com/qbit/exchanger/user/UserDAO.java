package com.qbit.exchanger.user;

import static com.qbit.exchanger.dao.util.DAOUtil.invokeInTransaction;
import com.qbit.exchanger.dao.util.TrCallable;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

/**
 *
 * @author Александр
 */
@Singleton
public class UserDAO {

	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	public static UserInfo findAndLock(EntityManager entityManager, String publicKey) {
		if ((entityManager == null) || (publicKey == null) || publicKey.isEmpty()) {
			return null;
		}
		return entityManager.find(UserInfo.class, publicKey, LockModeType.PESSIMISTIC_WRITE);
	}

	public UserInfo find(String publicKey) {
		if ((publicKey == null) || publicKey.isEmpty()) {
			return null;
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			return entityManager.find(UserInfo.class, publicKey);
		} finally {
			entityManager.close();
		}
	}
	
	public UserInfo getOrCreate(final String publicKey) {
		return invokeInTransaction(entityManagerFactory, new TrCallable<UserInfo>() {

			@Override
			public UserInfo call(EntityManager entityManager) {
				UserInfo user = entityManager.find(UserInfo.class, publicKey, LockModeType.PESSIMISTIC_WRITE);;
				if (user == null) {
					user = new UserInfo();
					user.setPublicKey(publicKey);
					user.setRegistrationDate(new Date());
					entityManager.persist(user);
				}
				return user;
			}
		});
	}
}
