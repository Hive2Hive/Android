package org.hive2hive.mobile.files.tasks;

import android.app.ProgressDialog;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.BaseProgressTask;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.mobile.files.AndroidFile;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico
 */
public class FileListTask extends BaseProgressTask {

	private static final Logger LOG = LoggerFactory.getLogger(FileListTask.class);

	public FileListTask(H2HApplication context, ISuccessFailListener listener, ProgressDialog progressDialog) {
		super(context, listener, progressDialog);
	}

	@Override
	protected String[] getProgressMessages() {
		return new String[]{context.getString(R.string.progress_files_title)};
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		if (context.h2hNode() != null && context.h2hNode().isConnected()) {
			IFileManager fileManager = context.h2hNode().getFileManager();
			try {
				IProcessComponent<FileNode> process = fileManager.createFileListProcess();
				FileNode root = process.execute();
				AndroidFile androidRoot = new AndroidFile(root, null);
				context.currentTree(androidRoot);
				LOG.debug("Successfully obtained the file list");
			} catch (NoPeerConnectionException | NoSessionException | InvalidProcessStateException | ProcessExecutionException e) {
				LOG.error("Cannot obtain the latest file list");
				return false;
			}

			return true;
		}

		return false;
	}
}
