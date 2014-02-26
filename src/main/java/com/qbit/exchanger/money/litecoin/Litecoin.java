package com.qbit.exchanger.money.litecoin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.litecoin.core.AbstractWalletEventListener;
import com.google.litecoin.core.Address;
import com.google.litecoin.core.AddressFormatException;
import com.google.litecoin.core.ECKey;
import com.google.litecoin.core.InsufficientMoneyException;
import com.google.litecoin.core.NetworkParameters;
import com.google.litecoin.core.ScriptException;
import com.google.litecoin.core.Transaction;
import com.google.litecoin.core.TransactionOutput;
import com.google.litecoin.core.Utils;
import com.google.litecoin.core.Wallet;
import com.google.litecoin.kits.WalletAppKit;
import com.google.litecoin.params.MainNetParams;
import com.google.litecoin.params.TestNet3Params;
import com.google.litecoin.script.Script;
import com.google.litecoin.utils.BriefLogFormatter;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyTransferCallback;
import com.qbit.exchanger.money.model.Transfer;
import java.io.File;
import java.math.BigInteger;
import java.util.logging.Logger;

import static com.google.litecoin.core.Utils.bytesToHexString;
import com.google.litecoin.crypto.KeyCrypterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Alexander_Sergeev
 */
public class Litecoin implements MoneyService {

        private static final Logger logger = Logger.getLogger(Litecoin.class.getName());

        private static final String WALLET_PATH = "./src/main/resources/litecoin";

        private NetworkParameters parameters;

        private WalletAppKit kit;

        public Litecoin() {
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

                // For debug purposes
                //kit.startAndWait();
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

        public void receiveMoney(Transfer transfer, MoneyTransferCallback callback) {
                AbstractWalletEventListener listener = new AbstractWalletEventListener() {
                        @Override
                        public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

                                BigInteger receivedValue = tx.getValueSentToMe(w);
                                for (TransactionOutput out : tx.getOutputs()) {
                                        try {
                                                Script scriptPubKey = out.getScriptPubKey();
                                                if (scriptPubKey.isSentToAddress()) {
                                                        System.out.println(scriptPubKey.getToAddress(parameters).toString());
                                                } else if (scriptPubKey.isSentToRawPubKey()) {
                                                        System.out.println("[pubkey:");
                                                        System.out.println(bytesToHexString(scriptPubKey.getPubKey()));
                                                        System.out.println("]");
                                                } else {
                                                        System.out.println(scriptPubKey);
                                                }
                                        } catch (ScriptException ex) {
                                                logger.severe(ex.getMessage());
                                        }
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
                getWallet().addEventListener(listener);
        }

        public void sendMoney(Transfer transfer, MoneyTransferCallback callback) {
                if ((transfer == null) || !transfer.isValid()) {
                        callback.error("Empty address or wrong money value");
                }
                try {
                        Address forwardingAddress = new Address(parameters, transfer.getAddress());

                        BigInteger value = Utils.toNanoCoins((int) transfer.getCoins(), (int) transfer.getCents());

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
                } catch (InsufficientMoneyException ex) {
                        Logger.getLogger(Litecoin.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public boolean test(Transfer transfer) {
                // For debug purposes
                return true;
        }

        public String getNewAddress() {
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

}
