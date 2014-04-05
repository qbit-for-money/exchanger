package com.qbit.exchanger.user;

import com.qbit.exchanger.common.model.Identifiable;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Entity
@Access(AccessType.FIELD)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInfo implements Identifiable<String>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final UserInfo EMPTY = new UserInfo();

	@Id
	private String publicKey;

	@Temporal(TemporalType.TIMESTAMP)
	private Date registrationDate;

	@Override
	public String getId() {
		return publicKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 11 * hash + Objects.hashCode(this.publicKey);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UserInfo other = (UserInfo) obj;
		if (!Objects.equals(this.publicKey, other.publicKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserInfo{" + "publicKey=" + publicKey + ", registrationDate=" + registrationDate + '}';
	}
}
