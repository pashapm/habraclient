package ru.client.habr;

import ru.client.habr.Dialogs.OnClickMenuItem;
import ru.client.habr.Dialogs.OnClickMessage;
import ru.client.habr.HabraEntry.HabraEntryType;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.webkit.WebView;

/**
 * @author WNeZRoS
 * 
 */
public class JSInterface {
	private Activity mParent = null;
	private WebView mJsForView = null;

	public JSInterface(Activity parent, WebView v) {
		mParent = parent;
		mJsForView = v;
	}
	
	public void addComment(int postID, int commentID) {
		Intent intent = new Intent(mParent, ActivityCommentEditor.class);
		intent.putExtra("post", postID);
		intent.putExtra("parent", commentID);
		mParent.startActivityForResult(intent, ActivityView.REQUEST_NEW_COMMENT);
	}
	
	public void onClickComment(final int commentID, final int postID, final String author, final boolean inFavs) {
		Log.d("js", "comment");
		
		String items[] = mParent.getResources().getStringArray(R.array.comment_menu);
		if(inFavs) items[items.length - 2] = mParent.getString(R.string.remove_favorites);
		
		Log.d("js", "comment 2");
		
		Dialogs.showDialogMenu(mParent.getString(R.string.comment), items, new OnClickMenuItem() {
			@Override
			public void onClick(int item, String itemText) {
				Uri selectedUri = Uri.parse("http://habrahabr.ru/post/" 
						+ postID + "/#comment_" + commentID);
				
				switch(item) {
				case 0:
					Intent openIntent = new Intent(Intent.ACTION_VIEW);
					openIntent.setData(selectedUri);
					mParent.startActivity(openIntent);
					return;
				case 1:
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.setType("text/plain");
					sendIntent.putExtra(Intent.EXTRA_TEXT, selectedUri.toString());
					mParent.startActivity(Intent.createChooser(sendIntent, null));
					return;
				case 2:
					ClipboardManager clipboard = (ClipboardManager) 
							mParent.getSystemService(Activity.CLIPBOARD_SERVICE); 
					clipboard.setText(selectedUri.toString());
					return;
				case 3: 
					addComment(postID, commentID);
					return;
				case 4:
					onClickRating(commentID, "c", postID);
					return;
				case 5:
					HabraEntry.changeFavorites(commentID, HabraEntry.HabraEntryType.COMMENT, inFavs);
					return;
				case 6: {
					mParent.startActivityForResult(new Intent(mParent.getBaseContext(), 
							ActivityView.class).setData(Uri.parse("http://" 
									+ author + ".habrahabr.ru/")), 0);
					return;
				}
				}
			}
		});
	}
	
	public void onClickKarma(final String username, final int userID) {
		Dialogs.showDialogMessage("Вы хотите влепить ", "-", null, "+", new OnClickMessage() {
			@Override
			public void onClick(int rel) {
				HabraUser.karmaUpdate(userID, username, rel);
			}
		});
	}
	
	public void onClickRating(final int id, final String type, final int postID) {
		Dialogs.showDialogMessage("Вы голосуете за ", "-1", "0", "+1", new OnClickMessage() {
			@Override
			public void onClick(int rel) {
				HabraEntryType entryType = HabraEntryType.UNKNOWN;
				switch(type.charAt(0)) {
				case 'p': entryType = HabraEntryType.POST; break;
				case 'c': entryType = HabraEntryType.COMMENT; break;
				case 'q': entryType = HabraEntryType.QUESTION; break;
				case 'a': entryType = HabraEntryType.ANSWER; break;
				}
				HabraEntry.vote(id, entryType, rel, postID);
			}
		});
	}
	
	public void logInfo(String info) {
		Log.i("JS", info);
	}
	
	public void pollVote(int postID, String action, int variants[]) {
		for(int i = 0; i < variants.length; i++) Log.d("var", i + ": " + variants[i]);
		
		if(action.equals("poll")) {
			action = "vote";
		} else {
			action = "pass";
		}
		
		HabraTopic.poll(postID, action, variants, new HabraTopic.OnPollResultListener() {
			@Override
			public void onFinish(String result) {
				if(result.indexOf("<message>ok</message>") != -1) {
					// TODO on ok
					mJsForView.loadUrl("javascript: pollForm.updateData('" + URLClient.encode(result) + "');");
				} else {
					// TODO on error
				}
			}
		}); 
	}
}
