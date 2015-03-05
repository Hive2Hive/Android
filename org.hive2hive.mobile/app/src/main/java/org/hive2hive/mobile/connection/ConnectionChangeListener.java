package org.hive2hive.mobile.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.common.ApplicationHelper;
import org.hive2hive.mobile.common.ConnectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico
 */
public class ConnectionChangeListener extends BroadcastReceiver {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionChangeListener.class);
	private final H2HApplication application;

	public ConnectionChangeListener(H2HApplication application) {
		this.application = application;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectionMode mode = ApplicationHelper.getConnectionMode(context);
		if (application.lastMode() == mode) {
			LOG.trace("Mode did not change, is still {}", mode);
			return;
		} else if (application.h2hNode() == null || !application.h2hNode().isConnected()) {
			LOG.trace("Connectivity mode did change to {} but node is not connected", mode);
			return;
		}

		LOG.debug("Network connection changed to {}", mode);

		// TODO take actions (reconnect node)

		application.lastMode(mode);
	}
}
