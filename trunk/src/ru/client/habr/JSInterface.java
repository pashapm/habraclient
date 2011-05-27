/**
 * 
 */
package ru.client.habr;

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
		Toast.makeText(ActivityMain.getContext(), "onClickComment(" + commentID + ", " 
				+ postID + ", '" + author + "', " + inFavs + ");", Toast.LENGTH_LONG);
		// TODO comment, add in favs, vote
	}
	
	public void onClickKarma(String username, int userID) {
		Toast.makeText(ActivityMain.getContext(), "onClickKarma('" + username + "', " + userID + ");", Toast.LENGTH_LONG);
		// TODO change karma
	}
	
	public void onClickRating(int id, String type, int postID) {
		Toast.makeText(ActivityMain.getContext(), "onClickRating(" + id + ", '" + type + "', " + postID + ");", Toast.LENGTH_LONG);
		// TODO change rating
	}
	
	public void pollVote(int postID, String action, int variants[]) {
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
