package org.hive2hive.mobile.files;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.UserPermissionComparator;
import org.hive2hive.mobile.files.tasks.FileDeleteTask;
import org.hive2hive.mobile.files.tasks.FileDownloadTask;
import org.hive2hive.mobile.files.tasks.FileShareTask;
import org.hive2hive.mobile.files.tasks.FileUpdateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FilesFragment extends ListFragment {

	private static final Logger LOG = LoggerFactory.getLogger(FilesFragment.class);

	private H2HApplication context;
	private FileArrayAdapter fileAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

		// Inflate the layout for this fragment
		View view = super.onCreateView(localInflater, container, savedInstanceState);
		localInflater.inflate(R.layout.fragment_files, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	public String getPath() {
		return getArguments().getString("path");
	}

	public FileArrayAdapter getFileAdapter() {
		return fileAdapter;
	}

	private FilesActivity getFilesActivity() {
		return (FilesActivity) getActivity();
	}

	public void fillFileList(AndroidFile folder, H2HApplication context) {
		this.context = context;
		if (folder == null) {
			return;
		}

		// Put the data into the list
		List<AndroidFile> children = new ArrayList<>(folder.getChildren());
		fileAdapter = new FileArrayAdapter(context, children);
		setListAdapter(fileAdapter);
	}

	private void openFile(final File file) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);

		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = FilenameUtils.getExtension(file.getName());
		String type = mime.getMimeTypeFromExtension(ext);
		intent.setDataAndType(Uri.fromFile(file), type);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			// happens when the file has an unknown suffix / type
			Toast.makeText(getActivity(), R.string.files_error_open, Toast.LENGTH_LONG).show();
		}

		getFilesActivity().resetLogoutHint();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AndroidFile node = (AndroidFile) getListAdapter().getItem(position);
		if (node.isFolder() && (node.getState() == FileState.IN_SYNC || node.getState() == FileState.SHARED || node.getState() == FileState.TOP_SHARED)) {
			getFilesActivity().showFolder(node);
		} else {
			FileState state = node.getState();
			switch (state) {
				case IN_SYNC: // fall-through
				case SHARING:
					openFile(node.getFile());
					break;
				case DOWNLOADING:
					// TODO show dialog to cancel download
					break;
				case ON_AIR: // fall-through
				case OUTDATED:
					new FileDownloadTask(context.h2hNode().getFileManager(), fileAdapter, node).execute();
					break;
			}
			LOG.debug("Selected file '{}' for action...", node.getFile());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		AndroidFile item = fileAdapter.getItem(info.position);

		switch (item.getState()) {
			case ON_AIR:
			case OUTDATED:
				menu.add(0, R.string.context_files_download, 0, R.string.context_files_download);
				if (item.canWrite(context.currentUser())) {
					menu.add(0, R.string.context_files_update, 0, R.string.context_files_update);
				}
			case SHARED:
				menu.add(0, R.string.context_files_open, 0, R.string.context_files_open);
				if (item.isFile()) {
					menu.add(0, R.string.context_files_delete_locally, 0, R.string.context_files_delete_locally);
				} else {
					menu.add(0, R.string.context_folder_share_info, 0, R.string.context_folder_share_info);
				}
				if (item.canWrite(context.currentUser())) {
					menu.add(0, R.string.context_delete_globally, 0, R.string.context_delete_globally);
				}
				break;
			case TOP_SHARED:
				menu.add(0, R.string.context_files_open, 0, R.string.context_files_open);
				menu.add(0, R.string.context_folder_share, 0, R.string.context_folder_share);
				menu.add(0, R.string.context_folder_share_info, 0, R.string.context_folder_share_info);
				menu.add(0, R.string.context_delete_globally, 0, R.string.context_delete_globally);
				break;
			case IN_SYNC:
				menu.add(0, R.string.context_files_open, 0, R.string.context_files_open);
				if (item.isFile()) {
					menu.add(0, R.string.context_files_delete_locally, 0, R.string.context_files_delete_locally);
				} else {
					menu.add(0, R.string.context_folder_share, 0, R.string.context_folder_share);
				}
				if (item.canWrite(context.currentUser())) {
					menu.add(0, R.string.context_delete_globally, 0, R.string.context_delete_globally);
				}
				break;
			case SHARING:
			case UPLOADING:
			case DOWNLOADING:
			default:
				if (item.canWrite(context.currentUser())) {
					menu.add(0, R.string.context_delete_globally, 0, R.string.context_delete_globally);
				}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		getFilesActivity().resetLogoutHint();

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		AndroidFile node = fileAdapter.getItem(info.position);

		switch (item.getItemId()) {
			case R.string.context_files_download:
				new FileDownloadTask(context.h2hNode().getFileManager(), fileAdapter, node).execute();
				break;
			case R.string.context_files_update:
				new FileUpdateTask(context.h2hNode().getFileManager(), fileAdapter, node).execute();
				break;
			case R.string.context_files_delete_locally:
				if (node.getFile().delete()) {
					node.setState(FileState.ON_AIR, fileAdapter);
				} else {
					Toast.makeText(getActivity(), R.string.files_error_delete_failed, Toast.LENGTH_SHORT).show();
				}
				break;
			case R.string.context_delete_globally:
				if (node.isFolder() && !node.getChildren().isEmpty()) {
					// show warning that non-empty folders cannot be deleted
					Toast.makeText(getActivity(), R.string.files_error_delete_nonempty, Toast.LENGTH_SHORT).show();
				} else {
					new FileDeleteTask(context.h2hNode().getFileManager(), fileAdapter, node).execute();
				}
				break;
			case R.string.context_files_open:
				if (node.isFile()) {
					openFile(node.getFile());
				} else {
					getFilesActivity().showFolder(node);
				}
				break;
			case R.string.context_folder_share:
				shareFolder(node);
				break;
			case R.string.context_folder_share_info:
				showShareInfo(node);
				break;
			default:
				super.onContextItemSelected(item);
		}

		return true;
	}

	public void shareFolder(final AndroidFile file) {
		ContextThemeWrapper wrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = vi.inflate(R.layout.dialog_share, null);

		new AlertDialog.Builder(wrapper, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
				.setTitle(R.string.share_folder_title)
				.setMessage(R.string.share_folder_message)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						EditText usernameField = (EditText) view.findViewById(R.id.share_username);
						CheckBox writeCheckbox = (CheckBox) view.findViewById(R.id.share_write_permissions);

						PermissionType perm = writeCheckbox.isChecked() ? PermissionType.WRITE : PermissionType.READ;
						new FileShareTask(context.h2hNode().getFileManager(), fileAdapter, file, usernameField.getText().toString(), perm).execute();
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	private void showShareInfo(AndroidFile node) {
		// Concatenate user list
		List<UserPermission> permissions = new ArrayList<>(node.getPermissions());
		Collections.sort(permissions, new UserPermissionComparator());
		StringBuilder sb = new StringBuilder(getString(R.string.share_info_message, node.getFile().getName())).append("\n");
		for (UserPermission permission : permissions) {
			sb.append("\u2022 ").append(permission.getUserId());
			if (permission.getPermission() == PermissionType.READ) {
				sb.append(" ").append(getString(R.string.share_info_readonly));
			}
			sb.append("\n");
		}

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.share_info_title)
				.setMessage(sb.toString()).setNeutralButton(android.R.string.ok, null).show();
	}

}