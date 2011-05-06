package ru.client.habr;

/**
 * @author WNeZRoS
 * ����� ������ �� ������
 */
public final class HabraAnswer extends HabraEntry {
	
	protected HabraEntryType type = HabraEntryType.ANSWER;
	public String avatar = null;
	public int rating = 0;
	public boolean isSolution = false;
	public HabraEntry[] comments = null;
		
	/**
	 * @param questionID ID �������
	 * @return ������ �� �����
	 */
	public String getUrl(int questionID) {
		return getUrl() + questionID + "/#answer_" + id;
	}
	
	public String getUrl() {
		return "http://habrahabr.ru/qa/";
	}
	
	/**
	 * @return ����� ��� HTML
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false);
	}
	
	/**
	 * @param noAvatar ������ ������
	 * @return ����� ��� HTML
	 */
	public String getDataAsHTML(boolean noAvatar) {
		return "<div class=\"comment_holder vote_holder answer\" id=\"answer_" + id + "\">" 
		+ "<div class=\"msg-meta\"><ul class=\"menu info author hcard\">" 
		+ "<li class=\"avatar\">" + (noAvatar ? "" : "<a href=\"http://" + author 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>") + "</li>" 
		+ "<li class=\"fn nickname username\"><a href=\"http://" + author 
		+ ".habrahabr.ru/\" class=\"url\">" + author + "</a>,</li>" 
		+ "<li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"mark\"><span>" + rating 
		+ "</span></li></ul></div>" + content + "</div>";
	}
	
	/**
	 * @return ����������� � ������ ��� HTML ���
	 */
	public String getCommentsAsHTML() {
		String data = "";
		if(comments == null) return data;

		for(int i = 0; i < comments.length; i++) {
			data += comments[i].getDataAsHTML();
		}
			
		return data;
	}
}
