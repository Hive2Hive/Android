package org.hive2hive.mobile.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.mobile.connection.ConnectActivity;
import org.hive2hive.mobile.files.FilesActivity;
import org.hive2hive.mobile.preference.SettingsActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A login screen that offers login via username/password.
 *
 * @author Nico
 */
public class LoginActivity extends Activity {

	private static final Logger LOG = LoggerFactory.getLogger(LoginActivity.class);
	private static final String STORE_CREDENTIALS = "store";
	private static final String STORED_USER_ID = "userid";
	private static final String STORED_PASSWORD = "pin";
	private static final String STORED_PIN = "password";

	private H2HApplication context;

	// UI references.
	private EditText usernameView;
	private EditText passwordView;
	private EditText pinView;
	private CheckBox storeCredentials;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_login);

		context = (H2HApplication) getApplicationContext();
		if (context.h2hNode() == null || !context.h2hNode().isConnected()) {
			// head back to the connection page
			Intent intent = new Intent(context, ConnectActivity.class);
			startActivity(intent);
			return;
		}

		// Set up the login form.
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);
		pinView = (EditText) findViewById(R.id.pin);
		storeCredentials = (CheckBox) findViewById(R.id.store_credentials);

		// set stored credentials
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		usernameView.setText(prefs.getString(STORED_USER_ID, ""), TextView.BufferType.EDITABLE);
		passwordView.setText(prefs.getString(STORED_PASSWORD, ""), TextView.BufferType.EDITABLE);
		pinView.setText(prefs.getString(STORED_PIN, ""), TextView.BufferType.EDITABLE);
		storeCredentials.setChecked(prefs.getBoolean(STORE_CREDENTIALS, false));
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void login(View view) {
		if (context.h2hNode() == null) {
			return;
		}

		try {
			if (context.h2hNode().getUserManager().isLoggedIn()) {
				// already logged in
				startFileView();
				return;
			}
		} catch (NoPeerConnectionException e) {
			LOG.warn("Cannot determine the login state", e);
		}

		// Reset errors.
		usernameView.setError(null);
		passwordView.setError(null);

		// Store values at the time of the login attempt.
		String username = usernameView.getText().toString();
		String password = passwordView.getText().toString();
		String pin = pinView.getText().toString();

		boolean cancel = false;
		View focusView = null;


		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password)) {
			passwordView.setError(getString(R.string.error_field_required));
			focusView = passwordView;
			cancel = true;
		}

		// Check for a valid pin, if the user entered one.
		if (TextUtils.isEmpty(pin)) {
			pinView.setError(getString(R.string.error_field_required));
			focusView = pinView;
			cancel = true;
		}

		// Check for a valid username address.
		if (TextUtils.isEmpty(username)) {
			usernameView.setError(getString(R.string.error_field_required));
			focusView = usernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// create the progress dialog
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(getString(R.string.progress_login_title));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);

			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			UserLoginTask loginTask = new UserLoginTask(context, username, password, pin, new LoginListener(username, password, pin), dialog);
			loginTask.execute((Void) null);
		}
	}

	private void startFileView() {
		Intent intent = new Intent(getApplicationContext(), FilesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void storeCredentials(String username, String password, String pin) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (storeCredentials.isChecked()) {
			// store the credentials
			prefs.edit().putBoolean(STORE_CREDENTIALS, true).putString(STORED_USER_ID, username).putString(STORED_PASSWORD, password).putString(STORED_PIN, pin).commit();
		} else {
			// erase existing credentials
			prefs.edit().putBoolean(STORE_CREDENTIALS, false).remove(STORED_USER_ID).remove(STORED_PASSWORD).remove(STORED_PIN).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_help) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_login_url)));
			startActivity(browserIntent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class LoginListener implements ISuccessFailListener {

		private final String username;
		private final String password;
		private final String pin;

		public LoginListener(String username, String password, String pin) {
			this.username = username;
			this.password = password;
			this.pin = pin;
		}

		@Override
		public void onSuccess() {
			storeCredentials(username, password, pin);
			startFileView();
		}

		@Override
		public void onFail() {
			passwordView.setError(getString(R.string.error_incorrect_credentials));
			passwordView.requestFocus();
		}
	}
}



