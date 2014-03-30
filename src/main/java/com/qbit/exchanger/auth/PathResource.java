package com.qbit.exchanger.auth;

import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Alexander_Sergeev
 */
@Path("index")
public class PathResource {
	
	@GET
	@Produces("text/html")
	public Response index() {
		URI requestUri = URI.create("http://localhost:8084/exchanger/");
		return Response.seeOther(requestUri).build();
	}
}
