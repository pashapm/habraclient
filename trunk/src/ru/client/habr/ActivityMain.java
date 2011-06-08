package ru.client.habr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ru.client.habr.R;
import ru.client.habr.Dialogs.OnClickMessage;
import ru.client.habr.HabraLogin.UserInfoListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

/**
 * @author WNeZRoS
 */
public class ActivityMain extends Activity {	
	public final static int REQUEST_LOGIN = 0;
	private final int ASSET_REV = 46;
	
	public static Context sAppContext = null;
	boolean first = false;
	
	UserInfoListener userInfo = new UserInfoListener() {
		@Override
		public void onFinish(String userName) {
			if(userName == null || userName.length() > 0) {
				startActivityForResult(new Intent(getBaseContext(), ActivityView.class)
						.setData(getIntent().getData()), ActivityView.REQUEST_NEW_VIEW);
			} else {
				// Ошибка, данные не получены
				showNoConnectionDialog(this);
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Dialogs.setContext(this);
		initialize();
	}
	
	public void onResume() {
		super.onResume();
		Dialogs.setContext(this);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ActivityMain.onActivityResult", "Result to " + requestCode + " is " + resultCode);
		
		switch(requestCode) {
		case REQUEST_LOGIN:
			HabraLogin.getHabraLogin().parseUserData(userInfo);
			break;
		case ActivityView.REQUEST_NEW_VIEW:
			exit();
			break;
		default: 
			findViewById(R.id.layoutLoading).setVisibility(View.GONE);
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public void onBackPressed() {
		exit();
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void initialize() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
			showNoConnectionDialog(null);
			return;
		}
		
		if(CookieSaver.getCookieSaver() == null) {
			first = true;
			unpackAssets(getFilesDir());
			
			sAppContext = getApplicationContext();
			
			new CookieSaver(this);
			URLClient.getUrlClient().insertCookies(CookieSaver.getCookieSaver().getCookies());
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			if(preferences.getBoolean("prefFirstStart", true)) {
				startActivityForResult(new Intent(getBaseContext(), 
						ActivityLogin.class), REQUEST_LOGIN);
			} else {
				HabraLogin.getHabraLogin().parseUserData(userInfo);
			}
		} else {
			startActivityForResult(new Intent(getBaseContext(), ActivityView.class)
					.setData(getIntent().getData()), ActivityView.REQUEST_NEW_VIEW);
		}
	}
	
	private void showNoConnectionDialog(final UserInfoListener l) {
		Dialogs.showDialogMessage(getString(R.string.user_data_is_null), 
				getString(R.string.exit), null, getString(R.string.repeat), new OnClickMessage() {
			@Override
			public void onClick(int rel) {
				if(rel == -1) {
					exit();
					return;
				}
				
				if(l == null) initialize();
				else HabraLogin.getHabraLogin().parseUserData(l);
			}
		});
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
		File revFile = new File(where, "habraclient.rev");
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