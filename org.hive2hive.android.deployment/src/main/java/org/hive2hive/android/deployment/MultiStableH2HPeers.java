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
 * Creates multiple peers. The first one is the 'initial' peer, all others bootstrap to it.
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

		Config config = ConfigFactory.load("deployment-multiple.conf");
		int startPort = config.getInt("StartPort");
		String externalAddressString = config.getString("ExternalAddress");
		InetAddress externalAddress = null;
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

		for (int i = 0; i < numPeers; i++) {
			InetAddress bootstrapAddress = null;
			if (i > 0) {
				if (externalAddress == null) {
					// bootstrap by default to 127.0.0.1
					bootstrapAddress = InetAddress.getLocalHost();
				} else {
					// bootstrap to external address if existing
					bootstrapAddress = externalAddress;
				}
			}

			// iterate port to not reuse the same twice
			new StableH2HPeer(startPort + i, i != 0, bootstrapAddress, startPort, externalAddress, acceptData,
					enableRelaying, androidServer);
		}

		logger.debug("{} peers started!", numPeers);
	}
}
