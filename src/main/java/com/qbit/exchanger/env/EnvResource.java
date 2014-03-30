package com.qbit.exchanger.env;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("env")
@Singleton
public class EnvResource {

	@Inject
	private Env env;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Env get() {
		return env;
	}
}
