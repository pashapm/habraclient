package ru.client.habr;

import ru.client.habr.R;
import ru.client.habr.AsyncDataSender.OnSendDataFinish;
import android.util.Log;

/**
 * @author WNeZRoS
 * ����� ����������� � ������� ��� ������
 */
public class HabraEntry {
	protected static enum HabraEntryType {
		UNKNOWN,
		POST,
		COMMENT,
		QUESTION,
		ANSWER,
	};
	
	public static abstract class OnSendFinish {
		public abstract void onFinish(boolean ok, String data);
	}
	
	protected HabraEntryType type = HabraEntryType.UNKNOWN;
	public int id;
	public String content;
	public String author;
	public String date;
	
	public String getDataAsHTML() {
		return "<div id=\"comment_" + id 
		+ "\" class=\"comment_holder quest_comment\"><div class=\"entry-content\">" 
		+ "<div class=\"entry-content-only\">" + content 
		+ "&nbsp;<span class=\"fn comm\"><a href=\"http://" + author.replace('_', '-')
		+ ".habrahabr.ru/\">" + author + "</a>,&nbsp;<abbr class=\"published\">" 
		+ date + "</abbr></span></div></div></div>";
	}
	
	public String getUrl() {
		return "http://habrahabr.ru/";
	}
	
	public String getUrl(int parentId) {
		return getUrl();
	}
	
	public final static boolean vote(int id, HabraEntryType type, int mark, int postID) {
		String[][] post = {{"action", "vote"}, {"signed_id", String.valueOf(postID)}, 
				{"target_id", String.valueOf(id)}, {"mark", String.valueOf(mark)},
				{"target_name", "change"}};
		
		Log.d("VOTE", String.valueOf(type));
		
		switch(type) {
		case POST: post[4][1] = "post"; break;
		case COMMENT: post[4][1] = "post_comment"; break;
		case QUESTION: post[4][1] = "qa_question"; break;
		case ANSWER: post[4][1] = "qa_answer"; break;
		default: return false;
		}
		
		new AsyncDataSender("http://habrahabr.ru/ajax/voting/", "http://habrahabr.ru/", new OnSendDataFinish() {
			@Override
			public void onFinish(String result) {
				if(result.contains("<message>ok</message>")) 
					Dialogs.showToast(R.string.vote_ok);
				else 
					Dialogs.showToast(R.string.vote_failed);
			}
		}).execute(post);
		
		return true;
	}
	
	public final boolean vote(int mark, int postID) {
		return vote(id, type, mark, postID);
	}
	
	public final static boolean changeFavorites(int id, HabraEntryType type, final boolean isRemove) {
		String[][] post = {{"action", isRemove ? "remove" : "add"}, 
				{"target_type", "change"}, {"target_id", String.valueOf(id)}};
		
		switch(type) {
		case POST: post[1][1] = "posts"; break;
		case COMMENT: post[1][1] = "comments"; break;
		case QUESTION: post[1][1] = "questions"; break;
		default: return false;
		}
		
		new AsyncDataSender("http://habrahabr.ru/ajax/favorites/", "http://habrahabr.ru/", new OnSendDataFinish() {
			@Override
			public void onFinish(String result) {
				if(result.contains("<message>ok</message>")) 
					Dialogs.showToast(isRemove ? R.string.favorite_removed : R.string.favorite_added);
				else 
					Dialogs.showToast(R.string.favorite_failed);
			}
		}).execute(post);

		return true;
	}

	public final boolean changeFavorites() {
		switch(type) {
		case POST: 
			HabraTopic t = (HabraTopic) this;
			return changeFavorites(id, type, t.inFavs);
		case COMMENT:
			HabraComment c = (HabraComment) this;
			return changeFavorites(id, type, c.inFavs);
		case QUESTION: 
			HabraQuest q = (HabraQuest) this;
			return changeFavorites(id, type, q.inFavs);
		default: return false;
		}
	}
	
	public final boolean isFavorite() {
		switch(type) {
		case POST: 
			HabraTopic t = (HabraTopic) this;
			return t.inFavs;
		case COMMENT:
			HabraComment c = (HabraComment) this;
			return c.inFavs;
		case QUESTION: 
			HabraQuest q = (HabraQuest) this;
			return q.inFavs;
		default: return false;
		}
	}
	
	public final Class<?> getEntryClass() {
		switch(type) {
		case POST: return HabraTopic.class;
		case COMMENT: return HabraComment.class;
		case QUESTION: return HabraQuest.class;
		case ANSWER: return HabraAnswer.class;
		default: return HabraEntry.class;
		}
	}
	
	public static void send(int questionID, int answerID, String message, final OnSendFinish c) {
		/*
		 * Comment to answer: http://habrahabr.ru/ajax/qa/comment
		 * question_id={ID}
		 * answer_id={ANSWER_ID|0?}
		 * text={MSG}
		 */
		String post[][] = {{"question_id", String.valueOf(questionID)}, 
				{"answer_id", String.valueOf(answerID)},
				{"text", message}};
		
		new AsyncDataSender("http://habrahabr.ru/ajax/qa/comment", 
				"http://habrahabr.ru/qa/" + questionID, new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							if(c != null) c.onFinish(true, result);
						} else {
							if(c != null) c.onFinish(false, result);
						}
					}
		}).execute(post);
	}
}
