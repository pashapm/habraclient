package ru.client.habr;

/**
 * @author WNeZRoS
 * Класс комментария к посту
 */
public final class HabraComment 
{
	/**
	 * Аватар автора
	 */
	public String avatar = null;
	
	/**
	 * Автор
	 */
	public String author = null;
	
	/**
	 * Дата публикации
	 */
	public String date = null;
	
	/**
	 * Рейтинг
	 */
	public int rating = 0;
	
	/**
	 * Ид комментария
	 */
	public int id = 0;
	
	/**
	 * Текст
	 */
	public String text = null;
	
	/**
	 * Ответ на комментарий
	 */
	public HabraComment replyTo = null;
	
	/**
	 * Размер вложенности
	 */
	public int padding = 0;
	
	/**
	 * У меня в избранном
	 */
	public boolean inFavs = false;
	
	/**
	 * @return текст комментария как HTML код
	 */
	public String getCommentAsHTML() {
		return "<div id=\"comment_" + id + "\" class=\"comment_holder padding" 
		+ (padding >= 20 ? "Big" : String.valueOf(padding)) 
		+ "\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\">" 
		+ "<li class=\"avatar\">" + "<a href=\"http://" + author 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>" 
		+ "</li><li class=\"fn nickname username\"><a href=\"http://" 
		+ author + ".habrahabr.ru/\" class=\"url\">" + author 
		+ "</a>,</li><li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"mark\"><span>" 
		+ ( rating < 0 ? "-" : (rating == 0 ? "" : "+") ) + rating 
		+ "</span></li></ul></div>" + text + "</div>";
	}
	
	/**
	 * @param noAvatar скрыть аватар
	 * @return текст комментария как HTML код
	 */
	public String getCommentAsHTML(boolean noAvatar) {
		return "<div id=\"comment_" + id + "\" class=\"comment_holder padding" 
		+ (padding >= 20 ? "Big" : String.valueOf(padding)) 
		+ "\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\">" 
		+ (noAvatar ? "" : "<li class=\"avatar\">" + "<a href=\"http://" + author 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>") 
		+ "</li><li class=\"fn nickname username\"><a href=\"http://" 
		+ author + ".habrahabr.ru/\" class=\"url\">" + author 
		+ "</a>,</li><li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"mark\"><span>" 
		+ ( rating < 0 ? "-" : (rating == 0 ? "" : "+") ) + rating 
		+ "</span></li></ul></div>" + text + "</div>";
	}
	
	/**
	 * Гословать в +
	 * @param postID ИД поста 
	 * @return успешность
	 */
	public boolean voteUp(int postID) {
		String[][] post = {{"action","vote"}, {"target_name","post_comment"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}, {"signed_id",String.valueOf(postID)}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Голосовать в -
	 * @param postID ИД поста
	 * @return успешность
	 */
	public boolean voteDown(int postID) {
		String[][] post = {{"action","vote"}, {"target_name","post_comment"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}, {"signed_id",String.valueOf(postID)}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Добавить в избранное
	 * @return успешность
	 */
	public boolean addToFavorites() {
		String[][] post = {{"action","add"}, {"target_type","comments"}, {"target_id",String.valueOf(id)}};
		inFavs = URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/").contains("<message>ok</message>");
		return inFavs;
	}
	
	/**
	 * Удалить из избранного
	 * @return успешность
	 */
	public boolean removeFromFavorites() {
		String[][] post = {{"action","remove"}, {"target_type","comments"}, {"target_id",String.valueOf(id)}};
		inFavs = !URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		
		return !inFavs;
	}
}
