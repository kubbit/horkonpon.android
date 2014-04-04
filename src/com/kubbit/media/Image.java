package com.kubbit.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;

import com.kubbit.utils.Log;

public class Image
{
	private static final int JPG_QUALITY = 90;
	private static final int MIN_DIMENSION = 800;

	private Bitmap bmp;
	private int orientation;

	public Image(File file)
	{
		this.getOrientation(file);
		this.createBitmap(file);
	}

	public Bitmap getBitmap()
	{
		return this.bmp;
	}

	private void createBitmap(File picture)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();

		// reduce size to avoid out of memory errors with high resolution photos
		// first, get picture dimensions and obtain best inSampleSize reduction
		// to match MIN_DIMENSION
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
		int largest = Math.max(options.outWidth, options.outHeight);
		options.inSampleSize = Math.max(largest / MIN_DIMENSION, 1);

		options.inJustDecodeBounds = false;
		this.bmp = BitmapFactory.decodeFile(picture.getAbsolutePath(), options);
	}

	public byte[] getJPG()
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		this.bmp.compress(Bitmap.CompressFormat.JPEG, JPG_QUALITY, output);

		return output.toByteArray();
	}

	public void rotate(int angle)
	{
		if (angle == 0)
			return;

		Matrix mat = new Matrix();
		mat.postRotate(angle);

		this.bmp = Bitmap.createBitmap(this.bmp, 0, 0, this.bmp.getWidth(), this.bmp.getHeight(), mat, true);
	}

	public void rotateToOrigin()
	{
		int angle = 0;

		switch (this.orientation)
		{
			case ExifInterface.ORIENTATION_ROTATE_270:
				angle = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				angle = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				angle = 90;
				break;
		}

		this.rotate(angle);
	}

	private void getOrientation(File picture)
	{
		try
		{
			ExifInterface exif = new ExifInterface(picture.getAbsolutePath());
			this.orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}
	}
}
