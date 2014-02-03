package com.qbit.exchanger.services.yandex;

import com.qbit.exchanger.services.core.OperationStatus;
import com.qbit.exchanger.services.core.OperationResult;
import com.qbit.exchanger.services.core.ProcessingException;
import java.math.BigDecimal;
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
	@Path("getUrl")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUrl(@QueryParam("mobile") boolean mobile) {
		return yandexMoneyService.getAuthorizeUri(mobile);
	}

	@GET
	@Path("receive")
	@Produces(MediaType.APPLICATION_JSON)
	public OperationResult proceedPayment(@QueryParam("code") String tempCode, @QueryParam("error") String error) throws ProcessingException {
		OperationResult result = new OperationResult(OperationStatus.IN_PROGRESS);
		if (tempCode != null) {
			result = yandexMoneyService.receiveMoney(tempCode, BigDecimal.valueOf(5));
		} else {
			result.setStatus(OperationStatus.ERROR);
			result.setText(error);
		}
		return result;
	}

	@GET
	@Path("send")
	@Produces(MediaType.APPLICATION_JSON)
	public OperationResult send() throws ProcessingException {
		OperationResult result = yandexMoneyService.sendMoney("41001960727686", BigDecimal.valueOf(5));
		return result;
	}
}
