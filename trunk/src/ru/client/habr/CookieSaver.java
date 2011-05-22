package ru.client.habr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author WNeZRoS
 * ��������� ������ � ����
 */
public final class CookieSaver extends SQLiteOpenHelper {
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "cookies";
	private static final String TABLE_NAME = "cookies";
	private static CookieSaver mCookieSaver = null;
	
	/**
	 * @param context
	 */
	public CookieSaver(Context context) {
		super(context, DB_NAME, null,DB_VERSION);
		mCookieSaver = this;
	}

	/**
	 * ������ �������� ���� CookieSaver ����� �� �����������
	 * @return ��������� CookieSaver
	 */
	public static CookieSaver getCookieSaver() {
		if(mCookieSaver == null) 
			Log.e("CookieSaver.getCookieSaver", "mCookieSaver == null");
		return mCookieSaver;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME 
				+ " (`name` VARCHAR(64) NOT NULL, `value` VARCHAR(128) NOT NULL, " 
				+ "`domain` VARCHAR(128) NOT NULL, `path` VARCHAR(256) NOT NULL, " 
				+ "`date` DATETIME NOT NULL, UNIQUE (`name`));");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		
	}
	
	/**
	 * @return ���������� ���� �� ����
	 */
	public Cookie[] getCookies() {
		List<Cookie> cookies = new ArrayList<Cookie>();
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.rawQuery("SELECT name,value,domain,path,date FROM " + TABLE_NAME + ";", null);
		
		if(c == null) return null;
		if(!c.moveToFirst()) {
			c.close();
			db.close();
			return null;
		}
		
		Log.d("CookieSaver", "do ... while");
		
		do {			
			BasicClientCookie cookie = new BasicClientCookie(c.getString(0), c.getString(1));
			cookie.setDomain(c.getString(2));
			cookie.setPath(c.getString(3));
			
			try {
				cookie.setExpiryDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(c.getString(4)));
			} catch (ParseException e) {
				Log.w("ParseException", e.getMessage());
			}
			
			Log.i("CookieSaver.getCookie", cookie.getName());
			Log.i("CookieSaver.getCookie", "Val: " + cookie.getValue());
			Log.i("CookieSaver.getCookie", "Dom: " + cookie.getDomain());
			Log.i("CookieSaver.getCookie", "Pat: " + cookie.getPath());
			Log.i("CookieSaver.getCookie", "Dat: " + cookie.getExpiryDate().toGMTString());
			Log.i("CookieSaver.getCookie", "Com: " + cookie.getComment());
			Log.i("CookieSaver.getCookie", "CoU: " + cookie.getCommentURL());
			Log.i("CookieSaver.getCookie", "Ver: " + cookie.getVersion());
			
			cookies.add(cookie);
		} while (c.moveToNext());
		
		c.close();
		db.close();
		
		Log.d("CookieSaver", "return cookies");
		
		return cookies.toArray(new Cookie[0]);
	}
	
	/**
	 * ��������� � ���� ����
	 * @param cook ����
	 */
	public void putCookie(Cookie cook) {	
		Log.i("CookieSaver.putCookie", cook.getName());
		Log.i("CookieSaver.putCookie", "Val: " + cook.getValue());
		Log.i("CookieSaver.putCookie", "Dom: " + cook.getDomain());
		Log.i("CookieSaver.putCookie", "Pat: " + cook.getPath());
		Log.i("CookieSaver.putCookie", "Dat: " + cook.getExpiryDate().toGMTString());
		Log.i("CookieSaver.putCookie", "Com: " + cook.getComment());
		Log.i("CookieSaver.putCookie", "CoU: " + cook.getCommentURL());
		Log.i("CookieSaver.putCookie", "Ver: " + cook.getVersion());
		
		SQLiteDatabase db = getWritableDatabase();
		try {
		db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES ('" 
				+ cook.getName() + "',  '" + cook.getValue() + "',  '" 
				+ cook.getDomain() + "',  '" + cook.getPath() + "',  '" 
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cook.getExpiryDate()) + "');");
		} catch(SQLException e) {
			Log.w("CookieSaver.putCookie", "SQLException: " + e.getMessage());
		}
		db.close();
	}
	
	/**
	 * ��������� ���� � ���� �� �������
	 * @param cooks ����
	 */
	public void putCookies(Cookie[] cooks) {
		for(int i = 0; i < cooks.length; i++)
			putCookie(cooks[i]);
	}
	
	/**
	 * ������� ���� �� ���
	 */
	public void clearCookies() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME + ";");
		db.close();
	}
	
	public void close() {
		mCookieSaver = null;
	}
}