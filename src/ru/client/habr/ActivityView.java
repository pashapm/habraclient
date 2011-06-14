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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.KeyEvent;
import ru.client.habr.R;
import ru.client.habr.AsyncDataLoader.LoaderData;
import ru.client.habr.AsyncDataLoader.PageType;
import ru.client.habr.Dialogs.OnClickMenuItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

/**
 * @author WNeZRoS
 */

/*
 * TODO Show navigation when press MENU and hide with options menu
 * TODO Make light border for navigations background
 */

public class ActivityView extends Activity {	
	public final static int REQUEST_NEW_VIEW = 1;
	public final static int REQUEST_NEW_COMMENT = 3;
	
	private WebView mResultView = null;
	private SharedPreferences mPreferences = null;
	private WifiManager mWifi = null;
	private String mLastEntryTitle = null;
	private Uri mSelectedUri = null;
	private Uri mLastLoadedUri = null;
	private static boolean userBarWasUpdated = false;
	private ToggleButton mMainMenuButtons[] = null;
	private ToggleButton mFlatMenuButtons[] = null;
	private Button mTypeMenuButtons[] = null;
	private boolean mNoEntries = false;
	private boolean mLoadImages = false;
	
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
			mNoEntries = false;
			
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
					mNoEntries = true;
				} else {
					data += getNavLinksForUri(Uri.parse(url));
				}
			} break;
			case POST: {
				HabraTopicParser parser = new HabraTopicParser(pageData);
				HabraTopic topic = parser.parse();
				
				if(topic == null) {
					mNoEntries = true;
					return pageData;
				}
				
				data = topic.getDataAsHTML();
				data += "<div id=\"comments\">";
				
				HabraCommentParser commentParser = new HabraCommentParser(pageData);
				HabraComment comment = null;
				
				while((comment = commentParser.parse(topic.id)) != null) {
					data += comment.getDataAsHTML();
				}
				data += "</div>";
				data += "<h2><a onClick=\"js.addComment(" + topic.id + ", 0, 'c');\">"
						+ getString(R.string.commenting) + "</a></h2>";
				
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
					mNoEntries = true;
				} else {
					data += getNavLinksForUri(Uri.parse(url));
				}
			} break;
			case QUEST: {
				HabraQuestParser parser = new HabraQuestParser(pageData);
				HabraQuest quest = parser.parse();
				
				if(quest == null) {
					mNoEntries = true;
					return pageData;
				}
				
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
				data += "<h2><a onClick=\"js.addComment(" + quest.id + ", 0, 'a');\">"
				+ getString(R.string.commenting) + "</a></h2>";
				
				mLastEntryTitle = quest.title;
			} break;
			case USER: {
				HabraUser user = HabraUser.parse(pageData);
				mLastEntryTitle = user.username;
				
				if(user == null) {
					mNoEntries = true;
					return pageData;
				}
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
	
	private static JSInterface sJSInterface;
	
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
		
		mMainMenuButtons = new ToggleButton[5];
		mMainMenuButtons[0] = (ToggleButton) findViewById(R.id.buttonNavPost);
		mMainMenuButtons[1] = (ToggleButton) findViewById(R.id.buttonNavQA);
		mMainMenuButtons[2] = (ToggleButton) findViewById(R.id.buttonNavBlog);
		mMainMenuButtons[3] = (ToggleButton) findViewById(R.id.buttonNavPeople);
		mMainMenuButtons[4] = (ToggleButton) findViewById(R.id.buttonNavCompany);
		
		mFlatMenuButtons = new ToggleButton[4];
		mFlatMenuButtons[0] = (ToggleButton) findViewById(R.id.buttonNavSection1);
		mFlatMenuButtons[1] = (ToggleButton) findViewById(R.id.buttonNavSection2);
		mFlatMenuButtons[2] = (ToggleButton) findViewById(R.id.buttonNavSection3);
		mFlatMenuButtons[3] = (ToggleButton) findViewById(R.id.buttonNavSection4);
		
		mTypeMenuButtons = new Button[3];
		mTypeMenuButtons[0] = (Button) findViewById(R.id.buttonNavHabred);
		mTypeMenuButtons[1] = (Button) findViewById(R.id.buttonNavNew);
		mTypeMenuButtons[2] = (Button) findViewById(R.id.buttonNavUnhabred);
		
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
							ActivityView.class).setData(Uri.parse(url)), REQUEST_NEW_VIEW);
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
					
					final PageType type = AsyncDataLoader.getPageTypeByURI(mSelectedUri);
					switch(type) {
					case POST:
					case QUEST: {
						String temp = mSelectedUri.getQueryParameter("infavs");
						
						final boolean inFavs = temp != null && temp.equals("true");
						final String author = mSelectedUri.getQueryParameter("author");
						final int id = Integer.valueOf(mSelectedUri.getLastPathSegment());
						
						final String menuItems[] = getResources().getStringArray(
								type == PageType.QUEST ? R.array.quest_menu : R.array.post_menu);
						if(inFavs) menuItems[menuItems.length - 2] = getString(R.string.remove_favorites);
						
						mSelectedUri = Uri.parse(mSelectedUri.getScheme() + "://" 
								+ mSelectedUri.getHost() + mSelectedUri.getPath());

						Dialogs.showDialogMenu(getString(R.string.menu), menuItems, new OnClickMenuItem() {
							@Override
							public void onClick(int item, String itemText) {
								onMenuItemSelected(item, type == PageType.QUEST ? 
										R.array.quest_menu : R.array.post_menu, author, id, inFavs);
							}
						});
					} break;
					default: {
						final String menuItems[] = getResources().getStringArray(R.array.default_menu);
						
						Dialogs.showDialogMenu(getString(R.string.menu), menuItems, new OnClickMenuItem() {
							@Override
							public void onClick(int item, String itemText) {
								onMenuItemSelected(item, R.array.default_menu, null, 0, false);
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
		
		sJSInterface = new JSInterface(this, mResultView);
		mResultView.addJavascriptInterface(sJSInterface, "js");
		
		mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		showNavPanels(false);
		
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
		
		Dialogs.setContext(this);
		AsyncDataLoader.getDataLoader().setLoaderData(mAnyDataLoader);
		
		mResultView.getSettings().setBuiltInZoomControls(
				mPreferences.getBoolean("prefEnableZoom", true));
		mResultView.getSettings().setPluginsEnabled(
				mPreferences.getBoolean("prefEnableFlash", false));
		
		mLoadImages = mPreferences.getString("prefLoadImages", "2").equals("1") 
		|| (mPreferences.getString("prefLoadImages", "2").equals("2") 
		&& mWifi.getConnectionInfo().getNetworkId() != -1);
		
		mResultView.getSettings().setLoadsImagesAutomatically(mLoadImages);
		
		mResultView.setInitialScale(Integer.valueOf(mPreferences.getString("prefDefaultScale", "150")));
		
		if(!mPreferences.getBoolean("prefUserBarNotUpdate", false) 
				|| !ActivityView.userBarWasUpdated) updateUserBar();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ActivityMain.onActivityResult", "Result to " + requestCode + " is " + resultCode);
		
		switch(requestCode) {
		case REQUEST_NEW_VIEW:
			if(resultCode == RESULT_CANCELED) exit(resultCode);
			else AsyncDataLoader.getDataLoader().removeLastRequestFromHistory();
			break;
		case REQUEST_NEW_COMMENT:
			if(resultCode == RESULT_OK) {
				// TODO if ok
			} else {
				// TODO if fail
			}
			break;
		}
	}
	
	public void onBackPressed() {
		exit(RESULT_OK);
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		mAnyDataLoader = null;
		AsyncDataLoader.getDataLoader().cancel(true);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i("ActivityView.onKeyDown", "keyCode is " + keyCode);

		switch(keyCode) {
		/*case KeyEvent.KEYCODE_MENU:
			showNavPanels();
			return false;*/
		case KeyEvent.KEYCODE_BACK:
			if(hideNavPanels()) return true;
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onMenuItemSelected(int item, int menu_id, String entryAuthor, int entryID, boolean entryInFavs) {	
		switch(item) {
		case 0:
			Intent openIntent = new Intent(Intent.ACTION_VIEW);
			openIntent.setData(mSelectedUri);
			startActivity(openIntent);
			return true;
		case 1:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, mSelectedUri.toString());
			startActivity(Intent.createChooser(sendIntent, null));
			return true;
		case 2:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
			clipboard.setText(mSelectedUri.toString());
			return true;
		case 3: 
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse(mSelectedUri + "#comments")), REQUEST_NEW_VIEW);
			return true;
		case 4:
			sJSInterface.onClickRating(entryID, 
					(menu_id == R.array.quest_menu ? "q" : "p"), 0);
			return true;
		case 5:
			HabraEntry.changeFavorites(entryID, 
					(menu_id == R.array.quest_menu ? 
							HabraEntry.HabraEntryType.QUESTION : HabraEntry.HabraEntryType.POST), entryInFavs);
			return true;
		case 6: {
			startActivityForResult(new Intent(getBaseContext(), 
					ActivityView.class).setData(Uri.parse("http://" 
							+ entryAuthor + ".habrahabr.ru/")), REQUEST_NEW_VIEW);
			return true;
		}
		default:
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_nav:
			showNavPanels();
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
			loadData(Uri.parse("http://habrahabr.ru/?hl=all"));
			break;
		case R.id.buttonNavQA:
			loadData(Uri.parse("http://habrahabr.ru/qa/"));
			break;
		case R.id.buttonNavPeople:
			mLastLoadedUri = Uri.parse("http://habrahabr.ru/people/");
			mResultView.loadData("Coming soon...", "text/html", "utf-8");
			break;
		case R.id.buttonNavBlog:
			mLastLoadedUri = Uri.parse("http://habrahabr.ru/bloglist/");
			mResultView.loadData("Coming soon...", "text/html", "utf-8");
			break;
		case R.id.buttonNavCompany:
			mLastLoadedUri = Uri.parse("http://habrahabr.ru/companies/");
			mResultView.loadData("Coming soon...", "text/html", "utf-8");
			break;
		case R.id.buttonNavSection1:
			if(mMainMenuButtons[0].isChecked()) {
				loadData(Uri.parse("http://habrahabr.ru/?fl=hl"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/"));
			}
			break;
		case R.id.buttonNavSection2:
			if(mMainMenuButtons[0].isChecked()) {
				loadData(Uri.parse("http://habrahabr.ru/?fl=all"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/hot/"));
			}
			break;
		case R.id.buttonNavSection3:
			if(mMainMenuButtons[0].isChecked()) {
				loadData(Uri.parse("http://habrahabr.ru/?fl=blogs"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/popular/"));
			}
			break;
		case R.id.buttonNavSection4:
			if(mMainMenuButtons[0].isChecked()) {
				loadData(Uri.parse("http://habrahabr.ru/?fl=corporative"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/unanswered/"));
			}
			break;
		case R.id.buttonNavHabred:
			if(mMainMenuButtons[0].isChecked()) {
				if(mFlatMenuButtons[0].isChecked()) loadData(Uri.parse("http://habrahabr.ru/?fl=hl"));
				else if(mFlatMenuButtons[1].isChecked()) loadData(Uri.parse("http://habrahabr.ru/?fl=all"));
				else if(mFlatMenuButtons[2].isChecked()) loadData(Uri.parse("http://habrahabr.ru/?fl=blogs"));
				else if(mFlatMenuButtons[3].isChecked()) loadData(Uri.parse("http://habrahabr.ru/?fl=corporative"));
				else loadData(Uri.parse("http://habrahabr.ru/blogs/" + mLastLoadedUri.getPathSegments().get(1) + "/"));
			} else {
				if(mFlatMenuButtons[0].isChecked()) loadData(Uri.parse("http://habrahabr.ru/qa/"));
				else if(mFlatMenuButtons[1].isChecked()) loadData(Uri.parse("http://habrahabr.ru/qa/hot/"));
				else if(mFlatMenuButtons[2].isChecked()) loadData(Uri.parse("http://habrahabr.ru/qa/popular/"));
				else if(mFlatMenuButtons[3].isChecked()) loadData(Uri.parse("http://habrahabr.ru/qa/unanswered/"));
			}
			break;
		case R.id.buttonNavNew:
			if(mMainMenuButtons[0].isChecked()) {
				if(mFlatMenuButtons[0].isChecked()) loadData(Uri.parse("http://habrahabr.ru/new/?fl=hl"));
				else if(mFlatMenuButtons[1].isChecked()) loadData(Uri.parse("http://habrahabr.ru/new/?fl=all"));
				else if(mFlatMenuButtons[2].isChecked()) loadData(Uri.parse("http://habrahabr.ru/new/?fl=blogs"));
				else if(mFlatMenuButtons[3].isChecked()) loadData(Uri.parse("http://habrahabr.ru/new/?fl=corporative"));
				else loadData(Uri.parse("http://habrahabr.ru/blogs/" + mLastLoadedUri.getPathSegments().get(1) + "/new/"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/new/"));
			}
			break;
		case R.id.buttonNavUnhabred:
			if(mMainMenuButtons[0].isChecked()) {
				if(mFlatMenuButtons[0].isChecked()) loadData(Uri.parse("http://habrahabr.ru/unhabred/?fl=hl"));
				else if(mFlatMenuButtons[1].isChecked()) loadData(Uri.parse("http://habrahabr.ru/unhabred/?fl=all"));
				else if(mFlatMenuButtons[2].isChecked()) loadData(Uri.parse("http://habrahabr.ru/unhabred/?fl=blogs"));
				else if(mFlatMenuButtons[3].isChecked()) loadData(Uri.parse("http://habrahabr.ru/unhabred/?fl=corporative"));
				else loadData(Uri.parse("http://habrahabr.ru/blogs/" + mLastLoadedUri.getPathSegments().get(1) + "/unhabred/"));
			} else {
				loadData(Uri.parse("http://habrahabr.ru/qa/unhabred/"));
			}
			break;
		default: 
			mResultView.loadData("WTF o_O", "text/html", "utf-8");
			break;
		}
		showNavPanels(false);
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
	
	private void showNavPanels(boolean visibility) {
		findViewById(R.id.layoutAllNavPanels).setVisibility(visibility ? View.VISIBLE : View.GONE);
		
		if(visibility) {
			Uri lastLoadedUri = Uri.parse(mLastLoadedUri.toString().replaceAll("/page[0-9]+/", "/"));
			List<String> pathSegments = lastLoadedUri.getPathSegments();
			
			if(pathSegments.size() == 0 || pathSegments.get(0).equals("new") || pathSegments.get(0).equals("unhabred")) {
				setActiveMainMenuButton(0);
				
				String fl = lastLoadedUri.getQueryParameter("fl");
				if(fl == null) setActiveFlatMenuButton(1);
				else if(fl.equals("hl")) setActiveFlatMenuButton(0);
				else if(fl.equals("blogs")) setActiveFlatMenuButton(2);
				else if(fl.equals("corporative")) setActiveFlatMenuButton(3);
				else setActiveFlatMenuButton(1);
				
				setFlatMenuText(false);
				
				if(pathSegments.size() == 0) setActiveSubMenuButton(0);
				else setActiveSubMenuButton(pathSegments.get(0).equals("new") ? 1 
						: pathSegments.get(0).equals("unhabred") ? 2 : 0);
			} else if(pathSegments.get(0).equals("qa")) {
				setActiveMainMenuButton(1);
				
				if(pathSegments.size() == 1) {
					setActiveFlatMenuButton(0);
				} else {
					if(pathSegments.get(1).equals("hot")) setActiveFlatMenuButton(1);
					else if(pathSegments.get(1).equals("popular")) setActiveFlatMenuButton(2);
					else if(pathSegments.get(1).equals("unanswered")) setActiveFlatMenuButton(3);
					else setActiveFlatMenuButton(0);
				}
				
				setFlatMenuText(true);
				
				if(pathSegments.size() == 1) setActiveSubMenuButton(0);
				else setActiveSubMenuButton(pathSegments.get(1).equals("new") ? 1 
						: pathSegments.get(1).equals("unhabred") ? 2 : -1);
			} else if(pathSegments.get(0).equals("blogs")) {
				setActiveMainMenuButton(0);
				setActiveFlatMenuButton(-1);
				if(pathSegments.size() == 2) setActiveSubMenuButton(0);
				else setActiveSubMenuButton(pathSegments.get(2).equals("new") ? 1 
							: pathSegments.get(2).equals("unhabred") ? 2 : 0);
			} else if(pathSegments.get(0).equals("bloglist")) {
				setActiveMainMenuButton(2);
				setActiveFlatMenuButton(-1);
				setActiveSubMenuButton(-1);
			} else if(pathSegments.get(0).equals("people")) {
				setActiveMainMenuButton(3);
				setActiveFlatMenuButton(-1);
				setActiveSubMenuButton(-1);
			} else if(pathSegments.get(0).equals("companies")) {
				setActiveMainMenuButton(4);
				setActiveFlatMenuButton(-1);
				setActiveSubMenuButton(-1);
			}
		}
	}
	
	private void setActiveMainMenuButton(int button) {
		for(int i = 0; i < mMainMenuButtons.length; i++) {
			if(i == button) mMainMenuButtons[i].setChecked(true);
			else mMainMenuButtons[i].setChecked(false);
		}
	}
	
	private void setActiveFlatMenuButton(int button) {
		if(button == -1)
			findViewById(R.id.layoutNavPanel2).setVisibility(View.GONE);
		else findViewById(R.id.layoutNavPanel2).setVisibility(View.VISIBLE);
		
		for(int i = 0; i < mFlatMenuButtons.length; i++) {
			if(i == button) mFlatMenuButtons[i].setChecked(true);
			else mFlatMenuButtons[i].setChecked(false);
		}
	}
	
	private void setFlatMenuText(boolean qa) {
		if(qa) {
			mFlatMenuButtons[0].setText(R.string.inbox);
			mFlatMenuButtons[1].setText(R.string.hot);
			mFlatMenuButtons[2].setText(R.string.popular);
			mFlatMenuButtons[3].setText(R.string.no_answers);
		} else {
			mFlatMenuButtons[0].setText(R.string.lenta);
			mFlatMenuButtons[1].setText(R.string.all);
			mFlatMenuButtons[2].setText(R.string.tematic);
			mFlatMenuButtons[3].setText(R.string.corporative);
		}
	}
	
	private void setActiveSubMenuButton(int button) {
		if(button == -1)
			findViewById(R.id.layoutNavPanel3).setVisibility(View.GONE);
		else findViewById(R.id.layoutNavPanel3).setVisibility(View.VISIBLE);
		
		for(int i = 0; i < mTypeMenuButtons.length; i++) {
			if(i == button) mTypeMenuButtons[i].setTextColor(getResources().getColor(R.color.menu_button_active));
			else mTypeMenuButtons[i].setTextColor(getResources().getColor(R.color.menu_button));
		}
	}
	
	private void showNavPanels() {
		showNavPanels(findViewById(R.id.layoutAllNavPanels).getVisibility() == View.GONE);
	}
	
	private boolean hideNavPanels() {
		if(findViewById(R.id.layoutAllNavPanels).getVisibility() == View.VISIBLE) {
			showNavPanels(false);
			return true;
		}
		return false;
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
		
		// Удаляем видеоролики и подкасты со страницы если флеш выключен
		data = mPreferences.getBoolean("prefEnableFlash", false) 
		? RemoveNode.replaceAudioScriptToFlash(data) : RemoveNode.removeMultimedia(data);
		
		// Если не нужно загружать изображения вырезаем их 
		data = mLoadImages ? data : RemoveNode.removeImage(data);
		
		if(mNoEntries) {
			mResultView.loadDataWithBaseURL("http://habrahabr.ru/", data, 
					"text/html", "utf-8", null);
		}
		
		
		data = "<html>\n<head>\n" 
			+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" 
			+ "<link href=\"../files/general.css\" rel=\"stylesheet\"/>\n" 
			+ "<script type=\"text/javascript\" src=\"../files/general.js\"></script>\n"
			+ "<title>" + title + "</title>\n</head>\n<body>\n<div class=\"main-content\">\n" 
			+ data + "\n</div>\n</body>\n</html>";
		
		if(title.length() == 0 || !mPreferences.getBoolean("prefUseCache", true))
		{
			File cacheFile = new File(getCacheDir(), "tmp.htm");
			
			try {
				FileOutputStream outStream = new FileOutputStream(cacheFile);
				outStream.write(data.getBytes());
				outStream.close();
				
				mResultView.loadUrl("file://" + URLClient.encode(cacheFile.getAbsolutePath()) 
						+ "#" + mLastLoadedUri.getFragment());
			} catch (IOException e) {
				Log.w("ActivityView.finishLoading", "IOException: " + e.getMessage());
				
				mResultView.loadDataWithBaseURL("file://" + getCacheDir().getAbsolutePath(), data, 
						"text/html", "utf-8", null);
			}
			return;
		}
		
		File cacheFile = new File(getCacheDir(), title.replaceAll("[/|\\?]+", "!") + ".html");
		try {
			FileOutputStream outStream = new FileOutputStream(cacheFile);
			outStream.write(data.getBytes());
			outStream.close();
			
			mResultView.loadUrl("file://" + URLClient.encode(cacheFile.getAbsolutePath()) 
					+ "#" + mLastLoadedUri.getFragment());
		} catch (IOException e) {
			Log.w("ActivityView.finishLoading", "IOException: " + e.getMessage());
			
			Dialogs.showToast(R.string.not_cache);
			
			mResultView.loadDataWithBaseURL("file://" + getCacheDir().getAbsolutePath(), data, 
					"text/html", "utf-8", null);
		}
		
		findViewById(R.id.procLoading).setVisibility(View.GONE);
	}
	
	private void loadData(Uri uri) {
		Log.d("onCreate", "Load data"); 
		
		if(uri != null) {
			mLastLoadedUri = uri;
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
			
			mLastLoadedUri = Uri.parse(url);
			AsyncDataLoader.getDataLoader().execute(url);
		}
	}
}