package com.kidgeniusdesigns.snapapp;

import java.util.Arrays;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.habosa.javasnap.Friend;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;

public class FriendsList extends ListActivity {
	String[] userNamesAlphabetic;
	ArrayAdapter<String> adapter;
	EditText inputSearch;

	// List<Friend> myFriends;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_list);
		((MyApplication) getApplication())
				.getTracker(MyApplication.TrackerName.APP_TRACKER);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		inputSearch = (EditText) findViewById(R.id.inputSearch);

		userNamesAlphabetic = new String[SnapData.myFriendsNames.size()];

		int i = 0;
		for (String fr : SnapData.myFriendsNames) {
			userNamesAlphabetic[i] = fr;
			i++;
		}
		Arrays.sort(userNamesAlphabetic);
		adapter = new ArrayAdapter<String>(this, R.layout.list_item,
				SnapData.myFriendsNames);
		setListAdapter(adapter);

		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				FriendsList.this.adapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String userNameClicked = (String) getListAdapter().getItem(position);

		// set current friend and start next activity
		for (Friend fr : SnapData.myFriends) {
			if (fr.getUsername().contains(userNameClicked)) {
				Intent i = new Intent(this, FriendsSnapActivity.class);
				SnapData.currentFriend = fr;
				i.putExtra("sender", fr.getUsername());
				startActivity(i);
			}
		}
	}

	public void scrollDown(View v){
		getListView().smoothScrollToPosition(userNamesAlphabetic.length-1);
	}

	public void goToTop(View v) {
		getListView().smoothScrollToPosition(0);
	}
}