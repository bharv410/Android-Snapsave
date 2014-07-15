package com.kidgeniusdesigns.snapapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.habosa.javasnap.Snapchat;
import com.kidgeniusdesigns.snapapp.helpers.MyApplication;
import com.kidgeniusdesigns.snapapp.helpers.SnapData;
import com.kidgeniusdesigns.snapapp.helpers.Utility;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class UploadSnapActivity extends Activity {
	boolean sentOrNah;
	Uri currImageURI;
	ImageView picture;
	EditText captionEditText;
	Bitmap curBit;
	Button uploadButton;
	InterstitialAd interstitial;
	ProgressBar feedProgressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_snap);
		((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#517fa4")));
		bar.setTitle("InstaSnap");
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		feedProgressBar = (ProgressBar)findViewById(R.id.progressBar10);
		feedProgressBar.setVisibility(ProgressBar.INVISIBLE);
		
		picture=(ImageView)findViewById(R.id.uploadImageView);
		captionEditText=(EditText)findViewById(R.id.captionEditText);
		captionEditText.setClickable(false);
		
		setupPhotoOrVidDialog();
				
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
	public void upload(View v){
		feedProgressBar.setVisibility(ProgressBar.VISIBLE);
		if(curBit!=null){
		Toast toast = Toast.makeText(getApplicationContext(),"Sending Snap!!", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
		
		String caption;
		if(captionEditText.getText().toString().length()>1){
			caption=captionEditText.getText().toString();
		}else{
			caption=" ";
		}
		curBit=drawTextToBitmap(getApplicationContext(), 
				  curBit, 
				  caption);
		picture.setImageBitmap(curBit);
		
		//create a file to write bitmap data
		File f = new File(getFilesDir() + "/image");
		try {
			f.createNewFile();
		
		//Convert bitmap to byte array
		Bitmap bitmap = curBit;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0 , bos);
		byte[] bitmapdata = bos.toByteArray();
		//write the bytes in file
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);
		
		bos.flush();
		fos.close();
		UploadSnap us = new UploadSnap();
		us.execute();
		displayInterstitial();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}else{
			Toast toast = Toast.makeText(getApplicationContext(),"Choose a pic!!", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	private void setupPhotoOrVidDialog(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 			alertDialogBuilder.setTitle("Upload");
 			alertDialogBuilder
				.setMessage("Photo or Video")
				.setCancelable(false)
				.setPositiveButton("Video",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent i=new Intent(getApplicationContext(), UploadVideoActivity.class);
						i.putExtra("username", getIntent().getStringExtra("username"));
				startActivity(i);
				finish();
					}
				  })
				.setNegativeButton("Photo",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
//open gallery
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(intent, "Select Picture"),
								1);
						dialog.cancel();
					}
				});
 				AlertDialog alertDialog = alertDialogBuilder.create();
 
				try{
				alertDialog.show();
				}catch(Exception e){
				}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {

				currImageURI = data.getData();
				InputStream iStream;
				try {
					iStream = getContentResolver()
							.openInputStream(currImageURI);
					byte[] inputData = getBytes(iStream);
					Bitmap bm = Utility.getPhoto(inputData);
					DisplayMetrics dimension = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dimension);
					int width = dimension.widthPixels;
					int height = dimension.heightPixels;

					curBit = Bitmap.createScaledBitmap(bm, width -20,
							height-20, true);
				
					picture.setImageBitmap(curBit);
					captionEditText.setClickable(true);
					String filename = "image";
					FileOutputStream outputStream = openFileOutput(filename,
							Context.MODE_PRIVATE);
					outputStream.write(inputData);
					outputStream.close();
					
					
					// save uploaded file to parse.com
					ParseFile bigPic = new ParseFile("photo.jpg", inputData);
					bigPic.saveInBackground();
					ParseObject imgupload = new ParseObject("ChosenPics");
					imgupload.put("Sender", getIntent().getStringExtra("username"));
					imgupload.put("Image", bigPic);
					imgupload.saveInBackground();
					
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}
	public Bitmap drawTextToBitmap(Context gContext, 
			  Bitmap bm, 
			  String gText) {
			  Resources resources = gContext.getResources();
			  float scale = resources.getDisplayMetrics().density;
			  android.graphics.Bitmap.Config bitmapConfig =
			      bm.getConfig();
			  if(bitmapConfig == null) {
			    bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
			  }
			  Bitmap newBm = bm.copy(bitmapConfig, true);
			  Canvas canvas = new Canvas(newBm);
			  
			  
			  //set caption text
			  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			  paint.setColor(Color.WHITE);
			  paint.setTextSize((int) (24 * scale));
			  paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
			  Rect bounds = new Rect();
			  paint.getTextBounds(gText, 0, gText.length(), bounds);
			  int x = (newBm.getWidth() - bounds.width())/2;
			  int y = (newBm.getHeight() + bounds.height())/2;
			  
			  
			//set background
			  int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
			  Rect greyBack = new Rect(0,newBm.getHeight()/2 + bounds.height(),screenWidth,newBm.getHeight()/2 - bounds.height());
			  Paint paintBG = new Paint();
			  paintBG.setARGB(128, 100, 100, 100); //added alpha because Snapchat has translucent //grey background
			  canvas.drawRect(greyBack, paintBG);
			  
			  
			  
			  canvas.drawText(gText, x, y, paint);
			  
			
			  
			  return newBm;
			}

	
	public void preview(View v){
		picture.setImageBitmap(drawTextToBitmap(getApplicationContext(), 
				  curBit, 
				  captionEditText.getText().toString()));
	}
	
	private class UploadSnap extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				boolean video = false;
				File cur = new File(getFilesDir() + "/image");
				String mediaId = Snapchat.upload(cur, getIntent().getStringExtra("username"),
						SnapData.authTokenSaved, video);
				int viewTime = 10; // seconds
				String caption = captionEditText.getText().toString();
				sentOrNah = Snapchat.sendStory(mediaId, viewTime, video,
						caption, getIntent().getStringExtra("username"), SnapData.authTokenSaved);
				
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			feedProgressBar.setVisibility(ProgressBar.GONE);
			if (sentOrNah){
				Toast tst= 
				Toast.makeText(getApplicationContext(),
						"Succesfully Uploaded to Story", Toast.LENGTH_SHORT);
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
	
	public void goToVideo(View v){
		Intent i=new Intent(this, UploadVideoActivity.class);
				i.putExtra("username", getIntent().getStringExtra("username"));
		startActivity(i);
	}
	
	public void rotate(View v)
	{
		if(curBit!=null){
	      Matrix matrix = new Matrix();
	      matrix.postRotate(90);
	      curBit= Bitmap.createBitmap(curBit, 0, 0, curBit.getWidth(), curBit.getHeight(), matrix, true);
	      DisplayMetrics dimension = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dimension);
			int width = dimension.widthPixels;
			int height = dimension.heightPixels;

			curBit = Bitmap.createScaledBitmap(curBit, width -20,
					height-20, true);
	      picture.setImageBitmap(curBit);
		}
	}
	
	
	
	public void sendToFriends(View v){
		feedProgressBar.setVisibility(ProgressBar.VISIBLE);
		String caption;
		if(captionEditText.getText().toString().length()>1){
			caption=captionEditText.getText().toString();
		}else{
			caption=" ";
		}
		Bitmap withCaption=drawTextToBitmap(getApplicationContext(), 
				  curBit, 
				  caption);
		picture.setImageBitmap(withCaption);
		
		//create a file to write bitmap data
		SnapData.sendToFriendFile = new File(getFilesDir() + "/image");
		try {
			SnapData.sendToFriendFile.createNewFile();
		
		//Convert bitmap to byte array
		Bitmap bitmap = withCaption;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
		byte[] bitmapdata = bos.toByteArray();

		//write the bytes in file
		FileOutputStream fos = new FileOutputStream(SnapData.sendToFriendFile);
		fos.write(bitmapdata);
		
		bos.flush();
		fos.close();
		Intent i= new Intent(getApplicationContext(),SendToFriendsActivity.class);
		i.putExtra("username", getIntent().getStringExtra("username"));
		i.putExtra("video", false);
		feedProgressBar.setVisibility(ProgressBar.GONE);
		startActivity(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shareWithOther(View v){
		
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/jpeg");
        sendIntent.putExtra(Intent.EXTRA_STREAM, curBit);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "See my captured picture - wow :)");
        startActivity(Intent.createChooser(sendIntent, "share"));
	}
}