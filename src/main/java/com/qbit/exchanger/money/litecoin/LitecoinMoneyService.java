package com.qbit.exchanger.money.litecoin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.litecoin.core.*;
import com.google.litecoin.crypto.KeyCrypterException;
import com.google.litecoin.kits.NewWalletAppKit;
import com.google.litecoin.params.MainNetParams;
import com.google.litecoin.params.TestNet2Params;
import com.google.litecoin.utils.BriefLogFormatter;
import com.qbit.exchanger.buffer.BufferDAO;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.litecoin.script.Script;
import com.qbit.exchanger.admin.CryptoService;
import com.qbit.exchanger.admin.WTransaction;
import com.qbit.exchanger.money.model.Currency;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LITECOIN
 *
 * @author Alexander_Sergeev
 */
@Singleton
public class LitecoinMoneyService implements CryptoService {

	private static final BigInteger COIN = new BigInteger("100000000", 10);

	private static final BigInteger MIN_FEE = new BigInteger("100000", 10);

	private final Logger logger = LoggerFactory.getLogger(LitecoinMoneyService.class);

	private ConcurrentMap<String, QueueItem> paymentQueue;

	private NetworkParameters parameters;

	private NewWalletAppKit kit;

	private String dbName;

	@Inject
	private Env env;
	@Inject
	private BufferDAO bufferDAO;

	private class QueueItem {

		private Transfer transfer;
		private MoneyTransferCallback callback;

		private QueueItem(Transfer transfer, MoneyTransferCallback callback) {
			this.transfer = transfer;
			this.callback = callback;
		}

		public Transfer getTransfer() {
			return transfer;
		}

		public void setTransfer(Transfer transfer) {
			this.transfer = transfer;
		}

		public MoneyTransferCallback getCallback() {
			return callback;
		}

		public void setCallback(MoneyTransferCallback callback) {
			this.callback = callback;
		}
	}

	@PostConstruct
	public void init() {
		BriefLogFormatter.init();
		if (env.isLitecoinTestnet()) {
			parameters = TestNet2Params.get();
			dbName = env.getLitecoinTestDBName();
		} else {
			parameters = MainNetParams.get();
			dbName = env.getLitecoinDBName();
		}
		kit = new NewWalletAppKit(parameters, new File(env.getLitecoinWalletPath()), "sample", env.isFullChain(), true);

		kit.setDbName(dbName);
		kit.setHostname(env.getCryptoDBHostname());
		kit.setUsername(env.getCryptoDBUsername());
		kit.setPassword(env.getCryptoDBPassword());
		kit.setFullStoreDepth(1000);

		kit.startAndWait();
		paymentQueue = new ConcurrentHashMap<>();

		AbstractWalletEventListener listener = getPaymentListener();
		getWallet().addEventListener(listener);
	}

	@PreDestroy
	public void destroy() {
		try {
			kit.stopAndWait();
		} catch (Throwable ex) {
			// Do nothing
		}
	}

	@Override
	public void process(Transfer transfer, MoneyTransferCallback callback) {
		switch (transfer.getType()) {
			case IN:
				receiveMoney(transfer, callback);
				break;
			case OUT:
				sendMoney(transfer, callback);
				break;
		}
	}

	@Override
	public boolean test(Transfer transfer) {
		boolean result;
		if (TransferType.IN.equals(transfer.getType())) {
			result = testReceive(transfer);
		} else {
			result = testSend(transfer);
		}
		return result;
	}

	private AbstractWalletEventListener getPaymentListener() {
		AbstractWalletEventListener listener = new AbstractWalletEventListener() {
			@Override
			public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
				final BigInteger receivedValue = tx.getValueSentToMe(w);

				logger.info("Received tx for {}: {}", Utils.bitcoinValueToFriendlyString(receivedValue), tx);
				logger.info("Transaction will be forwarded after it confirms.");

				Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
					@Override
					public void onSuccess(Transaction result) {
						for (TransactionOutput out : result.getOutputs()) {
							try {
								Script scriptPubKey = out.getScriptPubKey();
								String address = scriptPubKey.getToAddress(parameters).toString();
								if (getWalletAddress().contains(address)) {
									QueueItem item = paymentQueue.remove(address);
									if (item != null) {
										BigDecimal am = new BigDecimal(Utils.bitcoinValueToFriendlyString(receivedValue));
										item.callback.success(new Amount(am, Currency.LITECOIN.getCentsInCoin()));
										break;
									}
								}
							} catch (ScriptException ex) {
								logger.error(ex.getMessage(), ex);
							}
						}
						logger.info("Success");
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

	private void receiveMoney(Transfer transfer, MoneyTransferCallback callback) {
		paymentQueue.put(transfer.getAddress(), new QueueItem(transfer, callback));
	}

	/*
	 *	1 coin = 100 cents
	 * 	1 cent = 1000000 nanocents
	 *   Example: 1 coin 2345 cents = 1.00002345
	 */
	private void sendMoney(Transfer transfer, MoneyTransferCallback callback) {
		if ((transfer == null) || !transfer.isPositive()) {
			callback.error("Empty address or wrong money value");
			return;
		}
		try {
			Address forwardingAddress = new Address(parameters, transfer.getAddress());

			final BigInteger amountToSend = toNanoCoins(transfer.getAmount().getCoins(), transfer.getAmount().getCents());

			logger.info("Forwarding {} BTC", Utils.bitcoinValueToFriendlyString(amountToSend));

//			final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
			final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);

			logger.info("Sending ...");

			assert sendResult != null;

			callback.success(transfer.getAmount());

			// A future that will complete once the transaction message has been successfully
			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.info("Sent coins onwards! Transaction hash is {}", sendResult.tx.getHashAsString());
				}
			}, MoreExecutors.sameThreadExecutor());

		} catch (KeyCrypterException | InsufficientMoneyException ex) {
			throw new RuntimeException(ex);
		} catch (AddressFormatException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			bufferDAO.deleteReservation(Currency.LITECOIN, transfer.getAmount());
		}
	}

	private boolean testReceive(Transfer transfer) {
		boolean result;
		if ((transfer != null) && transfer.isPositive()) {
			result = getWalletAddress().contains(transfer.getAddress());
		} else {
			//("Invalid transfer");
			result = false;
		}
		return result;
	}

	private boolean testSend(Transfer transfer) {
		if ((transfer == null) || !transfer.isPositive()) {
			return false;
		}
		boolean result;
		BigInteger transferAmount = toNanoCoins(transfer.getAmount().getCoins(), transfer.getAmount().getCents());
		if (transferAmount.compareTo(getWallet().getBalance().subtract(MIN_FEE)) < 0) {
			Amount balance = new Amount(getBalance().toBigDecimal(), Currency.LITECOIN.getCentsInCoin());
			result = bufferDAO.reserveAmount(Currency.LITECOIN, balance, transfer.getAmount());
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public String generateAddress() {
		ECKey key = new ECKey();
		getWallet().addKey(key);
		Address address = key.toAddress(parameters);
		return address.toString();
	}

	public List<String> getWalletAddress() {
		List<ECKey> keys = getWallet().getKeys();
		List<String> result = new ArrayList<>(keys.size());
		for (ECKey key : keys) {
			Address address = key.toAddress(parameters);
			result.add(address.toString());
		}
		return result;
	}

	@Override
	public Amount getBalance() {
		BigInteger balance = getWallet().getBalance();
		return new Amount(new BigDecimal(Utils.bitcoinValueToFriendlyString(balance)), Currency.LITECOIN.getCentsInCoin());
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
	
	@Override
	public void sendMoney(Amount amount, String address) {
		if ((amount == null) || !amount.isValid() || address == null) {
			logger.error("Empty address or wrong money value");
			return;
		}
		BigInteger transferAmount = toNanoCoins(amount.getCoins(), amount.getCents());
		boolean result = transferAmount.compareTo(getWallet().getBalance().add(MIN_FEE)) == -1;
			if (result) {
				Amount balance = new Amount(getBalance().toBigDecimal(), Currency.LITECOIN.getCentsInCoin());
				result = bufferDAO.reserveAmount(Currency.LITECOIN, balance, amount);
			} else {
				logger.error("Not enough money in the system buffer");
				return;
			}
		try {
			Address forwardingAddress = new Address(parameters, address);

			final BigInteger amountToSend = toNanoCoins(amount.getCoins(), amount.getCents());

			logger.info("Forwarding {} BTC", com.google.bitcoin.core.Utils.bitcoinValueToFriendlyString(amountToSend));
;
			final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);

			logger.info("Sending coins to admin address...");

			assert sendResult != null;		

			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.info("Sent coins onwards! Transaction hash is {}", sendResult.tx.getHashAsString());
				}
			}, MoreExecutors.sameThreadExecutor());

		} catch (KeyCrypterException | InsufficientMoneyException e) {
			throw new RuntimeException(e);
		} catch (AddressFormatException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			bufferDAO.deleteReservation(Currency.LITECOIN, amount);
		}
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

	private WTransaction toWTransaction(Transaction tr) {
		WTransaction wtr = new WTransaction();
		BigInteger am = tr.getValue(getWallet());
		wtr.setAmount(toAmount(am));

		BigInteger amFromMe = tr.getValueSentFromMe(getWallet());
		wtr.setAmountSentFromMe(toAmount(amFromMe));

		BigInteger amToMe = tr.getValueSentToMe(getWallet());
		wtr.setAmountSentToMe(toAmount(amToMe));

		wtr.setTrHash(tr.getHashAsString());

		wtr.setAddress(getTransactionAddress(tr));

		wtr.setUpdateTime(tr.getUpdateTime());

		return wtr;
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
}
