package com.qbit.exchanger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("env")
public class EnvResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Env get() {
		return Env.inst();
	}
}
