package ru.client.habr;

import org.apache.http.cookie.Cookie;
import ru.client.habr.AsyncDataLoader.LoaderData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
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

/**
 * @author WNeZRoS
 * Основной класс
 */
public class Habrahabr extends Activity {
	private WebView mResultView = null;
	private SharedPreferences mPreferences = null;
	private WifiManager mWifi = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
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
		mResultView.getSettings().setUserAgentString(URLClient.USER_AGENT);
		
		mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		loadData(false);
		
		/*final HorizontalScrollView scrollNavPanel = (HorizontalScrollView) findViewById(R.id.scrollNavPanel);
		final LinearLayout layoutData = (LinearLayout) findViewById(R.id.layoutData);
		
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, "It's send?");
		Intent chooser = Intent.createChooser(sendIntent, null);
		startActivity(chooser);*/
	}
	
	public void onStart() {
		super.onStart();
		
		if(mPreferences.getBoolean("prefFullScreen", false)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	
	public void onResume() {
		super.onResume();
		
		mResultView.getSettings().setBuiltInZoomControls(
				mPreferences.getBoolean("prefEnableZoom", true));
		mResultView.getSettings().setPluginsEnabled(
				mPreferences.getBoolean("prefEnableFlash", false));
		
		updateUserBar();
	}
	
	public void onBackPressed() {
		CookieSaver.getCookieSaver().putCookies(URLClient.getUrlClient().getCookies());
		CookieSaver.getCookieSaver().close();
		finish();
		System.exit(0);
	}
	
	public void onDestroy() {
		super.onDestroy();
		AsyncDataLoader.getDataLoader().cancel(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
	
	/**
	 * Обрабатывает клик по имени пользователя в юзер баре
	 * @param v
	 */
	public void onClickUserName(View v) {
		
	}
	
	/**
	 * Обрабатывает клик по кнопке "Избранное" в юзер баре
	 * @param v
	 */
	public void onClickFavorites(View v) {
		
	}
	
	/**
	 * Обрабатывает клик по кнопке "Личная почта" в юзер баре
	 * @param v
	 */
	public void onClickPM(View v) {
		
	}
	
	/**
	 * Обрабатывает клики по кнопкам навигации
	 * @param v
	 */
	public void onClickNav(View v) {
		switch(v.getId()) {
		case R.id.buttonNavPost:
			loadPostsList("http://habrahabr.ru/");
			break;
		case R.id.buttonNavQA:
			loadQuestsList("http://habrahabr.ru/qa/");
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
	
	/**
	 * Обновляет информацию в юзер баре
	 */
	private void updateUserBar() {
		if(HabraLogin.getHabraLogin().getUserName() == null) {
			findViewById(R.id.scrollUserBar).setVisibility(View.GONE);
		} else {
			TextView titleName = (TextView) findViewById(R.id.titleUserName);
			TextView titleKarma = (TextView) findViewById(R.id.titleKarma);
			TextView titleForce = (TextView) findViewById(R.id.titleHabraForce);
			
			HabraLogin.getHabraLogin().parseUserKarmaAndForce();
			titleName.setText(HabraLogin.getHabraLogin().getUserName());
			titleKarma.setText(String.valueOf(HabraLogin.getHabraLogin().getUserKarma()));
			titleForce.setText(String.valueOf(HabraLogin.getHabraLogin().getUserRating()));
			
			findViewById(R.id.scrollUserBar).setVisibility(mPreferences
					.getBoolean("prefUserBarHide", false) ? View.GONE : View.VISIBLE);
			titleKarma.setVisibility(mPreferences
					.getBoolean("prefUserBarHideMark", false) ? View.GONE : View.VISIBLE);
			titleForce.setVisibility(mPreferences
					.getBoolean("prefUserBarHideForce", false) ? View.GONE : View.VISIBLE);
			findViewById(R.id.buttonFavorites).setVisibility(mPreferences
					.getBoolean("prefUserBarHideFavorites", false) ? View.GONE : View.VISIBLE);
			findViewById(R.id.buttonPrivateMail).setVisibility(mPreferences
					.getBoolean("prefUserBarHidePM", false) ? View.GONE : View.VISIBLE);
		}
	}
	
	private void startLoading() {
		mResultView.loadData("<style>body {background:black;margin:0 0 0 0;padding: 0 0 0 0; width:100%25;}</style>", "text/plain", "utf-8");
		findViewById(R.id.procLoading).setVisibility(View.VISIBLE);
	}
	
	private void finishLoading(String data) {	
		data = mPreferences.getBoolean("prefEnableFlash", false) ? data 
				: RemoveNode.removeVideo(data);
		data = mPreferences.getString("prefLoadImages", "2").equals("1") 
				|| (mPreferences.getString("prefLoadImages", "2").equals("2") 
				&& mWifi.getConnectionInfo().getNetworkId() != -1) ? data 
						: RemoveNode.removeImage(data);
		
		mResultView.loadDataWithBaseURL("file:///android_asset/", 
				"<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />" 
				+ "<link href=\"general.css\" rel=\"stylesheet\"/></head>" + data,
				"text/html", "utf-8", null);
		findViewById(R.id.procLoading).setVisibility(View.GONE);
	}
	
	private void loadPostsList(String url) {
		AsyncDataLoader.getDataLoader().execute(new LoaderData(url, false) {
			public void finish(String data) { 
				finishLoading(data); 
			}
			public String update(String pageData) {
				String data = "";
				HabraTopicParser parser = new HabraTopicParser(pageData);
				HabraTopic topic = null;
				
				boolean hideContent = mPreferences.getBoolean("prefHidePostContent", false);
				boolean hideTags = mPreferences.getBoolean("prefHidePostTags", false);
				boolean hideMark = mPreferences.getBoolean("prefHidePostMark", false);
				boolean hideDate = mPreferences.getBoolean("prefHidePostDate", false);
				boolean hideFavs = mPreferences.getBoolean("prefHidePostFavs", false);
				boolean hideAuthor = mPreferences.getBoolean("prefHidePostAuthor", false);
				boolean hideComments = mPreferences.getBoolean("prefHidePostComments", false);
				
				while((topic = parser.parseTopicFromList()) != null) {
					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, 
							hideDate, hideFavs, hideAuthor, hideComments);
				}
				return data;
			}
			public void start() { 
				startLoading(); 
			}
		});
	}
	
	private void loadFullPost(String url) {
		AsyncDataLoader.getDataLoader().execute(new LoaderData(url, false) {
			public void finish(String data) { 
				finishLoading(data); 
			}
			public String update(String pageData) {
				HabraTopicParser parser = new HabraTopicParser(pageData);
				HabraTopic topic = parser.parseFullTopic();
				String data = topic.getDataAsHTML();
				data += "<div id=\"comments\">";
				
				HabraCommentParser commentParser = new HabraCommentParser(pageData);
				HabraComment comment = null;
				
				while((comment = commentParser.parseComment()) != null) {
					data += comment.getCommentAsHTML();
				}
				data += "</div>";
				return data;
			}
			public void start() { 
				startLoading(); 
			}
		});
	}
	
	private void loadQuestsList(String url) {
		AsyncDataLoader.getDataLoader().execute(new LoaderData(url, false) {
			public void finish(String data) { 
				finishLoading(data); 
			}
			public String update(String pageData) {
				String data = "";
				HabraQuestParser parser = new HabraQuestParser(pageData);
				HabraQuest quest = null;
				
				boolean hideContent = mPreferences.getBoolean("prefHideQuestContent", false);
				boolean hideTags = mPreferences.getBoolean("prefHideQuestTags", false);
				boolean hideMark = mPreferences.getBoolean("prefHideQuestMark", false);
				boolean hideDate = mPreferences.getBoolean("prefHideQuestDate", false);
				boolean hideFavs = mPreferences.getBoolean("prefHideQuestFavs", false);
				boolean hideAuthor = mPreferences.getBoolean("prefHideQuestAuthor", false);
				boolean hideAnswers = mPreferences.getBoolean("prefHideQuestComments", false);
				
				while((quest = parser.parseQuestFromList()) != null) {
					data += quest.getDataAsHTML(hideContent, hideTags, hideMark, 
							hideAnswers, hideDate, hideFavs, hideAuthor);
				}
				return data;
			}
			public void start() { 
				startLoading(); 
			}
		});
	}
	
	private void loadFullQuest(String url) {
		AsyncDataLoader.getDataLoader().execute(new LoaderData(url, false) {
			public void finish(String data) { 
				finishLoading(data); 
			}
			public String update(String pageData) {
				HabraQuestParser parser = new HabraQuestParser(pageData);
				HabraQuest quest = parser.parseFullQuest();
				String data = quest.getDataAsHTML();
				data += quest.getCommentsAsHTML();
				data += "<div id=\"comments\">";
				
				HabraAnswerParser answerParser = new HabraAnswerParser(pageData);
				HabraAnswer answer = null;
				
				while((answer = answerParser.parseAnswer()) != null) {
					data += answer.getDataAsHTML();
					data += answer.getCommentsAsHTML();
				}
				data += "</div>";
				return data;
			}
			public void start() { 
				startLoading(); 
			}
		});
	}
	
	private void loadData(boolean notUpdate) {
		Log.d("onCreate", "Load data"); 
		
		if(getIntent().getData() != null) {
			String[] path = getIntent().getData().getPathSegments().toArray(new String[0]);
			if(path.length == 0 || path[0].equals("new")) {
				Log.d("Habrahabr.loadData", "CODE: path.length == 0 || path[0].equals(\"new\")");
				Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
				loadPostsList(getIntent().getData().toString());
			} else if((path.length == 1 && path[0].equals("qa")) 
					|| (path.length == 2 && path[1].equals("new"))) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\") || " 
						+ "(path.length == 2 && path[1].equals(\"new\"))");
				Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
				loadQuestsList(getIntent().getData().toString());
			} else if(path[0].equals("qa")) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\")");
				Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
				loadFullQuest(getIntent().getData().toString());
			} else if(path[0].equals("post") || path[0].equals("blogs") 
					|| path[0].equals("linker") || path[0].equals("company")) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"post\") || path[0].equals(\"blogs\")...");
				Log.d("Habrahabr.loadData", "PATH: " + getIntent().getData().toString());
				String url = "http://habrahabr.ru/post/";
				
				if(path.length == 3) url += path[2]; // blogs/.../{ID} OR linker/go/{ID}
				else if(path.length == 4) url += path[3]; // company/blog/.../{ID}
				else url += path[1]; // post/{ID}
				
				url += "/";
				if(getIntent().getData().getQuery() != null) url += "?" + getIntent().getData().getQuery();
				if(getIntent().getData().getFragment() != null) url += "#" + getIntent().getData().getFragment();
				loadFullPost(url);
			}
		} else {
			Log.i("pref", mPreferences.getString("prefMainScreenContent", "000"));
	    	switch(Integer.valueOf(mPreferences.getString("prefMainScreenContent", "2"))) {
	    	case 1: // LENTA
	    		if(!HabraLogin.getHabraLogin().isLogged())
	    			finishLoading("Ленту могут видеть только авторизированные пользователи хабра.");
	    		else loadPostsList("http://habrahabr.ru/?fl=hl");
	    		break;
	    	case 2: // ALL
	    		loadPostsList("http://habrahabr.ru/?fl=all");
	    		break;
	    	case 3: // NEW ALL
	    		loadPostsList("http://habrahabr.ru/new/?fl=all");
	    		break;
	    	case 4: // Q&A
	    		loadQuestsList("http://habrahabr.ru/qa/");
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