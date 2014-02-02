package com.qbit.exchanger.utils;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author Александр
 */
public final class DAOUtils {
	
	private DAOUtils() {
	}
	
	public static <T> T get(EntityManager entityManager, Class<T> entityClass, Object id, T empty) {
		try {
			T result = entityManager.find(entityClass, id);
			return ((result != null) ? result : empty);
		} catch (Exception ex) {
			return empty;
		}
	}
	
	public static <T> List<T> findAll(EntityManager entityManager, Class<T> entityClass) {
		CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
		criteriaQuery.select(criteriaQuery.from(entityClass));
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	public static <T> List<T> findRange(EntityManager entityManager, Class<T> entityClass, int from, int to) {
		CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
		criteriaQuery.select(criteriaQuery.from(entityClass));
		Query query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(to - from + 1);
		query.setFirstResult(from);
		return query.getResultList();
	}

	public static <T> int count(EntityManager entityManager, Class<T> entityClass) {
		CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
		criteriaQuery.select(entityManager.getCriteriaBuilder().count(criteriaQuery.from(entityClass)));
		Query query = entityManager.createQuery(criteriaQuery);
		return ((Long) query.getSingleResult()).intValue();
	}
}
