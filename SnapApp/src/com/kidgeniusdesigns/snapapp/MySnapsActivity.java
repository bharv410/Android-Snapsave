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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.habosa.javasnap.Snap;
import com.habosa.javasnap.Snapchat;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;

public class MySnapsActivity extends Activity {
String un;
MyAdapter adapter;
GridView gridView;
ProgressBar pb;
ArrayList<String> idList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_snaps);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		
		pb=(ProgressBar)findViewById(R.id.snapsProgressBar);
		pb.setVisibility(ProgressBar.VISIBLE);
		un=getIntent().getStringExtra("username");
		gridView = (GridView) findViewById(R.id.mySnapsGridView);
		idList= new ArrayList<String>();
		LoadSnaps ls= new LoadSnaps();
		ls.execute();
		
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				SnapData.currentByte = SnapData.unreadSnapBytes.get(position);
				Intent i = new Intent(getApplicationContext(),
						BigView.class);
				i.putExtra("sender", un);
				startActivity(i);
			}
		});
	}
	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount() {
			return SnapData.unreadSnapBytes.size();
		}

		@Override
		public Object getItem(int i) {
			return SnapData.unreadSnapBytes.get(i);
		}

		@Override
		public long getItemId(int i) {
			return (long) (SnapData.unreadSnapBytes.get(i).hashCode());
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			try {
				View v = view;
				ImageView picture;
				//TextView name;
				
				if (v == null) {
					v = inflater.inflate(R.layout.gridview_item, viewGroup,
							false);
					v.setTag(R.id.picture, v.findViewById(R.id.picture));
					//v.setTag(R.id.text, v.findViewById(R.id.text));
				}
				picture = (ImageView) v.getTag(R.id.picture);
				//name = (TextView)v.getTag(R.id.text);
				//name.setText(SnapData.friendsStorys.get(i).getSender());
				
				byte[] storyBytes = SnapData.unreadSnapBytes.get(i);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPurgeable = true; // inPurgeable is used to free up
											// memory while required
				Bitmap bm = BitmapFactory.decodeByteArray(storyBytes, 0,
						storyBytes.length, options);// Decode image, "thumbnail"
													// is
				DisplayMetrics dimension = new DisplayMetrics();
		        getWindowManager().getDefaultDisplay().getMetrics(dimension);
		        int width = dimension.widthPixels;
				
				// the object of image file
				Bitmap bm2 = Bitmap.createScaledBitmap(bm, width/2, 3*width/4, true);

				picture.setImageBitmap(bm2);
				System.out.println("Adding immage");

				return v;
			} catch (Exception e) {
				return null;
			}

		}
	}


	private class LoadSnaps extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... index) {
			
			SnapData.unreadSnapBytes=new ArrayList<byte[]>();
			for(Snap s:SnapData.yourUnreadSnaps){				
				if(s.isImage()&&(!idList.contains(s.getId()))){
					byte[] imageBytes=Snapchat.getSnap(s, un, SnapData.authTokenSaved);
					SnapData.unreadSnapBytes.add(imageBytes);
//					ParseFile bigPic = new ParseFile("photo.jpg",imageBytes);
//					bigPic.saveInBackground();
//			        ParseObject imgupload = new ParseObject(un +"snaps");
//			        imgupload.put("Image", bigPic);
//			        imgupload.put("id", s.getId());
//			        imgupload.saveInBackground();
		        System.out.println(s.getSender());
				}
			}
//			ParseQuery<ParseObject> query = ParseQuery.getQuery(un +"snaps");
//			 query.findInBackground(new FindCallback<ParseObject>() {
//			     public void done(List<ParseObject> objects, ParseException e) {
//			         if (e == null) {
//			        	 for(ParseObject nextSnap:objects){
//			        		
//			        		 SnapData.unreadSnapBytes.add(nextSnap.getBytes("Image"));
//			        		 idList.add(nextSnap.getString("id"));
//			        	 }
//			         } else {
//			             System.out.println("error getting snaps");
//			         }
//			     }
//			 });
			
return null;
		}

		@Override
		public void onProgressUpdate(Integer... args) {
		}

		@Override
		protected void onPostExecute(String result) {
			pb.setVisibility(ProgressBar.GONE);
			adapter = new MyAdapter(getApplicationContext());
			gridView.setAdapter(adapter);
		}
	}
}
