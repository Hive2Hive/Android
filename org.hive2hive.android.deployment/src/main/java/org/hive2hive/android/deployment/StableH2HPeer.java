package org.hive2hive.android.deployment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import net.tomp2p.connection.ConnectionBean;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.relay.RelayType;
import net.tomp2p.relay.android.AndroidRelayServerConfig;
import net.tomp2p.relay.buffer.MessageBufferConfiguration;

import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.configs.NetworkConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryPutMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Creates a single stable peer with the configuration in the separate file
 * This can be used to either create a new P2P network or add a node to an existing network.
 * 
 * @author Nico
 *
 */
public class StableH2HPeer {

	// File Configuration
	private static final IFileConfiguration fileConfig = FileConfiguration.createDefault();

	private static final Logger logger = LoggerFactory.getLogger(StableH2HPeer.class);

	static {
		// increase timeouts
		ConnectionBean.DEFAULT_CONNECTION_TIMEOUT_TCP = 20000;
		ConnectionBean.DEFAULT_TCP_IDLE_SECONDS = 12;
		ConnectionBean.DEFAULT_UDP_IDLE_SECONDS = 12;
	}

	public static void main(String[] args) throws UnknownHostException {
		Config config = ConfigFactory.load("deployment-single.conf");
		int port = config.getInt("Port");
		boolean bootstrapEnabled = config.getBoolean("Bootstrap.enabled");
		String inetString = config.getString("Bootstrap.address");
		InetAddress bootstrapAddress = InetAddress.getByName(inetString);
		int bootstrapPort = config.getInt("Bootstrap.port");

		InetAddress externalAddress = null;
		String externalAddressString = config.getString("ExternalAddress");
		if (!"auto".equalsIgnoreCase(externalAddressString)) {
			externalAddress = InetAddress.getByName(externalAddressString);
		}

		boolean acceptData = config.getBoolean("AcceptData");

		boolean enableRelaying = config.getBoolean("Relay.enabled");
		AndroidRelayServerConfig androidServer = null;
		if (enableRelaying) {
			String gcmKey = config.getString("Relay.GCM.api-key");
			long bufferTimeout = config.getDuration("Relay.GCM.buffer-age-limit", TimeUnit.MILLISECONDS);
			MessageBufferConfiguration buffer = new MessageBufferConfiguration().bufferAgeLimit(bufferTimeout);
			androidServer = new AndroidRelayServerConfig(gcmKey, 5, buffer);
		}

		new StableH2HPeer(port, bootstrapEnabled, bootstrapAddress, bootstrapPort, externalAddress, acceptData,
				enableRelaying, androidServer);
	}

	public StableH2HPeer(int port, boolean bootstrapEnabled, InetAddress bootstrapAddress, int bootstrapPort,
			InetAddress externalAddress, boolean acceptData, boolean enableRelaying, AndroidRelayServerConfig androidConfig) {

		IH2HNode node = H2HNode.createNode(fileConfig);
		NetworkConfiguration netConf = NetworkConfiguration.createInitial();
		netConf.setPort(port);
		if (bootstrapEnabled) {
			netConf.setBootstrap(bootstrapAddress);
			netConf.setBootstrapPort(bootstrapPort);
		}

		if (!node.connect(netConf)) {
			logger.error("Peer cannot connect!");
			return;
		}

		if (externalAddress != null) {
			logger.debug("Binding to address {}", externalAddress);
			PeerAddress peerAddress = node.getPeer().peerBean().serverPeerAddress().changeAddress(externalAddress);
			node.getPeer().peerBean().serverPeerAddress(peerAddress);
		}

		if (!acceptData) {
			logger.debug("Denying all data requests on this peer");
			H2HStorageMemory storageLayer = (H2HStorageMemory) node.getPeer().storageLayer();
			storageLayer.setPutMode(StorageMemoryPutMode.DENY_ALL);
		}

		// start relaying if required
		if (enableRelaying) {
			logger.debug("Starting relay functionality...");

			PeerBuilderNAT nat = new PeerBuilderNAT(node.getPeer().peer());
			if (androidConfig != null) {
				nat.addRelayServerConfiguration(RelayType.ANDROID, androidConfig);
			}
			nat.start();
		}
	}
}
