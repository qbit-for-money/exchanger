package com.qbit.exchanger.money.yandex;

import com.qbit.exchanger.env.Env;
import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("yandex")
@Singleton
public class YandexMoneyResource {

	private static final String WALLET_PARAM_NAME = "wallet";

	@Inject
	private YandexMoneyService yandexMoneyService;

	@Inject
	private Env env;

	@GET
	@Path("authorizeUrl")
	@Produces(MediaType.APPLICATION_JSON)
	public URL getAuthorizeUrl(@QueryParam("mobile") boolean mobile) {
		URL urlWrapper = new URL();
		urlWrapper.setUrl(yandexMoneyService.getAuthorizeUri(mobile));
		return urlWrapper;
	}

	@GET
	@Path("proceedAuth")
	public Response proceedAuth(@QueryParam("code") String code, @QueryParam("error") String error) throws IOException {
		String wallet = null;
		if (code != null) {
			wallet = yandexMoneyService.exchangeAndStoreToken(code);
		} else {
			throw new RuntimeException((error != null) ? error : "code is empty!");
		}
		String redirectUrl = env.getYandexResourceRedirectUrl();
		String redirectRoute = env.getYandexResourceRedirectRoute();

		// hack for angularjs without html5 mode
		URI uri = UriBuilder.fromPath(redirectUrl).fragment("{route}").build(redirectRoute + "?" + WALLET_PARAM_NAME + "=" + wallet);
//		URI uri = UriBuilder.fromPath(REDIRECT_PATH).queryParam(WALLET_PARAM_NAME, wallet).build();
		return Response.seeOther(uri).build();
	}
}
