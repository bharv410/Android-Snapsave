package com.kidgeniusdesigns.snapapp;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.habosa.javasnap.Snapchat;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;
import com.kidgeniusdesigns.snapapp.login.Friend;

public class SendToFriendsActivity extends Activity implements OnSeekBarChangeListener{
Boolean sentOrNah;
List<String> recipients;
int viewTime;
Boolean video;
ArrayList<Friend> countryList;
MyCustomAdapter dataAdapter = null;


private SeekBar seekbar; 
private TextView textProgress;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		
		setContentView(R.layout.activity_send_to_friends);
		
		seekbar = (SeekBar)findViewById(R.id.seekBar1); // make seekbar object
		seekbar.setOnSeekBarChangeListener(this);
		textProgress = (TextView)findViewById(R.id.time);
		
		recipients = new ArrayList<String>();
		video=getIntent().getBooleanExtra("false", false);
		viewTime=10;
		
		countryList = new ArrayList<Friend>();
		 for(com.habosa.javasnap.Friend f:SnapData.myFriends){
			  Friend country = new Friend("",f.getUsername(),false);
			  countryList.add(country);
		  }
		 
		//create an ArrayAdaptar from the String Array
		  dataAdapter = new MyCustomAdapter(this,
		    R.layout.checkbox_listitem, countryList);
		  ListView listView = (ListView) findViewById(R.id.friendsLV);
		  // Assign adapter to ListView
		  listView.setAdapter(dataAdapter);
		 
		 
		  
		 
		  
		  listView.setOnItemClickListener(new OnItemClickListener() {
		   public void onItemClick(AdapterView<?> parent, View view,
		     int position, long id) {
		    Toast.makeText(getApplicationContext(),
		      "Click the checkbox", 
		      Toast.LENGTH_LONG).show();
		   }
		  });
		
	}
	@Override
    public void onProgressChanged(SeekBar seekBar, int progress,
    		boolean fromUser) {
    	textProgress.setText("Send for "+progress+" secs");
    	viewTime=progress;
    }
	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    	
    }
	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    	seekBar.setSecondaryProgress(seekBar.getProgress()); // set the shade of the previous value.
    }
	private class SendSnap extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				String mediaId=Snapchat.upload(SnapData.sendToFriendFile, getIntent().getStringExtra("username"), SnapData.authTokenSaved, video);

				sentOrNah=Snapchat.send(mediaId, recipients,false,viewTime,getIntent().getStringExtra("username"), SnapData.authTokenSaved);
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			if (sentOrNah){
				Toast tst= 
				Toast.makeText(getApplicationContext(),
						"Snap was sent", Toast.LENGTH_SHORT);
				tst.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
						tst.show();
						finish();
			}else{
				Toast tst=Toast.makeText(getApplicationContext(),
						"Error occured please try again later", Toast.LENGTH_SHORT);
				tst.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
				tst.show();
			}
		}
	}
	
	private class MyCustomAdapter extends ArrayAdapter<Friend> {
		 
		  private ArrayList<Friend> countryList;
		 
		  public MyCustomAdapter(Context context, int textViewResourceId, 
		    ArrayList<Friend> countryList) {
		   super(context, textViewResourceId, countryList);
		   this.countryList = new ArrayList<Friend>();
		   this.countryList.addAll(countryList);
		  }
		 
		  private class ViewHolder {
		   TextView code;
		   CheckBox name;
		  }
		 
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		 
		   ViewHolder holder = null;
		   Log.v("ConvertView", String.valueOf(position));
		 
		   if (convertView == null) {
		   LayoutInflater vi = (LayoutInflater)getSystemService(
		     Context.LAYOUT_INFLATER_SERVICE);
		   convertView = vi.inflate(R.layout.checkbox_listitem, null);
		 
		   holder = new ViewHolder();
		   holder.code = (TextView) convertView.findViewById(R.id.code);
		   holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
		   convertView.setTag(holder);
		 
		    holder.name.setOnClickListener( new View.OnClickListener() {  
		     public void onClick(View v) {  
		      CheckBox cb = (CheckBox) v ;  
		      Friend country = (Friend) cb.getTag();  
		      country.setSelected(cb.isChecked());
		     }  
		    });  
		   } 
		   else {
		    holder = (ViewHolder) convertView.getTag();
		   }
		 
		   Friend country = countryList.get(position);
		   holder.code.setText("" +  country.getCode() + "");
		   holder.name.setText(country.getName());
		   holder.name.setChecked(country.isSelected());
		   holder.name.setTag(country);
		 
		   return convertView;
		 
		  }
		 
		 }
		 
		 public void checkButtonClick(View v) {
			   Toast tst= 
						Toast.makeText(getApplicationContext(),
								"Sending snap. Please wait...", Toast.LENGTH_SHORT);
						tst.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
								tst.show();
		 
		    ArrayList<Friend> countryList = dataAdapter.countryList;
		    for(int i=0;i<countryList.size();i++){
		     Friend country = countryList.get(i);
		     if(country.isSelected()){
		      recipients.add(country.getName());
		     }
		    }
		    SendSnap ss= new SendSnap();
			ss.execute();		 
		 }
}