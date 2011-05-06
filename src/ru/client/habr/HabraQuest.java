package ru.client.habr;

import android.util.Log;

/**
 * @author WNeZRoS
 * ����� �������
 */
public final class HabraQuest extends HabraEntry {	
	
	protected HabraEntryType type = HabraEntryType.QUESTION;
	public String title = null;
	public String[] tags = null;
	public int rating = 0;
	public int favoritesCount = 0;
	public int answerCount = 0;
	public boolean inFavs = false;
	public boolean accepted = false;
	public HabraEntry[] comments = null;
	
	/**
	 * ���������� URL �������
	 * @return URL �������
	 */
	public String getUrl() {
		return "http://habrahabr.ru/qa/" + id + "/";
	}
	
	private String getTagsAsString() {
		String tagsAsString = "";
		for(int i = 0; i < tags.length; i++) {
			tagsAsString += "<li>" + tags[i] + "</li>";
		}
		return tagsAsString;
	}
	
	/**
	 * ���������� HTML ��� �������
	 * @return HTML ���
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false, false, false, false, false, false, false);
	}
	
	/**
	 * ���������� HTML ��� ������
	 * @param noContent ������ �������
	 * @param noTags ������ ����
	 * @param noMark ������ ������
	 * @param noAnswersCount ������ ���-�� �������
	 * @param noDate ������ ����
	 * @param noFavs ������ ���-�� ���������� � ���������
	 * @param noAuthor ������ ������
	 * @return HTML ���
	 */
	public String getDataAsHTML(boolean noContent, boolean noTags, boolean noMark, 
			boolean noAnswersCount, boolean noDate, boolean noFavs, boolean noAuthor) {
		return "<div class=\"hentry question_hentry\" id=\"" + id 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getUrl() 
		+ "\" class=\"topic\">" + title + "</a></h2>" 
		+ (noContent ? "" : "<div class=\"content\">" + (content == null ? "" : content) + "</div>")
		+ (noTags ? "" : "<ul class=\"tags\">" + getTagsAsString() + "</ul>")
		+ (noMark && noAnswersCount && noDate && noAuthor ? "" 
				: "<div class=\"entry-info vote_holder answer-positive\">" 
		+ "<div class=\"corner tl\"></div><div class=\"corner tr\"></div>" 
		+ "<div class=\"entry-info-wrap\">" 
		+ (noMark ? "" : "<div class=\"mark\"><span>" 
			+ ( rating > 0 ? "+" : "" ) + rating + "</span></div>") 
		+ (noAnswersCount ? "" : "<div class=\"informative\"><span>" 
			+ answerCount + " " + getAnswer() + "</span></div>") 
		+ (noDate ? "" : "<div class=\"published\"><span>" + date + "</span></div>")
		+ (noFavs ? "" : "<div class=\"favs_count\"><span>" + favoritesCount + "</span></div>")
		+ (noAuthor ? "" : "<div class=\"vcard author full\"><a href=\"http://" 
			+ author + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" 
			+ author + "</span></a></div>") 
		+ "</div><div class=\"corner bl\"></div>" 
		+ "<div class=\"corner br\"></div></div>") + "</div>";
	}
	
	/**
	 * ����� ������������ � �������
	 * @return HTML ��� ������������
	 */
	public String getCommentsAsHTML() {
		String data = "";
		if(comments == null) return data;

		for(int i = 0; i < comments.length; i++) {
			data += comments[i].getDataAsHTML();
		}
			
		return data;
	}
	
	private String getAnswer() {
		int mod = answerCount - (answerCount / 10) * 10;
		Log.d("HabraQuest.getAnswer", "mod is " + mod);
		
		if((answerCount >= 6 && answerCount <= 20) || mod == 0 || mod >= 5)
			return "ответов";
		else if(mod == 1) 
			return "ответ";
		else if(mod >= 2 && mod <= 4)
			return "ответа";
		
		return "ответов";
	}
}
