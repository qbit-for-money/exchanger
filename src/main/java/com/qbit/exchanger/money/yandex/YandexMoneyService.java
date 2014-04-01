package com.qbit.exchanger.money.yandex;

import com.qbit.exchanger.buffer.BufferDAO;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Currency;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.money.api.InsufficientScopeException;
import ru.yandex.money.api.InvalidTokenException;
import ru.yandex.money.api.YandexMoney;
import ru.yandex.money.api.YandexMoneyImpl;
import ru.yandex.money.api.enums.Destination;
import ru.yandex.money.api.response.AccountInfoResponse;
import ru.yandex.money.api.response.ProcessPaymentResponse;
import ru.yandex.money.api.response.ReceiveOAuthTokenResponse;
import ru.yandex.money.api.response.RequestPaymentResponse;
import ru.yandex.money.api.rights.IdentifierType;
import ru.yandex.money.api.rights.Payment;
import ru.yandex.money.api.rights.Permission;

@Singleton
public class YandexMoneyService implements MoneyService {

	private final Logger logger = LoggerFactory.getLogger(YandexMoneyService.class);

	private YandexMoney yandexMoney;
	private final Map<String, String> tokens = new ConcurrentHashMap<>();

	@Inject
	private Env env;
	@Inject
	private BufferDAO bufferDAO;

	@PostConstruct
	public void init() {
		yandexMoney = new YandexMoneyImpl(env.getYandexClientId());
	}

	public String getAuthorizeUri(boolean mobile, BigDecimal amount) {
		Collection<Permission> scope = getPaymentScope(amount);
		return yandexMoney.authorizeUri(scope, env.getYandexRedirectUrl(), mobile);
	}

	public String getAuthorizeUri(boolean mobile) {
		Collection<Permission> scope = getPaymentScope();
		return yandexMoney.authorizeUri(scope, env.getYandexRedirectUrl(), mobile);
	}

	@Override
	public Amount getBalance() {
		try {
			AccountInfoResponse response = yandexMoney.accountInfo(env.getYandexToken());
			return new Amount(response.getBalance(), Currency.YANDEX_RUB.getCentsInCoin());
		} catch (IOException | InsufficientScopeException | InvalidTokenException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	@Override
	public void sendMoney(String address, Amount amount) throws Exception {
		sendMoney(address, amount, false);
	}
	
	@Override
	public void sendMoney(String address, Amount amount, boolean unreserve) throws Exception {
		if ((address == null) || (amount == null) || !amount.isPositive()) {
			throw new IllegalArgumentException("Invalid transfer");
		}

		try {
			String token = env.getYandexToken();
			RequestPaymentResponse response = requestPayment(token, address, amount.toBigDecimal(), env.getYandexOperationDescription());
			processPayment(token, response);
		} finally {
			if (unreserve) {
				bufferDAO.deleteReservation(Currency.YANDEX_RUB, amount);
			}
			String removedToken = tokens.remove(address);
			if (removedToken != null) {
				revokeToken(removedToken);
			}
		}
	}
	
	@Override
	public Amount receiveMoney(String address, Amount amount) throws Exception {
		if ((address == null) || (amount == null) || !amount.isPositive()) {
			throw new IllegalArgumentException("Invalid transfer");
		}
		
		try {
			String token = tokens.get(address);
			RequestPaymentResponse response = requestPayment(token, env.getYandexWallet(), amount.toBigDecimal(), env.getYandexOperationDescription());
			processPayment(token, response);
			return amount;
		} finally {
			String removedToken = tokens.remove(address);
			if (removedToken != null) {
				revokeToken(removedToken);
			}
		}
	}
	
	@Override
	public boolean reserve(String address, Amount amount) {
		if ((address == null) || (amount == null) || !amount.isPositive()) {
			return false;
		}
		boolean result;
		try {
			String token = env.getYandexToken();
			RequestPaymentResponse response = requestPayment(token, address, amount.toBigDecimal(), env.getYandexOperationDescription());
			if ((response != null) && response.isSuccess()) {
				result = bufferDAO.reserveAmount(Currency.YANDEX_RUB, getBalance(), amount);
			} else {
				result = false;
			}
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			result = false;
		}
		return result;
	}

	private void processPayment(String token, RequestPaymentResponse response) throws InsufficientScopeException, IOException, InvalidTokenException {
		if ((response != null) && response.isSuccess()) {
			ProcessPaymentResponse paymentResponse = processPayment(token, response.getRequestId());
			if (paymentResponse != null) {
				if (paymentResponse.isSuccess()) {
					// success
				} else {
					if (paymentResponse.getError() != null) {
						throw new IOException(paymentResponse.getError().getCode());
					} else {
						throw new IOException("Process payment response failed.");
					}
				}
			} else {
				throw new IOException("Process payment response failed.");
			}
		} else {
			if (response != null) {
				if (response.getError() != null) {
					throw new IOException(response.getError().getCode());
				} else {
					throw new IOException("Payment response failed.");
				}
			} else {
				throw new IOException("Payment response failed.");
			}
		}
	}

	public String exchangeAndStoreToken(String tempCode) throws IOException {
		String wallet = null;
		if (tempCode != null) {
			String token = exchangeToken(tempCode);
			wallet = getWalletFromToken(token);
			tokens.put(wallet, token);
		}
		return wallet;
	}

	private String exchangeToken(String code) throws IOException {
		String token = null;
		ReceiveOAuthTokenResponse tokenResponse = yandexMoney.receiveOAuthToken(code, env.getYandexRedirectUrl());
		if ((tokenResponse != null) && tokenResponse.isSuccess()) {
			token = tokenResponse.getAccessToken();
		}
		return token;
	}

	private String getWalletFromToken(String token) {
		if (token == null) {
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(token, ".", false);
		return tokenizer.nextToken();
	}

	private void revokeToken(String token) throws IOException, InvalidTokenException {
		yandexMoney.revokeOAuthToken(token);
	}

	private RequestPaymentResponse requestPayment(String token, String wallet, BigDecimal amount, String description) throws IOException, InvalidTokenException, InsufficientScopeException {
		return yandexMoney.requestPaymentP2PDue(token, wallet, IdentifierType.account, amount, description, description, null);
	}

	private ProcessPaymentResponse processPayment(String token, String requestId) throws IOException, InsufficientScopeException, InvalidTokenException {
		return yandexMoney.processPaymentByWallet(token, requestId);
	}

	private Collection<Permission> getPaymentScope(BigDecimal sum) {
		Permission scope = new Payment(Destination.toAccount, env.getYandexWallet(), sum.toString());
		return Collections.singletonList(scope);
	}

	private Collection<Permission> getPaymentScope() {
		Payment scope = new Payment();
		scope.toAccount(env.getYandexWallet());
		Permission result = scope;
		return Collections.singletonList(result);
	}
}
