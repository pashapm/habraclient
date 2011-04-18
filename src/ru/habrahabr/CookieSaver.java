package ru.habrahabr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CookieSaver extends SQLiteOpenHelper
{
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "cookies";

	public static final String TABLE_NAME = "cookies";

	public CookieSaver(Context context) {
		super(context, DB_NAME, null,DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (`name` VARCHAR(64) NOT NULL, `value` VARCHAR(128) NOT NULL, " +
				"`domain` VARCHAR(128) NOT NULL, `path` VARCHAR(256) NOT NULL, `date` INT NOT NULL, UNIQUE (`name`));");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) 
	{
		
	}
	
	public Cookie[] getCookies()
	{
		List<Cookie> cookies = new ArrayList<Cookie>();
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.rawQuery("SELECT name,value,domain,path,date FROM " + TABLE_NAME + ";", null);
		if(c == null) return null;
		if(!c.moveToFirst()) return null;
		
		Log.d("CookieSaver", "do ... while");
		
		do 
		{
			BasicClientCookie cookie = new BasicClientCookie(c.getString(0), c.getString(1));
			cookie.setExpiryDate(new Date(c.getInt(4) * 1000));
			cookie.setDomain(c.getString(2));
			cookie.setPath(c.getString(3));
			
			cookies.add(cookie);
		} 
		while (c.moveToNext());

		Log.d("CookieSaver", "return cookies");
		
		return cookies.toArray(new Cookie[0]);
	}
	
	public void putCookie(Cookie cook)
	{	
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES ('" + cook.getName() + "',  '" + cook.getValue() + 
				"',  '" + cook.getDomain() + "',  '" + cook.getPath() + "',  '" + 
				//cook.getExpiryDate().getYear() + "-" + cook.getExpiryDate().getMonth() + "-" + +cook.getExpiryDate().getDate() + " " +
				//cook.getExpiryDate().getHours() + ":" + cook.getExpiryDate().getMinutes() + ":" + cook.getExpiryDate().getSeconds() + "') " +
				(int)(cook.getExpiryDate().getTime() / 1000) + "') " +
				"ON DUPLICATE KEY UPDATE value='" + cook.getValue() + "', domain='" + cook.getDomain() + "', path='" + cook.getPath() + "', date='" + 
				//cook.getExpiryDate().getYear() + "-" + cook.getExpiryDate().getMonth() + "-" + +cook.getExpiryDate().getDate() + " " +
				//cook.getExpiryDate().getHours() + ":" + cook.getExpiryDate().getMinutes() + ":" + cook.getExpiryDate().getSeconds() + 
				(int)(cook.getExpiryDate().getTime() / 1000) + "';");
	}
	
	public void putCookies(Cookie[] cooks)
	{
		for(int i = 0; i < cooks.length; i++)
			putCookie(cooks[i]);
	}
	
	public void clearCookies()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME + ";");
	}
}