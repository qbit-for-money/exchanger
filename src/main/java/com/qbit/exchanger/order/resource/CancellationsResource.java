package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.auth.AuthFilter;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.service.OrderService;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("cancellations")
@Singleton
public class CancellationsResource {
	
	@Context
	private HttpServletRequest request;
	
	@Inject
	private OrderService orderService;
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo sendCancellationToken() throws OrderServiceException {
		return orderService.sendCancellationToken(AuthFilter.getUserId(request));
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo cancel(@QueryParam("token") String token, @QueryParam("address") String address) throws Exception {
		return orderService.cancel(AuthFilter.getUserId(request), token, address);
	}
}
