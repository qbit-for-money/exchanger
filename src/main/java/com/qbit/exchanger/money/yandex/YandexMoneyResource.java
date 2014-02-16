package com.qbit.exchanger.money.yandex;

import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("yandex")
public class YandexMoneyResource {

	private static final String REDIRECT_PATH = "https://localhost:8443/exchanger/";
	private static final String REDIRECT_ROUTE = "/steps/yandex";
	private static final String WALLET_PARAM_NAME = "wallet";

	@Inject
	private YandexMoneyService yandexMoneyService;

	@GET
	@Path("authorizeUrl")
	@Produces(MediaType.APPLICATION_JSON)
	public AuthorizeUrlWrapper getUrl(@QueryParam("mobile") boolean mobile) {
		AuthorizeUrlWrapper urlWrapper = new AuthorizeUrlWrapper();
		urlWrapper.setUrl(yandexMoneyService.getAuthorizeUri(mobile));
		return urlWrapper;
	}

	@GET
	@Path("proceedAuth")
	public Response proceedAuth(@QueryParam("code") String code, @QueryParam("error") String error) {
		String wallet = null;
		if (code != null) {
			wallet = yandexMoneyService.exchangeAndStoreToken(code);
		} else {
			throw new RuntimeException((error != null) ? error : "code is empty!");
		}

		// hack for angularjs without html5 mode
		URI uri = UriBuilder.fromPath(REDIRECT_PATH).fragment("{route}").build(REDIRECT_ROUTE + "?" + WALLET_PARAM_NAME + "=" + wallet);

//		URI uri = UriBuilder.fromPath(REDIRECT_PATH).queryParam(WALLET_PARAM_NAME, wallet).build();
		return Response.seeOther(uri).build();
	}
}
