package ru.client.habr;

/**
 * @author WNeZRoS
 * ����� ����������� � �����
 */
public final class HabraComment extends HabraEntry {
	
	protected HabraEntryType type = HabraEntryType.COMMENT;
	public String avatar = null;
	public int rating = 0;
	public boolean inFavs = false;
	public HabraComment[] childs = null;
	public boolean newReply = false;
	public int postID = 0;
	
	public String getUrl() {
		return getUrl() + postID + "/#comment_" + id;
	}
	
	/**
	 * @return ����� ����������� ��� HTML ���
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false);
	}
	
	public String getDataAsHTML(boolean noAvatar) {
		return "<div id=\"comment_" + id + "\" class=\"comment_holder" 
		+ "\"><div class=\"msg-meta" + (newReply ? " new-reply" : "") + "\"><ul class=\"menu info author hcard\">" 
		+ (noAvatar ? "" : "<li class=\"avatar\">" + "<a href=\"http://" + author.replace('_', '-') 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>") 
		+ "</li><li class=\"fn nickname username\"><a href=\"http://" 
		+ author.replace('_', '-') + ".habrahabr.ru/\" class=\"url\">" + author 
		+ "</a>,</li><li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"mark\" onClick=\"js.onClickRating(" 
		+ id + ", 'c', " + postID + ");\"><span class=\"" 
		+ (rating > 0 ? "plus" : (rating < 0 ? "minus" : "zero")) + "\">"
		+ (rating > 0 ? "+" : "") + rating 
		+ "</span></li></ul></div><div class=\"entry-content\" onClick=\"js.onClickComment(" 
		+ id + ", " + postID + ", '" + author + "', " + inFavs + ");\">" + content 
		+ "</div><div class=\"child\">" + getChildsDataAsHTML(noAvatar) + "</div></div>";
	}
	
	private String getChildsDataAsHTML(boolean noAvatar) {
		if(childs == null) return "";
		if(childs.length == 0) return "";
		String data = "";
		for(int i = 0; i < childs.length; i++)
			data += childs[i].getDataAsHTML(noAvatar);
		return data;
	}
}
