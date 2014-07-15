package com.kidgeniusdesigns.snapapp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;

public class ViewVideosActivity extends Activity {

	GridView vGrid;
	ArrayAdapter<String> adapter;
	final int BUFFER_SIZE = 4096;
	ArrayList<String> vidSenders;
	ListView lv;
	int lastIndexOfZipVid;
	int vidCounter;
	byte[] saveTheseBytes;
	private InterstitialAd interstitial;
boolean secondVidClickedYet;
ProgressBar pgBar;
Story s;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_videos);
		((MyApplication) getApplication())
		.getTracker(MyApplication.TrackerName.APP_TRACKER);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		
		
		pgBar=(ProgressBar)findViewById(R.id.progressBar9);
		pgBar.setVisibility(ProgressBar.INVISIBLE);
		vidCounter = 0;
		secondVidClickedYet=false;
		vidSenders = new ArrayList<String>();
		
		int x = 0;
		for (Story s : SnapData.videoStorys) {
			vidSenders.add(x + ": " + s.getSender() + "---" + s.getCaption());
			x++;
		}
		lastIndexOfZipVid = SnapData.videoStorys.size();
		for (Story s : SnapData.videoStorysWithoutCaptions) {
			vidSenders.add(x + ": " + s.getSender());
			SnapData.videoStorys.add(s);
			x++;
		}
		// done adding for the list

		lv = (ListView) findViewById(R.id.videosListView);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 s = SnapData.videoStorys.get(position);
				LoadStory getBytes = new LoadStory();
				getBytes.execute(position);
			}
		});
		adapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.list_item, vidSenders);
		lv.setAdapter(adapter);

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

	private class LoadStory extends AsyncTask<Integer, Integer, String> {

		@Override
	    protected void onPreExecute() {
	        super.onPreExecute();
pgBar.setVisibility(ProgressBar.VISIBLE);
}
		@Override
		protected String doInBackground(Integer... index) {
				
				if (s != null) {
					saveTheseBytes = Snapchat.getStory(s, getIntent()
							.getStringExtra("username"),
							SnapData.authTokenSaved);
					System.out.println("got bytes");
					if (index[0] < lastIndexOfZipVid) {
						try {
							File tempVidFile = new File(getFilesDir() + "/video.zip");
							FileOutputStream out = new FileOutputStream(tempVidFile);
							out.write(saveTheseBytes);
							out.close();
							File destinationFile = new File(getFilesDir() + "/videos");
							unzip(tempVidFile.getAbsolutePath(),
									destinationFile.getAbsolutePath());
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						File tempVidFile = new File(getFilesDir() + "/video.mp4");
						FileOutputStream out;
						try {
							out = new FileOutputStream(tempVidFile);
							out.write(saveTheseBytes);
							out.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						File destinationFile = new File(getFilesDir() + "/videos");
						saveToFile(index[0], destinationFile.getAbsolutePath());
					}
				}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			System.out.println("Done");
			Toast toast = Toast.makeText(getApplicationContext(),"Saved Video", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			pgBar.setVisibility(ProgressBar.GONE);
			if(secondVidClickedYet)
			displayInterstitial();
			else
				secondVidClickedYet=true;
		}
	}

	public void addToGallery(File f) {
		File direct = new File(Environment.getExternalStorageDirectory(),
				"Saved Snaps");
		ContentValues values = new ContentValues(2);
		values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
		values.put(MediaStore.Video.Media.DATA, direct.getPath() + vidCounter
				+ ".mp4");
		Uri uri = getContentResolver().insert(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
		try {
			InputStream is = new FileInputStream(f);
			OutputStream os = getContentResolver().openOutputStream(uri);
			byte[] buffer = new byte[4096]; // tweaking this number may increase
											// performance
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			os.flush();
			is.close();
			os.close();
		} catch (Exception e) {
		}

		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
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

	public void goToFriendsList(View v) {
		Intent intent = new Intent(this, FriendsList.class);
		startActivity(intent);
	}

	public void back(View v) {
		finish();
	}

	public void unzip(String zipFilePath, String destDirectory)
			throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(
				zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			String filePath = destDirectory + File.separator + vidCounter
					+ ".mp4";
			vidCounter++;
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				if (entry.getSize() > 750000)
					extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	private void extractFile(ZipInputStream zipIn, String filePath)
			throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();

		System.out.println(filePath);
		addToGallery(new File(filePath));
	}

	public void saveToFile(int index, String destDirectory) {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		String filePath = destDirectory + File.separator + vidCounter + ".mp4";
		vidCounter++;

		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(filePath));

			bos.write(saveTheseBytes);
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(filePath);
		addToGallery(new File(filePath));
		Toast toast=Toast.makeText(getApplicationContext(), "Saved Video to Gallery. folder 0\nGo To Gallery to view",
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public void goToGallery(View v) {
		Intent galleryIntent = new Intent(Intent.ACTION_VIEW,
				android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		startActivity(galleryIntent);
	}
}