/**
 * 
 */
package ru.client.habr;

import ru.client.habr.Dialogs.OnClickMessage;
import ru.client.habr.HabraEntry.HabraEntryType;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * @author WNeZRoS
 * 
 */
public class JSInterface {
	private WebView jsForView = null;

	public JSInterface(WebView v) {
		jsForView = v;
	}
	
	public void onClickComment(int commentID, int postID, String author, boolean inFavs) {
		Dialogs.getDialogs().showToast("onClickComment(" + commentID + ", " 
				+ postID + ", '" + author + "', " + inFavs + ");", Toast.LENGTH_LONG);
		// TODO comment, add in favs, vote
	}
	
	public void onClickKarma(final String username, final int userID) {
		Dialogs.getDialogs().showDialogMessage("Вы хотите влепить ", "-", null, "+", new OnClickMessage() {
			@Override
			public void onClick(int rel) {
				HabraUser.karmaUpdate(userID, username, rel);
			}
		});
	}
	
	public static void onClickRating(final int id, final String type, final int postID) {
		Dialogs.getDialogs().showDialogMessage("Вы голосуете за ", "-1", "0", "+1", new OnClickMessage() {
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
		
		HabraTopic.poll(postID, action, variants, new HabraTopic.OnPollResultListener() {
			@Override
			public void onFinish(String result) {
				if(result.indexOf("<message>ok</message>") != -1) {
					// TODO on ok
					jsForView.loadUrl("javascript: pollForm.updateData('" + result + "');");// TODO url encode result
				} else {
					// TODO on error
				}
			}
		}); 
	}
}
