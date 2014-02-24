package com.qbit.exchanger.money.yandex;

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
	private static final String CLIENT_ID = "B83AF6B23CA9C5E0CA7AAFC2F1B98CDAEEAD59A49DED9A4BEE52B8F85A19D20B";
	private static final String REDIRECT_URI = "https://localhost:8443/exchanger/webapi/yandex/proceedAuth";
	private static final String STORE_WALLET = "41001954722279";
	private static final String STORE_TOKEN = "41001954722279.41FA1CFDB8228302CA23314BFEF423E2A3A719994AFE60A5"
			+ "6CB63DC438FE08B94CD8AFFC231EEE9404A19F0943D1D2B15E211561AD73A899951C62FE0C9891A761641F089F9C57D0"
			+ "6D5955FCDDB2DA609F6B7986E09EDFDFA580C5F543F4EA2091093BE448338B4D5E93D58D3F484BB804342607C48D1B7C5DF9AF1A86B271C6";
	private static final String OPERATION_DESCRIPTION = "test";

	private final YandexMoney yandexMoney;
	private final Map<String, String> tokens;

	public YandexMoneyService() {
<<<<<<< HEAD
		tokens = new HashMap<String, String>();
=======
		tokens = new ConcurrentHashMap<>();
>>>>>>> origin/Yandex-service
		yandexMoney = new YandexMoneyImpl(CLIENT_ID);
	}

	public String getAuthorizeUri(Boolean mobile, BigDecimal amount) {
		Collection<Permission> scope = getPaymentScope(amount);
		return yandexMoney.authorizeUri(scope, REDIRECT_URI, mobile);
	}

	public String getAuthorizeUri(Boolean mobile) {
		Collection<Permission> scope = getPaymentScope();
		return yandexMoney.authorizeUri(scope, REDIRECT_URI, mobile);
	}

	public BigDecimal getBalance() throws RuntimeException {
		try {
			AccountInfoResponse response = yandexMoney.accountInfo(STORE_TOKEN);
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
			wallet = STORE_WALLET;
		} else {
			token = STORE_TOKEN;
			wallet = transfer.getAddress();
		}

		try {
			RequestPaymentResponse response = requestPayment(token, wallet, transfer.getAmount().toBigDecimal(), OPERATION_DESCRIPTION);
			if (response != null && response.isSuccess()) {
				ProcessPaymentResponse paymentResponse = processPayment(token, response.getRequestId());
				if (paymentResponse != null && paymentResponse.isSuccess()) {
					callback.success();
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
			tokens.remove(transfer.getAddress());
			revokeToken(token);
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
				wallet = STORE_WALLET;
			} else {
				token = STORE_TOKEN;
				wallet = transfer.getAddress();
			}

			try {
				RequestPaymentResponse response = requestPayment(token, wallet, transfer.getAmount().toBigDecimal(), OPERATION_DESCRIPTION);
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
			ReceiveOAuthTokenResponse tokenResponse = yandexMoney.receiveOAuthToken(code, REDIRECT_URI);
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
		List<Permission> permissions = new LinkedList<Permission>();
		permissions.add(new PaymentP2P());
		permissions.add(new AccountInfo());
		permissions.add(new OperationDetails());
		permissions.add(new OperationHistory());
		return Collections.unmodifiableList(permissions);
	}

	private Collection<Permission> getPaymentScope(BigDecimal sum) {
		Permission scope = new Payment(Destination.toAccount, STORE_WALLET, sum.toString());
		return Collections.singletonList(scope);
	}

	private Collection<Permission> getPaymentScope() {
		Payment scope = new Payment();
		scope.toAccount(STORE_WALLET);
		Permission result = scope;
		return Collections.singletonList(result);
	}
}
