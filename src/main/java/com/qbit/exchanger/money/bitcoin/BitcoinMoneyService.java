package com.qbit.exchanger.money.bitcoin;

import com.qbit.commons.util.AtomicBigDecimal;
import com.qbit.exchanger.money.core.AddressInfo;
import com.google.bitcoin.core.*;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.bitcoin.wallet.WalletTransaction;
import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.qbit.exchanger.money.core.CryptoService;
import com.qbit.exchanger.money.core.WTransaction;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.model.Amount;
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
import static com.qbit.commons.rest.util.RESTClientUtil.*;

/**
 * BITCOIN
 * 
* @author Ivan_Rakitnyh
 */
@Singleton
public class BitcoinMoneyService implements CryptoService {

	public static final BigInteger COIN = new BigInteger("100000000", 10);
	public static final BigInteger MIN_FEE = new BigInteger("10000", 10);
	
	public final static String BLOCKR_API_BASE_URL = "https://btc.blockr.io/api/v1/address/balance/";

	private final Logger logger = LoggerFactory.getLogger(BitcoinMoneyService.class);

	@Inject
	private Env env;

	private NetworkParameters parameters;
	private WalletAppKit kit;
	private AtomicBigDecimal balance;

	@PostConstruct
	public void init() {
		BriefLogFormatter.init();

		if (env.isBitcoinTestnet()) {
			parameters = TestNet3Params.get();
		} else {
			parameters = MainNetParams.get();
		}
		kit = new WalletAppKit(parameters, new File(env.getBitcoinWalletPath()), "sample");
		kit.startAndWait();
		balance = new AtomicBigDecimal(getWalletBalance());
		getWallet().addEventListener(getPaymentListener());
	}

	@PreDestroy
	public void destroy() {
		try {
			kit.stopAndWait();
		} catch (Throwable ex) {
			// Do nothing
		}
	}
	
	private AbstractWalletEventListener getPaymentListener() {
		AbstractWalletEventListener listener = new AbstractWalletEventListener() {
			@Override
			public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
				final BigInteger receivedValue = tx.getValueSentToMe(wallet);

				Futures.addCallback(tx.getConfidence().getDepthFuture(2), new FutureCallback<Transaction>() {
					@Override
					public void onSuccess(Transaction result) {
						BigDecimal value = new BigDecimal(Utils.bitcoinValueToFriendlyString(receivedValue));
						balance.addAndGet(value);
					}

					@Override
					public void onFailure(Throwable t) {
						throw new RuntimeException(t);
					}
				});
			}
		};
		
		return listener;
	}
	
	private BigDecimal getWalletBalance() {
		BigInteger walletBalance = getWallet().getBalance().subtract(MIN_FEE).max(BigInteger.ZERO);
		return new BigDecimal(Utils.bitcoinValueToFriendlyString(walletBalance));
	}

	@Override
	public Amount getBalance() {
		return new Amount(balance.getValue(), Currency.BITCOIN.getCentsInCoin());
	}

	@Override
	public Amount getBalance(String address) {
		
		String path = (address);
		
		try {
			AddressInfo addressInfo = get(BLOCKR_API_BASE_URL, path, "confirmations", "2", AddressInfo.class, true);
			if (logger.isInfoEnabled()) {
				logger.info("Address Info: [{}]", addressInfo);
			}
			return new Amount(new BigDecimal(addressInfo.getData().getBalance()), Currency.BITCOIN.getCentsInCoin());
		} catch (Exception ex) {
			if(logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
			return Amount.zero(Currency.BITCOIN.getCentsInCoin());
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
			logger.info("[{}] Forwarding {} BTC", address, Utils.bitcoinValueToFriendlyString(amountToSend));
		}

		// final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
		final Wallet.SendResult sendResult = getWallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);
		balance.addAndGet(amount.toBigDecimal().negate());
		// A future that will complete once the transaction message has been successfully
		if (logger.isInfoEnabled()) {
			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.info("[{}][{}] Sent {} BTC.", address, sendResult.tx.getHashAsString(),
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

	private static Amount toAmount(BigInteger nanoCoins) {
		return new Amount(new BigDecimal(Utils.bitcoinValueToFriendlyString(nanoCoins.abs())), Currency.BITCOIN.getCentsInCoin());
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
				if(logger.isErrorEnabled()) {
					logger.error(ex.getMessage());
				}
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
