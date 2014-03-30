package com.qbit.exchanger.user;

import com.qbit.exchanger.auth.AuthFilter;
import javax.inject.Inject;
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
public class UsersResource {
	
	@Context
	private HttpServletRequest hsRequest;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("current")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo current() {
		String userPublicKey = null;
		Object objUserId = hsRequest.getSession().getAttribute(AuthFilter.USER_ID);
		if (objUserId != null) {
			userPublicKey = (String) objUserId;
		}
		return userDAO.getOrCreate(userPublicKey);
	}
}
