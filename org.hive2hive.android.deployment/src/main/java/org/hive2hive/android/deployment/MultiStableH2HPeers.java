package org.hive2hive.android.deployment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import net.tomp2p.relay.android.AndroidRelayServerConfig;
import net.tomp2p.relay.buffer.MessageBufferConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Creates multiple peers which bootstrap to an existing peer.
 * This can be used to increase the number of seed nodes in the P2P network.
 * 
 * @author Nico
 *
 */
public class MultiStableH2HPeers {

	private static final Logger logger = LoggerFactory.getLogger(StableH2HPeer.class);

	public static void main(String[] args) throws UnknownHostException {
		if (args.length == 0) {
			logger.error("Integer argument for number of peers required!");
			return;
		}

		Integer numPeers = Integer.valueOf(args[0]);

		for (int i = 0; i < numPeers; i++) {
			Config config = ConfigFactory.load("deployment.conf");
			int port = config.getInt("Port") + i;
			boolean bootstrapEnabled = config.getBoolean("Bootstrap.enabled");

			if (!bootstrapEnabled) {
				logger.error("Bootstrapping should be enabled. Create a stable peer first!", numPeers);
				return;
			}

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
			String gcmKey = config.getString("Relay.GCM.api-key");
			long bufferTimeout = config.getDuration("Relay.GCM.buffer-age-limit", TimeUnit.MILLISECONDS);
			MessageBufferConfiguration buffer = new MessageBufferConfiguration().bufferAgeLimit(bufferTimeout);
			AndroidRelayServerConfig androidServer = new AndroidRelayServerConfig(gcmKey, 5, buffer);

			new StableH2HPeer(port, bootstrapEnabled, bootstrapAddress, bootstrapPort, externalAddress, acceptData,
					enableRelaying, androidServer);
		}

		logger.debug("{} peers started!", numPeers);
	}
}
