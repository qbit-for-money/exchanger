package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.service.OrderService;
import com.qbit.exchanger.order.service.exception.OrderServiceException;
import static com.qbit.exchanger.rest.util.RESTUtil.*;
import java.text.ParseException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	public OrderInfo getActiveByUser(@QueryParam("userPublicKey") String userPublicKey) throws OrderServiceException {
		return orderService.getActiveByUser(userPublicKey);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo getByUserAndTimestamp(@QueryParam("userPublicKey") String userPublicKey,
			@QueryParam("creationDate") String creationDateStr) throws OrderServiceException, ParseException {
		return orderService.getByUserAndTimestamp(userPublicKey, toDate(creationDateStr));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public OrderInfo create(OrderInfo order) throws OrderServiceException {
		return orderService.create(order);
	}
}
