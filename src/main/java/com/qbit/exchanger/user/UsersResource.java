package com.qbit.exchanger.user;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("users")
public class UsersResource {

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("current")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo current() {
		String userPublicKey = null; // TODO
		return userDAO.getOrCreate(userPublicKey);
	}
}
