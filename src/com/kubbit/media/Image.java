package com.kubbit.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class Image
{
	public class Properties
	{
		private int width;
		private int height;
		private int orientation;

		public int getWidth()
		{
			return this.width;
		}
		public int getHeight()
		{
			return this.height;
		}
		public int getOrientation()
		{
			return this.orientation;
		}

		public Properties(int width, int height, int orientation)
		{
			this.width = width;
			this.height = height;

			this.orientation = orientation;
		}
	}

	private Bitmap bmp;
	private File file;
	private Properties properties;

	public Properties getProperties()
	{
		if (this.properties == null)
			this.loadProperties();

		return this.properties;
	}

	public File getFile()
	{
		return this.file;
	}

	public Bitmap getBitmap()
	{
		if (this.bmp == null)
			this.loadBitmap();
	
		return this.bmp;
	}

	public Image(File file) throws Exception
	{
		if (file == null)
			throw new Exception("com.kubbit.media.Image: null file");

		this.file = file;
	}

	/*
	 * Load image properties by getting just the needed information from the file
	 * without fully loading the bitmap
	 */
	private void loadProperties()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(this.file.getAbsolutePath(), options);

		int orientation = this.getOrientation();

		this.properties = new Properties(options.outWidth, options.outHeight, orientation);
	}

	/*
	 * Decode file into a Bitmap
	 */
	private void loadBitmap()
	{
		this.loadBitmap(null);
	}

	private void loadBitmap(BitmapFactory.Options options)
	{
		this.bmp = BitmapFactory.decodeFile(this.file.getAbsolutePath(), options);

		this.rotateToOrigin();
	}

	/*
	 * Load a scaled down version of the original image using minimum width / height as starting point
	 * Fast but does not give exact resolution
	 */
	public void loadScaledBitmap(int minSize)
	{
		int largest = Math.max(this.getProperties().getWidth(), this.getProperties().getHeight());

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = Math.max(largest / minSize, 1);

		options.inJustDecodeBounds = false;

		this.loadBitmap(options);
	}

	/*
	 * Resize bitmap using min width / height as starting point
	 */
	public void resize(int minSize)
	{
		int width = this.getBitmap().getWidth();
		int height = this.getBitmap().getHeight();

		float ratio = (float)width / (float)height;

		if (ratio > 0)
		{
		    width = minSize;
		    height = (int)(width / ratio);
		}
		else
		{
		    height = minSize;
		    width = (int)(height * ratio);
		}

		this.resize(width, height);
	}

	/*
	 * Resize bitmap to provided dimensions
	 */
	public void resize(int width, int height)
	{
		this.bmp = Bitmap.createScaledBitmap(this.getBitmap(), width, height, true);
	}

	private byte[] getJPG(int quality)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		this.bmp.compress(Bitmap.CompressFormat.JPEG, quality, output);

		return output.toByteArray();
	}

	public void writeJPG(File destination, int quality) throws Exception
	{
		Utils.writeToFile(this.getJPG(quality), destination);
	}

	public void rotate(int angle)
	{
		if (angle == 0)
			return;

		Matrix mat = new Matrix();
		mat.postRotate(angle);

		this.bmp = Bitmap.createBitmap(this.bmp, 0, 0, this.bmp.getWidth(), this.bmp.getHeight(), mat, true);
	}

	private void rotateToOrigin()
	{
		int angle = 0;

		switch (this.getProperties().getOrientation())
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

	private int getOrientation()
	{
		try
		{
			ExifInterface exif = new ExifInterface(this.file.getAbsolutePath());

			return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}

		return 0;
	}
}
