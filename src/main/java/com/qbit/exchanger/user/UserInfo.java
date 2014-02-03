package com.qbit.exchanger.user;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Entity
@XmlRootElement
public class UserInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final UserInfo EMPTY = new UserInfo();
	
	@Id
	private String publicKey;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date registrationDate;
	
	private String name;
	
	private String email;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
		return "UserInfo{" + "publicKey=" + publicKey + '}';
	}
}
