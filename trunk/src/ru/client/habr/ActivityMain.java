package ru.client.habr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author WNeZRoS
 * �������� �����
 */
public class ActivityMain extends Activity {	
	final int ASSET_REV = 25;
	boolean first = false;
	static String sCacheDir = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		sCacheDir = getCacheDir().getAbsolutePath();
		
		if(CookieSaver.getCookieSaver() == null) {
			first = true;
			unpackAssets(getCacheDir());
			
			new CookieSaver(this);
			URLClient.getUrlClient().insertCookies(CookieSaver.getCookieSaver().getCookies());
			HabraLogin.getHabraLogin().parseUserData();
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			if(preferences.getBoolean("prefFirstStart", true)) {
				startActivityForResult(new Intent(getBaseContext(), 
						ActivityLogin.class), R.layout.first_login);
			} else {
				startActivityForResult(new Intent(getBaseContext(), 
						ActivityView.class).setData(getIntent().getData()), R.layout.view);
			}
		} else {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(getIntent().getData()), R.layout.view);
		}
	}
	
	public void onStart() {
		super.onStart();
	}
	
	public void onResume() {
		super.onResume();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case R.layout.first_login:
			startActivityForResult(new Intent(getBaseContext(), ActivityView.class), R.layout.view);
			break;
		case R.layout.view:
			exit();
			break;
		default: super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public void onBackPressed() {
		exit();
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void exit() {
		if(first) {
			CookieSaver.getCookieSaver().putCookies(URLClient.getUrlClient().getCookies());
			CookieSaver.getCookieSaver().close();
		}
		
		finish();
		
		if(first) System.exit(0);
	}
	
	private void unpackAssets(File where) {
		File revFile = new File(getCacheDir(), "habraclient.rev");
		try {
			FileInputStream revInputStream = new FileInputStream(revFile);
			int rev = revInputStream.read();
			if(rev >= ASSET_REV) return;
			revInputStream.close();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			Log.w("Habrahabr.unpackAssets", "IOException: " + e.getMessage());
		}
		
		try {
			OutputStream revOut = new FileOutputStream(revFile);
			revOut.write(ASSET_REV);
			revOut.close();
		} catch (FileNotFoundException e) {
			Log.e("Habrahabr.unpackAssets", "FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("Habrahabr.unpackAssets", "FileNotFoundException: " + e.getMessage());
		}
		
		Log.i("Habrahabr.unpackAssets", "Update cache directory");
		
		AssetManager assetManager = getAssets();
		
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("Habrahabr.unpackAssets", "IOException: " + e.getMessage());
		}
		
		for(int i = 0; i < files.length; i++) {
			try {
				InputStream in = assetManager.open(files[i]);
				OutputStream out = new FileOutputStream(new File(where, files[i]));
				
				byte[] buffer = new byte[1024];
				int read;
				while((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				
				in.close();
				out.close();
			} catch(Exception e) {
				Log.e("Habrahabr.unpackAssets", "Exception: " + e.getMessage());
			}
		}
	}
}