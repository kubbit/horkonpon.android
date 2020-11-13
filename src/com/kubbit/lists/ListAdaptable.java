package com.kubbit.lists;

import android.graphics.Bitmap;

public interface ListAdaptable
{
	Long getId();
	String toListText();
	String toListSubText();
	Bitmap toPicture();
}
