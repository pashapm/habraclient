package ru.client.habr;

/**
 * @author WNeZRoS
 * Класс комментария к вопросу или ответу
 */
public final class HabraQAComment {
	/**
	 * ID комментария
	 */
	public int id;
	
	/**
	 * Текст
	 */
	public String text;
	
	/**
	 * Автор
	 */
	public String author;
	
	/**
	 * Дата
	 */
	public String date;
	
	/**
	 * @return Текст комментария в HTML
	 */
	public String getDataAsHTML() {
		return "<div id=\"comment_" + String.valueOf(id) 
		+ "\" class=\"comment_holder vote_holder\"><div class=\"entry-content\">" 
		+ "<div class=\"entry-content-only\">" + text 
		+ "&nbsp;<span class=\"fn comm\"><a href=\"http://" + author 
		+ ".habrahabr.ru/\">" + author + "</a>,&nbsp;<abbr class=\"published\">" 
		+ date + "</abbr></span></div></div></div>";
	}
}
