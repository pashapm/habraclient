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
import android.text.ClipboardManager;
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
import ru.client.habr.Dialogs.OnClickMenuItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

/**
 * @author WNeZRoS
 */
public class ActivityView extends Activity {	
	private WebView mResultView = null;
	private SharedPreferences mPreferences = null;
	private WifiManager mWifi = null;
	private String mLastEntryTitle = null;
	private Uri mSelectedUri = null;
	private static boolean userBarWasUpdated = false;
	
	private LoaderData mAnyDataLoader = new LoaderData() {
		public void finish(String data) {
			switch(pageType) {
			case POST_LIST: finishLoading("Хабрахабр - " + Uri.parse(url).getPath().replace('/', '@'), data); break;
			case POST: finishLoading(mLastEntryTitle, data); break;
			case QUEST_LIST: finishLoading("Хабрахабр QA - " + Uri.parse(url).getPath().replace('/', '@'), data); break; 
			case QUEST: finishLoading(mLastEntryTitle, data); break;
			case USER: finishLoading(mLastEntryTitle, data); break;
			default: finishLoading(url, data); break;
			}
		}
		public String update(String pageData) {
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
				
				HabraCommentParser commentParser = new HabraCommentParser(pageData);
				HabraComment comment = null;
				
				while((comment = commentParser.parse(topic.id)) != null) {
					data += comment.getDataAsHTML();
				}
				data += "</div>";
				
				mLastEntryTitle = topic.title;
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
				
				HabraAnswerParser answerParser = new HabraAnswerParser(pageData);
				HabraAnswer answer = null;
				
				while((answer = answerParser.parse()) != null) {
					data += answer.getDataAsHTML();
					data += answer.getCommentsAsHTML();
				}
				data += "</div>";
				
				mLastEntryTitle = quest.title;
			} break;
			case USER: {
				HabraUser user = HabraUser.parse(pageData);
				mLastEntryTitle = user.username;
				
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
					mSelectedUri = Uri.parse(wv.getHitTestResult().getExtra());
					
					if(!mSelectedUri.getHost().contains("habrahabr.ru")) 
						return false;
					
					switch(AsyncDataLoader.getPageTypeByURI(mSelectedUri)) {
					case POST:
					case QUEST: {
						String temp = mSelectedUri.getQueryParameter("infavs");
						
						final boolean inFavs = temp != null && temp.equals("true");
						final String author = mSelectedUri.getQueryParameter("author");
						final int id = Integer.valueOf(mSelectedUri.getLastPathSegment());
						
						final String menuItems[] = getResources().getStringArray(R.array.post_menu);
						if(inFavs) menuItems[menuItems.length - 2] = getString(R.string.remove_favorites);
						
						Log.i("mSelectedUri", mSelectedUri.toString());
						
						mSelectedUri = Uri.parse(mSelectedUri.getScheme() + "://" 
								+ mSelectedUri.getHost() + mSelectedUri.getPath());
						
						Log.i("mSelectedUri", mSelectedUri.toString());
						Log.i("uri", author + " " + id + " " + inFavs);
						
						Dialogs.getDialogs().showDialogMenu(getString(R.string.menu), menuItems, new OnClickMenuItem() {
							@Override
							public void onClick(int item, String itemText) {
								onMenuItemSelected(item, author, id, inFavs);
							}
						});
					} break;
					default: {
						final String menuItems[] = getResources().getStringArray(R.array.default_menu);
						
						Dialogs.getDialogs().showDialogMenu(getString(R.string.menu), menuItems, new OnClickMenuItem() {
							@Override
							public void onClick(int item, String itemText) {
								onMenuItemSelected(item, null, 0, false);
							}
						});
					}
					}
					
					return true;
				case WebView.HitTestResult.IMAGE_TYPE:
					Log.d("onLongClick", "Click on <img src='" + wv.getHitTestResult().getExtra() + "'...");
					return true;
				}
				return false;
			}
		});
		
		mResultView.addJavascriptInterface(new JSInterface(mResultView), "js");
		
		mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		findViewById(R.id.scrollNavPanel).setVisibility(View.GONE);
		
		AsyncDataLoader.getDataLoader().setLoaderData(mAnyDataLoader);
		loadData(getIntent().getData());
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
		
		Dialogs.getDialogs().setContext(this);
		AsyncDataLoader.getDataLoader().setLoaderData(mAnyDataLoader);
		
		mResultView.getSettings().setBuiltInZoomControls(
				mPreferences.getBoolean("prefEnableZoom", true));
		mResultView.getSettings().setPluginsEnabled(
				mPreferences.getBoolean("prefEnableFlash", false));
		
		mResultView.setInitialScale(Integer.valueOf(mPreferences.getString("prefDefaultScale", "150")));
		
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
	
	public boolean onMenuItemSelected(int item, String entryAuthor, int entryID, boolean entryInFavs) {	
		switch(item) {
		case 0:
			Intent openIntent = new Intent(Intent.ACTION_VIEW);
			startActivity(openIntent);
			return true;
		case 1:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, mSelectedUri);
			startActivity(Intent.createChooser(sendIntent, null));
			return true;
		case 2:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			clipboard.setText(mSelectedUri.toString());
			return true;
		case 3: {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse(mSelectedUri + "#comments")), 0);
			return true;
		}
		case 4:
			JSInterface.onClickRating(entryID, (mSelectedUri.getPathSegments().get(0).equals("qa") ? "q" : "p"), 0);
			return true;
		case 5:
			HabraEntry.changeFavorites(entryID, 
					(mSelectedUri.getPathSegments().get(0).equals("qa") ? 
							HabraEntry.HabraEntryType.QUESTION : HabraEntry.HabraEntryType.POST), entryInFavs);
			return true;
		case 6: {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse("http://" 
							+ entryAuthor + ".habrahabr.ru/")), 0);
			return true;
		}
		default:
			return false;
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
		case R.id.menu_nav:
			findViewById(R.id.scrollNavPanel).setVisibility(
					findViewById(R.id.scrollNavPanel).getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
			return true;
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
			mResultView.loadDataWithBaseURL(getCacheDir().getAbsolutePath(), data, 
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
			
			Dialogs.getDialogs().showToast(R.string.not_cache, Toast.LENGTH_LONG);
			
			mResultView.loadDataWithBaseURL(getCacheDir().getAbsolutePath(), data, 
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