package com.kidgeniusdesigns.snapapp.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.kidgeniusdesigns.snapapp.R;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;

public class MainActivity extends Activity {
	Button loginButton;
	EditText username, password;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

		username=(EditText)findViewById(R.id.usernameBox);
		password=(EditText)findViewById(R.id.passwordBox);
		loginButton=(Button)findViewById(R.id.loginButton);
		username.setHint("username");
		getNameAndPw();
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
	public void logIn(View v){
		
		saveToFile(username.getText().toString(),password.getText().toString());
		Intent i= new Intent(this, LoginActivity.class);
		i.putExtra("username", username.getText().toString());
		i.putExtra("password", password.getText().toString());
		startActivity(i);
	}
	
public void saveToFile(String un, String pw) {
		try {
			FileWriter out = new FileWriter(new File(getApplicationContext()
					.getFilesDir(), "username.txt"));
			out.write(un);
			out.close();
			
			out = new FileWriter(new File(getApplicationContext()
					.getFilesDir(), "password.txt"));
			out.write(pw);
			out.close();
			
		} catch (IOException e) {
			System.out.print(e);
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
			username.setText(line);
		}
		in.close();

		in = new BufferedReader(new FileReader(new File(
				getApplicationContext().getFilesDir(), "password.txt")));
		while ((line = in.readLine()) != null) {
			System.out.println(line);
			password.setText(line);
		}
	} catch (FileNotFoundException e) {
		System.out.println(e);
	} catch (IOException e) {
		System.out.println(e);
	}
	if(username.getText().toString()!=""){
		loginButton.performClick();
	}
	
}
public void emailBen(View v){
	String to = "contactinstasnap@gmail.com";
	String subject = "InstaSnap issues";
	String message ="Hey, I am having problems with ";

	Intent email = new Intent(Intent.ACTION_SEND);
	email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
	email.putExtra(Intent.EXTRA_SUBJECT, subject);
	email.putExtra(Intent.EXTRA_TEXT, message);

	// need this to prompts email client only
	email.setType("message/rfc822");

	startActivity(Intent.createChooser(email, "Choose an Email client"));

}

public void checkSnapChat(View v){
	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://downdetector.com/status/snapchat"));
	startActivity(browserIntent);
	
}
}