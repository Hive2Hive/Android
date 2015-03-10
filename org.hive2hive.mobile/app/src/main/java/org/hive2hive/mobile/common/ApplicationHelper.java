package org.hive2hive.mobile.common;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.provider.OpenableColumns;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Nico Rutishauser on 08.09.14.
 */
public class ApplicationHelper {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationHelper.class);
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String EXTERNAL_IP_LOOKUP_URL = "http://wtfismyip.com/text";

	/**
	 * Kills the application immediately. Only use in emergency!
	 */
	public static void killApplication() {
		android.os.Process.killProcess(Process.myPid());
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	public static boolean checkPlayServices(Activity activity) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				LOG.info("This device is not supported.");
			}
			return false;
		}
		return true;
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Maps a URI starting with 'content:' to a real file name
	 */
	public static String getFileNameContentURI(Context context, Uri contentUri) {
		Cursor returnCursor =
				context.getContentResolver().query(contentUri, null, null, null, null);
		int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		returnCursor.moveToFirst();
		return returnCursor.getString(nameIndex);
	}

	/**
	 * @return the current connection mode
	 */
	public static ConnectionMode getConnectionMode(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiInfo.isConnected()) {
			return ConnectionMode.WIFI;
		}
		NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileInfo.isConnected()) {
			return ConnectionMode.CELLULAR;
		}

		return ConnectionMode.OFFLINE;
	}
}
