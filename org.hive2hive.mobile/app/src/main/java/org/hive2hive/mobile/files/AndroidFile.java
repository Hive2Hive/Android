package org.hive2hive.mobile.files;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.security.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Nico
 */
public class AndroidFile implements Comparable<AndroidFile> {

	private static final Logger LOG = LoggerFactory.getLogger(AndroidFile.class);

	private final boolean isFile;
	private final File file;
	private final String path;
	private final Set<AndroidFile> children = new TreeSet<>();
	private final Set<UserPermission> permissions;
	private AndroidFile parent;
	private FileState state;

	private TextView detailsView;
	private ImageView imageView;
	private ProgressBar progressBar;

	/**
	 * Recursively copy the tree from the @link FileNode}
	 */
	public AndroidFile(FileNode node, AndroidFile parent) {
		this.file = node.getFile();
		this.isFile = node.isFile();
		this.path = node.getPath();
		this.parent = parent;
		this.permissions = node.getUserPermissions();

		if (node.isFolder()) {
			if (node.isShared() && parent != null) {
				if (parent.isShared()) {
					this.state = FileState.SHARED;
				} else {
					this.state = FileState.TOP_SHARED;
				}
			} else {
				this.state = FileState.IN_SYNC;
			}
			for (FileNode child : node.getChildren()) {
				children.add(new AndroidFile(child, this));
			}
		} else if (node.getFile().exists()) {
			try {
				// check if hash of file same as the hash of the item
				if (HashUtil.compare(node.getFile(), node.getMd5())) {
					this.state = FileState.IN_SYNC;
				} else {
					this.state = FileState.OUTDATED;
				}
			} catch (IOException e) {
				LOG.warn("Cannot compare the hash of the item {} with the hash in the user profile", node, e);
				this.state = FileState.OUTDATED;
			}
		} else {
			this.state = FileState.ON_AIR;
		}
	}

	/**
	 * Default constructor. Add children separately
	 */
	public AndroidFile(File file, boolean isFile, AndroidFile parent, FileState state, Set<UserPermission> permissions) {
		this.file = file;
		this.isFile = isFile;
		this.parent = parent;
		this.permissions = permissions;

		if (isFile) {
			this.path = parent.getPath() + file.getName();
		} else {
			this.path = parent.getPath() + file.getName() + File.separator;
		}
		this.state = state;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isFolder() {
		return !isFile();
	}

	public File getFile() {
		return file;
	}

	public Set<UserPermission> getPermissions() {
		return permissions;
	}

	public boolean canWrite(String userId) {
		for (UserPermission permission : permissions) {
			if (permission.getPermission() == PermissionType.WRITE && permission.getUserId().equalsIgnoreCase(userId)) {
				return true;
			}
		}
		return false;
	}

	public boolean isShared() {
		return permissions.size() > 1;
	}

	public String getPath() {
		return path;
	}

	public FileState getState() {
		return state;
	}

	public void setState(FileState state) {
		this.state = state;
	}

	public void setState(FileState state, FileArrayAdapter adapter) {
		this.state = state;
		if (imageView != null) {
			imageView.setImageDrawable(adapter.getContext().getResources().getDrawable(state.getIconId(isFile())));
		}
		if (detailsView != null) {
			detailsView.setText(state.getMessageId());
		}

		adapter.updateView(false);
	}

	public Set<AndroidFile> getChildren() {
		return children;
	}

	public AndroidFile getParent() {
		return parent;
	}

	public void setParent(AndroidFile parent) {
		this.parent = parent;
	}

	public void setDetailsView(TextView detailsView) {
		this.detailsView = detailsView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public AndroidFile findByFile(File file) {
		if (this.file.equals(file)) {
			// can stop the search, we found it here
			return this;
		}

		for (AndroidFile child : children) {
			AndroidFile found = child.findByFile(file);
			if (found != null) {
				return found;
			}
		}

		// no child matches
		return null;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof AndroidFile) {
			AndroidFile other = ((AndroidFile) o);
			return other.isFile == isFile() && other.getPath().equalsIgnoreCase(getPath());
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(AndroidFile other) {
		if (other == null) {
			return 1;
		} else if (other.isFile() != isFile()) {
			// folders first
			return isFolder() ? -1 : 1;
		} else {
			// same type than other, order by default file
			return file.getName().toLowerCase().compareTo(other.getFile().getName().toLowerCase());
		}
	}
}
