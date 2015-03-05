package org.hive2hive.mobile.files;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.mobile.H2HApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Nico
 */
@Listener(references = References.Strong)
public class AndroidFileEventListener implements IFileEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(AndroidFileEventListener.class);
	private final FilesActivity activity;
	private final H2HApplication context;

	// used to temporarily mark shared folders correctly
	private final Map<File, Set<UserPermission>> currentShareInvitations = new HashMap<>(1);

	public AndroidFileEventListener(FilesActivity activity, H2HApplication context) {
		this.activity = activity;
		this.context = context;
	}

	private FilesFragment getFragment() {
		return activity.getCurrentFragment();
	}

	private AndroidFile createAndroidFile(File file, boolean isFile, AndroidFile parent) {
		FileState state;
		Set<UserPermission> permissions;
		if (isFile) {
			state = FileState.ON_AIR;
			permissions = new HashSet<>(parent.getPermissions());
		} else if (currentShareInvitations.containsKey(file)) {
			permissions = currentShareInvitations.remove(file);
			state = FileState.SHARED;
		} else {
			state = FileState.IN_SYNC;
			permissions = new HashSet<>(parent.getPermissions());
		}

		return new AndroidFile(file, isFile, parent, state, permissions);
	}

	@Override
	@Handler
	public void onFileAdd(IFileAddEvent fileEvent) {
		LOG.debug("Remotely added file {}", fileEvent.getFile());
		final FilesFragment fragment = getFragment();
		final AndroidFile parent = context.currentTree().findByFile(fileEvent.getFile().getParentFile());
		if (fragment != null && parent != null) {
			final AndroidFile file = createAndroidFile(fileEvent.getFile(), fileEvent.isFile(), parent);
			parent.getChildren().add(file);

			// updateView the UI because it's currently watched folder
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (parent.equals(activity.getCurrentFolder())) {
						fragment.getFileAdapter().add(file);
					}

					fragment.getFileAdapter().updateView(true);
				}
			});
		}
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		LOG.debug("Remotely updated file {}", fileEvent.getFile());
		final AndroidFile file = context.currentTree().findByFile(fileEvent.getFile());
		if (file == null) {
			return;
		}

		file.setState(FileState.OUTDATED);

		// updateView the UI when it's the currently watched folder
		final FilesFragment fragment = getFragment();
		if (fragment != null && file.getParent().equals(activity.getCurrentFolder())) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					file.setState(FileState.OUTDATED, fragment.getFileAdapter());
				}
			});
		}
	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		LOG.debug("Remotely deleted file {}", fileEvent.getFile());
		final AndroidFile file = context.currentTree().findByFile(fileEvent.getFile());
		if (file == null) {
			return;
		}

		// remove from model
		boolean removed = file.getParent().getChildren().remove(file);
		final FilesFragment fragment = getFragment();
		if (removed && fragment != null) {
			// need to updateView the current view
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (file.getParent().equals(activity.getCurrentFolder())) {
						fragment.getFileAdapter().remove(file);
					}
					fragment.getFileAdapter().updateView(false);
				}
			});
		}
	}

	@Override
	@Handler
	public void onFileMove(final IFileMoveEvent fileEvent) {
		LOG.debug("Remotely moved file from {} to {}", fileEvent.getSrcFile(), fileEvent.getDstFile());
		final AndroidFile srcFile = context.currentTree().findByFile(fileEvent.getSrcFile());
		if (srcFile != null) {
			// for UI notification
			final String oldPath = srcFile.getParent().getPath();
			// remove it from the old parent
			srcFile.getParent().getChildren().remove(srcFile);

			final AndroidFile dstParent = context.currentTree().findByFile(fileEvent.getDstFile().getParentFile());
			if (dstParent != null) {
				// add the file to the new parent
				srcFile.setParent(dstParent);
				srcFile.getPermissions().clear();
				srcFile.getPermissions().addAll(dstParent.getPermissions());
				dstParent.getChildren().add(srcFile);

				final FilesFragment fragment = getFragment();
				if (fragment != null) {
					// need to updateView the current view
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (oldPath.equals(activity.getCurrentFolder().getPath())) {
								fragment.getFileAdapter().remove(srcFile);
							} else if (dstParent.equals(activity.getCurrentFolder())) {
								fragment.getFileAdapter().add(srcFile);
							}
							fragment.getFileAdapter().updateView(true);
						}
					});
				}
			} else {
				LOG.warn("New parent for moved file {} not found", fileEvent.getDstFile());
			}
		} else {
			LOG.warn("Original moved file {} not found. Already moved?", fileEvent.getSrcFile());
		}
	}

	@Override
	@Handler
	public void onFileShare(IFileShareEvent fileEvent) {
		LOG.debug("Got notified that file {} has been shared by {}.", fileEvent.getFile(), fileEvent.getInvitedBy());
		currentShareInvitations.put(fileEvent.getFile(), fileEvent.getUserPermissions());
		// can ignore because 'addFile' will be triggered as well
	}
}
