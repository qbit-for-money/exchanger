package com.qbit.exchanger.services.core;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

	@Override
	public Response toResponse(ProcessingException e) {
		return Response.ok().entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
	}

}
