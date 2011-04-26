package ru.client.habr;

public class HabraComment 
{
	String avatar = null;			// Ссылка на аватар
	String author = null;			// Автор
	String date = null;				// Дата публикации
	int rating = 0;					// Рейтинг
	int id = 0;						// Ид
	String text = null;				// Текст
	HabraComment replyTo = null;	// Ответ на коммент
	int padding = 0;				// Лесенка
	boolean inFavs = false;			// В избранном
	
	public String getCommentAsHTML()
	{
		return "<div id=\"comment_" + id + "\" class=\"comment_holder padding" + 
			(padding >= 20 ? "Big" : String.valueOf(padding)) + 
			"\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\">" + 
			"<li class=\"avatar\"><a href=\"http://" + author + ".habrahabr.ru/\"><img src=\"" + avatar + 
			"\"/></a></li><li class=\"fn nickname username\"><a href=\"http://" + author + ".habrahabr.ru/\" class=\"url\">" + author + 
			"</a>,</li><li class=\"date\"><abbr class=\"published\">" + date + "</abbr></li><li class=\"mark\"><span>" +
			( rating < 0 ? "-" : (rating == 0 ? "" : "+") ) + rating + 
			"</span></li></ul></div>" + text + "</div>";
	}
	
	public boolean voteUp(URLClient url, int postID)
	{
		String[][] post = {{"action","vote"}, {"target_name","post_comment"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}, {"signed_id",String.valueOf(postID)}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean voteDown(URLClient url, int postID)
	{
		String[][] post = {{"action","vote"}, {"target_name","post_comment"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}, {"signed_id",String.valueOf(postID)}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean addToFavorites(URLClient url)
	{
		String[][] post = {{"action","add"}, {"target_type","comments"}, {"target_id",String.valueOf(id)}};
		inFavs = url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/").contains("<message>ok</message>");
		return inFavs;
	}
	
	public boolean removeFromFavorites(URLClient url)
	{
		String[][] post = {{"action","remove"}, {"target_type","comments"}, {"target_id",String.valueOf(id)}};
		inFavs = !url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		return !inFavs;
	}
}
