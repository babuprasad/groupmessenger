package edu.buffalo.cse.cse486586.groupmessenger;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that I used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
    	Log.v("GroupMessenger","insert Content Provider - start");
    	Log.v("GroupMessenger", "Content Values" + values.toString());
        
    	
        try {
			// Declaring SharedPreference as Data Storage - Babu
			SharedPreferences sharedPreference = getContext().getSharedPreferences("GroupMessengerSP", Context.MODE_PRIVATE);
			String keyToInsert = values.getAsString("key");
			String valueToInsert = values.getAsString("value");
			
			/*
			 * As we are using shared preference which replace the existing value, 
			 * we check for key existence before insert operation - Babu 
			 */
			if(sharedPreference.getString(keyToInsert, "null").compareTo("null")==0)
			{
				Editor editor = sharedPreference.edit();
				editor.putString(keyToInsert, valueToInsert);
				editor.commit();
			}
			else
			{
				//throw new InvalidKeyException("Duplicate Key");
				return null;
			}
		} catch (Exception e) {
			Log.e("GroupMessenger", "Exception occured. ");
			e.printStackTrace();
		}
        
        
        Log.v("GroupMessenger","insert Content Provider - end");
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
    	
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         * 
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         * 
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
    	
    	
    	try {
			/*
			 * Addded By Babu - Code to query key and values from the storage
			 * from the shared preference used during insert function
			 */
			Log.v("GroupMessenger","query content provider - start");
			Log.v("GroupMessenger","uri :" + uri);
			Log.v("GroupMessenger","selection :" + selection);        
			
			SharedPreferences sharedPreference = getContext().getSharedPreferences("GroupMessengerSP", Context.MODE_PRIVATE);
			MatrixCursor cursor = new MatrixCursor(new String[]{"key","value"});
			String value = sharedPreference.getString(selection, "null");
			
			/*
			 * Build cursor from the data retrieved - Babu
			 */
			if(value.equals("null"))        
				Log.w("GroupMessenger", "Key does not exist");        
			else        
				cursor.addRow(new String[]{selection,value});       
			
			Log.v("GroupMessenger","query content provider - end ");
			return cursor;
		} catch (Exception e) {
			Log.e("GroupMessenger", "Exception occured. ");
			e.printStackTrace();
		}
        
        return null;       
  
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
