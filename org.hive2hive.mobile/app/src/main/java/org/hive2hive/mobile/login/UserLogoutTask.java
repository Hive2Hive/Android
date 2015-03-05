package org.hive2hive.mobile.login;

import android.app.ProgressDialog;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.BaseProgressTask;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an asynchronous logout task
 */
public class UserLogoutTask extends BaseProgressTask {
	private static final Logger LOG = LoggerFactory.getLogger(UserLogoutTask.class);

	public UserLogoutTask(H2HApplication context, ISuccessFailListener listener, ProgressDialog progressDialog) {
		super(context, listener, progressDialog);
	}

	@Override
	protected String[] getProgressMessages() {
		String[] progressMessages = new String[1];
		progressMessages[0] = context.getString(R.string.progress_logout_msg);
		return progressMessages;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		IH2HNode node = context.h2hNode();
		if (node == null || !node.isConnected()) {
			LOG.error("H2HNode is null or not connected (anymore)");
			// TODO head back to the connection activity
			return false;
		}

		try {
			if (!node.getUserManager().isLoggedIn()) {
				LOG.info("Not logged in");
				return true;
			}
			LOG.debug("Start logging out...");
			node.getUserManager().createLogoutProcess().execute();
			LOG.debug("Successfully logged out");
			return true;
		} catch (InvalidProcessStateException | ProcessExecutionException | NoPeerConnectionException | NoSessionException e) {
			LOG.error("Cannot logout properly", e);
			return false;
		}
	}
}
