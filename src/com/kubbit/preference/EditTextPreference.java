package com.kubbit.preference;

import android.content.Context;
import android.text.InputType;
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
		int variation = this.getEditText().getInputType() & InputType.TYPE_MASK_VARIATION;

		if ((variation == InputType.TYPE_TEXT_VARIATION_PASSWORD)
		 || (variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
			return super.getSummary();

		return this.getText();
	}
}
