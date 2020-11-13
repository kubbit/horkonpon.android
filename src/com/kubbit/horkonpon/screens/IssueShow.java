package com.kubbit.horkonpon.screens;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kubbit.horkonpon.*;
import com.kubbit.media.Camera;
import com.kubbit.media.Image;
import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class IssueShow extends Activity
{
	protected final int MARGIN_BETWEEN_THUMBNAILS = 5;

	protected ImageView ivPhoto;
	protected LinearLayout lThumbnails;
	protected LinearLayout lThumbnailsContainer;
	protected TextView tvDate;
	protected TextView tvComment;

	protected Camera camera;

	protected Issue issue;
	protected Image defaultImage;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.issue_show);

		this.ivPhoto = (ImageView)findViewById(R.id.img);
		this.lThumbnailsContainer = (LinearLayout)findViewById(R.id.thumbnails_cntr);
		this.lThumbnails = (LinearLayout)findViewById(R.id.thumbnails);
		this.tvDate = (TextView)findViewById(R.id.date);
		this.tvComment = (TextView)findViewById(R.id.comment);

		this.issue = IssueList.getInstance(this).getSelected();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		this.load();
	}

	private void load()
	{
		if (this.issue == null)
			return;

		this.loadThumbnails();

		if (this.defaultImage != null)
			this.loadImage(this.defaultImage);

		this.tvDate.setText(Utils.dateToStrLocalized(this.context, this.issue.getUpdateDate()));

		this.issue.read();

		String text = "";
		this.issue.getMessages().sort();
		for (Message m: this.issue.getMessages())
			text = text + m.getText() + "\n\n";

		this.tvComment.setText(text);

		this.getToolBar().setTitle(this.issue.getLocality());

		this.issue.sync();
	}

	private void loadImage(Image img)
	{
		this.ivPhoto.setImageBitmap(img.getBitmap());
		this.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	private void loadThumbnails()
	{
		this.defaultImage = null;

		this.lThumbnails.removeAllViews();

		try
		{
			for (DBFile f: this.issue.getFiles())
			{
				final Image img = new Image(f.getFile());

				final ImageView thumbView = new ImageView(this.context);
				thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				thumbView.setImageBitmap(f.getThumbnail());
				thumbView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
					    loadImage(img);
					}
				});

				this.lThumbnails.addView(thumbView);

				// set height to match parent, otherwise it depends on the image dimensions
				thumbView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				// add margin between thumbnails
				((ViewGroup.MarginLayoutParams)(thumbView.getLayoutParams())).rightMargin = MARGIN_BETWEEN_THUMBNAILS;
				// refresh view after changes
				thumbView.requestLayout();

				if (this.defaultImage == null)
					this.defaultImage = img;
			}

			// hide thumbnails panel if there are no pictures
			this.lThumbnailsContainer.setVisibility(this.issue.getFiles().size() == 0 ? View.GONE : View.VISIBLE);
		}
		catch (Exception e)
		{
			Log.debug(e.getMessage());
		}
	}

	private void takePicture()
	{
		try
		{
			this.camera = new Camera(this);
			this.camera.open();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
			Toast.makeText(this.context, R.string.error_while_loading_picture, Toast.LENGTH_LONG).show();
		}
	}

	public void imgOnClick(View v)
	{
		this.takePicture();
	}

	public void messagesOnClick(View v)
	{
		this.showScreen(Messages.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		try
		{
			if (resultCode != Activity.RESULT_OK)
				return;

			switch (requestCode)
			{
				case Constants.ActivityResultCode.TAKE_PICTURE:
					try
					{
						Image img = this.camera.getImage();

						this.loadImage(img);
						img.loadScaledBitmap(Constants.IMAGE_MIN_SIZE);

						DBFile file = new DBFile(this.context);
						img.writeJPG(file.getFile(), Constants.IMAGE_QUALITY);
						this.issue.addFile(file);

						this.defaultImage = img;
					}
					catch (Exception e)
					{
						Log.error(e.getMessage());
						Toast.makeText(this.context, R.string.error_while_loading_picture, Toast.LENGTH_LONG).show();
					}

					break;
			}
		}
		finally
		{
			this.camera.clear();
			this.camera = null;
		}
	}
}
