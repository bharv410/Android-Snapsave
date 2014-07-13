package com.kidgeniusdesigns.snapapp;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;

public class FriendsSnapActivity extends Activity {
	MyAdapter adapter;
	GridView gridView;
	ProgressBar friendProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_snap);
		((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		
		if(getIntent().getStringExtra("sender")!=null)
		bar.setTitle(getIntent().getStringExtra("sender"));
		bar.setIcon(
				   new ColorDrawable(getResources().getColor(android.R.color.transparent)));   
		
		gridView = (GridView) findViewById(R.id.gridview2);
		LoadStories ls = new LoadStories();
		ls.execute();
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				SnapData.currentByte = SnapData.friendsByteList.get(position);
				System.out.println(SnapData.friendsStorys.get(position).getId());
				Intent intnr = new Intent(getApplicationContext(), BigView.class);
				intnr.putExtra("sender",getIntent().getStringExtra("sender"));
				intnr.putExtra("snapid", SnapData.friendsStorys.get(position).getId());
				startActivity(intnr);
			}
		});
		friendProgressBar= (ProgressBar)findViewById(R.id.friendsProgressBar);
		friendProgressBar.setVisibility(ProgressBar.VISIBLE);
	}
	@Override
    protected void onStart() {
        super.onStart();
      //Get an Analytics tracker to report app starts & uncaught exceptions etc.
    	GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    /* (non-Javadoc)
    * @see android.app.Activity#onStop()
    */
    @Override
    protected void onStop() {
        super.onStop();
      //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }
	public void goToFriendsList(View v){
		Intent i=new Intent(this,FriendsList.class);
		startActivity(i);
	}
	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount() {
			return SnapData.friendsByteList.size();
		}

		@Override
		public Object getItem(int i) {
			return SnapData.friendsByteList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return (long) (SnapData.friendsByteList.get(i).hashCode());
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			try {
				View v = view;
				ImageView picture;
				TextView name;
				
				if (v == null) {
					v = inflater.inflate(R.layout.gridview_item, viewGroup,
							false);
					v.setTag(R.id.picture, v.findViewById(R.id.picture));
					v.setTag(R.id.text, v.findViewById(R.id.text));
				}
				picture = (ImageView) v.getTag(R.id.picture);
				name = (TextView)v.getTag(R.id.text);
				name.setText(SnapData.friendsStorys.get(i).getSender());
				
				byte[] storyBytes = SnapData.friendsByteList.get(i);
				BitmapFactory.Options options = new BitmapFactory.Options();// Create
																			// object
																			// of
																			// bitmapfactory's
																			// option
																			// method
																			// for
																			// further
																			// option
																			// use
				options.inPurgeable = true; // inPurgeable is used to free up
											// memory while required
				Bitmap bm = BitmapFactory.decodeByteArray(storyBytes, 0,
						storyBytes.length, options);// Decode image, "thumbnail"
													// is
				DisplayMetrics dimension = new DisplayMetrics();
		        getWindowManager().getDefaultDisplay().getMetrics(dimension);
		        int width = dimension.widthPixels;
				
				// the object of image file
				Bitmap bm2 = Bitmap.createScaledBitmap(bm, width/2, 3*width/4, true);// convert
																			// decoded
																			// bitmap
																			// into
																			// well
																			// scalled
																			// Bitmap
																			// format.

				picture.setImageBitmap(bm2);
				System.out.println("Adding immage");

				return v;
			} catch (Exception e) {
				return null;
			}

		}
	}
	
	
	
	//-------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------
	//--------friendsStorys-----------------------------------------------------------------------
	//-------------------------------------------------------------------------------

	private class LoadStories extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... index) {
			
			SnapData.friendsStorys=new ArrayList<Story>();
			SnapData.friendsByteList=new ArrayList<byte[]>();
			// get storys

			for(Story s:SnapData.myStorys){
				if(s.getSender().contains(SnapData.currentFriend.getUsername())){
					SnapData.friendsByteList.add(Snapchat.getStory(s, getIntent().getStringExtra("sender"),
							SnapData.authTokenSaved));
					SnapData.friendsStorys.add(s);
				}
				
			}
return null;
		}

		@Override
		public void onProgressUpdate(Integer... args) {
		}

		@Override
		protected void onPostExecute(String result) {
			friendProgressBar.setVisibility(ProgressBar.GONE);
			adapter = new MyAdapter(getApplicationContext());
			gridView.setAdapter(adapter);
		}
	}

	
	public void back(View v){
		finish();
	}
}