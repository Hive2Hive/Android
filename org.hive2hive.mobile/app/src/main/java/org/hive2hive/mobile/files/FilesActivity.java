package org.hive2hive.mobile.files;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.hive2hive.mobile.H2HApplication;
import org.hive2hive.mobile.R;
import org.hive2hive.mobile.common.ApplicationHelper;
import org.hive2hive.mobile.common.ISuccessFailListener;
import org.hive2hive.mobile.connection.ConnectActivity;
import org.hive2hive.mobile.files.tasks.FileListTask;
import org.hive2hive.mobile.files.tasks.FileUploadTask;
import org.hive2hive.mobile.login.UserLogoutTask;
import org.hive2hive.mobile.preference.SettingsActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class FilesActivity extends Activity {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectActivity.class);
	private static final int ADD_FILE_INTENT_CODE = 1;

	private H2HApplication context;
	private AndroidFileEventListener listener;

	// changing variables
	private AndroidFile currentFolder;
	private FilesFragment currentFragment;
	private boolean logoutHintShown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_files);
		getActionBar().setHomeButtonEnabled(true);

		// get the file taste list if not existing
		context = (H2HApplication) getApplicationContext();
		if (context.currentTree() == null) {
			loadFileList();
		} else {
			// show the root
			showFolder(context.currentTree());
		}

		if (listener == null) {
			// register the file listener to detect remote file events
			listener = new AndroidFileEventListener(this, context);
			context.h2hNode().getFileManager().subscribeFileEvents(listener);
		}
	}

	/**
	 * Gets the file list (asynchronous) and shows a loading dialog
	 */
	private void loadFileList() {
		// start the activity to get the file list
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(getString(R.string.progress_files_title));
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		new FileListTask(context, new FileNodeListener(currentFolder), dialog).execute((Void) null);
	}

	public void showFolder(AndroidFile node) {
		if (node == null) {
			return;
		} else {
			LOG.debug("Showing folder '{}'", node.getPath());
		}

		currentFolder = node;
		setTitle(File.separator + node.getPath());
		FilesFragment fragment = getOrCreate(node);
		activateFragment(fragment);
		fragment.fillFileList(node, context);

		resetLogoutHint();
	}

	private FilesFragment getOrCreate(AndroidFile node) {
		FilesFragment fragment = (FilesFragment) getFragmentManager().findFragmentByTag(node.getPath());
		if (fragment == null) {
			// Create a new Fragment to be placed in the activity layout
			fragment = new FilesFragment();
			Bundle bundle = new Bundle();
			bundle.putString("path", node.getPath());
			fragment.setArguments(bundle);
		}
		return fragment;
	}

	private void activateFragment(FilesFragment toActivate) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		FilesFragment existing = (FilesFragment) getFragmentManager().findFragmentById(R.id.files_container);
		if (existing == null) {
			// first time
			ft.add(R.id.files_container, toActivate, toActivate.getPath()).commit();
		} else {
			if (existing.getPath().contains(toActivate.getPath())) {
				// go one level up
				ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
			} else {
				// go into a folder
				ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
			}
			// Replace the fragment
			ft.replace(R.id.files_container, toActivate, toActivate.getPath()).commit();
		}

		currentFragment = toActivate;
	}

	public void resetLogoutHint() {
		// reset the logout hint
		logoutHintShown = false;
	}

	public AndroidFile getCurrentFolder() {
		return currentFolder;
	}

	public FilesFragment getCurrentFragment() {
		return currentFragment;
	}

	@Override
	public void onBackPressed() {
		if (currentFolder == null) {
			super.onBackPressed();
		} else if (currentFolder.isRoot()) {
			if (logoutHintShown) {
				logout();
			} else {
				LOG.debug("User is already at root level. Ask to logout");
				Toast.makeText(FilesActivity.this, R.string.files_logout_hint, Toast.LENGTH_LONG).show();
				logoutHintShown = true;
			}
		} else {
			showFolder(currentFolder.getParent());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_files, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		resetLogoutHint();

		if (id == R.id.action_refresh) {
			// clear all states
			loadFileList();
		} else if (id == R.id.action_logout) {
			logout();
		} else if (id == android.R.id.home) {
			if (currentFolder == null || currentFolder.isRoot()) {
				LOG.debug("Ignoring that the user pressed 'home'");
			} else {
				showFolder(currentFolder.getParent());
			}
		} else if (id == R.id.action_add_file) {
			if (currentFolder.canWrite(context.currentUser())) {
				selectFile();
			} else {
				Toast.makeText(FilesActivity.this, R.string.files_error_readonly, Toast.LENGTH_LONG).show();
			}
		} else if (id == R.id.action_add_folder) {
			if (currentFolder.canWrite(context.currentUser())) {
				createFolder();
			} else {
				Toast.makeText(FilesActivity.this, R.string.files_error_readonly, Toast.LENGTH_LONG).show();
			}
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
		} else if (id == R.id.action_help) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_files_url)));
			startActivity(browserIntent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_FILE_INTENT_CODE && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			LOG.debug("Selected file to add: {}", uri.getPath());

			File dst;
			try {
				if (uri.getScheme().equals("file")) {
					File src = new File(uri.getPath());
					dst = new File(currentFolder.getFile(), src.getName());
					FileUtils.copyFile(src, dst);
					LOG.debug("Successfully copied file {} to {}", src, dst);
				} else if (uri.getScheme().equals("content")) {
					InputStream stream = getContentResolver().openInputStream(uri);
					dst = new File(currentFolder.getFile(), ApplicationHelper.getFileNameContentURI(this, uri));
					FileUtils.copyInputStreamToFile(stream, dst);
					stream.close();
					LOG.debug("Successfully copied file stream to {}", dst);
				} else {
					LOG.error("Unknown file URI scheme: {}", uri.getScheme());
					return;
				}
			} catch (IOException e) {
				LOG.error("Copying file {} failed", uri, e);
				Toast.makeText(FilesActivity.this, R.string.files_error_copy, Toast.LENGTH_LONG).show();
				return;
			}

			AndroidFile androidFile = new AndroidFile(dst, true, currentFolder, FileState.UPLOADING, currentFolder.getPermissions());
			new FileUploadTask(context.h2hNode().getFileManager(), currentFragment.getFileAdapter(), androidFile).execute();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectFile() {
		// requires any file browser installed which listens on these intents
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");

		try {
			startActivityForResult(intent, ADD_FILE_INTENT_CODE);
		} catch (ActivityNotFoundException ex) {
			// can happen if the phone does not have a file manager
			Toast.makeText(this, R.string.files_error_browse, Toast.LENGTH_LONG).show();
		}
	}

	private void createFolder() {
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

		new AlertDialog.Builder(this)
				.setTitle(R.string.create_folder_title)
				.setMessage(R.string.create_folder_message)
				.setView(input)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().toString().trim().isEmpty()) {
							// don't allow empty folder names
							return;
						}

						File folder = new File(currentFolder.getFile(), input.getText().toString().trim());
						folder.mkdirs();
						AndroidFile androidFile = new AndroidFile(folder, false, currentFolder, FileState.UPLOADING, currentFolder.getPermissions());
						new FileUploadTask(context.h2hNode().getFileManager(), currentFragment.getFileAdapter(), androidFile).execute();
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	private void logout() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(getString(R.string.progress_logout_title));
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		new UserLogoutTask(context, new LogoutListener(), dialog).execute();
	}

	/**
	 * Listens on the result when the file list is fetched
	 */
	private class FileNodeListener implements ISuccessFailListener {

		private final AndroidFile currentFolder;

		FileNodeListener(AndroidFile currentFolder) {
			this.currentFolder = currentFolder;
		}

		@Override
		public void onSuccess() {
			if (currentFolder == null) {
				// first time here, show root
				showFolder(context.currentTree());
				return;
			}

			// show current view if existing
			AndroidFile found = context.currentTree().findByFile(currentFolder.getFile());
			if (found != null) {
				showFolder(found);
			} else {
				// otherwise, show the root
				showFolder(context.currentTree());
			}
		}

		@Override
		public void onFail() {
			LOG.error("Cannot get the file taste list");
			Toast.makeText(FilesActivity.this, R.string.files_error_filelist, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Listens to the logout async task
	 */
	private class LogoutListener implements ISuccessFailListener {

		@Override
		public void onSuccess() {
			context.logout();
			finish();
		}

		@Override
		public void onFail() {
			Toast.makeText(FilesActivity.this, R.string.error_logout_failed, Toast.LENGTH_SHORT).show();
			context.logout();
			finish();
		}
	}
}
