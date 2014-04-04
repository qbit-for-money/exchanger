/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.bitcoin.kits;

import com.google.bitcoin.core.AbstractBlockChain;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.CheckpointManager;
import com.google.bitcoin.core.DownloadListener;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.FullPrunedBlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerAddress;
import com.google.bitcoin.core.PeerEventListener;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.store.PostgresFullPrunedBlockStore;
import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.WalletProtobufSerializer;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Utility class that wraps the boilerplate needed to set up a new SPV bitcoinj
 * app. Instantiate it with a directory and file prefix, optionally configure a
 * few things, then use start or startAndWait. The object will construct and
 * configure a {@link BlockChain}, {@link SPVBlockStore}, {@link Wallet} and
 * {@link PeerGroup}. Depending on the value of the blockingStartup property,
 * startup will be considered complete once the block chain has fully
 * synchronized, so it can take a while.</p>
 *
 * <p>
 * To add listeners and modify the objects that are constructed, you can either
 * do that by overriding the {@link #onSetupCompleted()} method (which will run
 * on a background thread) and make your changes there, or by waiting for the
 * service to start and then accessing the objects from wherever you want.
 * However, you cannot access the objects this class creates until startup is
 * complete.</p>
 *
 * <p>
 * The asynchronous design of this class may seem puzzling (just use
 * {@link #startAndWait()} if you don't want that). It is to make it easier to
 * fit bitcoinj into GUI apps, which require a high degree of responsiveness on
 * their main thread which handles all the animation and user interaction. Even
 * when blockingStart is false, initializing bitcoinj means doing potentially
 * blocking file IO, generating keys and other potentially intensive operations.
 * By running it on a background thread, there's no risk of accidentally causing
 * UI lag.</p>
 *
 * <p>
 * Note that {@link #startAndWait()} can throw an unchecked
 * {@link com.google.common.util.concurrent.UncheckedExecutionException} if
 * anything goes wrong during startup - you should probably handle it and use
 * {@link Exception#getCause()} to figure out what went wrong more precisely.
 * Same thing if you use the async start() method.</p>
 */
public class NewWalletAppKit extends AbstractIdleService {

	private final String filePrefix;
	private final NetworkParameters params;
	private volatile SPVBlockStore vStore;
	private volatile Wallet vWallet;
	private volatile PeerGroup vPeerGroup;
	private volatile PeerGroup vPeerGroupFull;

	private final File directory;
	private volatile File vWalletFile;

	private boolean useAutoSave = true;
	private PeerAddress[] peerAddresses;
	private PeerEventListener downloadListener;
	private boolean autoStop = true;
	private InputStream checkpoints;
	private boolean blockingStartup = true;
	private String userAgent, version;

	private volatile AbstractBlockChain vChain;
	private volatile AbstractBlockChain vChainFull;
	private volatile FullPrunedBlockStore vFPBStore;
	private final boolean isFullChain;
	private final boolean isPostgresBdStore;
	private int fullStoreDepth;
	private String hostname;
	private String dbName;
	private String username;
	private String password;

	public NewWalletAppKit(NetworkParameters params, File directory, String filePrefix, boolean isFullChain, boolean isPostgresBdStore) {
		this.params = checkNotNull(params);
		this.directory = checkNotNull(directory);
		this.filePrefix = checkNotNull(filePrefix);
		this.isFullChain = isFullChain;
		this.isPostgresBdStore = isPostgresBdStore;
	}

	public void setHostname(String hostname) {
		this.hostname = checkNotNull(hostname);
	}

	public void setDbName(String dbName) {
		this.dbName = checkNotNull(dbName);
	}

	public void setUsername(String username) {
		this.username = checkNotNull(username);
	}

	public void setPassword(String password) {
		this.password = checkNotNull(password);
	}

	public void setFullStoreDepth(int fullStoreDepth) {
		this.fullStoreDepth = fullStoreDepth;
	}

	/**
	 * Will only connect to the given addresses. Cannot be called after
	 * startup.
	 */
	public NewWalletAppKit setPeerNodes(PeerAddress... addresses) {
		checkState(state() == Service.State.NEW, "Cannot call after startup");
		this.peerAddresses = addresses;
		return this;
	}

	/**
	 * Will only connect to localhost. Cannot be called after startup.
	 */
	public NewWalletAppKit connectToLocalHost() {
		try {
			final InetAddress localHost = InetAddress.getLocalHost();
			return setPeerNodes(new PeerAddress(localHost, params.getPort()));
		} catch (UnknownHostException e) {
			// Borked machine with no loopback adapter configured properly.
			throw new RuntimeException(e);
		}
	}

	/**
	 * If true, the wallet will save itself to disk automatically whenever
	 * it changes.
	 */
	public NewWalletAppKit setAutoSave(boolean value) {
		checkState(state() == Service.State.NEW, "Cannot call after startup");
		useAutoSave = value;
		return this;
	}

	/**
	 * If you want to learn about the sync process, you can provide a
	 * listener here. For instance, a {@link DownloadListener} is a good
	 * choice.
	 */
	public NewWalletAppKit setDownloadListener(PeerEventListener listener) {
		this.downloadListener = listener;
		return this;
	}

	/**
	 * If true, will register a shutdown hook to stop the library. Defaults
	 * to true.
	 */
	public NewWalletAppKit setAutoStop(boolean autoStop) {
		this.autoStop = autoStop;
		return this;
	}

	/**
	 * If set, the file is expected to contain a checkpoints file calculated
	 * with BuildCheckpoints. It makes initial block sync faster for new
	 * users - please refer to the documentation on the bitcoinj website for
	 * further details.
	 */
	public NewWalletAppKit setCheckpoints(InputStream checkpoints) {
		this.checkpoints = checkNotNull(checkpoints);
		return this;
	}

	/**
	 * If true (the default) then the startup of this service won't be
	 * considered complete until the network has been brought up, peer
	 * connections established and the block chain synchronised. Therefore
	 * {@link #startAndWait()} can potentially take a very long time. If
	 * false, then startup is considered complete once the network activity
	 * begins and peer connections/block chain sync will continue in the
	 * background.
	 */
	public NewWalletAppKit setBlockingStartup(boolean blockingStartup) {
		this.blockingStartup = blockingStartup;
		return this;
	}

	/**
	 * Sets the string that will appear in the subver field of the version
	 * message.
	 *
	 * @param userAgent A short string that should be the name of your app,
	 * e.g. "My Wallet"
	 * @param version A short string that contains the version number, e.g.
	 * "1.0-BETA"
	 */
	public NewWalletAppKit setUserAgent(String userAgent, String version) {
		this.userAgent = checkNotNull(userAgent);
		this.version = checkNotNull(version);
		return this;
	}

	/**
	 * <p>
	 * Override this to load all wallet extensions if any are necessary.</p>
	 *
	 * <p>
	 * When this is called, chain(), store(), and peerGroup() will return
	 * the created objects, however they are not initialized/started</p>
	 */
	protected void addWalletExtensions() throws Exception {
	}

	/**
	 * This method is invoked on a background thread after all objects are
	 * initialised, but before the peer group or block chain download is
	 * started. You can tweak the objects configuration here.
	 */
	protected void onSetupCompleted() {
	}

	@Override
	protected void startUp() throws Exception {
		// Runs in a separate thread.
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				throw new IOException("Could not create named directory.");
			}
		}
		FileInputStream walletStream = null;
		if (isFullChain) {
			//startFull(walletStream);
			startJoint(walletStream);
		} else {
			startSPV(walletStream);
		}
	}

	private void startSPV(FileInputStream walletStream) throws Exception {
		try {
			File chainFile = new File(directory, filePrefix + ".spvchain");
			boolean chainFileExists = chainFile.exists();
			vWalletFile = new File(directory, filePrefix + ".wallet");
			boolean shouldReplayWallet = vWalletFile.exists() && !chainFileExists;

			vStore = new SPVBlockStore(params, chainFile);
			if (!chainFileExists && checkpoints != null) {
				// Ugly hack! We have to create the wallet once here to learn the earliest key time, and then throw it
				// away. The reason is that wallet extensions might need access to peergroups/chains/etc so we have to
				// create the wallet later, but we need to know the time early here before we create the BlockChain
				// object.
				long time = Long.MAX_VALUE;
				if (vWalletFile.exists()) {
					Wallet wallet = new Wallet(params);
					FileInputStream stream = new FileInputStream(vWalletFile);
					new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(stream), wallet);
					time = wallet.getEarliestKeyCreationTime();
				}
				CheckpointManager.checkpoint(params, checkpoints, vStore, time);
			}
			vChain = new BlockChain(params, vStore);
			vPeerGroup = new PeerGroup(params, vChain);
			if (this.userAgent != null) {
				vPeerGroup.setUserAgent(userAgent, version);
			}
			if (vWalletFile.exists()) {
				walletStream = new FileInputStream(vWalletFile);
				vWallet = new Wallet(params);
				addWalletExtensions(); // All extensions must be present before we deserialize
				new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(walletStream), vWallet);
				if (shouldReplayWallet) {
					vWallet.clearTransactions(0);
				}
			} else {
				vWallet = new Wallet(params);
				vWallet.addKey(new ECKey());
				addWalletExtensions();
			}
			if (useAutoSave) {
				vWallet.autosaveToFile(vWalletFile, 1, TimeUnit.SECONDS, null);
			}
			// Set up peer addresses or discovery first, so if wallet extensions try to broadcast a transaction
			// before we're actually connected the broadcast waits for an appropriate number of connections.
			if (peerAddresses != null) {
				for (PeerAddress addr : peerAddresses) {
					vPeerGroup.addAddress(addr);
				}
				peerAddresses = null;
			} else {
				vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
			}
			vChain.addWallet(vWallet);
			vPeerGroup.addWallet(vWallet);
			onSetupCompleted();

			if (blockingStartup) {
				vPeerGroup.startAndWait();
				// Make sure we shut down cleanly.
				installShutdownHook();
				// TODO: Be able to use the provided download listener when doing a blocking startup.
				final DownloadListener listener = new DownloadListener();
				vPeerGroup.startBlockChainDownload(listener);
				listener.await();
			} else {
				Futures.addCallback(vPeerGroup.start(), new FutureCallback<Service.State>() {
					@Override
					public void onSuccess(Service.State result) {
						final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
						vPeerGroup.startBlockChainDownload(l);
					}

					@Override
					public void onFailure(Throwable t) {
						throw new RuntimeException(t);
					}
				});
			}
		} catch (BlockStoreException e) {
			throw new IOException(e);
		} finally {
			if (walletStream != null) {
				walletStream.close();
			}
		}
	}
	
	private void startJoint(FileInputStream walletStream) throws Exception {
		try {
			File chainFile = new File(directory, filePrefix + ".spvchain");
			boolean chainFileExists = chainFile.exists();
			vWalletFile = new File(directory, filePrefix + ".wallet");
			boolean shouldReplayWallet = vWalletFile.exists() && !chainFileExists;

			vStore = new SPVBlockStore(params, chainFile);
			vFPBStore = new PostgresFullPrunedBlockStore(params, fullStoreDepth, hostname, dbName, username, password);
			if (!chainFileExists && checkpoints != null) {
				// Ugly hack! We have to create the wallet once here to learn the earliest key time, and then throw it
				// away. The reason is that wallet extensions might need access to peergroups/chains/etc so we have to
				// create the wallet later, but we need to know the time early here before we create the BlockChain
				// object.
				long time = Long.MAX_VALUE;
				if (vWalletFile.exists()) {
					Wallet wallet = new Wallet(params);
					FileInputStream stream = new FileInputStream(vWalletFile);
					new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(stream), wallet);
					time = wallet.getEarliestKeyCreationTime();
				}
				CheckpointManager.checkpoint(params, checkpoints, vStore, time);
			}
			vChain = new BlockChain(params, vStore);
			vPeerGroup = new PeerGroup(params, vChain);
			
			vChainFull = new FullPrunedBlockChain(params, vFPBStore);
			vPeerGroupFull = new PeerGroup(params, vChainFull);
			
			if (this.userAgent != null) {
				vPeerGroup.setUserAgent(userAgent, version);
				vPeerGroupFull.setUserAgent(userAgent, version);
			}
			if (vWalletFile.exists()) {
				walletStream = new FileInputStream(vWalletFile);
				vWallet = new Wallet(params);
				addWalletExtensions(); // All extensions must be present before we deserialize
				new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(walletStream), vWallet);
				if (shouldReplayWallet) {
					vWallet.clearTransactions(0);
				}
			} else {
				vWallet = new Wallet(params);
				vWallet.addKey(new ECKey());
				addWalletExtensions();
			}
			if (useAutoSave) {
				vWallet.autosaveToFile(vWalletFile, 1, TimeUnit.SECONDS, null);
			}
			// Set up peer addresses or discovery first, so if wallet extensions try to broadcast a transaction
			// before we're actually connected the broadcast waits for an appropriate number of connections.
			if (peerAddresses != null) {
				for (PeerAddress addr : peerAddresses) {
					vPeerGroup.addAddress(addr);
					vPeerGroupFull.addAddress(addr);
				}
				peerAddresses = null;
			} else {
				vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
				vPeerGroupFull.addPeerDiscovery(new DnsDiscovery(params));
			}
			vChain.addWallet(vWallet);
			vPeerGroup.addWallet(vWallet);
			
			vChainFull.addWallet(vWallet);
			vPeerGroupFull.addWallet(vWallet);
			
			onSetupCompleted();

			if (blockingStartup) {
				vPeerGroup.startAndWait();
				vPeerGroupFull.startAndWait();
				// Make sure we shut down cleanly.
				installShutdownHook();
				// TODO: Be able to use the provided download listener when doing a blocking startup.
				final DownloadListener listener = new DownloadListener();
				vPeerGroup.startBlockChainDownload(listener);
				vPeerGroupFull.startBlockChainDownload(listener);
				listener.await();
			} else {
				Futures.addCallback(vPeerGroup.start(), new FutureCallback<Service.State>() {
					@Override
					public void onSuccess(Service.State result) {
						final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
						vPeerGroup.startBlockChainDownload(l);
					}

					@Override
					public void onFailure(Throwable t) {
						throw new RuntimeException(t);
					}
				});
				Futures.addCallback(vPeerGroupFull.start(), new FutureCallback<Service.State>() {
					@Override
					public void onSuccess(Service.State result) {
						final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
						vPeerGroupFull.startBlockChainDownload(l);
					}

					@Override
					public void onFailure(Throwable t) {
						throw new RuntimeException(t);
					}
				});
			}
		} catch (BlockStoreException e) {
			throw new IOException(e);
		} finally {
			if (walletStream != null) {
				walletStream.close();
			}
		}
	}

	private void startFull(FileInputStream walletStream) throws Exception {
		try {
			File chainFile = new File(directory, filePrefix + ".blockchain");
			boolean chainFileExists = chainFile.exists();
			vWalletFile = new File(directory, filePrefix + ".wallet");
			boolean shouldReplayWallet = vWalletFile.exists() && !chainFileExists;

			if (!isPostgresBdStore) {
				vFPBStore = new H2FullPrunedBlockStore(params, chainFile.getName(), 100);

			} else {
				if (fullStoreDepth == 0 || hostname == null || dbName == null || username == null || password == null) {
					throw new IllegalArgumentException();
				}
				vFPBStore = new PostgresFullPrunedBlockStore(params, fullStoreDepth, hostname, dbName, username, password);
			}

			if (!chainFileExists && checkpoints != null) {
				// Ugly hack! We have to create the wallet once here to learn the earliest key time, and then throw it
				// away. The reason is that wallet extensions might need access to peergroups/chains/etc so we have to
				// create the wallet later, but we need to know the time early here before we create the BlockChain
				// object.
				long time = Long.MAX_VALUE;
				if (vWalletFile.exists()) {
					Wallet wallet = new Wallet(params);
					FileInputStream stream = new FileInputStream(vWalletFile);
					new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(stream), wallet);
					time = wallet.getEarliestKeyCreationTime();
				}
				CheckpointManager.checkpoint(params, checkpoints, vFPBStore, time);
			}

			vChain = new FullPrunedBlockChain(params, vFPBStore);

			vPeerGroup = new PeerGroup(params, vChain);
			if (this.userAgent != null) {
				vPeerGroup.setUserAgent(userAgent, version);
			}
			if (vWalletFile.exists()) {
				walletStream = new FileInputStream(vWalletFile);
				vWallet = new Wallet(params);
				addWalletExtensions(); // All extensions must be present before we deserialize
				new WalletProtobufSerializer().readWallet(WalletProtobufSerializer.parseToProto(walletStream), vWallet);
				if (shouldReplayWallet) {
					vWallet.clearTransactions(0);
				}
			} else {
				vWallet = new Wallet(params);
				vWallet.addKey(new ECKey());
				addWalletExtensions();
			}
			if (useAutoSave) {
				vWallet.autosaveToFile(vWalletFile, 1, TimeUnit.SECONDS, null);
			}
			// Set up peer addresses or discovery first, so if wallet extensions try to broadcast a transaction
			// before we're actually connected the broadcast waits for an appropriate number of connections.
			if (peerAddresses != null) {
				for (PeerAddress addr : peerAddresses) {
					vPeerGroup.addAddress(addr);
				}
				peerAddresses = null;
			} else {
				vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
			}
			vChain.addWallet(vWallet);
			vPeerGroup.addWallet(vWallet);
			onSetupCompleted();

			if (blockingStartup) {
				vPeerGroup.startAndWait();
				// Make sure we shut down cleanly.
				installShutdownHook();
				// TODO: Be able to use the provided download listener when doing a blocking startup.
				final DownloadListener listener = new DownloadListener();
				vPeerGroup.startBlockChainDownload(listener);
				listener.await();
			} else {
				Futures.addCallback(vPeerGroup.start(), new FutureCallback<Service.State>() {
					@Override
					public void onSuccess(Service.State result) {
						final PeerEventListener l = downloadListener == null ? new DownloadListener() : downloadListener;
						vPeerGroup.startBlockChainDownload(l);
					}

					@Override
					public void onFailure(Throwable t) {
						throw new RuntimeException(t);
					}
				});
			}
		} catch (BlockStoreException e) {
			throw new IOException(e);
		} finally {
			if (walletStream != null) {
				walletStream.close();
			}
		}
	}

	private void installShutdownHook() {
		if (autoStop) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						NewWalletAppKit.this.stopAndWait();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	@Override
	protected void shutDown() throws Exception {
		// Runs in a separate thread.
		try {
			vPeerGroup.stopAndWait();
			vPeerGroupFull.stopAndWait();
			vWallet.saveToFile(vWalletFile);
			if (vStore != null) {
				vStore.close();
			}
			if (vFPBStore != null) {
				vFPBStore.close();
			}

			vPeerGroup = null;
			vWallet = null;
			vStore = null;
			vFPBStore = null;
			vChain = null;
			
			vChainFull = null;
			vPeerGroupFull = null;
		} catch (BlockStoreException e) {
			throw new IOException(e);
		}
	}

	public NetworkParameters params() {
		return params;
	}

	public AbstractBlockChain chain() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vChain;
	}
	
	public AbstractBlockChain chainFull() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vChainFull;
	}

	public SPVBlockStore store() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vStore;
	}

	public FullPrunedBlockStore storeFull() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vFPBStore;
	}

	public Wallet wallet() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vWallet;
	}

	public PeerGroup peerGroup() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vPeerGroup;
	}
	
	public PeerGroup peerGroupFull() {
		checkState(state() == Service.State.STARTING || state() == Service.State.RUNNING, "Cannot call until startup is complete");
		return vPeerGroupFull;
	}

	public File directory() {
		return directory;
	}

}
