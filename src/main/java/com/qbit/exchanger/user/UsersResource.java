package com.qbit.exchanger.user;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("users")
@Singleton
public class UsersResource {
	
	@Context
	HttpServletRequest hsRequest;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("current")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo current() {
		String userPublicKey = null;
		Object objUserId = hsRequest.getSession().getAttribute("user_id");
		if (objUserId != null) {
			userPublicKey = (String) objUserId;
		}
		return userDAO.getOrCreate(userPublicKey);
	}
}
