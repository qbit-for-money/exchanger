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

	@Inject
	private MailService mailService;

	@GET
	@Path("{publicKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserInfo get(@PathParam("publicKey") String publicKey) {
		return userDAO.find(publicKey);
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
		if (userInfo == null) {
			return;
		}
		userInfo.setPublicKey(publicKey);
		userDAO.edit(userInfo);
		sendMailAboutUserProfileUpdate(userInfo);
	}

	private void sendMailAboutUserProfileUpdate(UserInfo userInfo) {
		if ((userInfo.getEmail() != null) && !userInfo.getEmail().isEmpty()) {
			mailService.send(userInfo.getEmail(), "Welcome to Bitgates",
				"Welcome to Bitgates\n\n"
				+ "Hello, " + userInfo.getEmail() + ".\n"
				+ "Your public key: " + userInfo.getPublicKey() + "\n\n"
				+ "http://bitgates.com/");
		}
	}
}
