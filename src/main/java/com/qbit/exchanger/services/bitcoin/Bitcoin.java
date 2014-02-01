package com.qbit.exchanger.services.bitcoin;

import com.google.bitcoin.core.*;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.crypto.KeyCrypterException;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.common.util.concurrent.ForwardingService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.qbit.exchanger.services.core.Money;
import com.qbit.exchanger.services.core.MoneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * BITCOIN
 *
 * @author Ivan_Rakitnyh
 */
public class Bitcoin implements MoneyService {

	private static Logger logger = LoggerFactory.getLogger(ForwardingService.class);

	private static final String WALLET_PATH = "./src/main/resources/bitcoin";

	private NetworkParameters parameters;

	private WalletAppKit kit;

	public Bitcoin() {
		init(true);
	}

	private void init(boolean testnet) {
		BriefLogFormatter.init();
		if(testnet) {
			parameters = TestNet3Params.get();
		} else {
			parameters = MainNetParams.get();
		}
		kit = new WalletAppKit(parameters, new File(WALLET_PATH), "sample");
		kit.startAndWait();
		// AbstractWalletEventListener listener = getPaymentListener();
		//getWallet().addEventListener(listener);
	}

	@Override
	public void receiveMoney(Money moneyToRecive, Money moneyToSend, MoneyService sendService) {
		AbstractWalletEventListener listener = new AbstractWalletEventListener() {
			@Override
			public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

				if (thisAddress == moneyToRecive.getAddress()) {
					BigInteger waitValue = Utils.toNanoCoins(moneyToRecive.getCoins(), moneyToRecive.getCents());
					BigInteger receivedValue = tx.getValueSentToMe(w);
					if (waitValue == receivedValue) {
						sendService.sendMoney(moneyToSend);
					}
				}

				logger.info("Received tx for " + Utils.bitcoinValueToFriendlyString(value) + ": " + tx);
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
	}

	@Override
	public void sendMoney(Money moneyToSend) throws AddressFormatException {
		if(moneyToSend.getAddress() == null || moneyToSend.getCoins() < 0 || moneyToSend.getCents() < 0) {
			// SOMTHING WRONG
			throw new AddressFormatException("Empty address or wrong money value");
		}
		Address forwardingAddress = new Address(parameters, moneyToSend.getAddress());
		try {
			BigInteger value = Utils.toNanoCoins(moneyToSend.getCoins(), moneyToSend.getCents());

			logger.info("Forwarding " + Utils.bitcoinValueToFriendlyString(value) + " BTC");

			final BigInteger amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
			final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);

			logger.info("Sending ...");

			assert sendResult != null;

			// A future that will complete once the transaction message has been successfully
			sendResult.broadcastComplete.addListener(new Runnable() {
				@Override
				public void run() {
					logger.info("Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
				}
			}, MoreExecutors.sameThreadExecutor());

		} catch (KeyCrypterException e) {
			throw new RuntimeException(e);
		}
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
		for(ECKey key : keys) {
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
		return  kit.wallet();
	}
}
