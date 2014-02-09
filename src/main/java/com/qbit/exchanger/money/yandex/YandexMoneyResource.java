package com.qbit.exchanger.money.yandex;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("yandex")
public class YandexMoneyResource {

	@Inject
	private YandexMoneyService yandexMoneyService;

	@GET
	@Path("authorizeUrl")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUrl(@QueryParam("mobile") boolean mobile) {
		return yandexMoneyService.getAuthorizeUri(mobile);
	}
}
