package ru.client.habr;

import android.util.Log;

/**
 * @author WNeZRoS
 * Парсер комментариев
 */
public final class HabraCommentParser {
	private String mData = null;
	private int mStartPosition = 0;
	private int mPadding = 0;
	private HabraComment mReplyTo = null;
	private HabraComment mPrevComment = null;
	
	/**
	 * Парсит комментарии из постов
	 * @param data Данные HTML страницы поста
	 */
	public HabraCommentParser(String data) {
		mData = data;
	}
	
	/**
	 * Парсит следующий комментарий
	 * @return следующий комментарий или null
	 */
	public HabraComment parseComment() {
		if(mData == null || mStartPosition == -1) return null;
		HabraComment comment = new HabraComment();
		
		int startPosition = mData.indexOf("<li id=\"comment_", mStartPosition);
		if(startPosition == -1) return null;
		
		// Проверяем, на какой комментарий этот ответ
		Log.d("CommentParser", "PaddingCheck");
		String check = mData.substring(startPosition - 30, startPosition);
		if(check.indexOf("<ul class") != -1) {
			if(mPrevComment != null) {
				mPadding++;
				mReplyTo = mPrevComment;
			}
		} else if(check.indexOf("</ul") != -1) {
			mPadding--;
			mReplyTo = mReplyTo.replyTo;
		}
		
		String commentData = mData.substring(startPosition, 
				startPosition = mData.indexOf("<div id=\"reply_form_", startPosition));
		
		Log.d("CommentParser", "Parse ID");
		int lastIndex = 16;
		comment.id = Integer.valueOf(commentData.substring(lastIndex, 
				lastIndex = commentData.indexOf('"', lastIndex)));
		
		Log.d("CommentParser", "Parse Avatar");
		comment.avatar = new String(commentData.substring(
				lastIndex = (commentData.indexOf("<img src=", lastIndex) + 10), 
				lastIndex = commentData.indexOf('"', lastIndex)));
		
		Log.d("CommentParser", "Parse Author");
		lastIndex += 7;
		comment.author = new String(commentData.substring(lastIndex, 
				lastIndex = commentData.indexOf('"', lastIndex)));
		
		Log.d("CommentParser", "Parse Date");
		comment.date = new String(commentData.substring(
				lastIndex = (commentData.indexOf('>', 
						commentData.indexOf("<abbr", lastIndex)) + 1), 
				lastIndex = commentData.indexOf('<', lastIndex)));
		
		comment.inFavs = commentData.indexOf("class=\"fav_added", lastIndex) != -1;
		
		Log.d("CommentParser", "Parse Rating");
		String rs = commentData.substring(
				lastIndex = (commentData.indexOf("mark\"><span>", lastIndex) + 12), 
				lastIndex = (commentData.indexOf('<', lastIndex)));
    	comment.rating = (rs.charAt(0) == '-' ? -1 : 1);
    	rs = "0" + rs.substring(1);
    	comment.rating *= Integer.parseInt(rs);
		
		Log.d("CommentParser", "Parse Text");
		int endIndex = commentData.indexOf("<p class=\"reply", lastIndex);
		if(endIndex == -1) {
			comment.text = "<div class=\"entry-content\">" + new String(
					commentData.substring(lastIndex 
							= (commentData.indexOf("entry-content\">", lastIndex) + 15)));
		} else
			comment.text = "<div class=\"entry-content\">" + new String(
					commentData.substring(lastIndex 
							= (commentData.indexOf("entry-content\">", 
									lastIndex) + 15), endIndex)) + "</div>";
		
		Log.d("CommentParser", "reply + padding");
		comment.replyTo = mReplyTo;
		comment.padding = mPadding;
		
		Log.d("CommentParser", "Save position");
		mStartPosition = startPosition;
		mPrevComment = comment;
		return comment;
	}
}
