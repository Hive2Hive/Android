package org.hive2hive.mobile.preference;

import android.app.Activity;
import android.os.Bundle;

import org.hive2hive.mobile.R;

/**
 * @author Nico
 */
public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.action_settings);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}
}