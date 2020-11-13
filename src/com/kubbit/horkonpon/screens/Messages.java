package com.kubbit.horkonpon.screens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import com.kubbit.horkonpon.*;
import com.kubbit.lists.ListAdaptable;
import com.kubbit.lists.ListAdapter;

public class Messages extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnTouchListener
{
	private ListView lstMessages;
	private EditText edWrite;
	private SwipeRefreshLayout swipeRefresh;
	private static BroadcastReceiver receiver;
	Parcelable listState;

	protected Issue issue;

	// receiver to reload messages after checking for new messages from server
	private class SyncReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			load();
		}
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.messages);

		this.lstMessages = (ListView)findViewById(R.id.lstMessages);
		this.edWrite = (EditText)findViewById(R.id.write);
		this.swipeRefresh = findViewById(R.id.pullToRefresh);

		this.issue = IssueList.getInstance(this.context).getSelected();

		this.swipeRefresh.setOnRefreshListener(this);
		this.lstMessages.setOnTouchListener(this);

		this.load();
	}

	private void registerReceiver()
	{
		receiver = new SyncReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_KEY);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
	}

	private void load()
	{
		if (this.issue == null)
			return;

		this.getToolBar().setTitle(this.issue.getInterlocutorText());

		ArrayList<ListAdaptable> literales = new ArrayList<>();
		ListAdapter listAdapter = new ListAdapter(this.context, R.layout.messages_row_mine, literales, 0, R.id.main_text, R.id.sub_text);

		this.lstMessages.setAdapter(listAdapter);

		// sort messages by date
		this.issue.getMessages().sort();

		for (Message m: this.issue.getMessages())
			listAdapter.add(m, m.isMine() ? R.layout.messages_row_mine : R.layout.messages_row_theirs);

		// restore list state (with scroll position)
		if (this.listState != null)
			this.lstMessages.onRestoreInstanceState(listState);
	}

	@Override
	public void onRefresh()
	{
		this.issue.sync(true);

		this.swipeRefresh.setRefreshing(false);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (event.getAction() != android.view.MotionEvent.ACTION_UP)
			return false;

		if (this.lstMessages.getAdapter().getCount() == 0
		 || (this.lstMessages.getLastVisiblePosition() == this.lstMessages.getAdapter().getCount() - 1
		 && this.lstMessages.getChildAt(this.lstMessages.getChildCount() - 1).getBottom() <= this.lstMessages.getHeight()))
		{
			this.swipeRefresh.setRefreshing(true);

			onRefresh();
		}

		return false;
	}

	@Override
	public void onPause()
	{
		// save list state (with scroll position)
		this.listState = this.lstMessages.onSaveInstanceState();

		super.onPause();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		this.registerReceiver();
	}

	@Override
	protected void onStop()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

		super.onStop();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		this.load();
	}

	public void sendOnClick(View v)
	{
		this.issue.addMessage(this.edWrite.getText().toString());

		// clear input box
		this.edWrite.setText("");

		// refresh message list
		this.load();
	}
}
