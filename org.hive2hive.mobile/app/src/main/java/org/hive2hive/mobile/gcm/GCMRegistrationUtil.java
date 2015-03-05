package org.hive2hive.mobile.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.hive2hive.mobile.connection.ConnectActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Nico Rutishauser on 08.09.14.
 * <p/>
 * If the application is running for the first time on this device, a new registration ID will be created by GCM.
 * Else, the existing ID will be used. Then, this id is forwarded to all relay peers.
 */
public class GCMRegistrationUtil {

	private static final Logger LOG = LoggerFactory.getLogger(GCMRegistrationUtil.class);
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "app_version";
	private static final String PROPERTY_GCM_SENDER = "gcm_sender";

	private GCMRegistrationUtil() {
		// only static method
	}

	/**
	 * Obtain the registration ID from the application store or create a new one.
	 */
	public static String getRegistrationId(Context context, long gcmSenderId) {
		int currentAppVersion = getAppVersion(context);
		String registrationID = getStoredRegistrationId(context, currentAppVersion, gcmSenderId);
		if (registrationID == null || registrationID.isEmpty()) {
			registrationID = obtainRegistrationId(context, gcmSenderId);
			LOG.debug("Successfully obtained a new registration ID {} for version {} and sender id {}", registrationID, currentAppVersion, gcmSenderId);
			storeRegistrationId(context, registrationID, currentAppVersion, gcmSenderId);
		} else {
			LOG.debug("Reusing existing registration ID: {}", registrationID);
		}

		return registrationID;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private static String getStoredRegistrationId(Context context, int currentAppVersion, long gcmSenderId) {
		SharedPreferences prefs = context.getSharedPreferences(ConnectActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		String registrationId = prefs.getString(PROPERTY_REG_ID, null);
		if (registrationId == null) {
			LOG.info("No registration id stored. Obtain new one");
			return null;
		}

		// Check if the app version and the gcm sender id of the registered prefs match
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		if (registeredVersion != currentAppVersion) {
			LOG.info("App version changed from {} to {}! Obtain new registration id.", registeredVersion, currentAppVersion);
			return null;
		}

		long registeredGcmSenderId = prefs.getLong(PROPERTY_GCM_SENDER, Long.MIN_VALUE);
		if (registeredGcmSenderId != gcmSenderId) {
			LOG.info("GCM sender id {} does not match with stored one ({}). Obtain new registration id.", gcmSenderId, registeredGcmSenderId);
			return null;
		}

		return registrationId;
	}

	private static void storeRegistrationId(Context context, String registrationId, int currentAppVersion, long gcmSenderId) {
		SharedPreferences prefs = context.getSharedPreferences(ConnectActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		prefs.edit().putString(PROPERTY_REG_ID, registrationId).putInt(PROPERTY_APP_VERSION, currentAppVersion).putLong(PROPERTY_GCM_SENDER, gcmSenderId).commit();
	}

	private static String obtainRegistrationId(Context context, long gcmSenderId) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

		try {
			return gcm.register(String.valueOf(gcmSenderId));
		} catch (IOException e) {
			LOG.error("Cannot obtain a registration ID for sender id {}", gcmSenderId, e);
			return null;
		}
	}
}
