package com.qbit.exchanger.money.bitcoin;

import com.google.bitcoin.core.*;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.crypto.KeyCrypterException;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.core.MoneyService;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.google.bitcoin.core.Utils.bytesToHexString;
import static com.google.common.base.Preconditions.checkArgument;

import com.qbit.exchanger.money.model.Amount;
import com.qbit.exchanger.money.model.Transfer;
import com.qbit.exchanger.money.model.TransferType;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BITCOIN
 *
 * @author Ivan_Rakitnyh
 */
@Singleton
public class BitcoinMoneyService implements MoneyService {

	private static final BigInteger COIN = new BigInteger("100000000", 10);

	private static final BigInteger MIN_FEE = new BigInteger("10000", 10);

	private static final Logger logger = Logger.getLogger(BitcoinMoneyService.class.getName());

	private static final String WALLET_PATH = "./src/main/resources/bitcoin";

	private ConcurrentMap<String, QueueItem> paymentQueue;

	private NetworkParameters parameters;

	private WalletAppKit kit;

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

	public BitcoinMoneyService() {
		init(true);
	}

	private void init(boolean testnet) {
		BriefLogFormatter.init();
		if (testnet) {
			parameters = TestNet3Params.get();
		} else {
			parameters = MainNetParams.get();
		}
		kit = new WalletAppKit(parameters, new File(WALLET_PATH), "sample");
<<<<<<< HEAD:src/main/java/com/qbit/exchanger/money/bitcoin/BitcoinMoneyService.java
		kit.startAndWait();

		paymentQueue = new ConcurrentHashMap<String, QueueItem>();

		AbstractWalletEventListener listener = getPaymentListener();
		getWallet().addEventListener(listener);
=======
		// For debug purposes
		//kit.startAndWait();
		// AbstractWalletEventListener listener = getPaymentListener();
		//getWallet().addEventListener(listener);
>>>>>>> origin/Yandex-service:src/main/java/com/qbit/exchanger/money/bitcoin/Bitcoin.java
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
						Amount amount = item.getTransfer().getAmount();
						BigInteger expectedValue = toNanoCoins(amount.getCoins(), amount.getCents());
						if (receivedValue.compareTo(expectedValue) >= 0 ) {
							item.callback.success();
						}
					}
				} catch (ScriptException e) {
					e.printStackTrace();
				}

//				for (TransactionOutput out : tx.getOutputs()) {
//					try {
//						Script scriptPubKey = out.getScriptPubKey();
//						if (scriptPubKey.isSentToAddress()) {
//							System.out.println(scriptPubKey.getToAddress(parameters).toString());
//						} else if (scriptPubKey.isSentToRawPubKey()) {
//							System.out.println("[pubkey:");
//							System.out.println(bytesToHexString(scriptPubKey.getPubKey()));
//							System.out.println("]");
//						} else {
//							System.out.println(scriptPubKey);
//						}
//					} catch (ScriptException ex) {
//						logger.severe(ex.getMessage());
//					}
//				}

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

			BigInteger value = toNanoCoins(transfer.getAmount().getCoins(), transfer.getAmount().getCents());

			logger.log(Level.INFO, "Forwarding {0} BTC", Utils.bitcoinValueToFriendlyString(value));

			final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
			final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);

			logger.info("Sending ...");

			assert sendResult != null;

			callback.success();

			// A future that will complete once the transaction message has been successfully
			sendResult.broadcastComplete.addListener(new Runnable() {

				@Override
				public void run() {
					logger.log(Level.INFO, "Sent coins onwards! Transaction hash is {0}", sendResult.tx.getHashAsString());
				}
			}, MoreExecutors.sameThreadExecutor());

		} catch (KeyCrypterException e) {
			throw new RuntimeException(e);
		} catch (AddressFormatException ex) {
			logger.severe(ex.getMessage());
		}
	}

<<<<<<< HEAD:src/main/java/com/qbit/exchanger/money/bitcoin/BitcoinMoneyService.java
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
=======
	@Override
	public boolean test(Transfer transfer) {
		// For debug purposes
		return true;
>>>>>>> origin/Yandex-service:src/main/java/com/qbit/exchanger/money/bitcoin/Bitcoin.java
	}

	public String getNewAddress() {
		ECKey key = new ECKey();
		getWallet().addKey(key);
		Address address = key.toAddress(parameters);
		return address.toString();
	}

	public List<String> getWalletAddress() {
		List<ECKey> keys = getWallet().getKeys();
		List<String> result = new ArrayList<String>(keys.size());
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
