package org.hive2hive.mobile.files.tasks;

import android.widget.Toast;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.files.AndroidFile;
import org.hive2hive.mobile.files.FileArrayAdapter;
import org.hive2hive.mobile.files.FileState;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * @author Nico
 */
public class FileUpdateTask extends BaseFileTask {

	public FileUpdateTask(IFileManager fileManager, FileArrayAdapter adapter, AndroidFile file) {
		super(fileManager, adapter, file);
	}

	@Override
	protected void beforeExecution() {
		file.setState(FileState.UPLOADING, adapter);
	}

	@Override
	protected IProcessComponent<Void> getProcess(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException {
		return fileManager.createUpdateProcess(file.getFile());
	}

	@Override
	protected void afterExecution(boolean success) {
		if (success) {
			file.setState(FileState.IN_SYNC, adapter);
		} else {
			file.setState(FileState.OUTDATED, adapter);
			Toast.makeText(adapter.getContext(), adapter.getContext().getString(R.string.files_error_upload, file.getPath()), Toast.LENGTH_LONG).show();
		}
	}
}
