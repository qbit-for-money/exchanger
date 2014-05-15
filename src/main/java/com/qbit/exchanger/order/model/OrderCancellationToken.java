package com.qbit.exchanger.order.model;

import com.qbit.commons.model.Identifiable;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Александр
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "OrderCancellationToken.find",
			query = "SELECT t FROM OrderCancellationToken t WHERE t.orderId = :orderId and t.creationDate > :deadline"),
	@NamedQuery(name = "OrderCancellationToken.cleanUp",
			query = "DELETE FROM OrderCancellationToken t WHERE t.creationDate < :deadline")
})
@Access(AccessType.FIELD)
public class OrderCancellationToken implements Identifiable<String>, Serializable {

	@Id
	private String token;

	private String orderId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;

	@Override
	public String getId() {
		return token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
