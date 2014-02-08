package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.service.OrderService;
import com.qbit.exchanger.order.service.OrderServiceSecurityException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("orders")
public class OrdersResource {
	
	@Inject
	private OrderService orderService;
	
	@GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderInfo getActiveOrder(@QueryParam("userPublicKey") String userPublicKey) throws OrderServiceSecurityException {
		return orderService.getActiveOrder(userPublicKey);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public OrderInfo create(OrderInfo order) throws OrderServiceSecurityException {
		return orderService.create(order);
	}
}
