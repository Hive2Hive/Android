package org.hive2hive.mobile.common;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.IFileAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Files are located at a public location, whereas cache is stored internally
 * Created by Nico Rutishauser on 24.11.14.
 */
public class AndroidFileAgent implements IFileAgent {

	private static final Logger LOG = LoggerFactory.getLogger(AndroidFileAgent.class);

	private final File rootDir;
	private final File cacheDir;

	public AndroidFileAgent(Context context, String username) {
		cacheDir = new File(context.getCacheDir(), username);
		rootDir = getStorageLocation(context, username);
		if (!rootDir.exists() && !rootDir.mkdirs()) {
			LOG.error("Root directory not created");
		}
	}

	public static File getStorageLocation(Context context, String username) {
		File folder;
		if (ApplicationHelper.isExternalStorageWritable()) {
			// Get the directory for the user's public pictures directory.
			folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Hive2Hive");
		} else {
			LOG.info("Need to save logs to internal storage because external is not available");
			folder = new File(context.getFilesDir(), "Hive2Hive");
		}

		if (username == null) {
			return folder;
		} else {
			return new File(folder, username);
		}
	}

	@Override
	public File getRoot() {
		return rootDir;
	}

	@Override
	public void writeCache(String key, byte[] data) throws IOException {
		File file = new File(cacheDir, key);
		FileUtils.writeByteArrayToFile(file, data);
	}

	@Override
	public byte[] readCache(String key) throws IOException {
		File file = new File(cacheDir, key);
		return FileUtils.readFileToByteArray(file);
	}
}
