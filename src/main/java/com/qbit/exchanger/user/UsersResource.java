package com.qbit.exchanger.user;

import com.qbit.exchanger.mail.MailService;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
		return userDAO.find(userPublicKey);
	}
}
