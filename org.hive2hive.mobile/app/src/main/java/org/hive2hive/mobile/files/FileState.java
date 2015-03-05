package org.hive2hive.mobile.files;

import org.hive2hive.mobile.R;

/**
 * @author Nico
 */
public enum FileState {

	/**
	 * The file exists in the latest version on the disk The next state will be {@link org.hive2hive.mobile.files.FileState#OUTDATED} or {@link org.hive2hive.mobile.files.FileState#ON_AIR} if it's deleted locally
	 */
	IN_SYNC(R.drawable.ic_blank_ex, R.drawable.ic_folder, R.string.file_state_in_sync),

	/**
	 * The file is currently downloading. The next state will be {@link org.hive2hive.mobile.files.FileState#IN_SYNC}
	 */
	DOWNLOADING(R.drawable.ic_download_inex, R.drawable.ic_folder_loading, R.string.file_state_downloading),

	/**
	 * The file does only exist in the user profile ({@link org.hive2hive.core.processes.files.list.FileNode}, but not on the phone itself. The next state will be {@link org.hive2hive.mobile.files.FileState#DOWNLOADING}
	 */
	ON_AIR(R.drawable.ic_cloud_inex, R.drawable.ic_folder, R.string.file_state_on_air),

	/**
	 * The file exists in an older version on the phone. A new version could be downloaded. Thus, the next state is {@link org.hive2hive.mobile.files.FileState#DOWNLOADING}.
	 */
	OUTDATED(R.drawable.ic_loading_inex, R.drawable.ic_folder_loading, R.string.file_state_outdated),

	/**
	 * The file is currently uploading (new file or new version). Next state is {@link org.hive2hive.mobile.files.FileState#IN_SYNC}.
	 */
	UPLOADING(R.drawable.ic_upload_ex, R.drawable.ic_folder_upload, R.string.file_state_uploading),

	/**
	 * The user selected the file to be deleted. There's no next state because the file is completely removed from the network.
	 */
	DELETING(R.drawable.ic_loading_inex, R.drawable.ic_folder_loading, R.string.file_state_deleting),

	/**
	 * The user is currently sharing the folder with another user. The next state is again {@link org.hive2hive.mobile.files.FileState#SHARED}.
	 */
	SHARING(R.drawable.ic_loading_inex, R.drawable.ic_folder_loading, R.string.file_state_sharing),

	/**
	 * This folder is shared with another user
	 */
	TOP_SHARED(R.drawable.ic_loading_inex, R.drawable.ic_folder_shared, R.string.file_state_shared),

	/**
	 * The folder is currently shared with another user, but not the root share folder.
	 */
	SHARED(R.drawable.ic_loading_inex, R.drawable.ic_folder, R.string.file_state_in_sync);


	private final int fileIconId;
	private final int folderIconId;
	private final int messageId;

	private FileState(int fileIconId, int folderIconId, int messageId) {
		this.fileIconId = fileIconId;
		this.folderIconId = folderIconId;
		this.messageId = messageId;
	}

	public int getIconId(boolean isFile) {
		if (isFile) {
			return fileIconId;
		} else {
			return folderIconId;
		}
	}

	public int getMessageId() {
		return messageId;
	}
}
