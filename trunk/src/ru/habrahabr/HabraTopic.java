package ru.habrahabr;

public class HabraTopic
{
	public static enum HabraTopicType
	{
		Post,		// Простой пост
		Link,		// Ссылка
		Translate,	// Перевод
	}
	
	HabraTopicType type = HabraTopicType.Post;	// Тип топика
	String title = null;					// Заголовок
	String content = null;					// Текст топика
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
	boolean isCorporativeBlog = false;
	
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
	public String getTopicDataAsHTML()
	{
		return "<div class=\"hentry\"><h2 class=\"title\"><a href=\"" + 
			getBlogURL() + "\" class=\"blog\">" + blogName + "</a> &rarr; <a href=\"" + 
			getTopicURL() + "\" class=\"topic\">" + title + "</a></h2><div class=\"content\">" + 
			content + "</div><ul class=\"tags\">" + tags + "</ul><div class=\"infopanel\"><div class=\"corner tl\"></div><div class=\"corner tr\"></div><div class=\"info\"><div class=\"voting\">" + 
			rating + "</div><div class=\"published\"><span>" + date + "</span></div><div class=\"favs\">" + 
			Integer.toString(favorites) + "</div><div class=\"author\"><a href=\"http://" + author + ".habrahabr.ru/\" class=\"nickname url\"><span>" + 
			author + "</span></a></div><div class=\"comments\"><a href=\"" + getTopicURL() + "#comments\"><span class=\"all\">" + 
			Integer.toString(commentsCount) + "</span> <span class=\"new\">+ " + Integer.toString(commentsDiff) + 
			"</span></a></div></div><div class=\"corner bl\"></div><div class=\"corner br\"></div></div></div>";
	}
}