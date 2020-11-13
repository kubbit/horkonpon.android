package com.kubbit.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.kubbit.horkonpon.R;

public class ListAdapter extends ArrayAdapter<ListAdaptable>
{
	private final ArrayList<ListAdaptable> items;
	private final Context context;
	private final int defaultResource;
	private final HashMap<Long, Integer> resources = new HashMap<Long, Integer>();
	private final int pictureId;
	private final int mainTextId;
	private final int subTextId;

	public ListAdapter(Context context, int textViewResourceId, ArrayList<ListAdaptable> items, int pictureId, int mainTextId, int subTextId)
	{
		super(context, textViewResourceId, items);

		this.context = context;

		this.items = items;

		this.defaultResource = textViewResourceId;
		this.pictureId = pictureId;
		this.mainTextId = mainTextId;
		this.subTextId = subTextId;
	}

	@Override
	public void add(ListAdaptable item)
	{
		this.add(item, 0);
	}

	public void add(ListAdaptable item, int resourceId)
	{
		super.add(item);

		Long id = this.getItemId(this.getPosition(item));
		this.resources.put(id, resourceId);
	}

	@Override
	public long getItemId(int position)
	{
		return this.getItem(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ListAdaptable m = this.getItem(position);

		int resource = this.resources.get(this.getItemId(position));
		if (resource == 0)
			resource = this.defaultResource;

		LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View v = vi.inflate(resource, null);

		if (m == null)
			return v;

		ImageView ivPicture = (ImageView)v.findViewById(this.pictureId);
		TextView txtMain = (TextView)v.findViewById(this.mainTextId);
		TextView txtSub = (TextView)v.findViewById(this.subTextId);

		if (ivPicture != null)
		{
			if (m.toPicture() != null)
			{
				ivPicture.setImageBitmap(m.toPicture());
				ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
			}
			else
			{
				ivPicture.setImageResource(R.drawable.camera);
				ivPicture.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			}
		}

		if (txtMain != null)
			txtMain.setText(m.toListText());

		if (txtSub != null)
			txtSub.setText(m.toListSubText());

		return v;
	}
}
