package org.hive2hive.mobile.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.tomp2p.relay.buffer.BufferRequestListener;

import org.hive2hive.mobile.H2HApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Nico Rutishauser on 08.09.14.
 * Handles incoming GCM messages
 */
public class GCMIntentService extends IntentService {

	private static final Logger LOG = LoggerFactory.getLogger(GCMIntentService.class);

	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// the message type is in the intent
		String messageType = gcm.getMessageType(intent);

		Bundle extras = intent.getExtras();
		if (extras.isEmpty()) {
			LOG.warn("Received empty GCM message of type {}", messageType);
			return;
		}

		LOG.debug("Received message of type {} with extras {}", messageType, extras.toString());

		H2HApplication application = (H2HApplication) getApplicationContext();
		BufferRequestListener handler = application.bufferListener();
		if (handler == null) {
			LOG.warn("Ignoring message because peer is not connected yet / anymore");
		} else {
			LOG.debug("Passing GCM message to handler");
			handler.sendBufferRequest(extras.getString("collapse_key"));
		}
	}
}
