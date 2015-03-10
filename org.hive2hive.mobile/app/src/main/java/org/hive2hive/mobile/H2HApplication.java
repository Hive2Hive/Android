package org.hive2hive.mobile;

import android.app.Application;
import android.content.Context;

import net.tomp2p.connection.ConnectionBean;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.relay.buffer.BufferRequestListener;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.network.IPeerHolder;
import org.hive2hive.mobile.common.ApplicationHelper;
import org.hive2hive.mobile.common.ConnectionMode;
import org.hive2hive.mobile.common.RelayMode;
import org.hive2hive.mobile.files.AndroidFile;

/**
 * To be able to configure some things when the application starts / ends / ...
 */
public class H2HApplication extends Application implements IPeerHolder {

	private BufferRequestListener bufferListener;
	private IH2HNode h2hNode;
	private RelayMode relayMode;
	private INetworkConfiguration networkConfig;
	private AndroidFile treeRoot;
	private ConnectionMode lastMode;
	private String userId;

	@Override
	public void onCreate() {
		super.onCreate();

		// Prevent using IPv6, prefer IPv4
		System.setProperty("java.net.preferIPv4Stack", "true");

		lastMode = ApplicationHelper.getConnectionMode(this);

//		ConnectionChangeListener connectionChangeListener = new ConnectionChangeListener(this);
//		IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//		registerReceiver(connectionChangeListener, filter);

		// increase timeouts
		ConnectionBean.DEFAULT_CONNECTION_TIMEOUT_TCP = 20000;
		ConnectionBean.DEFAULT_TCP_IDLE_SECONDS = 12;
		ConnectionBean.DEFAULT_UDP_IDLE_SECONDS = 12;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		// enable multidex
//		MultiDex.install(this);
	}

	/**
	 * Singleton-like application data that needs to be present in the whole application.
	 * Note that the content of this data is removed once the process is killed.
	 */

	public void relayMode(RelayMode relayMode) {
		this.relayMode = relayMode;
	}

	public RelayMode relayMode() {
		return relayMode;
	}

	public void bufferListener(BufferRequestListener bufferListener) {
		this.bufferListener = bufferListener;
	}

	public BufferRequestListener bufferListener() {
		return bufferListener;
	}

	public void h2hNode(IH2HNode h2hNode) {
		this.h2hNode = h2hNode;
	}

	public IH2HNode h2hNode() {
		return h2hNode;
	}

	public void networkConfig(INetworkConfiguration networkConfig) {
		this.networkConfig = networkConfig;
	}

	public INetworkConfiguration networkConfig() {
		return networkConfig;
	}


	public void logout() {
		treeRoot = null;
		userId = null;
	}

	/**
	 * Holds the latest file list of the currently logged in user
	 *
	 * @param treeRoot
	 */
	public void currentTree(AndroidFile treeRoot) {
		this.treeRoot = treeRoot;
	}

	/**
	 * @return the last file taste list or <code>null</code> if not fetched yet
	 */
	public AndroidFile currentTree() {
		return treeRoot;
	}

	public void currentUser(String userId) {
		this.userId = userId;
	}

	public String currentUser() {
		return userId;
	}

	@Override
	public PeerDHT getPeer() {
		if (h2hNode == null) {
			return null;
		}
		return h2hNode.getPeer();
	}

	/**
	 * @return the last connection mode
	 */
	public ConnectionMode lastMode() {
		return lastMode;
	}

	/**
	 * Set the last mode in order to detect changes when the {@link org.hive2hive.mobile.connection.ConnectionChangeListener} is triggered
	 */
	public void lastMode(ConnectionMode lastMode) {
		this.lastMode = lastMode;
	}
}
