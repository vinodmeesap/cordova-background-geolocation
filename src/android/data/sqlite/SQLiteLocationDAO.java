package com.transistorsoft.cordova.bggeo.data.sqlite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.transistorsoft.cordova.bggeo.data.LocationDAO;

public class SQLiteLocationDAO implements LocationDAO {
	private static final String TAG = "SQLiteLocationDAO";
	private Context context;
	
	public SQLiteLocationDAO(Context context) {
		this.context = context;
	}
	
	/*
	public JSONObject[] getAllLocations() {
		SQLiteDatabase db = null;
		Cursor c = null;
		List<JSONObject> all = new ArrayList<JSONObject>();
		try {
			db = new LocationOpenHelper(context).getReadableDatabase();
			c = db.query(LocationOpenHelper.LOCATION_TABLE_NAME, null);
			while (c.moveToNext()) {
				all.add(hydrate(c));
			}
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return all.toArray(new JSONObject[all.size()]);
	}

	public boolean persistLocation(JSONObject location) {
		SQLiteDatabase db = new LocationOpenHelper(context).getWritableDatabase();
		db.beginTransaction();
		ContentValues values = getContentValues(location);
		long rowId = db.insert(LocationOpenHelper.LOCATION_TABLE_NAME, null, values);
		Log.d(TAG, "After insert, rowId = " + rowId);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		if (rowId > -1) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteLocation(JSONObject location) {
		SQLiteDatabase db = new LocationOpenHelper(context).getWritableDatabase();
		db.beginTransaction();
		db.delete(LocationOpenHelper.LOCATION_TABLE_NAME, "timestamp = ?", location.getString("timestamp"));
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	
	private JSONObject hydrate(Cursor c) {
		JSONObject l = new JSONObject(c);
		return l;
	}
	
	private ContentValues getContentValues(JSONObject location) {
		ContentValues values = new ContentValues();
		values.put("latitude", location.get("latitude"));
		values.put("longitude", location.get("longitude"));
		values.put("timestamp", location.get("timestamp"));
		values.put("accuracy",  location.get("accuracy"));
		values.put("altitude", location.get("altitude"));
		values.put("bearing", location.get("bearing"));
		values.put("speed", location.get("speed"));
		return values;
	}
	*/
}