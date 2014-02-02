package com.qbit.exchanger.user;

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
@Path("user")
public class UserResource {
	
	@Inject
	private UserDAO userDAO;
	
	@GET
    @Path("{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
	public UserInfo get(@PathParam("publicKey") String publicKey) {
		return userDAO.get(publicKey);
	}

	@POST
    @Produces(MediaType.APPLICATION_JSON)
	public UserInfo create() {
		return userDAO.create();
	}

	@PUT
    @Path("{publicKey}")
    @Consumes(MediaType.APPLICATION_JSON)
	public void edit(@PathParam("publicKey") String publicKey, UserInfo userInfo) {
		userInfo.setPublicKey(publicKey);
		userDAO.edit(userInfo);
	}
}
