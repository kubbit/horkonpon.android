package com.kubbit.preference;

import android.content.Context;
import android.util.AttributeSet;

public class ListPreference extends android.preference.ListPreference
{
	public ListPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ListPreference(Context context)
	{
		super(context);
	}

	@Override
	public CharSequence getSummary()
	{
		return this.getEntry();
	}
}
