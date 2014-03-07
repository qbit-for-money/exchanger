package com.qbit.exchanger.money.yandex;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import ru.yandex.money.api.YandexMoney;
import ru.yandex.money.api.YandexMoneyImpl;
import ru.yandex.money.api.enums.Destination;
import ru.yandex.money.api.response.AccountInfoResponse;
import ru.yandex.money.api.response.ProcessPaymentResponse;
import ru.yandex.money.api.response.ReceiveOAuthTokenResponse;
import ru.yandex.money.api.response.RequestPaymentResponse;
import ru.yandex.money.api.rights.AccountInfo;
import ru.yandex.money.api.rights.IdentifierType;
import ru.yandex.money.api.rights.OperationDetails;
import ru.yandex.money.api.rights.OperationHistory;
import ru.yandex.money.api.rights.Payment;
import ru.yandex.money.api.rights.PaymentP2P;
import ru.yandex.money.api.rights.Permission;

@Singleton
public class YandexMoneyService implements MoneyService {

	private static final Logger LOGGER = Logger.getLogger(YandexMoneyService.class.getName());

	private YandexMoney yandexMoney;
	private final Map<String, String> tokens = new ConcurrentHashMap<>();

	@Inject
	private Env env;

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

	public BigDecimal getBalance() throws RuntimeException {
		try {
			AccountInfoResponse response = yandexMoney.accountInfo(env.getYandexToken());
			return response.getBalance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void process(Transfer transfer, MoneyTransferCallback callback) {
		String wallet;
		String token;
		if (TransferType.IN.equals(transfer.getType())) {
			token = tokens.get(transfer.getAddress());
			wallet = env.getYandexWallet();
		} else {
			token = env.getYandexToken();
			wallet = transfer.getAddress();
		}

		try {
			RequestPaymentResponse response = requestPayment(token, wallet, transfer.getAmount().toBigDecimal(), env.getYandexOperationDescription());
			if (response != null && response.isSuccess()) {
				ProcessPaymentResponse paymentResponse = processPayment(token, response.getRequestId());
				if (paymentResponse != null && paymentResponse.isSuccess()) {
					callback.success(null);
				} else {
					callback.error(paymentResponse != null ? paymentResponse.getError().getCode() : null);
				}
			} else {
				callback.error(response != null ? response.getError().getCode() : null);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			callback.error(e.getMessage());
		} finally {
			String removedToken = tokens.remove(transfer.getAddress());
			if (removedToken != null) {
				revokeToken(removedToken);
			}
		}
	}

	@Override
	public boolean test(Transfer transfer) {
		boolean result = false;
		if ((transfer != null) && transfer.isValid()) {
			String wallet;
			String token;
			if (TransferType.IN.equals(transfer.getType())) {
				token = tokens.get(transfer.getAddress());
				wallet = env.getYandexWallet();
			} else {
				token = env.getYandexToken();
				wallet = transfer.getAddress();
			}

			try {
				RequestPaymentResponse response = requestPayment(token, wallet, transfer.getAmount().toBigDecimal(), env.getYandexOperationDescription());
				if ((response != null) && response.isSuccess()) {
					result = true;
				}
			} catch (Exception e) {
				LOGGER.severe(e.getMessage());
				result = false;
			}
		}
		return result;
	}

	public String exchangeAndStoreToken(String tempCode) {
		String wallet = null;
		if (tempCode != null) {
			String token = exchangeToken(tempCode);
			wallet = getWalletFromToken(token);
			tokens.put(wallet, token);
		}
		return wallet;
	}

	private String exchangeToken(String code) throws RuntimeException {
		String token = null;
		try {
			ReceiveOAuthTokenResponse tokenResponse = yandexMoney.receiveOAuthToken(code, env.getYandexRedirectUrl());
			if (tokenResponse != null && tokenResponse.isSuccess()) {
				token = tokenResponse.getAccessToken();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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

	private void revokeToken(String token) {
		try {
			yandexMoney.revokeOAuthToken(token);
		} catch (Exception ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	private RequestPaymentResponse requestPayment(String token, String wallet, BigDecimal amount, String description) throws RuntimeException {
		RequestPaymentResponse response = null;
		try {
			response = yandexMoney.requestPaymentP2PDue(token, wallet, IdentifierType.account, amount, description, description, null);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return response;
	}

	private ProcessPaymentResponse processPayment(String token, String requestId) throws RuntimeException {
		ProcessPaymentResponse response = null;
		try {
			response = yandexMoney.processPaymentByWallet(token, requestId);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return response;
	}

	/**
	 * Creates permissions for this application.
	 *
	 * @return
	 */
	private Collection<Permission> getAppPaymentScope() {
		List<Permission> permissions = new LinkedList<>();
		permissions.add(new PaymentP2P());
		permissions.add(new AccountInfo());
		permissions.add(new OperationDetails());
		permissions.add(new OperationHistory());
		return Collections.unmodifiableList(permissions);
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

	@Override
	public String generateAddress() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
