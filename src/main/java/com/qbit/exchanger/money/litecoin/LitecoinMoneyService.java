package com.qbit.exchanger.money.litecoin;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.litecoin.core.*;
import com.google.litecoin.kits.WalletAppKit;
import com.google.litecoin.params.MainNetParams;
import com.google.litecoin.params.TestNet2Params;
import com.google.litecoin.script.Script;
import com.google.litecoin.utils.BriefLogFormatter;
import com.qbit.exchanger.money.core.CryptoService;
import com.qbit.exchanger.money.core.WTransaction;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.AddressInfo;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.AtomicBigDecimal;
import com.qbit.exchanger.money.model.Currency;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.qbit.exchanger.rest.util.RESTClientUtil.*;

/**
 * LITECOIN
 *
 * @author Alexander_Sergeev
 */
@Singleton
public class LitecoinMoneyService implements CryptoService {

	public static final BigInteger COIN = new BigInteger("100000000", 10);
	public static final BigInteger MIN_FEE = new BigInteger("100000", 10);

	public final static String BLOCKR_API_BASE_URL = "https://ltc.blockr.io/api/v1/address/balance/";

	private final Logger logger = LoggerFactory.getLogger(LitecoinMoneyService.class);

	@Inject
	private Env env;

	private NetworkParameters parameters;
	private WalletAppKit kit;
	private AtomicBigDecimal balance;

	@PostConstruct
	public void init() {
		BriefLogFormatter.init();
		if (env.isLitecoinTestnet()) {
			parameters = TestNet2Params.get();
		} else {
			parameters = MainNetParams.get();
		}
		kit = new WalletAppKit(parameters, new File(env.getLitecoinWalletPath()), "sample");
		kit.startAndWait();
		balance = new AtomicBigDecimal(getWalletBalance());
	}

	@PreDestroy
	public void destroy() {
		try {
			kit.stopAndWait();
		} catch (Throwable ex) {
			// Do nothing
		}
	}
	
	private BigDecimal getWalletBalance() {
		BigInteger walletBalance = getWallet().getBalance().subtract(MIN_FEE).max(BigInteger.ZERO);
		return new BigDecimal(Utils.bitcoinValueToFriendlyString(walletBalance));
	}

	@Override
	public Amount getBalance() {
		return new Amount(balance.getValue(), Currency.LITECOIN.getCentsInCoin());
	}
	
	@Override
	public void addBalance(Amount amount) {
		balance.addAndGet(amount.toBigDecimal());
	}

	@Override
	public Amount getBalance(String address) {
		String path = (address + "?confirmations=2");

		try {
			AddressInfo addressInfo = get(BLOCKR_API_BASE_URL, path, AddressInfo.class, true);
			if (logger.isInfoEnabled()) {
				logger.info("[{}] Address Info: ", addressInfo, address);
			}
			return new Amount(new BigDecimal(addressInfo.getData().getBalance()), Currency.LITECOIN.getCentsInCoin());
		} catch (Exception ex) {
			if (logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
			return Amount.zero(Currency.LITECOIN.getCentsInCoin());
		}
	}

	@Override
	public String generateAddress() {
		ECKey key = new ECKey();
		getWallet().addKey(key);
		Address address = key.toAddress(parameters);
		return address.toString();
	}

	/*
	 * 1 coin = 100 cents
	 * 1 cent = 1000000 nanocents
	 * Example: 1 coin 2345 cents = 1.00002345
	 */
	@Override
	public void sendMoney(final String address, Amount amount) throws Exception {
		if ((address == null) || (amount == null) || !amount.isPositive()) {
			throw new IllegalArgumentException("Invalid transfer");
		}

		Address forwardingAddress = new Address(parameters, address);

		final BigInteger amountToSend = toNanoCoins(amount.getCoins(), amount.getCents());

		if (logger.isInfoEnabled()) {
			logger.info("[{}] Forwarding {} LTC", address, Utils.bitcoinValueToFriendlyString(amountToSend));
		}

		// final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
		final Wallet.SendResult sendResult = getWallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);
		balance.addAndGet(amount.toBigDecimal().negate());
		// A future that will complete once the transaction message has been successfully
		if (logger.isInfoEnabled()) {
			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.info("[{}][{}] Sent {} LTC.", address, sendResult.tx.getHashAsString(),
							Utils.bitcoinValueToFriendlyString(amountToSend));
				}
			}, MoreExecutors.sameThreadExecutor());
		}
	}

	@Override
	public Amount receiveMoney(String address, Amount amount) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<WTransaction> getWalletTransactions() {
		Iterable<WalletTransaction> tansactions = getWallet().getWalletTransactions();
		List<WTransaction> wTransactions = new ArrayList<>();
		for (WalletTransaction walletTr : tansactions) {
			Transaction tr = walletTr.getTransaction();
			wTransactions.add(toWTransaction(tr));
		}
		return wTransactions;
	}

	private Wallet getWallet() {
		return kit.wallet();
	}

	private static BigInteger toNanoCoins(long coins, long cents) {
		checkArgument(cents < 100000000);
		checkArgument(cents >= 0);
		checkArgument(coins >= 0);
		checkArgument(coins < NetworkParameters.MAX_MONEY.divide(COIN).longValue());
		BigInteger bi = BigInteger.valueOf(coins).multiply(COIN);
		bi = bi.add(BigInteger.valueOf(cents));
		return bi;
	}

	private static Amount toAmount(BigInteger nanoCoins) {
		return new Amount(new BigDecimal(Utils.bitcoinValueToFriendlyString(nanoCoins.abs())), Currency.LITECOIN.getCentsInCoin());
	}

	private WTransaction toWTransaction(Transaction transaction) {
		WTransaction result = new WTransaction();
		BigInteger amount = transaction.getValue(getWallet());
		result.setAmount(toAmount(amount));

		BigInteger amountFromMe = transaction.getValueSentFromMe(getWallet());
		result.setAmountSentFromMe(toAmount(amountFromMe));

		BigInteger amountToMe = transaction.getValueSentToMe(getWallet());
		result.setAmountSentToMe(toAmount(amountToMe));

		result.setAddress(getTransactionAddress(transaction));

		if (transaction.getConfidence() != null) {
			result.setDepth(transaction.getConfidence().getDepthInBlocks());
		}

		result.setTrHash(transaction.getHashAsString());
		result.setUpdateTime(transaction.getUpdateTime());
		return result;
	}

	private String getTransactionAddress(Transaction tx) {
		for (TransactionOutput out : tx.getOutputs()) {
			try {
				Script scriptPubKey = out.getScriptPubKey();
				String address = scriptPubKey.getToAddress(parameters).toString();
				if (getWalletAddress().contains(address)) {
					return address;
				}
			} catch (ScriptException ex) {
				logger.error(ex.getMessage());
			}
		}
		return null;
	}

	private List<String> getWalletAddress() {
		List<ECKey> keys = getWallet().getKeys();
		List<String> result = new ArrayList<>(keys.size());
		for (ECKey key : keys) {
			Address address = key.toAddress(parameters);
			result.add(address.toString());
		}
		return result;
	}
}
