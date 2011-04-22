package ru.client.habr;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;

public class Habrahabr extends Activity {
    
	WebView mResultView = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new CookieSaver(this);
        URLClient.getUrlClient().insertCookies(CookieSaver.getCookieSaver().getCookies());
        new HabraLogin(getCacheDir().getAbsolutePath());
        
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
        Log.d("onCreate", "Load temp data");
        mResultView.loadData("Loading...", "text/html", "utf-8");
        
        /*String out = "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\" media=\"all\"/></head>";
        String data = urlClient.getURL("http://habrahabr.ru/?fl=all");
        
        HabraTopicParser parser = new HabraTopicParser(data);
        HabraTopic topic = null;
        while((topic = parser.parseTopicFromList()) != null)
        {
        	Log.d("while", topic.title);
        	out += RemoveImage.remove(topic.getTopicDataAsHTML());
        }*/
        
        loadData();
        
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
        	loadData();
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
        
        if(preferences.getBoolean("prefFullScreen", false))
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    
    public void loadData()
    {
    	Log.d("onCreate", "Load data");
        
        String out = "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\" media=\"all\"/></head>";
        String data = URLClient.getUrlClient().getURL("http://habrahabr.ru/");
        
        if(data == null)
        {
        	Log.w("onCreate", "data is null");
        }
        else
        {
		    /*HabraTopicParser parser = new HabraTopicParser(data);
		    HabraTopic topic = null;
		    
		    while((topic = parser.parseTopicFromList()) != null)
		    {
		    	out += topic.getDataAsHTML();
		    	out += "<div style='margin-left:20px;'>";
		    }*/
        	
        	out = data;
		   
		    mResultView.stopLoading();
		    mResultView.loadDataWithBaseURL("file:///android_asset/", out, "text/html", "utf-8", null);
        }
    }
}