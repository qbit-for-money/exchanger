package com.qbit.exchanger.user;

import com.qbit.exchanger.auth.AuthFilter;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
	private HttpServletRequest request;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("current")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo current() {
		return userDAO.find(AuthFilter.getUserId(request));
	}
	
	@POST
	@Path("logout")
	public boolean logout() {
		if(request.getSession().getAttribute(AuthFilter.USER_ID_KEY) != null) {
			request.getSession().removeAttribute(AuthFilter.USER_ID_KEY);
			return true;
		}
		return false;
	}
}
