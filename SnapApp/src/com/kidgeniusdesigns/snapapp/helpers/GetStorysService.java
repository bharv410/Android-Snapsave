package com.kidgeniusdesigns.snapapp.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class GetStorysService extends IntentService {
	String un, pw, authTokenSaved;
	List<Story> myStorys;

	
	public GetStorysService(){
		super("GetStorysService");
	}
	 @Override
	  protected void onHandleIntent(Intent intent) {
		 getNameAndPw();
			System.out.println(un);
			System.out.println(pw);
			Login lg = new Login();
			lg.execute();

	  }
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		getNameAndPw();
//		System.out.println(un);
//		System.out.println(pw);
//		Login lg = new Login();
//		lg.execute();
//		return Service.START_FLAG_REDELIVERY;
//	}

//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}

	private class Login extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... index) {
			JSONObject loginObj = Snapchat.login(un, pw);

			if (loginObj != null) {
				try {
					authTokenSaved = loginObj
							.getString(Snapchat.AUTH_TOKEN_KEY);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Story[] notdownloadable = Snapchat.getStories(un,authTokenSaved);
				myStorys = new ArrayList<Story>();
				for (Story s : Story.filterDownloadable(notdownloadable)) {

					// pics go in mystorys
					if (s.isImage())
						myStorys.add(s);
				}

				for (Story s : myStorys) {
						byte[] storyBytes=Snapchat.getStory(s, un, authTokenSaved);
						System.out.println("Got snap from: "+s.getSender());
						// save all stories to parse.com for later viewing
						ParseFile bigPic = new ParseFile("photo.jpg",storyBytes);
						bigPic.saveInBackground();
				        ParseObject imgupload = new ParseObject(un);
				        imgupload.put("Sender", s.getSender());
				        imgupload.put("Image", bigPic);
				        imgupload.put("Caption", s.getCaption());
				        imgupload.put("SnapId", s.getId());
				        imgupload.saveInBackground();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}
	}
	public void getNameAndPw(){
		String line;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(new File(
					getApplicationContext().getFilesDir(), "username.txt")));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				un=line;
			}
			in.close();

			in = new BufferedReader(new FileReader(new File(
					getApplicationContext().getFilesDir(), "password.txt")));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				pw=line;
			}
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
