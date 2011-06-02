package ru.client.habr;

import java.util.Date;

import ru.client.habr.AsyncDataSender.OnSendDataFinish;

/**
 * @author WNeZRoS
 * Комментарий к посту
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
	 * @return Данные комментария в виде HTML кода.
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false);
	}
	
	/**
	 * @param noAvatar убрать аватары
	 * @return Данные комментария в виде HTML кода.
	 */
	public String getDataAsHTML(boolean noAvatar) {
		return "<div id=\"comment_" + id + "\" class=\"comment_holder" 
		+ "\"><div class=\"msg-meta" + (HabraLogin.getHabraLogin().isLogged() 
				&& author.equals(HabraLogin.getHabraLogin().getUserName()) 
				? " my-reply" : (newReply ? " new-reply" : "")) 
				+ "\"><ul class=\"menu info author hcard\">" 
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
	
	/**
	 * Отправляет новый комментарий
	 * @param content Текст комментария
	 * @param postID ID поста
	 * @param parentID ID комментария который комментируем или 0
	 * @param c Обработчик
	 */
	public static void send(String content, int postID, int parentID, final OnSendFinish c) {
		String post[][] = {{"comment[target_type]", "post"}, 
				{"comment[parent_id]", String.valueOf(parentID)}, 
				{"timefield", String.valueOf(new Date().getTime())}, 
				{"comment[target_id]", String.valueOf(postID)}, 
				{"comment[message]", content}};
		
		new AsyncDataSender("http://habrahabr.ru/ajax/comments/add/", 
				"http://habrahabr.ru/post/" + postID, new OnSendDataFinish() {
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
