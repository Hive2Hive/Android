package org.hive2hive.mobile.login;

import android.app.ProgressDialog;

import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.AndroidFileAgent;
import org.hive2hive.mobile.common.BaseProgressTask;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends BaseProgressTask {
	private static final Logger LOG = LoggerFactory.getLogger(UserLoginTask.class);

	private final String username;
	private final String password;
	private final String pin;

	public UserLoginTask(H2HApplication context, String username, String password, String pin, ISuccessFailListener listener, ProgressDialog progressDialog) {
		super(context, listener, progressDialog);
		this.username = username;
		this.password = password;
		this.pin = pin;
	}

	@Override
	protected String[] getProgressMessages() {
		String[] progressMessages = new String[4];
		progressMessages[0] = context.getString(R.string.progress_login_encrypt);
		progressMessages[1] = context.getString(R.string.progress_login_register_check);
		progressMessages[2] = context.getString(R.string.progress_login_register);
		progressMessages[3] = context.getString(R.string.progress_login_login);
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

		IUserManager userManager = node.getUserManager();

		// create credentials here (takes some time)
		publishProgress(0);
		UserCredentials credentials = new UserCredentials(username, password, pin);

		try {
			LOG.debug("Check if user {} is already registered.", username);
			publishProgress(1);
			if (!userManager.isRegistered(username)) {
				LOG.debug("Start registering user {}.", username);
				publishProgress(2);
				userManager.createRegisterProcess(credentials).execute();
				LOG.debug("User {} successfully registered.", username);
			}
		} catch (NoPeerConnectionException | ProcessExecutionException | InvalidProcessStateException e) {
			LOG.error("Cannot check if user registered or cannot register user {}", username, e);
			return false;
		}

		try {
			LOG.debug("Start logging in user {}", username);
			publishProgress(3);
			AndroidFileAgent fileAgent = new AndroidFileAgent(context, username);
			userManager.createLoginProcess(credentials, fileAgent).execute();
			LOG.info("User {} successfully logged in", username);
			context.currentUser(username);
			return true;
		} catch (NoPeerConnectionException | ProcessExecutionException | InvalidProcessStateException e) {
			LOG.error("Cannot login user {}", credentials.getUserId(), e);
			return false;
		}
	}
}
