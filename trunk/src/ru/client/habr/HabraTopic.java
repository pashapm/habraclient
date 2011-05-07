package ru.client.habr;

/**
 * @author WNeZRoS
 * ����� �����������
 */
public final class HabraTopic extends HabraEntry {
	
	/**
	 * @author WNeZRoS
	 * ��� �����
	 */
	protected static enum HabraTopicType {
		POST,
		LINK,
		TRANSLATE,
		PODCAST,
	}
	
	protected HabraTopicType postType = HabraTopicType.POST;
	
	public String title = null;
	public String[] tags = null;
	public int rating = 0;
	public int favoritesCount = 0;
	public int commentsCount = 0;
	public int commentsDiff = 0;
	public String additional = null;

	public String blogURL = null;
	public String blogName = null;
	public boolean isCorporativeBlog = false;

	public boolean inFavs = false;

	public HabraTopic() {
		type = HabraEntryType.POST;
	}
	
	/**
	 * �������� ����� �����
	 * @return ����� �����
	 */
	public String getUrl() {
		return getBlogUrl() + id + "/";
	}
	
	/**
	 * �������� ���� �����, � ������� ����� �����
	 * @return ����� �����
	 */
	public String getBlogUrl() {
		return blogURL;
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
	
	/**
	 * ���������� HTML ��� ��� �����
	 * @param noContent �� ���������� �������
	 * @param noTags �� ���������� ����
	 * @param noMark �� ���������� ������
	 * @param noDate �� ���������� ���� ����������
	 * @param noFavs �� ���������� ���-�� ���������� � ���������
	 * @param noAuthor �� ���������� ������
	 * @param noComments �� ���������� ���-�� ������������
	 * @return HTML ��� �����
	 */
	public String getDataAsHTML(boolean noContent, boolean noTags, boolean noMark, 
			boolean noDate, boolean noFavs, boolean noAuthor, boolean noComments) {
		return "<div class=\"hentry\" id=\"post_" + id 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getUrl() 
		+ "\" class=\"blog\">" + blogName + "</a> &rarr; <a href=\"" 
		+ getUrl() + "\" class=\"topic\">" + title + "</a></h2>" 
		+ (noContent ? "" : "<div class=\"content\">" + content + "</div>") 
		+ (tags.length > 1 && !noTags ? "<ul class=\"tags\">" + getTagsAsString() + "</ul>" : "") 
		+ (noMark && noDate && noFavs && noAuthor && noComments ? "" 
				: "<div class=\"entry-info\"><div class=\"corner tl\"></div>" 
					+ "<div class=\"corner tr\"></div><div class=\"entry-info-wrap\">" 
					+ (noMark ? "" : "<div class=\"mark\"><span class=\"" 
						+ (rating > 0 ? "plus" : (rating < 0 ? "minus" : "zero")) + "\">" 
						+ (rating == 99999 ? "&#8212;" : (rating > 0 ? "+" : "") + rating) + "</span></div>") 
					+ (noDate ? "" : "<div class=\"published\"><span>" + date + "</span></div>") 
					+ (noFavs ? "" : "<div class=\"favs_count\"><span>" 
							+ favoritesCount + "</span></div>") 
					+ (noAuthor ? "" : "<div class=\"vcard author full\"><a href=\"http://" 
							+ author + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" 
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
}