package com.kubbit.horkonpon.screens;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import com.kubbit.horkonpon.*;
import com.kubbit.lists.*;
import com.kubbit.utils.Notification;

public class IssueBrowse extends Activity implements AdapterView.OnItemClickListener
{
	private IssueList issues;
	private ListView lstIsssues;
	Parcelable listState;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.issue_list);

		this.lstIsssues = (ListView)findViewById(R.id.lstIssues);
		registerForContextMenu(this.lstIsssues);

		this.issues = IssueList.getInstance(this.context);
	}

	private void load()
	{
		ArrayList<ListAdaptable> literales = new ArrayList<>();
		ListAdapter listAdapter = new ListAdapter(this.context, R.layout.issue_list_row, literales, R.id.picture, R.id.main_text, R.id.sub_text);

		this.lstIsssues.setAdapter(listAdapter);
		this.lstIsssues.setOnItemClickListener(this);

		// sort issues by date
		this.issues.sort(false);

		for (Issue g: this.issues)
			listAdapter.add(g);

		// restore list state (with scroll position)
		if (this.listState != null)
			this.lstIsssues.onRestoreInstanceState(listState);

		Notification.remove(this, Sender.NOTIFICATION_ID_UNREAD);
	}

	@Override
	public void onPause()
	{
		// save list state (with scroll position)
		this.listState = this.lstIsssues.onSaveInstanceState();

		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		this.load();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.issues.setSelected((Issue)parent.getItemAtPosition(position));

		this.showScreen(IssueShow.class);
	}
}
