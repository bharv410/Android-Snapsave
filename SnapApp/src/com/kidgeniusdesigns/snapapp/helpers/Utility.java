package com.kidgeniusdesigns.snapapp.helpers;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class Utility {
 // convert from bitmap to byte array
 public static byte[] getBytes(Bitmap bitmap) {
  ByteArrayOutputStream stream = new ByteArrayOutputStream();
  bitmap.compress(CompressFormat.PNG, 0, stream);
  return stream.toByteArray();
 }

  // convert from byte array to bitmap
 public static Bitmap getPhoto(byte[] image) {
  return BitmapFactory.decodeByteArray(image, 0, image.length);
 }
 
 /** 
  * Read bytes from a File into a byte[].
  * 
  * @param file The File to read.
  * @return A byte[] containing the contents of the File.
  * @throws IOException Thrown if the File is too long to read or couldn't be
  * read fully.
  */
 public static byte[] readBytesFromFile(File file) throws IOException {
   InputStream is = new FileInputStream(file);
   
   // Get the size of the file
   long length = file.length();

   // You cannot create an array using a long type.
   // It needs to be an int type.
   // Before converting to an int type, check
   // to ensure that file is not larger than Integer.MAX_VALUE.
   if (length > Integer.MAX_VALUE) {
     throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
   }

   // Create the byte array to hold the data
   byte[] bytes = new byte[(int)length];

   // Read in the bytes
   int offset = 0;
   int numRead = 0;
   while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
       offset += numRead;
   }

   // Ensure all the bytes have been read in
   if (offset < bytes.length) {
       throw new IOException("Could not completely read file " + file.getName());
   }

   // Close the input stream and return bytes
   is.close();
   return bytes;
}
 
 /**
  * Writes the specified byte[] to the specified File path.
  * 
  * @param theFile File Object representing the path to write to.
  * @param bytes The byte[] of data to write to the File.
  * @throws IOException Thrown if there is problem creating or writing the 
  * File.
  */
 public static void writeBytesToFile(File theFile, byte[] bytes) throws IOException {
   BufferedOutputStream bos = null;
   
 try {
   FileOutputStream fos = new FileOutputStream(theFile);
   bos = new BufferedOutputStream(fos); 
   bos.write(bytes);
 }finally {
   if(bos != null) {
     try  {
       //flush and close the BufferedOutputStream
       bos.flush();
       bos.close();
     } catch(Exception e){}
   }
 }
 }
 
 public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}