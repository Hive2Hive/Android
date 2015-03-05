package org.hive2hive.mobile.files.tasks;

import android.os.AsyncTask;
import android.view.View;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.mobile.files.AndroidFile;
import org.hive2hive.mobile.files.FileArrayAdapter;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico
 */
public abstract class BaseFileTask extends AsyncTask<Void, Void, Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(BaseFileTask.class);

	protected final FileArrayAdapter adapter;
	protected final AndroidFile file;
	private final IFileManager fileManager;

	public BaseFileTask(IFileManager fileManager, FileArrayAdapter adapter, AndroidFile file) {
		this.adapter = adapter;
		this.file = file;
		this.fileManager = fileManager;
	}

	@Override
	protected final void onPreExecute() {
		beforeExecution();
		if (file.getProgressBar() != null) {
			file.getProgressBar().setVisibility(View.VISIBLE);
			adapter.updateView(false);
		}
	}

	protected abstract void beforeExecution();

	@Override
	protected final Boolean doInBackground(Void... voids) {
		try {
			getProcess(fileManager).execute();
			return true;
		} catch (Exception e) {
			LOG.error("Cannot run the file process", e);
			return false;
		}
	}

	protected abstract IProcessComponent<Void> getProcess(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException;

	@Override
	protected final void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if (file.getProgressBar() != null) {
			file.getProgressBar().setVisibility(View.INVISIBLE);
			adapter.updateView(false);
		}

		afterExecution(success);
	}

	protected abstract void afterExecution(boolean success);
}
