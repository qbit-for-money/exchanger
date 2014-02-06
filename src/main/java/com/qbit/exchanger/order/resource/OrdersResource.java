package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path(OrdersResource.BASE_PATH)
public class OrdersResource {
	
	public static final String BASE_PATH = "orders";
	
	@Inject
	private OrderDAO orderDAO;
	
	@GET
	@Path("external/{externalId}")
    @Produces(MediaType.APPLICATION_JSON)
	public List<OrderInfo> findByExternalId(@QueryParam("userPublicKey") String userPublicKey, 
			@PathParam("externalId") String externalId) {
		return orderDAO.findByExternalId(userPublicKey, externalId);
	}
	
	@GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
	public List<OrderInfo> findActiveByUser(@QueryParam("userPublicKey") String userPublicKey) {
		return orderDAO.findActiveByUser(userPublicKey);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public OrderInfo create(OrderInfo order) {
		return orderDAO.create(order);
	}
}
