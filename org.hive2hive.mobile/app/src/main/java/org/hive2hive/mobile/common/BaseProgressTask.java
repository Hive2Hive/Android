package org.hive2hive.mobile.common;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.hive2hive.mobile.H2HApplication;

/**
 * @author Nico
 */
public abstract class BaseProgressTask extends AsyncTask<Void, Integer, Boolean> {

	protected final H2HApplication context;
	private final ISuccessFailListener listener;
	private final ProgressDialog progressDialog;

	private String[] progressMessages;

	public BaseProgressTask(H2HApplication context, ISuccessFailListener listener, ProgressDialog progressDialog) {
		this.context = context;
		this.listener = listener;
		this.progressDialog = progressDialog;
	}

	@Override
	protected void onPreExecute() {
		progressMessages = getProgressMessages();

		progressDialog.setProgress(0);
		progressDialog.setMax(progressMessages.length);
		progressDialog.setIndeterminate(false);

		progressDialog.setMessage(progressMessages[0]);
		progressDialog.show();
	}

	protected abstract String[] getProgressMessages();

	@Override
	protected final void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values != null && values.length > 0) {
			if (values[0] < progressMessages.length) {
				progressDialog.setMessage(progressMessages[values[0]]);
			}
			progressDialog.setProgress(values[0]);
		}
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			listener.onSuccess();
		} else {
			listener.onFail();
		}

		// close the progress dialog in any case
		progressDialog.dismiss();
	}
}
