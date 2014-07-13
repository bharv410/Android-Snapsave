package com.kidgeniusdesigns.snapapp.helpers;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.habosa.javasnap.Friend;
import com.habosa.javasnap.Snap;
import com.habosa.javasnap.Story;

public class SnapData {
	public static List<Friend> myFriends;
	public static List<String> myFriendsNames;
	public static List<Story> myStorys, friendsStorys, videoStorys,videoStorysWithoutCaptions;
	public static String authTokenSaved;
	public static List<byte[]> byteList,friendsByteList, videoByteList, unreadSnapBytes;
	public static byte[] currentByte;
	public static Friend currentFriend;
	
	public static List<Snap> yourUnreadSnaps;
	
	public static File sendToFriendFile;
	public SnapData(Context context) {
	}

}
