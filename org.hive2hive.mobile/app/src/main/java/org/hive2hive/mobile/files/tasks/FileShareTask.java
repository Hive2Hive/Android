package org.hive2hive.mobile.files.tasks;

import android.widget.Toast;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.files.AndroidFile;
import org.hive2hive.mobile.files.FileArrayAdapter;
import org.hive2hive.mobile.files.FileState;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * @author Nico
 */
public class FileShareTask extends BaseFileTask {

	private final String username;
	private final PermissionType permission;
	private final FileState stateBefore;

	public FileShareTask(IFileManager fileManager, FileArrayAdapter adapter, AndroidFile file, String username, PermissionType permission) {
		super(fileManager, adapter, file);
		this.username = username;
		this.permission = permission;
		this.stateBefore = file.getState();
	}

	@Override
	protected void beforeExecution() {
		file.setState(FileState.SHARING, adapter);
	}

	@Override
	protected IProcessComponent<Void> getProcess(IFileManager fileManager) throws NoSessionException, NoPeerConnectionException {
		return fileManager.createShareProcess(file.getFile(), username, permission);
	}

	@Override
	protected void afterExecution(boolean success) {
		if (success) {
			file.setState(FileState.TOP_SHARED, adapter);
			file.getPermissions().add(new UserPermission(username, permission));
		} else {
			file.setState(stateBefore, adapter);
			Toast.makeText(adapter.getContext(), adapter.getContext().getString(R.string.files_error_share, username), Toast.LENGTH_LONG).show();
		}
	}
}
