package com.qbit.exchanger.utils;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author Александр
 */
public final class DAOUtils {
	
	private DAOUtils() {
	}
	
	public static <T> T find(EntityManager entityManager, Class<T> entityClass, Object id, T empty) {
		try {
			T result = entityManager.find(entityClass, id);
			if (result != null) {
				entityManager.detach(result);
				return result;
			} else {
				return empty;
			}
		} catch (Exception ex) {
			return empty;
		}
	}
	
	public static <T> List<T> findAll(EntityManager entityManager, Class<T> entityClass) {
		CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(entityClass);
		criteriaQuery.select(criteriaQuery.from(entityClass));
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	public static <T> List<T> findRange(EntityManager entityManager, Class<T> entityClass, int from, int to) {
		CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(entityClass);
		criteriaQuery.select(criteriaQuery.from(entityClass));
		TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(to - from + 1);
		query.setFirstResult(from);
		return query.getResultList();
	}

	public static <T> int count(EntityManager entityManager, Class<T> entityClass) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(entityClass)));
		Query query = entityManager.createQuery(criteriaQuery);
		return ((Long) query.getSingleResult()).intValue();
	}
}
