package ru.client.habr;

import org.apache.http.cookie.Cookie;

import ru.client.habr.AsyncDataLoader.LoaderData;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
	SharedPreferences preferences = null;
	boolean isStartCalled = false;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
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
        
        /*final HorizontalScrollView scrollNavPanel = (HorizontalScrollView) findViewById(R.id.scrollNavPanel);
        final LinearLayout layoutData = (LinearLayout) findViewById(R.id.layoutData);
        
        bottom.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				    params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				    scrollNavPanel.setLayoutParams(params1);
				    
				    LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				    params2.bottomMargin = 60;
				    layoutData.setLayoutParams(params2);
				} else {
					LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				    params1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				    scrollNavPanel.setLayoutParams(params1);
				    
				    LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				    params2.topMargin = 60;
				    layoutData.setLayoutParams(params2);
				}
			}
        });*/
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
    	System.exit(0);
    }
    
    public void onStart()
    {
    	super.onStart();
    	updateUserBar();
        
        if(preferences.getBoolean("prefFullScreen", false))
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
        loadData(true);
        isStartCalled = true;
    }
    
    public void onResume()
    {
    	super.onResume();
    	if(!isStartCalled)
    	{
	    	updateUserBar();
	    	loadData(false);
    	}
    	else isStartCalled = false;
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	AsyncDataLoader.getDataLoader().cancel(true);
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
    
    public void onClickNav(View v)
    {
    	switch(v.getId())
    	{
    	case R.id.buttonNavPost:
    		AsyncDataLoader.getDataLoader().execute(new LoaderData("http://habrahabr.ru/", true) {
    			@Override
    			public void finish(String data) {
    				finishLoading(data);
    			}
    			public String update(String pageData) {
    				String data = "";
    				HabraTopicParser parser = new HabraTopicParser(pageData);
    				HabraTopic topic = null;
    				
    				boolean hideContent = preferences.getBoolean("prefHidePostContent", false);
    				boolean hideTags = preferences.getBoolean("prefHidePostTags", false);
    				boolean hideMark = preferences.getBoolean("prefHidePostMark", false);
    				boolean hideDate = preferences.getBoolean("prefHidePostDate", false);
    				boolean hideFavs = preferences.getBoolean("prefHidePostFavs", false);
    				boolean hideAuthor = preferences.getBoolean("prefHidePostAuthor", false);
    				boolean hideComments = preferences.getBoolean("prefHidePostComments", false);
    				
    				while((topic = parser.parseTopicFromList()) != null)
    				{
    					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, hideDate, hideFavs, hideAuthor, hideComments);
    				}
    		        return data;
    			}
    			@Override
    			public void start() {
    				startLoading();
    			}
            });
    		break;
    	case R.id.buttonNavQA:
    		AsyncDataLoader.getDataLoader().execute(new LoaderData("http://habrahabr.ru/qa/", true) {
    			@Override
    			public void finish(String data) {
    				finishLoading(data);
    			}
    			public String update(String pageData) {
    				String data = "";
    				HabraQuestParser parser = new HabraQuestParser(pageData);
    				HabraQuest quest = null;
    				while((quest = parser.parseQuestFromList()) != null)
    				{
    					data += quest.getDataAsHTML();
    				}
    				return data;
    			}
    			@Override
    			public void start() {
    				startLoading();
    			}
            });
    		break;
    	case R.id.buttonNavPeople:
    		mResultView.loadData("Coming soon...", "text/html", "utf-8");
    		break;
    	case R.id.buttonNavBlog:
    		mResultView.loadData("Coming soon...", "text/html", "utf-8");
    		break;
    	case R.id.buttonNavCompany:
    		mResultView.loadData("Coming soon...", "text/html", "utf-8");
    		break;
    	default: 
    		mResultView.loadData("WTF o_O", "text/html", "utf-8");
    		break;
    	}
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
	    	
	    	findViewById(R.id.scrollUserBar).setVisibility(preferences.getBoolean("prefUserBarHide", false) ? View.GONE : View.VISIBLE);
	    	titleKarma.setVisibility(preferences.getBoolean("prefUserBarHideMark", false) ? View.GONE : View.VISIBLE);
	    	titleForce.setVisibility(preferences.getBoolean("prefUserBarHideForce", false) ? View.GONE : View.VISIBLE);
	    	findViewById(R.id.buttonFavorites).setVisibility(preferences.getBoolean("prefUserBarHideFavorites", false) ? View.GONE : View.VISIBLE);
	    	findViewById(R.id.buttonPrivateMail).setVisibility(preferences.getBoolean("prefUserBarHidePM", false) ? View.GONE : View.VISIBLE);
    	}
    }
    
    public void startLoading()
    {
    	mResultView.loadData("<style>body {background:black;margin:0 0 0 0;padding: 0 0 0 0; width:100%25;}</style>", "text/plain", "utf-8");
		findViewById(R.id.procLoading).setVisibility(View.VISIBLE);
    }
    
    public void finishLoading(String data)
    {
    	mResultView.loadDataWithBaseURL("file:///android_asset/", "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /><link href=\"general.css\" rel=\"stylesheet\"/></head>" + data, "text/html", "utf-8", null);
		findViewById(R.id.procLoading).setVisibility(View.GONE);
    }
    
    public void loadData(boolean notUpdate)
    {
    	Log.d("onCreate", "Load data"); 
    	
    	// To load requered data use getIntent().getData()
    	
    	if(getIntent().getData() != null)
    	{
    		String[] path = getIntent().getData().getPathSegments().toArray(new String[0]);
    		if(path.length == 0 || path[0].equals("new"))
    		{
    			Log.d("Habrahabr.loadData", "CODE: path.length == 0 || path[0].equals(\"new\")");
    			Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
    			AsyncDataLoader.getDataLoader().execute(new LoaderData(getIntent().getData().toString(), false) {
                	public void finish(String data) { finishLoading(data); }
                	public String update(String pageData) {
                		String data = "";
                		HabraTopicParser parser = new HabraTopicParser(pageData);
        				HabraTopic topic = null;
        				
        		    	boolean hideContent = preferences.getBoolean("prefHidePostContent", false);
        				boolean hideTags = preferences.getBoolean("prefHidePostTags", false);
        				boolean hideMark = preferences.getBoolean("prefHidePostMark", false);
        				boolean hideDate = preferences.getBoolean("prefHidePostDate", false);
        				boolean hideFavs = preferences.getBoolean("prefHidePostFavs", false);
        				boolean hideAuthor = preferences.getBoolean("prefHidePostAuthor", false);
        				boolean hideComments = preferences.getBoolean("prefHidePostComments", false);
        				
        				while((topic = parser.parseTopicFromList()) != null)
        				{
        					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, hideDate, hideFavs, hideAuthor, hideComments);
        				}
        				return data;
                	}
        			public void start() { startLoading(); }
        		});
    		}
    		else if((path.length == 1 && path[0].equals("qa")) || (path.length == 2 && path[1].equals("new")))
    		{
    			Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\") || (path.length == 2 && path[1].equals(\"new\"))");
    			Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
    			AsyncDataLoader.getDataLoader().execute(new LoaderData(getIntent().getData().toString(), false) {
                	public void finish(String data) { finishLoading(data); }
                	public String update(String pageData) {
                		String data = "";
                		HabraQuestParser parser = new HabraQuestParser(pageData);
        				HabraQuest quest = null;
        				
        				while((quest = parser.parseQuestFromList()) != null)
        				{
        					data += quest.getDataAsHTML();
        				}
        				return data;
                	}
        			public void start() { startLoading(); }
        		});
    		}
    		else if(path[0].equals("qa"))
    		{
    			Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\")");
    			Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
    			AsyncDataLoader.getDataLoader().execute(new LoaderData(getIntent().getData().toString(), false) {
                	public void finish(String data) { finishLoading(data); }
                	public String update(String pageData) {
                		HabraQuestParser parser = new HabraQuestParser(pageData);
        				HabraQuest quest = parser.parseFullQuest();
        				String data = quest.getDataAsHTML();
        				data += quest.getCommentsAsHTML();
        				
        				HabraAnswerParser answerParser = new HabraAnswerParser(pageData);
        				HabraAnswer answer = null;
        				
        				while((answer = answerParser.parseAnswer()) != null)
        				{
        					data += answer.getDataAsHTML();
        					data += answer.getCommentsAsHTML();
        				}
        				return data;
                	}
        			public void start() { startLoading(); }
        		});
    		}
    		else if(path[0].equals("post") || path[0].equals("blogs") || path[0].equals("linker") || path[0].equals("company"))
    		{
    			Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"post\") || path[0].equals(\"blogs\")...");
    			Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
    			String url = "http://habrahabr.ru/post/";
    			if(path.length == 3) url += path[2]; // blogs/.../{ID} OR linker/go/{ID}
    			else if(path.length == 4) url += path[3]; // company/blog/.../{ID}
    			else url += path[1]; // post/{ID}
    			url += "/";
    			if(getIntent().getData().getQuery() != null) url += "?" + getIntent().getData().getQuery();
    			if(getIntent().getData().getFragment() != null) url += "#" + getIntent().getData().getFragment();
    			
    			AsyncDataLoader.getDataLoader().execute(new LoaderData(url, false) {
                	public void finish(String data) { finishLoading(data); }
                	public String update(String pageData) {
                		HabraTopicParser parser = new HabraTopicParser(pageData);
        				HabraTopic topic = parser.parseFullTopic();
        				String data = topic.getDataAsHTML();
        				
        				HabraCommentParser commentParser = new HabraCommentParser(pageData);
        				HabraComment comment = null;
        				
        				while((comment = commentParser.parseComment()) != null)
        				{
        					data += comment.getCommentAsHTML();
        				}
        		    	
        				return data;
                	}
        			public void start() { startLoading(); }
    			});
    		}
    	}
    	else
    	{
    		Log.i("pref", preferences.getString("prefMainScreenContent", "000"));
	    	switch(Integer.valueOf(preferences.getString("prefMainScreenContent", "2")))
	    	{
	    	case 1: // LENTA
	    		if(!HabraLogin.getHabraLogin().isLogged())
	    			finishLoading("Ленту могут видеть только авторизированные пользователи хабра.");
	    		else AsyncDataLoader.getDataLoader().repeat(new LoaderData("http://habrahabr.ru/?fl=hl", false) {
	            	public void finish(String data) { finishLoading(data); }
	            	public String update(String pageData) {
	            		String data = "";
	            		HabraTopicParser parser = new HabraTopicParser(pageData);
	    				HabraTopic topic = null;
	    				
	    		    	boolean hideContent = preferences.getBoolean("prefHidePostContent", false);
	    				boolean hideTags = preferences.getBoolean("prefHidePostTags", false);
	    				boolean hideMark = preferences.getBoolean("prefHidePostMark", false);
	    				boolean hideDate = preferences.getBoolean("prefHidePostDate", false);
	    				boolean hideFavs = preferences.getBoolean("prefHidePostFavs", false);
	    				boolean hideAuthor = preferences.getBoolean("prefHidePostAuthor", false);
	    				boolean hideComments = preferences.getBoolean("prefHidePostComments", false);
	    				
	    				while((topic = parser.parseTopicFromList()) != null)
	    				{
	    					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, hideDate, hideFavs, hideAuthor, hideComments);
	    				}
	    				return data;
	            	}
	    			public void start() { startLoading(); }
	    		});
	    		break;
	    	case 2: // ALL
	    		AsyncDataLoader.getDataLoader().repeat(new LoaderData("http://habrahabr.ru/?fl=all", false) {
	            	public void finish(String data) { finishLoading(data); }
	            	public String update(String pageData) {
	            		String data = "";
	            		HabraTopicParser parser = new HabraTopicParser(pageData);
	    				HabraTopic topic = null;
	    				
	    		    	boolean hideContent = preferences.getBoolean("prefHidePostContent", false);
	    				boolean hideTags = preferences.getBoolean("prefHidePostTags", false);
	    				boolean hideMark = preferences.getBoolean("prefHidePostMark", false);
	    				boolean hideDate = preferences.getBoolean("prefHidePostDate", false);
	    				boolean hideFavs = preferences.getBoolean("prefHidePostFavs", false);
	    				boolean hideAuthor = preferences.getBoolean("prefHidePostAuthor", false);
	    				boolean hideComments = preferences.getBoolean("prefHidePostComments", false);
	    				
	    				while((topic = parser.parseTopicFromList()) != null)
	    				{
	    					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, hideDate, hideFavs, hideAuthor, hideComments);
	    				}
	    				return data;
	            	}
	    			public void start() { startLoading(); }
	    		});
	    		break;
	    	case 3: // NEW ALL
	    		AsyncDataLoader.getDataLoader().repeat(new LoaderData("http://habrahabr.ru/new/?fl=all", false) {
	            	public void finish(String data) { finishLoading(data); }
	            	public String update(String pageData) {
	            		String data = "";
	            		HabraTopicParser parser = new HabraTopicParser(pageData);
	    				HabraTopic topic = null;
	    				
	    		    	boolean hideContent = preferences.getBoolean("prefHidePostContent", false);
	    				boolean hideTags = preferences.getBoolean("prefHidePostTags", false);
	    				boolean hideMark = preferences.getBoolean("prefHidePostMark", false);
	    				boolean hideDate = preferences.getBoolean("prefHidePostDate", false);
	    				boolean hideFavs = preferences.getBoolean("prefHidePostFavs", false);
	    				boolean hideAuthor = preferences.getBoolean("prefHidePostAuthor", false);
	    				boolean hideComments = preferences.getBoolean("prefHidePostComments", false);
	    				
	    				while((topic = parser.parseTopicFromList()) != null)
	    				{
	    					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, hideDate, hideFavs, hideAuthor, hideComments);
	    				}
	    				return data;
	            	}
	    			public void start() { startLoading(); }
	    		});
	    		break;
	    	case 4: // Q&A
	    		AsyncDataLoader.getDataLoader().repeat(new LoaderData("http://habrahabr.ru/qa/", false) {
	            	public void finish(String data) { finishLoading(data); }
	            	public String update(String pageData) {
	            		String data = "";
	            		HabraQuestParser parser = new HabraQuestParser(pageData);
	    				HabraQuest quest = null;
	    				
	    				while((quest = parser.parseQuestFromList()) != null)
	    				{
	    					data += quest.getDataAsHTML();
	    				}
	    				return data;
	            	}
	    			public void start() { startLoading(); }
	    		});
	    		break;
	    	case 5:
	    		finishLoading("Coming soon...");
	    		break;
	    	default:
	    		finishLoading("Unknown page");
	    		break;
	    	}
	    }
    }
}