package com.kubbit.preference;

import android.content.Context;
import android.util.AttributeSet;

public class EditTextPreference extends android.preference.EditTextPreference
{
	public EditTextPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public EditTextPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public EditTextPreference(Context context)
	{
		super(context);
	}

	@Override
	public CharSequence getSummary()
	{
		return this.getText();
	}
}
