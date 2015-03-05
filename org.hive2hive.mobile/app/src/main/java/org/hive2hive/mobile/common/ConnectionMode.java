package org.hive2hive.mobile.common;

/**
 * @author Nico
 */
public enum ConnectionMode {
	WIFI("Wifi"),
	CELLULAR("3G"),
	OFFLINE("Offline");

	private final String name;

	private ConnectionMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
