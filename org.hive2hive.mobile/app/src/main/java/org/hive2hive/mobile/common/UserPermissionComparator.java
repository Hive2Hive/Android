package org.hive2hive.mobile.common;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;

import java.util.Comparator;

/**
 * @author Nico
 */
public class UserPermissionComparator implements Comparator<UserPermission> {

	@Override
	public int compare(UserPermission perm1, UserPermission perm2) {
		if (perm1.getPermission() == perm2.getPermission()) {
			return perm1.getUserId().toLowerCase().compareTo(perm2.getUserId().toLowerCase());
		} else {
			return perm1.getPermission() == PermissionType.WRITE ? -1 : 1;
		}
	}
}
