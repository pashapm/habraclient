package ru.client.habr;

/**
 * @author WNeZRoS
 * Класс ХабраТопика
 */
public final class HabraTopic {
	
	/**
	 * @author WNeZRoS
	 * Тип поста
	 */
	public static enum HabraTopicType {
		/**
		 * Простой пост
		 */
		Post,
		
		/**
		 * Топик-ссылка
		 */
		Link,
		
		/**
		 * Перевод
		 */
		Translate,
		
		/**
		 * Подкаст
		 */
		Podcast,
	}
	
	/**
	 * Тип поста
	 */
	public HabraTopicType type = HabraTopicType.Post;
	
	/**
	 * Заголовок
	 */
	public String title = null;
	
	/**
	 * Контент
	 */
	public String content = null;

	/**
	 * Тэги
	 */
	public String tags = null;
	
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
	public String rating = null;
	
	/**
	 * Кол-во добавлений в избранное
	 */
	public int favorites = 0;

	/**
	 * Кол-во комментариев
	 */
	public int commentsCount = 0;

	/**
	 * Кол-во добавленных коментариев с последнего просмотра
	 */
	public int commentsDiff = 0;

	/**
	 * Дополнительные сведения (ссылка, оригинал, файл)
	 */
	public String additional = null;

	/**
	 * ID поста
	 */
	public int id = 0;

	/**
	 * ID блога
	 */
	public String blogID = null;

	/**
	 * Название блога
	 */
	public String blogName = null;

	/**
	 * Флаг для обозначения корпоративного блога (/company/{NAME}/blog/{ID})
	 */
	public boolean isCorporativeBlog = false;

	/**
	 * В избранном у текущего пользователя
	 */
	public boolean inFavs = false;

	
	/**
	 * Собирает адрес поста
	 * @return Адрес поста
	 */
	public String getTopicURL() {
		return getBlogURL() + Integer.toString(id) + "/";
	}
	
	/**
	 * Собирает адес блога, в котором лежит поста
	 * @return Адрес блога
	 */
	public String getBlogURL() {
		return (isCorporativeBlog ? "http://habrahabr.ru/company/" + blogID 
				+ "/blog/" : "http://habrahabr.ru/blogs/" + blogID + "/");
	}
	
	/**
	 * Генерирует HTML код для поста
	 * @return HTML код поста
	 */
	public String getDataAsHTML() {
		return "<div class=\"hentry\" id=\"post_" + String.valueOf(id) 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getBlogURL() 
		+ "\" class=\"blog\">" + blogName + "</a> &rarr; <a href=\"" 
		+ getTopicURL() + "\" class=\"topic\">" + title 
		+ "</a></h2><div class=\"content\">" + content + "</div>" 
		+ (tags.length() > 1 ? "<ul class=\"tags\">" + tags + "</ul>" : "") 
		+ "<div class=\"entry-info\"><div class=\"corner tl\"></div>" 
		+ "<div class=\"corner tr\"></div><div class=\"entry-info-wrap\">" 
		+ "<div class=\"mark\">" + rating + "</div>" 
		+ "<div class=\"published\"><span>" + date + "</span></div>" 
		+ "<div class=\"favs_count\"><span>" + String.valueOf(favorites) + "</span></div>" 
		+ "<div class=\"vcard author full\"><a href=\"http://" + author 
		+ ".habrahabr.ru/\" class=\"fn nickname url\"><span>" + author 
		+ "</span></a></div><div class=\"comments\"><a href=\"" + getTopicURL() 
		+ "#comments\"><span class=\"all\">" + String.valueOf(commentsCount) 
		+ "</span>" + (commentsDiff > 0 ? " <span class=\"new\">+" 
		+ String.valueOf(commentsDiff) + "</span>" : "") + "</a></div></div>" 
		+ "<div class=\"corner bl\"></div><div class=\"corner br\"></div></div></div>";
	}
	
	/**
	 * Генерирует HTML код для поста
	 * @param noContent не показывать контент
	 * @param noTags не показывать теги
	 * @param noMark не показывать оценку
	 * @param noDate не показывать дату публикации
	 * @param noFavs не показывать кол-во добавлений в избранное
	 * @param noAuthor не показывать автора
	 * @param noComments не показывать кол-во комментариев
	 * @return HTML код поста
	 */
	public String getDataAsHTML(boolean noContent, boolean noTags, boolean noMark, 
			boolean noDate, boolean noFavs, boolean noAuthor, boolean noComments) {
		return "<div class=\"hentry\" id=\"post_" + String.valueOf(id) 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getBlogURL() 
		+ "\" class=\"blog\">" + blogName + "</a> &rarr; <a href=\"" 
		+ getTopicURL() + "\" class=\"topic\">" + title + "</a></h2>" 
		+ (noContent ? "" : "<div class=\"content\">" + content + "</div>") 
		+ (tags.length() > 1 && !noTags ? "<ul class=\"tags\">" + tags + "</ul>" : "") 
		+ (noMark && noDate && noFavs && noAuthor && noComments ? "" 
				: "<div class=\"entry-info\"><div class=\"corner tl\"></div>" 
					+ "<div class=\"corner tr\"></div><div class=\"entry-info-wrap\">" 
					+ (noMark ? "" : "<div class=\"mark\">" + rating + "</div>") 
					+ (noDate ? "" : "<div class=\"published\"><span>" + date + "</span></div>") 
					+ (noFavs ? "" : "<div class=\"favs_count\"><span>" 
							+ String.valueOf(favorites) + "</span></div>") 
					+ (noAuthor ? "" : "<div class=\"vcard author full\"><a href=\"http://" 
							+ author + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" 
							+ author + "</span></a></div>") 
					+ (noComments ? "" : "<div class=\"comments\"><a href=\"" 
							+ getTopicURL() + "#comments\"><span class=\"all\">" 
							+ Integer.toString(commentsCount) + "</span>" 
							+ (commentsDiff > 0 ? " <span class=\"new\">+" 
									+ Integer.toString(commentsDiff) + "</span>" : "") 
		+ "</a></div>") 
		+ "</div><div class=\"corner bl\"></div><div class=\"corner br\"></div>") 
		+ "</div></div>";
	}
	
	/**
	 * Голосовать в +
	 * @return удалось ли проголосовать
	 */
	public boolean voteUp() {
		String[][] post = {{"action","vote"}, {"target_name","post"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Голосовать в -
	 * @return удалось ли проголосовать
	 */
	public boolean voteDown() {
		String[][] post = {{"action","vote"}, {"target_name","post"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Добавить в избранное
	 * @return удалось ли добавить
	 */
	public boolean addToFavorites() {
		String[][] post = {{"action","add"}, {"target_type","post"}, {"target_id",String.valueOf(id)}};
		inFavs = URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/").contains("<message>ok</message>");
		
		return inFavs;
	}
	
	/**
	 * Удалить из избранного
	 * @return удалось ли удалить
	 */
	public boolean removeFromFavorites() {
		String[][] post = {{"action","remove"}, {"target_type","post"}, {"target_id",String.valueOf(id)}};
		inFavs = !URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		
		return !inFavs;
	}
}