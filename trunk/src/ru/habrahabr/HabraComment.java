package ru.habrahabr;

public class HabraComment 
{
	String avatar;			// Ссылка на аватар
	String author;			// Автор
	String date;			// Дата публикации
	int rating;				// Рейтинг
	int id;					// Ид
	String text;			// Текст
	HabraComment replyTo;	// Ответ на коммент
	int padding;			// Лесенка
	
	public String getCommentAsHTML()
	{
		return "<div id=\"comment_" + id + "\" class=\"comment_holder vote_holder\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\">" + 
			"<li class=\"avatar\"><a href=\"http://" + author + ".habrahabr.ru/\"><img src=\"" + avatar + 
			"\"/></a></li><li class=\"fn nickname username\"><a href=\"http://" + author + ".habrahabr.ru/\" class=\"url\">" + author + 
			"</a>,</li><li class=\"date\"><abbr class=\"published\">" + date + "</abbr></li><li class=\"mark\"><span>" +
			( rating < 0 ? "-" : (rating == 0 ? "" : "+") ) + rating + 
			"</span></li></ul></div>" + text + "</div>";
	}
}
