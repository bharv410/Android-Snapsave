package com.kidgeniusdesigns.snapapp;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;

public class FeedActivity extends Activity implements OnScrollListener {
	MyAdapter adapter;
	GridView gridView;
	Boolean finishedLoadingNextSnaps;
	int nextSnapIndex, numOfSnapsOnScreen;
	String un;
	boolean sentOrNah, adapterNotSetYet;

	ProgressBar feedProgressBar;
	
	private InterstitialAd interstitial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		((MyApplication) getApplication())
				.getTracker(MyApplication.TrackerName.APP_TRACKER);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		
		
		un = getIntent().getStringExtra("username");
		adapterNotSetYet = true;
		finishedLoadingNextSnaps = false;
		
		SnapData.byteList = new ArrayList<byte[]>();
		SnapData.videoByteList = new ArrayList<byte[]>();
		gridView = (GridView) findViewById(R.id.gridview);
		
		feedProgressBar = (ProgressBar)findViewById(R.id.feedProgressBar);
		feedProgressBar.setVisibility(ProgressBar.INVISIBLE);
		
		nextSnapIndex = 0;
		numOfSnapsOnScreen = 0;
		
		
		loadMore();
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				SnapData.currentByte = SnapData.byteList.get(position);
				Intent i = new Intent(getApplicationContext(),
						BigView.class);
				i.putExtra("sender", SnapData.myStorys.get(position)
						.getSender());
				i.putExtra("snapid", SnapData.myStorys.get(position)
						.getId());
				startActivity(i);
			}
		});

		gridView.setOnScrollListener(this);

		// set up ads
		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId("ca-app-pub-4742368221536941/6949454117");
		AdRequest adRequest = new AdRequest.Builder().build();
		interstitial.loadAd(adRequest);
	}

	public void displayInterstitial() {
			if (interstitial.isLoaded()) {
				interstitial.show();
			}
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

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return SnapData.byteList.size();
		}
		@Override
		public Object getItem(int i) {
			return SnapData.byteList.get(i);
		}
		@Override
		public long getItemId(int i) {
			return (long) (SnapData.byteList.get(i).hashCode());
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
				name = (TextView) v.getTag(R.id.text);
				name.setText(SnapData.myStorys.get(i).getSender());

				byte[] storyBytes = SnapData.byteList.get(i);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPurgeable = true; // inPurgeable is used to free up
											// memory while required
				Bitmap bm = BitmapFactory.decodeByteArray(storyBytes, 0,
						storyBytes.length, options);

				DisplayMetrics dimension = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dimension);
				int width = dimension.widthPixels;

				Bitmap bm2 = Bitmap.createScaledBitmap(bm, width / 2,
						3 * width / 4, true);
				picture.setImageBitmap(bm2);
				System.out.println("Adding immage");
				// when view is set increment num of images on screen
				numOfSnapsOnScreen++;
				
				if((numOfSnapsOnScreen==48)){
					displayInterstitial();
				}
				return v;
			} catch (Exception e) {
				e.printStackTrace();
				return view;
			}
		}
	}


	private class LoadStories extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... index) {
			finishedLoadingNextSnaps = false;
			
			int numLoading = 0;
				while (numLoading < 2) {
					if(nextSnapIndex<SnapData.myStorys.size()){
					Story s = SnapData.myStorys.get(nextSnapIndex);
					nextSnapIndex++;
					
					if (s != null) {
						if (!SnapData.byteList.contains(Snapchat.getStory(s,
								un, SnapData.authTokenSaved))) {
							SnapData.byteList.add(Snapchat.getStory(s, un,
									SnapData.authTokenSaved));
							numLoading++;
						}
					}
				}
				}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			feedProgressBar.setVisibility(ProgressBar.GONE);
			
			if (adapterNotSetYet) {
				adapter = new MyAdapter(getApplicationContext());
				gridView.setAdapter(adapter);
				adapterNotSetYet = false;
			} else {
				adapter.notifyDataSetChanged();
				gridView.invalidateViews();
			}
			finishedLoadingNextSnaps = true;
		}
	}

	private void loadMore() {
		feedProgressBar.setVisibility(ProgressBar.VISIBLE);
			LoadStories loadMore = new LoadStories();
			loadMore.execute();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount >= (totalItemCount - 8)) {
			// end has been reached. load more images
			if (finishedLoadingNextSnaps) {
				loadMore();
				System.out.println();
			}	
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	public void goToTop(View v) {
		gridView.smoothScrollToPosition(0);
	}
	public void goToFriendsList(View v) {
		Intent intent = new Intent(this, FriendsList.class);
		startActivity(intent);
		
	}
	public void uploadSomething(View v) {
		Intent i = new Intent(this, UploadSnapActivity.class);
		i.putExtra("username", getIntent().getStringExtra("username"));
		startActivity(i);
		
	}
	public void viewVids(View v) {	
		Intent i = new Intent(this, ViewVideosActivity.class);
		i.putExtra("username", getIntent().getStringExtra("username"));
		startActivity(i);
		
	}
	
	@Override  
	public void onBackPressed() {
	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 			alertDialogBuilder.setTitle("Logout?");
 			alertDialogBuilder
				.setMessage("Do you really wanna logout?")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						FeedActivity.super.onBackPressed();
				finish();
					}
				  })
				.setNegativeButton("Nope",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
 				AlertDialog alertDialog = alertDialogBuilder.create();
 
				try{
				alertDialog.show();
				}catch(Exception e){
				}
	}
	
		
	public void goToSnaps(View v){
		Intent i = new Intent(this, MySnapsActivity.class);
		i.putExtra("username", getIntent().getStringExtra("username"));
		startActivity(i);
	}
}