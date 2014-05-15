package com.qbit.exchanger.order.resource;

import com.qbit.commons.auth.AuthFilter;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.service.OrderService;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import static com.qbit.commons.rest.util.RESTUtil.*;
import java.text.ParseException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
@Path("orders")
@Singleton
public class OrdersResource {
	
	@Context
	private HttpServletRequest request;
	
	@Inject
	private OrderService orderService;

	@GET
	@Path("active")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo getActive() throws OrderServiceException {
		return orderService.getActiveByUser(AuthFilter.getUserId(request));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo getByTimestamp(@QueryParam("creationDate") String creationDateStr) throws OrderServiceException, ParseException {
		return orderService.getByUserAndTimestamp(AuthFilter.getUserId(request), toDate(creationDateStr));
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo create(OrderInfo order) throws OrderServiceException {
		return orderService.create(AuthFilter.getUserId(request), order);
	}
}
