package ru.client.habr;

/**
 * @author WNeZRoS
 * ����� ����������� � �����
 */
public final class HabraComment extends HabraEntry {
	
	protected HabraEntryType type = HabraEntryType.COMMENT;
	public String avatar = null;
	public int rating = 0;
	public HabraComment replyTo = null;
	public int padding = 0;
	public boolean inFavs = false;
	
	public String getUrl(int postId) {
		return getUrl() + postId + "/#comment_" + id;
	}
	
	public String getUrl() {
		return "http://habrahabr.ru/post/";
	}
	
	/**
	 * @return ����� ����������� ��� HTML ���
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false);
	}
	
	/**
	 * @param noAvatar ������ ������
	 * @return ����� ����������� ��� HTML ���
	 */
	public String getDataAsHTML(boolean noAvatar) {
		return "<div id=\"comment_" + id + "\" class=\"comment_holder padding" 
		+ (padding >= 20 ? "Big" : padding) 
		+ "\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\">" 
		+ (noAvatar ? "" : "<li class=\"avatar\">" + "<a href=\"http://" + author 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>") 
		+ "</li><li class=\"fn nickname username\"><a href=\"http://" 
		+ author + ".habrahabr.ru/\" class=\"url\">" + author 
		+ "</a>,</li><li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"mark\"><span>" 
		+ ( rating < 0 ? "-" : (rating == 0 ? "" : "+") ) + rating 
		+ "</span></li></ul></div>" + content + "</div>";
	}
}
