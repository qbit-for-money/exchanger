package com.qbit.exchanger.money.litecoin;

import com.google.litecoin.core.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.litecoin.kits.WalletAppKit;
import com.google.litecoin.params.MainNetParams;
import com.google.litecoin.params.TestNet3Params;
import com.google.litecoin.utils.BriefLogFormatter;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Transfer;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.google.litecoin.crypto.KeyCrypterException;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.TransferType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import com.qbit.exchanger.money.model.Currency;
import java.math.BigDecimal;

/**
 * LITECOIN
 *
 * @author Alexander_Sergeev
 */
@Singleton
public class LitecoinMoneyService implements MoneyService {

	private static final BigInteger COIN = new BigInteger("100000000", 10);

	private static final BigInteger MIN_FEE = new BigInteger("100000", 10);

	private static final Logger logger = Logger.getLogger(LitecoinMoneyService.class.getName());

	private ConcurrentMap<String, QueueItem> paymentQueue;

	private NetworkParameters parameters;

	private WalletAppKit kit;

	@Inject
	private Env env;

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
			parameters = TestNet3Params.get();
		} else {
			parameters = MainNetParams.get();
		}
		kit = new WalletAppKit(parameters, new File(env.getLitecoinWalletPath()), "sample");
		kit.startAndWait();
		paymentQueue = new ConcurrentHashMap<>();

		AbstractWalletEventListener listener = getPaymentListener();
		getWallet().addEventListener(listener);
	}

	@PreDestroy
	public void destroy() {
		kit.stopAndWait();
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
				BigInteger receivedValue = tx.getValueSentToMe(w);
				try {
					String address = tx.getOutputs().get(0).getScriptPubKey().getToAddress(parameters).toString();
					QueueItem item = paymentQueue.get(address);
					if (item != null) {
						BigDecimal am = new BigDecimal(Utils.bitcoinValueToFriendlyString(receivedValue));
						item.callback.success(new Amount(am, Currency.LITECOIN.getCentsInCoin()));
					}
				} catch (ScriptException ex) {
					logger.severe(ex.getMessage());
				}

				logger.log(Level.INFO, "Received tx for {0}: {1}", new Object[]{Utils.bitcoinValueToFriendlyString(receivedValue), tx});
				logger.info("Transaction will be forwarded after it confirms.");

				Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
					@Override
					public void onSuccess(Transaction result) {
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
		if ((transfer == null) || !transfer.isValid()) {
			callback.error("Empty address or wrong money value");
		}
		try {
			Address forwardingAddress = new Address(parameters, transfer.getAddress());

			final BigInteger amountToSend = toNanoCoins(transfer.getAmount().getCoins(), transfer.getAmount().getCents());

			logger.log(Level.INFO, "Forwarding {0} BTC", Utils.bitcoinValueToFriendlyString(amountToSend));

//			final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
			final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);

			logger.info("Sending ...");

			assert sendResult != null;

			callback.success(transfer.getAmount());

			// A future that will complete once the transaction message has been successfully
			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.INFO, "Sent coins onwards! Transaction hash is {0}", sendResult.tx.getHashAsString());
				}
			}, MoreExecutors.sameThreadExecutor());

		} catch (KeyCrypterException | InsufficientMoneyException e) {
			throw new RuntimeException(e);
		} catch (AddressFormatException ex) {
			logger.severe(ex.getMessage());
		}
	}

	private boolean testReceive(Transfer transfer) {
		boolean result;
		if ((transfer != null) && transfer.isValid()) {
			if (getWalletAddress().contains(transfer.getAddress())) {
				result = true;
			} else {
				//("Invalid address");
				result = false;
			}
		} else {
			//("Invalid transfer");
			result = false;
		}
		return result;
	}

	private boolean testSend(Transfer transfer) {
		boolean result;
		if ((transfer != null) && transfer.isValid()) {
			BigInteger transferAmount = toNanoCoins(transfer.getAmount().getCoins(), transfer.getAmount().getCents());
			if (transferAmount.compareTo(getWallet().getBalance().add(MIN_FEE)) == -1) {
				result = true;
			} else {
				//("Wallet is empty");
				result = false;
			}
		} else {
			//("Invalid transfer");
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

	public String getBalance() {
		BigInteger balance = getWallet().getBalance();
		return Utils.bitcoinValueToFriendlyString(balance);
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
}
