package org.hive2hive.mobile.files;

import java.util.Comparator;

/**
 * @author Nico
 */
public class AndroidFileComparator implements Comparator<AndroidFile> {

	@Override
	public int compare(AndroidFile file1, AndroidFile file2) {
		return file1.compareTo(file2);
	}
}
