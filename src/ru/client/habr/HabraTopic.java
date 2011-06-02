package ru.client.habr;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import ru.client.habr.AsyncDataSender.OnSendDataFinish;

/**
 * @author WNeZRoS
 * ����� �����������
 */
public final class HabraTopic extends HabraEntry {
	
	/**
	 * @author WNeZRoS
	 */
	protected static enum HabraTopicType {
		POST,
		LINK,
		TRANSLATE,
		PODCAST,
	}
	
	public static abstract class OnPollResultListener {
		public abstract void onFinish(String result);
	}
	
	protected HabraTopicType postType = HabraTopicType.POST;
	
	public String title = null;
	public String[] tags = new String[0];
	public int rating = 0;
	public int favoritesCount = 0;
	public int commentsCount = 0;
	public int commentsDiff = 0;
	public String additional = null;
	public String summary = null; // TODO parse

	public HabraBlog blog = null;

	public boolean inFavs = false;

	public HabraTopic() {
		type = HabraEntryType.POST;
	}
	
	public String getUrl() {
		return blog.getUrl() + id + "/";
	}
	
	public String getUrlWorkInfo() {
		return "?id=" + id + "&author=" + author + "&infavs=" + inFavs; 
	}
	
	private String getTagsAsString() {
		String tagsAsString = "";
		for(int i = 0; i < tags.length; i++) {
			tagsAsString += "<li>" + tags[i] + "</li>";
		}
		return tagsAsString;
	}
	
	/**
	 * ���������� HTML ��� ��� �����
	 * @return HTML ��� �����
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false, false, false, false, false, false, false);
	}
	
	public String getDataAsHTML(boolean noContent, boolean noTags, boolean noMark, 
			boolean noDate, boolean noFavs, boolean noAuthor, boolean noComments) {
		return "<div class=\"hentry\" id=\"post_" + id 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + blog.getUrl()
		+ "\" class=\"blog\">" + blog.name + "</a> &rarr; <a href=\"" 
		+ getUrl() + getUrlWorkInfo() + "\" class=\"topic\">" + title + "</a></h2>" 
		+ (noContent ? "" : "<div class=\"content\">" + content + "</div>") 
		+ (tags.length > 1 && !noTags ? "<ul class=\"tags\">" + getTagsAsString() + "</ul>" : "") 
		+ (noMark && noDate && noFavs && noAuthor && noComments ? "" 
				: "<div class=\"entry-info\"><div class=\"corner tl\"></div>" 
					+ "<div class=\"corner tr\"></div><div class=\"entry-info-wrap\">" 
					+ (noMark ? "" : "<div class=\"mark\" onClick=\"js.onClickRating(" 
						+ id + ", 'p', 0);\"><span class=\"" 
						+ (rating > 0 ? "plus" : (rating < 0 ? "minus" : "zero")) + "\">" 
						+ (rating == 99999 ? "&#8212;" : (rating > 0 ? "+" : "") + rating) + "</span></div>") 
					+ (noDate ? "" : "<div class=\"published\"><span>" + date + "</span></div>") 
					+ (noFavs ? "" : "<div class=\"favs_count\"><span>" 
							+ favoritesCount + "</span></div>") 
					+ (noAuthor ? "" : "<div class=\"vcard author full\"><a href=\"http://" 
							+ author.replace('_', '-') + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" 
							+ author + "</span></a></div>") 
					+ (noComments ? "" : "<div class=\"comments\"><a href=\"" 
							+ getUrl() + "#comments\"><span class=\"all\">" 
							+ commentsCount + "</span>" 
							+ (commentsDiff > 0 ? " <span class=\"new\">+" 
									+ commentsDiff + "</span>" : "") 
		+ "</a></div>") 
		+ "</div><div class=\"corner bl\"></div><div class=\"corner br\"></div>") 
		+ "</div></div>";
	}
	
	public static void poll(int postID, String action, int variants[], final OnPollResultListener l) {
		List<String[]> post = new ArrayList<String[]>(); 
		post.add(new String[] {"action", action});
		post.add(new String[] {"post_id", String.valueOf(postID)});
		
		if(action.equals("vote")) {
			for(int i = 0; i < variants.length; i++)
				post.add(new String[] {"variant[]", String.valueOf(variants[i])});
		}
		
		String d[][] = post.toArray(new String[0][]);
		for(int i = 0; i < d.length; i++)
			Log.i(d[i][0], d[i][1]);
		
		new AsyncDataSender("http://habrahabr.ru/ajax/poll/", "http://habrahabr.ru/", new OnSendDataFinish() {
			@Override
			public void onFinish(String result) {
				Log.i("result", "" + result.length());
				Log.i("result", result);
				if(l != null) l.onFinish(result);
			}
		}).execute(post.toArray(new String[0][]));
	}
	
	public void poll(String action, int variants[], final OnPollResultListener l) {
		poll(id, action, variants, l);
	}
}