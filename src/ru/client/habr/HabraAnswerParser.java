package ru.client.habr;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * @author WNeZRoS
 * ������ �������
 */
public final class HabraAnswerParser {
	private String mData = null;
	private int mStartPosition = 0;
	// TODO: 
	
	/**
	 * ������ ������ �� �������
	 * @param data ������ HTML ��������
	 */
	public HabraAnswerParser(String data) {
		mData = data;
	}
	
	/**
	 * ������ ��������� �����
	 * @return ��������� ����� ��� null
	 */
	public HabraAnswer parseAnswer() {
		if(mData == null || mStartPosition == -1) return null;
		HabraAnswer answer = new HabraAnswer();
		
		int startPosition = mData.indexOf("<li id=\"answer_", mStartPosition);
		if(startPosition == -1) return null;
		
		String answerData = mData.substring(startPosition, 
				startPosition = mData.indexOf("<div class=\"hsublevel", startPosition));
		
		Log.d("AnswerParser", "Parse ID");
		int lastIndex = 15;
		answer.id = Integer.valueOf(answerData.substring(lastIndex, 
				lastIndex = answerData.indexOf('"', lastIndex)));
		
		Log.d("AnswerParser", "Parse Avatar");
		answer.avatar = new String(answerData.substring(
				lastIndex = (answerData.indexOf("<img src=", lastIndex) + 10), 
				lastIndex = answerData.indexOf('"', lastIndex)));
		
		Log.d("AnswerParser", "Parse Author");
		lastIndex += 7;
		answer.author = new String(answerData.substring(lastIndex, 
				lastIndex = answerData.indexOf('"', lastIndex)));
		
		Log.d("AnswerParser", "Parse Date");
		answer.date = new String(answerData.substring(
				lastIndex = (answerData.indexOf('>', answerData.indexOf("<abbr", lastIndex)) + 1), 
				lastIndex = answerData.indexOf('<', lastIndex)));
		
		Log.d("AnswerParser", "Parse Rating");
		String rs = answerData.substring(
				lastIndex = (answerData.indexOf("mark\"><span>", lastIndex) + 12), 
				lastIndex = (answerData.indexOf('<', lastIndex)));
    	answer.rating = (rs.charAt(0) == '-' ? -1 : 1) ;
    	rs = "0" + rs.substring(1);
		answer.rating = Integer.valueOf(rs);
		
		Log.d("AnswerParser", "Parse Text");
		answer.content = new String(answerData.substring(
				lastIndex = (answerData.indexOf("<div class=\"entry-content entry-content-text answer-text\">", lastIndex)),
				lastIndex = answerData.indexOf("<ul class=\"hentry", lastIndex)));
		
		Log.d("AnswerParser", "Parse comments");
		String commentsData = new String(answerData.substring(lastIndex));
		HabraEntry comment = null;
		List<HabraEntry> commentsList = new ArrayList<HabraEntry>();
		
		Log.i("commentsList", String.valueOf(commentsList != null) + commentsList.toString());
		
		int subIndex = 0;
		while((subIndex = commentsData.indexOf("<li id=\"comment_", subIndex)) != -1) {
			Log.d("QuestParser", "new Comment");
			comment = new HabraEntry();
			subIndex += 16;
			
			Log.d("QuestParser", "Parse comment.id");
			comment.id = Integer.valueOf(commentsData.substring(subIndex, 
					subIndex = commentsData.indexOf('"', subIndex)));
			
			Log.d("QuestParser", "Parse comment.text");
			comment.content = new String(commentsData.substring(
					subIndex = (commentsData.indexOf("content-only\">", subIndex) + 14), 
					subIndex = commentsData.indexOf("&nbsp;<span class=\"fn", subIndex)));
			
			Log.d("QuestParser", "Parse comment.author");
			comment.author = new String(commentsData.substring(
					subIndex = (commentsData.indexOf("http://", subIndex) + 7), 
					subIndex = commentsData.indexOf('.', subIndex)));
			
			Log.d("QuestParser", "Parse comment.date");
			comment.date = new String(commentsData.substring(
					subIndex = (commentsData.indexOf('>', commentsData.indexOf("<abbr", subIndex)) + 1), 
					subIndex = commentsData.indexOf("</abbr", subIndex)));
			
			Log.d("QuestParser", "add(comment)");
			commentsList.add(comment);
			Log.d("QuestParser", "do while");
		}
		
		Log.d("QuestParser", "toArray");
		answer.comments = commentsList.toArray(new HabraEntry[0]);
		
		Log.d("CommentParser", "Save position");		
		mStartPosition = startPosition;
		return answer;
	}
}
