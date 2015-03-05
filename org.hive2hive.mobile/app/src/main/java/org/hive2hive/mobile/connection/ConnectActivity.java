package org.hive2hive.mobile.connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.mobile.BuildConfig;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.ApplicationHelper;
import org.hive2hive.mobile.common.ConnectionMode;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.mobile.common.RelayMode;
import org.hive2hive.mobile.login.LoginActivity;
import org.hive2hive.mobile.preference.SettingsActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ConnectActivity extends Activity {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectActivity.class);

	private static final String EXPERT_VIEW_SHOW = "expert";
	private static final String BOOTSTRAP_ADDRESS = "address";
	private static final String BOOTSTRAP_PORT = "port";

	private TextView loginField;
	private EditText addressField;
	private EditText portField;
	private Button connectBtn;
	private Spinner relaySpinner;
	private CheckBox expertCheckbox;
	private ViewGroup expertView;
	private H2HApplication application;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setTitle(R.string.bootstrap_activity_title);
		setContentView(R.layout.activity_connect);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		addressField = (EditText) findViewById(R.id.bootstrap_address);
		portField = (EditText) findViewById(R.id.bootstrap_port);
		connectBtn = (Button) findViewById(R.id.connect_button);
		relaySpinner = (Spinner) findViewById(R.id.bootstrap_relay_mode);
		loginField = (TextView) findViewById(R.id.bootstrap_goto_login);

		expertCheckbox = (CheckBox) findViewById(R.id.bootstrap_expert_check);
		expertView = (ViewGroup) findViewById(R.id.bootstrap_expert_view);
		expertCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				expertView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				prefs.edit().putBoolean(EXPERT_VIEW_SHOW, isChecked).commit();
			}
		});
		expertCheckbox.setChecked(prefs.getBoolean(EXPERT_VIEW_SHOW, false));

		application = (H2HApplication) getApplicationContext();
		if (!isConnected()) {
			/** Since this is the first activity, some checks need to be made here */
			if (ApplicationHelper.checkPlayServices(this)) {
				LOG.debug("Play services are ok. Can use GCM service.");
			} else {
				LOG.error("Cannot use GCM service. Stopping application now.");
				ApplicationHelper.killApplication();
			}

			// set default values
			addressField.setText(prefs.getString(BOOTSTRAP_ADDRESS, BuildConfig.DEFAULT_BOOTSTRAP_ADDRESS));
			portField.setText(prefs.getString(BOOTSTRAP_PORT, String.valueOf(BuildConfig.DEFAULT_BOOTSTRAP_PORT)));
			connectBtn.setText(R.string.bootstrap_button_connect);

			selectRecommendedMode();
		} else {
			// fill the values from current connection state
			INetworkConfiguration networkConfiguration = application.networkConfig();
			addressField.setText(networkConfiguration.getBootstrapAddress().getHostAddress());
			portField.setText(String.valueOf(networkConfiguration.getBootstrapPort()));
			connectBtn.setText(R.string.bootstrap_button_disconnect);
			relaySpinner.setSelection(application.relayMode().spinnerPosition());
		}

		enableBootstrapFields(isConnected());
	}

	private boolean isConnected() {
		return application.h2hNode() != null && application.h2hNode().isConnected();
	}

	@Override
	protected void onResume() {
		if (!isConnected()) {
			selectRecommendedMode();
		}

		super.onResume();
	}

	private void selectRecommendedMode() {
		List<String> spinnerArray = new ArrayList<String>();
		ConnectionMode mode = ApplicationHelper.getConnectionMode(this);
		int preferredPosition = 0;
		if (mode == ConnectionMode.WIFI) {
			spinnerArray.add(getString(R.string.relay_mode_gcm));
			spinnerArray.add(getString(R.string.relay_mode_tcp) + " " + getString(R.string.relay_mode_recommended));
			spinnerArray.add(getString(R.string.relay_mode_full));
			preferredPosition = RelayMode.TCP.spinnerPosition();
		} else {
			spinnerArray.add(getString(R.string.relay_mode_gcm) + " " + getString(R.string.relay_mode_recommended));
			spinnerArray.add(getString(R.string.relay_mode_tcp));
			preferredPosition = RelayMode.GCM.spinnerPosition();
		}

		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
		modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		relaySpinner.setAdapter(modeAdapter);

		// make selection
		relaySpinner.setSelection(preferredPosition);
	}

	/**
	 * Fired when the 'connect' / 'disconnect' button is pressed
	 */
	public void connectDisconnect(View view) {
		if (!isConnected()) {
			ConnectionMode connectionMode = ApplicationHelper.getConnectionMode(this);
			if (connectionMode == ConnectionMode.OFFLINE) {
				// show warning when no network (wifi or cellular)
				Toast.makeText(getApplicationContext(), R.string.warn_disconnected, Toast.LENGTH_LONG).show();
				return;
			}

			String address = addressField.getText().toString();
			int bootstrapPort = Integer.valueOf(portField.getText().toString());
			RelayMode relayMode = RelayMode.getByPosition(relaySpinner.getSelectedItemPosition());

			// checks for GCM
			long gcmSenderId = -1;
			if (relayMode == RelayMode.GCM) {
				String senderId = prefs.getString(getString(R.string.pref_gcm_sender_key), "-1");
				try {
					gcmSenderId = Long.parseLong(senderId);
				} catch (NumberFormatException e) {
					LOG.warn("Cannot parse the gcm sender id {} to a Long", senderId);
				}

				if (gcmSenderId <= 0) {
					LOG.warn("No or invalid GCM sender id provided, start preference activity");
					Toast.makeText(getApplicationContext(), R.string.warn_gcm_sender_missing, Toast.LENGTH_LONG).show();
					Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
					startActivity(intent);
					return;
				}
			}

			// create the progress dialog
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(getString(R.string.progress_connect_title));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);

			// disable buttons during connection
			connectBtn.setEnabled(false);
			enableBootstrapFields(true);

			LOG.debug("Connecting the peer...");
			final ConnectionSetupTask task = new ConnectionSetupTask(address, bootstrapPort, relayMode, gcmSenderId, application, new ConnectListener(), dialog);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (task != null) {
						task.cancel(true);
					}
				}
			});
			task.execute();
		} else {
			// create the progress dialog
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(getString(R.string.progress_disconnect_title));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);

			LOG.debug("Disconnecting the peer...");
			DisconnectTask task = new DisconnectTask(application, new DisconnectListener(), dialog);
			task.execute();
		}
	}

	public void login(View view) {
		// go back to the login
		if (isConnected()) {
			// start login activity
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}
	}

	private void enableBootstrapFields(boolean connected) {
		relaySpinner.setEnabled(!connected);
		addressField.setEnabled(!connected);
		portField.setEnabled(!connected);

		if (connected) {
			connectBtn.setText(R.string.bootstrap_button_disconnect);
			loginField.setVisibility(View.VISIBLE);
		} else {
			connectBtn.setText(R.string.bootstrap_button_connect);
			loginField.setVisibility(View.GONE);
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
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_connect_url)));
			startActivity(browserIntent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class ConnectListener implements ISuccessFailListener {

		@Override
		public void onSuccess() {
			LOG.info("Successfully connected this node");

			Toast.makeText(getApplicationContext(), R.string.bootstrap_connected, Toast.LENGTH_SHORT).show();
			connectBtn.setEnabled(true);
			enableBootstrapFields(true);

			// store the bootstrap details
			prefs.edit().putString(BOOTSTRAP_ADDRESS, addressField.getText().toString()).putString(BOOTSTRAP_PORT, portField.getText().toString()).commit();

			// start login activity
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}

		@Override
		public void onFail() {
			Toast.makeText(getApplicationContext(), R.string.bootstrap_disconnected,
					Toast.LENGTH_SHORT).show();
			connectBtn.setEnabled(true);
			enableBootstrapFields(false);
		}
	}

	private class DisconnectListener implements ISuccessFailListener {
		@Override
		public void onSuccess() {
			enableBootstrapFields(false);
		}

		@Override
		public void onFail() {
			enableBootstrapFields(true);
		}
	}
}
