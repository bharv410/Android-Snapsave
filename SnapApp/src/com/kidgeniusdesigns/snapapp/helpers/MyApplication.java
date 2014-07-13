package com.kidgeniusdesigns.snapapp.helpers;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.kidgeniusdesigns.snapapp.R;
import com.parse.Parse;

import java.util.HashMap;
 
public class MyApplication extends Application {
 
// The following line should be changed to include the correct property id.
private static final String PROPERTY_ID = "UA-52625155-1";
 
//Logging TAG
private static final String TAG = "InstaSnap";
 
public static int GENERAL_TRACKER = 0;
 
public enum TrackerName {
APP_TRACKER, // Tracker used only in this app.
GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
}
 
HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
 
public MyApplication() {
super();
}
public void onCreate() {
	  Parse.initialize(this, "kbIzB7KDmXTI2gPX80PIfQ6NKOfiwtsq9PT89aeD", "q0nwAxHRGcJW7oXiWz86fW8SJFnCQoG3Za0ECsbp");
	}
public synchronized Tracker getTracker(TrackerName trackerId) {
if (!mTrackers.containsKey(trackerId)) {
 
GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
Tracker t = (Tracker) ((trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
: (trackerId == TrackerName.GLOBAL_TRACKER));
mTrackers.put(trackerId, t);
 
}
return mTrackers.get(trackerId);
}
}
