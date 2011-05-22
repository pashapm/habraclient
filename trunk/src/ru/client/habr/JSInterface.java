/**
 * 
 */
package ru.client.habr;

/**
 * @author WNeZRoS
 * 
 */
public class JSInterface {
	
	/**
	 * 
	 */
	public JSInterface() {
		
	}
	
	public void onClickComment(int commentID, int postID, String author, boolean inFavs) {
		// TODO reply, vote, author profile, favorites
	}
	
	public void onClickUserName(String username, int userID) {
		// TODO send pm, friend
	}
	
	public void onClickKarma(String username, int userID) {
		// TODO change karma
	}
}
