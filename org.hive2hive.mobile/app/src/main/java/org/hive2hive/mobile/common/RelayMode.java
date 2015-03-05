package org.hive2hive.mobile.common;

/**
 * Created by Nico Rutishauser
 */
public enum RelayMode {
	GCM(0),
	TCP(1),
	FULL(2);

	private final int spinnerPosition;

	private RelayMode(int spinnerPosition) {
		this.spinnerPosition = spinnerPosition;
	}

	public int spinnerPosition() {
		return spinnerPosition;
	}

	public static RelayMode getByPosition(int pos) {
		for (RelayMode mode : RelayMode.values()) {
			if (mode.spinnerPosition() == pos) {
				return mode;
			}
		}

		// by default
		return GCM;
	}
}
