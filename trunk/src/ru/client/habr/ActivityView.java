package ru.client.habr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import ru.client.habr.R;
import ru.client.habr.AsyncDataLoader.LoaderData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WNeZRoS
 * TODO poll
 */
public class ActivityView extends Activity {	
	private WebView mResultView = null;
	private SharedPreferences mPreferences = null;
	private WifiManager mWifi = null;
	private String mSelectedUri = null;
	private HabraEntry mSelectedEntry = null;
	private List<HabraEntry> mLastEntries = new ArrayList<HabraEntry>();
	private static boolean userBarWasUpdated = false;
	
	private LoaderData mAnyDataLoader = new LoaderData() {
		public void finish(String data) {
			switch(pageType) {
			case POST_LIST: finishLoading("Хабрахабр - " + Uri.parse(url).getPath().replace('/', '@'), data); break;
			case POST: finishLoading(((HabraTopic)mLastEntries.get(0)).title, data); break;
			case QUEST_LIST: finishLoading("Хабрахабр QA - " + Uri.parse(url).getPath().replace('/', '@'), data); break; 
			case QUEST: finishLoading(((HabraQuest)mLastEntries.get(0)).title, data); break;
			case USER: finishLoading(Uri.parse(url).getHost(), data); break;
			default: finishLoading(url, data); break;
			}
		}
		public String update(String pageData) {
			mLastEntries.clear(); // XXX May be remove ?
			
			String data = "";
			
			switch(pageType) {
			case POST_LIST: {
				HabraTopicParser parser = new HabraTopicParser(pageData);
				HabraTopic topic = null;
				
				boolean hideContent = mPreferences.getBoolean("prefHidePostContent", false);
				boolean hideTags = mPreferences.getBoolean("prefHidePostTags", false);
				boolean hideMark = mPreferences.getBoolean("prefHidePostMark", false);
				boolean hideDate = mPreferences.getBoolean("prefHidePostDate", false);
				boolean hideFavs = mPreferences.getBoolean("prefHidePostFavs", false);
				boolean hideAuthor = mPreferences.getBoolean("prefHidePostAuthor", false);
				boolean hideComments = mPreferences.getBoolean("prefHidePostComments", false);
				
				while((topic = parser.parse()) != null) {
					mLastEntries.add(topic); // XXX this can be replace on "%url%?a={AUTHOR}&f={FAVS}&v={MARK}" XXX 
					data += topic.getDataAsHTML(hideContent, hideTags, hideMark, 
							hideDate, hideFavs, hideAuthor, hideComments);
				}
				
				if(data.length() == 0) {
					data = pageData;
				} else {
					data += getNavLinksForUri(Uri.parse(url));
				}
			} break;
			case POST: {
				HabraTopicParser parser = new HabraTopicParser(pageData);
				HabraTopic topic = parser.parse();
				
				if(topic == null) return pageData;
				
				data = topic.getDataAsHTML();
				data += "<div id=\"comments\">";
				
				mLastEntries.add(topic); // XXX
				
				HabraCommentParser commentParser = new HabraCommentParser(pageData);
				HabraComment comment = null;
				
				while((comment = commentParser.parse(topic.id)) != null) {
					mLastEntries.add(comment); // XXX
					data += comment.getDataAsHTML();
				}
				data += "</div>";
			} break;
			case QUEST_LIST: {
				HabraQuestParser parser = new HabraQuestParser(pageData);
				HabraQuest quest = null;
				
				boolean hideContent = mPreferences.getBoolean("prefHideQuestContent", false);
				boolean hideTags = mPreferences.getBoolean("prefHideQuestTags", false);
				boolean hideMark = mPreferences.getBoolean("prefHideQuestMark", false);
				boolean hideDate = mPreferences.getBoolean("prefHideQuestDate", false);
				boolean hideFavs = mPreferences.getBoolean("prefHideQuestFavs", false);
				boolean hideAuthor = mPreferences.getBoolean("prefHideQuestAuthor", false);
				boolean hideAnswers = mPreferences.getBoolean("prefHideQuestComments", false);
				
				while((quest = parser.parse()) != null) {
					mLastEntries.add(quest); // XXX
					data += quest.getDataAsHTML(hideContent, hideTags, hideMark, 
							hideAnswers, hideDate, hideFavs, hideAuthor);
				}
				
				if(data.length() == 0) {
					data = pageData;
				} else {
					data += getNavLinksForUri(Uri.parse(url));
				}
			} break;
			case QUEST: {
				HabraQuestParser parser = new HabraQuestParser(pageData);
				HabraQuest quest = parser.parse();
				
				if(quest == null) return pageData;
				
				data = quest.getDataAsHTML();
				data += quest.getCommentsAsHTML();
				data += "<div id=\"comments\">";
				mLastEntries.add(quest); // XXX
				
				HabraAnswerParser answerParser = new HabraAnswerParser(pageData);
				HabraAnswer answer = null;
				
				while((answer = answerParser.parse()) != null) {
					mLastEntries.add(answer); // XXX
					data += answer.getDataAsHTML();
					data += answer.getCommentsAsHTML();
				}
				data += "</div>";
			} break;
			case USER: {
				HabraUser user = HabraUser.parse(pageData);
				
				if(user == null) return pageData;
				return user.getDataAsHTML();
			}
			default: return pageData;
			}
			
			return data;
		}
		public void start() { 
			startLoading(); 
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mResultView = (WebView) findViewById(R.id.result);
		if(mResultView == null) Log.e("onCreate", "mResultView == null");
		
		if(HabraLogin.getHabraLogin().isLogged()) {
			updateUserBarData(HabraLogin.getHabraLogin());
		} else {
			findViewById(R.id.scrollUserBar).setVisibility(View.GONE);
		}
		
		Log.d("onCreate", "Settings");
		mResultView.getSettings().setAllowFileAccess(true);
		mResultView.getSettings().setJavaScriptEnabled(true);
		mResultView.getSettings().setUserAgentString(URLClient.USER_AGENT);
		mResultView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Uri.parse(url).getHost().contains("habrahabr.ru") 
						|| Uri.parse(url).getHost().equals("m.habrahabr.ru")) {
					startActivityForResult(new Intent(getBaseContext(), 
							ActivityView.class).setData(Uri.parse(url)), 0);
					return true;
				}
				
				Intent openIntent = new Intent(Intent.ACTION_VIEW);
				openIntent.setData(Uri.parse(url));
				startActivity(openIntent);
				return true;
			}
			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				return super.shouldOverrideKeyEvent(view, event);
			}
		});
		
		mResultView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				WebView wv = (WebView)v;
				switch(wv.getHitTestResult().getType()) {
				case WebView.HitTestResult.SRC_ANCHOR_TYPE:
					Log.d("onLongClick", "Click on <a href='" + wv.getHitTestResult().getExtra() + "'...");
					mSelectedUri = wv.getHitTestResult().getExtra();
					
					if(!Uri.parse(mSelectedUri).getHost().equals("habrahabr.ru")) 
						return false;
					
					mSelectedEntry = null;
					
					for(int i = 0; i < mLastEntries.size(); i++) {
						if(mLastEntries.get(i).getUrl(mLastEntries.get(0).id).equals(mSelectedUri)) {
							mSelectedEntry = mLastEntries.get(i);
							break;
						}
					}
					
					if(mSelectedEntry == null) return false; // TODO: Menu "Open, Share, Copy"
					
					/*if(mSelectedEntry.isFavorite())
						mResultViewMenu.findItem(R.id.menu_add_rem_fav).setTitle(R.string.remove_favorites);
					else
						mResultViewMenu.findItem(R.id.menu_add_rem_fav).setTitle(R.string.add_favorites);
					*/
					wv.showContextMenu();
					return true;
				case WebView.HitTestResult.IMAGE_TYPE:
					Log.d("onLongClick", "Click on <img src='" + wv.getHitTestResult().getExtra() + "'...");
					return true;
				}
				return false;
			}
		});
		
		mResultView.addJavascriptInterface(this, "js");
		
		mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		findViewById(R.id.scrollNavPanel).setVisibility(View.GONE);
		
		AsyncDataLoader.getDataLoader().setLoaderData(mAnyDataLoader);
		loadData(getIntent().getData());
		
		registerForContextMenu(mResultView);
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
		
		AsyncDataLoader.getDataLoader().setLoaderData(mAnyDataLoader);
		
		mResultView.getSettings().setBuiltInZoomControls(
				mPreferences.getBoolean("prefEnableZoom", true));
		mResultView.getSettings().setPluginsEnabled(
				mPreferences.getBoolean("prefEnableFlash", false));
		
		if(!mPreferences.getBoolean("prefUserBarNotUpdate", false) 
				|| !ActivityView.userBarWasUpdated) updateUserBar();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_CANCELED) exit(resultCode);
		else AsyncDataLoader.getDataLoader().removeLastRequestFromHistory();
	}
	
	public void onBackPressed() {
		exit(RESULT_OK);
	}
	
	public void onDestroy() {
		super.onDestroy();
		AsyncDataLoader.getDataLoader().cancel(true);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		switch(v.getId()) {
		case R.id.result:
			getMenuInflater().inflate(R.menu.post_list_menu, menu);
			break;
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			findViewById(R.id.scrollNavPanel).setVisibility(
					findViewById(R.id.scrollNavPanel).getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		Uri uri = Uri.parse(mSelectedUri);
		
		if(!uri.getHost().equals("habrahabr.ru")) return super.onContextItemSelected(item);
		
		switch(item.getItemId()) {
		case R.id.menu_open_post:
			Intent openIntent = new Intent(Intent.ACTION_VIEW);
			openIntent.setData(uri);
			startActivity(openIntent);
			return true;
		case R.id.menu_comment: {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse(mSelectedUri + "#comments")), 0);
			return true;
		}
		case R.id.menu_author: {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse("http://" 
							+ mSelectedEntry.author + ".habrahabr.ru/")), 0);
			return true;
		}
		case R.id.menu_share:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, mSelectedUri);
			startActivity(Intent.createChooser(sendIntent, null));
			return true;
		case R.id.menu_copy_link:
			return true;
		case R.id.menu_add_rem_fav:
			mSelectedEntry.changeFavorites();
			return true;
		case R.id.menu_vote_up:
			mSelectedEntry.vote(1, mLastEntries.get(0).id);
			return true;
		case R.id.menu_vote_zero:
			mSelectedEntry.vote(0, mLastEntries.get(0).id);
			return true;
		case R.id.menu_vote_down:
			mSelectedEntry.vote(-1, mLastEntries.get(0).id);
			return true;
		default: 
			return super.onContextItemSelected(item);
		}
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
		case R.id.menu_cache:
			final String[] cache = getCacheDir().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.contains(".html");
				}
			});
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick an item");
			builder.setItems(cache, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					mResultView.loadUrl("file://" + getCacheDir().getAbsolutePath() + "/" + cache[item]);
					mResultView.setVisibility(View.VISIBLE);
					findViewById(R.id.procLoading).setVisibility(View.GONE);
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
			return true;
		case R.id.menu_refresh:
			AsyncDataLoader.getDataLoader().reload();
			return true;
		case R.id.menu_preferences:
			startActivity(new Intent(getBaseContext(), HabraPreferences.class));
			return true;
		case R.id.menu_exit:
			exit(RESULT_CANCELED);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void onClickUserName(View v) {
		loadData(Uri.parse(HabraLogin.getHabraLogin().getProfileURL()));
	}
	
	public void onClickFavorites(View v) {
		
	}
	
	public void onClickPM(View v) {
		
	}
	
	public void onClickNav(View v) {
		switch(v.getId()) {
		case R.id.buttonNavPost:
			loadData(Uri.parse("http://habrahabr.ru/"));
			break;
		case R.id.buttonNavQA:
			loadData(Uri.parse("http://habrahabr.ru/qa/"));
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
		findViewById(R.id.scrollNavPanel).setVisibility(View.GONE);
	}
	
	private String getNavLinksForUri(Uri uri) {
		String bpath = "http://" + uri.getHost();
		String npath = "http://" + uri.getHost();
		List<String> paths = uri.getPathSegments();
		int cur = 0;
		
		for(int i = 0; i < paths.size(); i++) {
			if(paths.get(i).startsWith("page")) {
				cur = Integer.valueOf(paths.get(i).substring(4));
				bpath += "/page" + (cur <= 1 ? 1 : cur - 1);
				npath += "/page" + (cur + 1);
				continue;
			}
			bpath += "/" + paths.get(i);
			npath += "/" + paths.get(i);
		}
		if(cur == 0) {
			bpath += "/page1";
			npath += "/page2";
		}
		bpath += "/"; 
		npath += "/";
		
		Log.d("URI", npath);
		
		return "<h2 class=\"nav\"><a href=\"" + bpath + "?" + uri.getQuery() 
			+ "\">&#8592;&nbsp;сюда</a>&nbsp;&nbsp;<a href=\"" + npath 
			+ "?" + uri.getQuery()  + "\">туда&nbsp;&#8594;</a></h2>" ;
	}
	
	private void exit(int result) {
		setResult(result);
		finish();
	}
	
	private void updateUserBar() {
		if(HabraLogin.getHabraLogin().getUserName() == null) {
			findViewById(R.id.scrollUserBar).setVisibility(View.GONE);
		} else {
			HabraLogin.getHabraLogin().parseUserKarmaAndForce(new HabraLogin.KarmaListener() {
				@Override
				public void onFinish(HabraLogin login) {
					userBarWasUpdated = true;
					updateUserBarData(login);
				}
			});
		}
	}
	
	private void updateUserBarData(HabraLogin login) {
		final TextView titleName = (TextView) findViewById(R.id.titleUserName);
		final TextView titleKarma = (TextView) findViewById(R.id.titleKarma);
		final TextView titleForce = (TextView) findViewById(R.id.titleHabraForce);
		
		titleName.setText(login.getUserName());
		titleKarma.setText(String.valueOf(login.getUserKarma()));
		titleForce.setText(String.valueOf(login.getUserRating()));
		
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
	
	private void startLoading() {
		findViewById(R.id.procLoading).setVisibility(View.VISIBLE);
	}
	
	private void finishLoading(String title, String data) {	
		data = mPreferences.getBoolean("prefEnableFlash", false) ? data 
				: RemoveNode.removeVideo(data);
		data = mPreferences.getString("prefLoadImages", "2").equals("1") 
				|| (mPreferences.getString("prefLoadImages", "2").equals("2") 
				&& mWifi.getConnectionInfo().getNetworkId() != -1) ? data 
						: RemoveNode.removeImage(data);
		
		data = "<html>\n<head>\n" 
			+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" 
			+ "<link href=\"../files/general.css\" rel=\"stylesheet\"/>\n" 
			+ "<script type=\"text/javascript\" src=\"../files/general.js\"></script>\n" 
			+ "<title>" + title + "</title>\n</head>\n<body>\n<div class=\"main-content\">\n" 
			+ data + "\n</div>\n</body>\n</html>";
		
		if(title.length() == 0 || !mPreferences.getBoolean("prefUseCache", true))
		{
			mResultView.loadDataWithBaseURL("file:///android_asset/", data, 
					"text/html", "utf-8", null);
			return;
		}
		
		File cacheFile = new File(getCacheDir(), title.replaceAll("[/|\\?]+", "!") + ".html");
		try {
			FileOutputStream outStream = new FileOutputStream(cacheFile);
			outStream.write(data.getBytes());
			outStream.close();
			
			mResultView.loadUrl("file://" + URLClient.encode(cacheFile.getAbsolutePath()));
		} catch (IOException e) {
			Log.w("Habrahabr.finishLoading", "IOException: " + e.getMessage());
			
			Toast.makeText(getApplicationContext(), getString(R.string.not_cache), Toast.LENGTH_LONG).show();
			
			mResultView.loadDataWithBaseURL("file:///android_asset/", data, 
					"text/html", "utf-8", null);
		}
		
		findViewById(R.id.procLoading).setVisibility(View.GONE);
	}
	
	private void loadData(Uri uri) {
		Log.d("onCreate", "Load data"); 
		
		if(uri != null) {
			AsyncDataLoader.getDataLoader().execute(uri.toString());
		} else {
			String url = mPreferences.getString("prefMainScreenContent", "http://habrahabr.ru/?fl=all");
			if((url.equals("qwe") || url.equals("http://habrahabr.ru/?fl=hl")) 
					&& !HabraLogin.getHabraLogin().isLogged()) {
				mResultView.loadData(getString(R.string.need_login), "text/html", "utf-8");
				return;
			}
			else if(url.equals("qwe")) {
				url = HabraLogin.getHabraLogin().getProfileURL();
			}
			Log.i("ActivityView.loadData", url);
			
			AsyncDataLoader.getDataLoader().execute(url);
		}
	}
}