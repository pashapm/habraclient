package ru.client.habr;

import org.apache.http.cookie.Cookie;

import ru.client.habr.AsyncDataLoader.LoaderData;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

public class Habrahabr extends Activity {
    
	WebView mResultView = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new CookieSaver(this);
        URLClient.getUrlClient().insertCookies(CookieSaver.getCookieSaver().getCookies());
        HabraLogin.getHabraLogin().setCacheDir(getCacheDir().getAbsolutePath());
        HabraLogin.getHabraLogin().parseUserData();
        
        Log.d("onCreate", "SharedPref");
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("prefFirstStart", true))
        {
        	startActivity(new Intent(getBaseContext(), HabraLoginForm.class));
        }
        
        Log.d("onCreate", "mResultView");
        
        mResultView = (WebView) findViewById(R.id.result);
        if(mResultView == null) Log.e("onCreate", "mResultView == null");
        
        Log.d("onCreate", "Settings");
        mResultView.getSettings().setAllowFileAccess(true);
        mResultView.getSettings().setJavaScriptEnabled(true);
        
        /*Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "It's send?");
        Intent chooser = Intent.createChooser(sendIntent, null);
        startActivity(chooser);*/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
        case R.id.menuHelp:
        	Cookie[] cooks = URLClient.getUrlClient().getCookies();
        	for(int i = 0; i < cooks.length; i++) 
        		Log.i("DUMP", cooks[i].getName() + " : " + cooks[i].getValue());
        	return true;
        case R.id.menuRefresh:
        	AsyncDataLoader.getDataLoader().reload();
        	return true;
        case R.id.menuPreferences:
        	startActivity(new Intent(getBaseContext(), HabraPreferences.class));
        	return true;
        case R.id.menuExit:
        	onBackPressed();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void onBackPressed()
    {
    	CookieSaver.getCookieSaver().putCookies(URLClient.getUrlClient().getCookies());
    	CookieSaver.getCookieSaver().close();
    	finish();
    }
    
    public void onStart()
    {
    	super.onStart();
    	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	updateUserBar();
        
        if(preferences.getBoolean("prefFullScreen", false))
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
        loadData(false);
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	AsyncDataLoader.getDataLoader().cancel(true);
    }
    
    public void onResume()
    {
    	super.onResume();
    	updateUserBar();
    }
    
    public void onConfigurationChanged(Configuration newConfig)
    {
    	if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
    	{
    		// Это бужет нужно для панели навигации:
    		// Сверху/Снизу в портретном режиме
    		// Справа/Слева в ланшафтном
    	}
    	else
    	{
    		
    	}
    }
    
    public void onClickUserName(View v)
    {
    	
    }
    
    public void onClickFavorites(View v)
    {
    	
    }
    
    public void onClickPM(View v)
    {
    	
    }
    
    public void updateUserBar()
    { 	
    	if(HabraLogin.getHabraLogin().getUserName() == null)
    	{
    		findViewById(R.id.scrollUserBar).setVisibility(View.GONE);
    	}
    	else
    	{
    		TextView titleName = (TextView) findViewById(R.id.titleUserName);
    		TextView titleKarma = (TextView) findViewById(R.id.titleKarma);
    		TextView titleForce = (TextView) findViewById(R.id.titleHabraForce);
        	
	    	HabraLogin.getHabraLogin().parseUserKarmaAndForce();
	    	titleName.setText(HabraLogin.getHabraLogin().getUserName());
	    	titleKarma.setText(String.valueOf(HabraLogin.getHabraLogin().getUserKarma()));
	    	titleForce.setText(String.valueOf(HabraLogin.getHabraLogin().getUserRating()));
	    
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	    	
	    	findViewById(R.id.scrollUserBar).setVisibility(preferences.getBoolean("prefUserBarHide", false) ? View.GONE : View.VISIBLE);
	    	titleKarma.setVisibility(preferences.getBoolean("prefUserBarHideMark", false) ? View.GONE : View.VISIBLE);
	    	titleForce.setVisibility(preferences.getBoolean("prefUserBarHideForce", false) ? View.GONE : View.VISIBLE);
	    	findViewById(R.id.buttonFavorites).setVisibility(preferences.getBoolean("prefUserBarHideFavorites", false) ? View.GONE : View.VISIBLE);
	    	findViewById(R.id.buttonPrivateMail).setVisibility(preferences.getBoolean("prefUserBarHidePM", false) ? View.GONE : View.VISIBLE);
    	}
    }
    
    public void loadData(boolean force)
    {
    	Log.d("onCreate", "Load data");
        
        AsyncDataLoader.getDataLoader().execute(new LoaderData("http://habrahabr.ru/", force) {
			@Override
			public void finish(String data) {
		        mResultView.loadDataWithBaseURL("file:///android_asset/", "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\"/></head>" + data, "text/html", "utf-8", null);
				findViewById(R.id.procLoading).setVisibility(View.GONE);
			}
			@Override
			public void start() {
				mResultView.loadData("<style>body {background:black;margin:0 0 0 0;padding: 0 0 0 0; width:100%25;}</style>", "text/plain", "utf-8");
				findViewById(R.id.procLoading).setVisibility(View.VISIBLE);
			}
			@Override
			public void update(String data) {}
        });
    }
}