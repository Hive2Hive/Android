package org.hive2hive.mobile.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import org.hive2hive.core.H2HConstants;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class PortPickerPreference extends DialogPreference {

	public static final int MAX_VALUE = 65535;
	public static final int MIN_VALUE = 1;
	public static final int DEFAULT_PORT = H2HConstants.H2H_PORT;

	private NumberPicker picker;
	private int value;

	public PortPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PortPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected View onCreateDialogView() {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		picker = new NumberPicker(getContext());
		picker.setLayoutParams(layoutParams);

		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(picker);

		return dialogView;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		picker.setMinValue(MIN_VALUE);
		picker.setMaxValue(MAX_VALUE);
		picker.setValue(getValue());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setValue(picker.getValue());
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, DEFAULT_PORT);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue ? getPersistedInt(DEFAULT_PORT) : (Integer) defaultValue);
	}

	public void setValue(int value) {
		this.value = value;
		persistInt(this.value);
	}

	public int getValue() {
		return this.value;
	}
}