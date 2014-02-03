package com.qbit.exchanger.order.resource;

import com.qbit.exchanger.common.model.ResourceLink;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.utils.RESTUtils;
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
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@Path(OrdersResource.BASE_PATH)
public class OrdersResource {
	
	@XmlRootElement
	public static class OrderLinksList implements Serializable {

		private List<ResourceLink> orders;

		public OrderLinksList() {
		}

		public OrderLinksList(List<OrderInfo> orders) {
			this.orders = RESTUtils.toLinks(OrdersResource.BASE_PATH, orders);
		}

		public List<ResourceLink> getOrders() {
			return orders;
		}

		public void setOrders(List<ResourceLink> orders) {
			this.orders = orders;
		}
	}
	
	public static final String BASE_PATH = "orders";
	
	@Inject
	private OrderDAO orderDAO;
	
	@GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderInfo get(@PathParam("id") String id) {
		return orderDAO.find(id);
	}
	
	@GET
	@Path("external/{externalId}")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderLinksList findByExternalId(@PathParam("externalId") String externalId) {
		return new OrderLinksList(orderDAO.findByExternalId(externalId));
	}
	
	@GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderLinksList findActiveByUser(@QueryParam("userPublicKey") String userPublicKey) {
		return new OrderLinksList(orderDAO.findActiveByUser(userPublicKey));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public OrderInfo create(OrderInfo order) {
		return orderDAO.create(order);
	}
}
