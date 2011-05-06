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
import android.view.KeyEvent;
import ru.client.habr.AsyncDataLoader.LoaderData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WNeZRoS
 * �������� �����
 */
public class HabraView extends Activity {	
	private WebView mResultView = null;
	private SharedPreferences mPreferences = null;
	private WifiManager mWifi = null;
	private String mSelectedUri = null;
	private HabraEntry mSelectedEntry = null;
	private List<HabraEntry> mLastEntries = new ArrayList<HabraEntry>();
	private List<String> mHistory = new ArrayList<String>();
	
	private LoaderData mPostListLoader = new LoaderData() {
		public void finish(String data) { 
			mHistory.add(url);
			finishLoading("Хабрахабр - " + Uri.parse(url).getPath().replace('/', '@'), data); 
		}
		public String update(String pageData) {
			mLastEntries.clear();
			
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
			
			while((topic = parser.parse()) != null) {
				mLastEntries.add(topic);
				data += topic.getDataAsHTML(hideContent, hideTags, hideMark, 
						hideDate, hideFavs, hideAuthor, hideComments);
			}
			
			return data;
		}
		public void start() { 
			startLoading(); 
		}
	};
	
	private LoaderData mPostLoader = new LoaderData() {
		public void finish(String data) { 
			mHistory.add(url);
			finishLoading(((HabraTopic)mLastEntries.get(0)).title, data); 
		}
		public String update(String pageData) {
			mLastEntries.clear();
			
			HabraTopicParser parser = new HabraTopicParser(pageData);
			HabraTopic topic = parser.parse();//XXX parser.parseFullTopic();
			
			if(topic == null) return "";
			
			String data = topic.getDataAsHTML();
			data += "<div id=\"comments\">";
			
			mLastEntries.add(topic);
			
			HabraCommentParser commentParser = new HabraCommentParser(pageData);
			HabraComment comment = null;
			
			while((comment = commentParser.parseComment()) != null) {
				mLastEntries.add(comment);
				data += comment.getDataAsHTML();
			}
			data += "</div>";
			return data;
		}
		public void start() { 
			startLoading(); 
		}
	};
	
	private LoaderData mQuestListLoader = new LoaderData() {
		public void finish(String data) { 
			mHistory.add(url);
			finishLoading("Хабрахабр QA - " + Uri.parse(url).getPath().replace('/', '@'), data); 
		}
		public String update(String pageData) {
			mLastEntries.clear();
			
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
			
			while((quest = parser.parse()) != null) {
				mLastEntries.add(quest);
				data += quest.getDataAsHTML(hideContent, hideTags, hideMark, 
						hideAnswers, hideDate, hideFavs, hideAuthor);
			}
			return data;
		}
		public void start() { 
			startLoading(); 
		}
	};
	
	private LoaderData mQuestLoader = new LoaderData() {
		public void finish(String data) { 
			mHistory.add(url);
			finishLoading(((HabraQuest)mLastEntries.get(0)).title, data); 
		}
		public String update(String pageData) {
			mLastEntries.clear();
			
			HabraQuestParser parser = new HabraQuestParser(pageData);
			HabraQuest quest = parser.parse();
			
			if(quest == null) return "";
			
			String data = quest.getDataAsHTML();
			data += quest.getCommentsAsHTML();
			data += "<div id=\"comments\">";
			mLastEntries.add(quest);
			
			HabraAnswerParser answerParser = new HabraAnswerParser(pageData);
			HabraAnswer answer = null;
			
			while((answer = answerParser.parseAnswer()) != null) {
				mLastEntries.add(answer);
				data += answer.getDataAsHTML();
				data += answer.getCommentsAsHTML();
			}
			data += "</div>";
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
		
		Log.d("onCreate", "Settings");
		mResultView.getSettings().setAllowFileAccess(true);
		mResultView.getSettings().setJavaScriptEnabled(true);
		mResultView.getSettings().setUserAgentString(URLClient.USER_AGENT);
		mResultView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Uri.parse(url).getHost().equals("habrahabr.ru") 
						|| Uri.parse(url).getHost().equals("m.habrahabr.ru")) {
					startActivityForResult(new Intent(getBaseContext(), 
							HabraView.class).setData(Uri.parse(url)), 0);
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
		
		mResultView.getSettings().setBuiltInZoomControls(
				mPreferences.getBoolean("prefEnableZoom", true));
		mResultView.getSettings().setPluginsEnabled(
				mPreferences.getBoolean("prefEnableFlash", false));
		
		updateUserBar();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_CANCELED) exit(resultCode);
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
					HabraView.class).setData(Uri.parse(mSelectedUri + "#comments")), 0);
			return true;
		}
		case R.id.menu_author: {
			startActivityForResult(new Intent(getBaseContext(), 
					HabraView.class).setData(Uri.parse("http://" 
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
	
	/**
	 * ������������ ���� �� ����� ������������ � ���� ����
	 * @param v
	 */
	public void onClickUserName(View v) {
		
	}
	
	/**
	 * ������������ ���� �� ������ "���������" � ���� ����
	 * @param v
	 */
	public void onClickFavorites(View v) {
		
	}
	
	/**
	 * ������������ ���� �� ������ "������ �����" � ���� ����
	 * @param v
	 */
	public void onClickPM(View v) {
		
	}
	
	/**
	 * ������������ ����� �� ������� ���������
	 * @param v
	 */
	public void onClickNav(View v) {
		switch(v.getId()) {
		case R.id.buttonNavPost:
			loadPostsList("http://habrahabr.ru/", false);
			break;
		case R.id.buttonNavQA:
			loadQuestsList("http://habrahabr.ru/qa/", false);
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
	
	private void exit(int result) {
		setResult(result);
		finish();
	}
	
	/**
	 * ��������� ���������� � ���� ����
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
		mResultView.setVisibility(View.INVISIBLE);
		findViewById(R.id.procLoading).setVisibility(View.VISIBLE);
	}
	
	private void finishLoading(String title, String data) {	
		data = mPreferences.getBoolean("prefEnableFlash", false) ? data 
				: RemoveNode.removeVideo(data);
		data = mPreferences.getString("prefLoadImages", "2").equals("1") 
				|| (mPreferences.getString("prefLoadImages", "2").equals("2") 
				&& mWifi.getConnectionInfo().getNetworkId() != -1) ? data 
						: RemoveNode.removeImage(data);
		
		mResultView.setVisibility(View.VISIBLE);
		
		data = "<html>\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" 
			+ "<link href=\"general.css\" rel=\"stylesheet\"/>\n<title>" + title 
			+ "</title>\n</head>\n<body>" + data + "</body></html>";
		
		if(title.length() == 0)
		{
			mResultView.loadDataWithBaseURL("file:///android_asset/", data, 
					"text/html", "utf-8", null);
			return;
		}
		
		File cacheFile = new File(getCacheDir(), title.replaceAll("[.?!;]+( .*|.?)", "") + ".html");
		try {
			FileOutputStream outStream = new FileOutputStream(cacheFile);
			outStream.write(data.getBytes());
			outStream.close();
			
			mResultView.loadUrl("file://" + URLClient.encode(cacheFile.getAbsolutePath()));
		} catch (IOException e) {
			Log.w("Habrahabr.finishLoading", "IOException: " + e.getMessage());
			
			mResultView.loadDataWithBaseURL("file:///android_asset/", data, 
					"text/html", "utf-8", null);
		}
		
		findViewById(R.id.procLoading).setVisibility(View.GONE);
	}
	
	private void loadPostsList(String url, boolean noExecute) {
		AsyncDataLoader.getDataLoader().execute(mPostListLoader.setUrl(url), noExecute);
	}
	
	private void loadFullPost(String url, boolean noExecute) {
		AsyncDataLoader.getDataLoader().execute(mPostLoader.setUrl(url), noExecute);
	}
	
	private void loadQuestsList(String url, boolean noExecute) {
		AsyncDataLoader.getDataLoader().execute(mQuestListLoader.setUrl(url), noExecute);
	}
	
	private void loadFullQuest(String url, boolean noExecute) {
		AsyncDataLoader.getDataLoader().execute(mQuestLoader.setUrl(url), noExecute);
	}
	
	private void loadData(Uri url) {
		loadData(url, false);
	}
	
	private void loadData(Uri url, boolean noExecute) {
		Log.d("onCreate", "Load data"); 
		
		if(url != null) {
			String[] path = url.getPathSegments().toArray(new String[0]);
			if(path.length == 0 || path[0].equals("new")) {
				Log.d("Habrahabr.loadData", "CODE: path.length == 0 || path[0].equals(\"new\")");
				Log.d("Habrahabr.loadData", "PATH: " + url.toString());
				loadPostsList(url.toString(), noExecute);
			} else if((path.length == 1 && path[0].equals("qa")) 
					|| (path.length == 2 && path[1].equals("new"))) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\") || " 
						+ "(path.length == 2 && path[1].equals(\"new\"))");
				Log.d("Habrahabr.loadData", "PATH: " + url.toString());
				loadQuestsList(url.toString(), noExecute);
			} else if(path[0].equals("qa")) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"qa\")");
				Log.d("Habrahabr.loadData", "PATH: " + url.toString());
				loadFullQuest(url.toString(), noExecute);
			} else if(path[0].equals("post") || (path[0].equals("blogs") && path.length == 3)
					|| path[0].equals("linker") || (path[0].equals("company") || path.length == 4)) {
				Log.d("Habrahabr.loadData", "CODE: path[0].equals(\"post\") || path[0].equals(\"blogs\")...");
				Log.d("Habrahabr.loadData", "PATH: " + url.toString());
				String strUrl = "http://habrahabr.ru/post/";
				
				if(path.length == 3) strUrl += path[2]; // blogs/.../{ID} OR linker/go/{ID}
				else if(path.length == 4) strUrl += path[3]; // company/blog/.../{ID}
				else strUrl += path[1]; // post/{ID}
				
				strUrl += "/";
				if(url.getQuery() != null) strUrl += "?" + url.getQuery();
				if(url.getFragment() != null) strUrl += "#" + url.getFragment();
				loadFullPost(strUrl, noExecute);
			} else {
				finishLoading("", "Unknown");
			}
		} else {
			Log.i("pref", mPreferences.getString("prefMainScreenContent", "000"));
			switch(Integer.valueOf(mPreferences.getString("prefMainScreenContent", "2"))) {
			case 1: // LENTA
				if(!HabraLogin.getHabraLogin().isLogged())
					finishLoading("", "����� ����� ������ ������ ���������������� ������������ �����.");
				else loadPostsList("http://habrahabr.ru/?fl=hl", noExecute);
				break;
			case 2: // ALL
				loadPostsList("http://habrahabr.ru/?fl=all", noExecute);
				break;
			case 3: // NEW ALL
				loadPostsList("http://habrahabr.ru/new/?fl=all", noExecute);
				break;
			case 4: // Q&A
				loadQuestsList("http://habrahabr.ru/qa/", noExecute);
				break;
			case 5:
				finishLoading("", "Coming soon...");
				break;
			default:
				finishLoading("", "Unknown page");
					break;
				}
			}
		}
	}