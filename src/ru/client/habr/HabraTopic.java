package ru.client.habr;

public class HabraTopic
{
	public static enum HabraTopicType
	{
		Post,		// Простой пост
		Link,		// Ссылка
		Translate,	// Перевод
		Podcast,	// Подкаст
	}
	
	HabraTopicType type = HabraTopicType.Post;	// Тип топика
	String title = null;						// Заголовок
	String content = null;						// Текст топика
	String tags = null;							// Тэги
	// TODO: replace tag to String[]
	String author = null;						// Автор
	String date = null;							// Дата публикации
	String rating = null;						// Рейтинг
	Integer favorites = null;					// Кол-во добавления в избранное
	Integer commentsCount = null;				// Кол-во комментраиев
	Integer commentsDiff = null;				// Разница комментариев с прошлого просмотра
	String additional = null;					// Дополнительная информация (ссылка на оригинал)
	Integer id = null;							// ID топика
	String blogID = null;						// Блог, в котором размещён топик
	String blogName = null;
	boolean isCorporativeBlog = false;			// Это блог компании?
	boolean inFavs = false;						// В избранном
	
	/**
	 * Собирает адрес поста
	 * @return Адрес поста
	 */
	public String getTopicURL()
	{
		return getBlogURL() + Integer.toString(id) + "/";
	}
	
	/**
	 * Собирает адес блога, в котором лежит поста
	 * @return Адрес блога
	 */
	public String getBlogURL()
	{
		if(isCorporativeBlog) return "http://habrahabr.ru/company/" + blogID + "/blog/";
		return "http://habrahabr.ru/blogs/" + blogID + "/";
	}
	
	/**
	 * Генерирует HTML код для поста
	 * @return код поста
	 */
	public String getDataAsHTML()
	{
		return "<div class=\"hentry\"><h2 class=\"entry-title\"><a href=\"" + 
			getBlogURL() + "\" class=\"blog\">" + blogName + "</a> &rarr; <a href=\"" + 
			getTopicURL() + "\" class=\"topic\">" + title + "</a></h2><div class=\"content\">" + content + "</div>" + 
			(tags.length() > 1 ? "<ul class=\"tags\">" + tags + "</ul>" : "") + 
			"<div class=\"entry-info\"><div class=\"corner tl\"></div><div class=\"corner tr\"></div><div class=\"entry-info-wrap\"><div class=\"mark\">" + 
			rating + "</div><div class=\"published\"><span>" + date + "</span></div><div class=\"favs_count\">" + 
			Integer.toString(favorites) + "</div><div class=\"vcard author full\"><a href=\"http://" + author + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" + 
			author + "</span></a></div><div class=\"comments\"><a href=\"" + getTopicURL() + "#comments\"><span class=\"all\">" + 
			Integer.toString(commentsCount) + "</span>" + (commentsDiff > 0 ? "<span class=\"new\">+ " + Integer.toString(commentsDiff) + "</span>" : "") + 
			"</a></div></div><div class=\"corner bl\"></div><div class=\"corner br\"></div></div></div><hr>";
	}
	
	public boolean voteUp(URLClient url)
	{
		String[][] post = {{"action","vote"}, {"target_name","post"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean voteDown(URLClient url)
	{
		String[][] post = {{"action","vote"}, {"target_name","post"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean addToFavorites(URLClient url)
	{
		String[][] post = {{"action","add"}, {"target_type","post"}, {"target_id",String.valueOf(id)}};
		inFavs = url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/").contains("<message>ok</message>");
		return inFavs;
	}
	
	public boolean removeFromFavorites(URLClient url)
	{
		String[][] post = {{"action","remove"}, {"target_type","post"}, {"target_id",String.valueOf(id)}};
		inFavs = !url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		return !inFavs;
	}
}