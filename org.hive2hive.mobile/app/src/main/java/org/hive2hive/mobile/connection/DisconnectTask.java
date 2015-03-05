package org.hive2hive.mobile.connection;

import android.app.ProgressDialog;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.BaseProgressTask;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico
 */
public class DisconnectTask extends BaseProgressTask {

	private static final Logger LOG = LoggerFactory.getLogger(DisconnectTask.class);

	public DisconnectTask(H2HApplication context, ISuccessFailListener listener, ProgressDialog progressDialog) {
		super(context, listener, progressDialog);
	}

	@Override
	protected String[] getProgressMessages() {
		return new String[]{context.getString(R.string.progress_disconnect_shutdown)};
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		IH2HNode node = context.h2hNode();
		if (node != null && node.isConnected()) {
			LOG.debug("Start disconnecting the peer...");
			boolean shutdown = context.h2hNode().disconnect();

			if (shutdown) {
				LOG.debug("Successfully shut down the peer.");
				context.logout();
				context.h2hNode(null);
			} else {
				LOG.warn("Could not disconnect properly");
			}

			return shutdown;
		} else {
			// no need to disconnect
			return true;
		}
	}
}
