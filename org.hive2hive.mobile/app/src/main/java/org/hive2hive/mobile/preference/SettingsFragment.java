package org.hive2hive.mobile.preference;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import org.hive2hive.mobile.BuildConfig;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.AndroidFileAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Nico
 *         Inspired by http://www.vogella.com/tutorials/AndroidFileBasedPersistence/article.html
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(SettingsFragment.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// show the current value in the settings screen
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		try {
			String appVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			findPreference(getString(R.string.pref_app_version_key)).setSummary(appVersion);
		} catch (PackageManager.NameNotFoundException e) {
			LOG.warn("Cannot get the application version", e);
		}

		findPreference(getString(R.string.pref_h2h_version_key)).setSummary(BuildConfig.H2H_VERSION);

		// depends whether the user is logged in or not
		H2HApplication context = (H2HApplication) getActivity().getApplicationContext();
		File root = AndroidFileAgent.getStorageLocation(context, context.currentUser());
		findPreference(getString(R.string.pref_path_key)).setSummary(root.getAbsolutePath());

	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePreferences(findPreference(key));
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory cat = (PreferenceCategory) p;
			for (int i = 0; i < cat.getPreferenceCount(); i++) {
				initSummary(cat.getPreference(i));
			}
		} else {
			updatePreferences(p);
		}
	}

	private void updatePreferences(Preference p) {
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
		} else if (p instanceof PortPickerPreference) {
			PortPickerPreference ppPref = (PortPickerPreference) p;
			p.setSummary(String.valueOf(ppPref.getValue()));
		} else if (p instanceof ListPreference) {
			ListPreference lPref = ((ListPreference) p);
			p.setSummary(lPref.getEntry());
		}
	}
}
