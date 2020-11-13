package com.kubbit.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import java.io.File;

import com.kubbit.horkonpon.Activity;
import com.kubbit.horkonpon.Constants;
import com.kubbit.utils.Utils;

public class Camera
{	
	protected Activity context;
	protected File file;
	protected Image image;

	public Image getImage() throws Exception
	{
		if (this.image == null)
			this.image = new Image(this.file);

		return this.image;
	}

	public Camera(Activity context)
	{
		this.context = context;
	}

	public void clear()
	{
		this.image = null;

		if (this.file != null && this.file.exists())
			this.file.delete();

		this.file = null;
	}

	public void open() throws Exception
	{
		this.clear();

		this.file = Utils.getTemporaryFile(this.context, null);

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getFileUri(this.context, this.file));
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		context.startActivityForResult(intent, Constants.ActivityResultCode.TAKE_PICTURE);
	}
}