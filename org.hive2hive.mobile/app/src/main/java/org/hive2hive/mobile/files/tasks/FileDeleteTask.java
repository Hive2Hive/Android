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
public class FileDeleteTask extends BaseFileTask {

	private FileState stateBefore;

	public FileDeleteTask(IFileManager fileManager, FileArrayAdapter adapter, AndroidFile file) {
		super(fileManager, adapter, file);
	}

	@Override
	protected void beforeExecution() {
		stateBefore = file.getState();
		file.setState(FileState.DELETING, adapter);
	}

	@Override
	protected IProcessComponent<Void> getProcess(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException {
		return fileManager.createDeleteProcess(file.getFile());
	}

	@Override
	protected void afterExecution(boolean success) {
		if (success) {
			file.getFile().delete();
			adapter.remove(file);
			file.getParent().getChildren().remove(file);
		} else {
			file.setState(stateBefore, adapter);
			Toast.makeText(adapter.getContext(), adapter.getContext().getString(R.string.files_error_delete_failed), Toast.LENGTH_LONG).show();
		}
	}
}
