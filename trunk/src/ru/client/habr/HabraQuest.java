package ru.client.habr;

/**
 * @author WNeZRoS
 * Класс вопроса
 */
public final class HabraQuest 
{	
	/**
	 * ID вопроса
	 */
	public int id = 0;
	
	/**
	 * Заголовок
	 */
	public String title = null;
	
	/**
	 * Текст вопроса
	 */
	public String text = null;
	
	/**
	 * Теги
	 */
	public String tags = null;
	
	/**
	 * Рейтинг
	 */
	public int rating = 0;
	
	/**
	 * Дата публикации
	 */
	public String date = null;
	
	/**
	 * Автор
	 */
	public String author = null;
	
	/**
	 * Кол-во добавлений в избранное
	 */
	public int favsCount = 0;
	
	/**
	 * Кол-во ответов
	 */
	public int answerCount = 0;
	
	/**
	 * В избранном у меня
	 */
	public boolean inFavs = false;
	
	/**
	 * Решено?
	 */
	public boolean accepted = false;
	
	/**
	 * Комментарии к вопросу
	 */
	public HabraQAComment[] comments = null;
	
	/**
	 * Голосовать в +
	 * @return удалось ли проголосовать
	 */
	public boolean voteUp() {
		String[][] post = {{"action","vote"}, {"target_name","qa_question"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Голосовать в -
	 * @return удалось ли проголосовать
	 */
	public boolean voteDown() {
		String[][] post = {{"action","vote"}, {"target_name","qa_question"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}};
		return URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	/**
	 * Добавить в избранное
	 * @return удалось ли добавить
	 */
	public boolean addToFavorites() {
		String[][] post = {{"action","add"}, {"target_type","questions"}, {"target_id",String.valueOf(id)}};
		inFavs = URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		
		if(inFavs) favsCount++;
		return inFavs;
	}
	
	/**
	 * Удалить из избранного
	 * @return удалось ли удалить
	 */
	public boolean removeFromFavorites() {
		String[][] post = {{"action","remove"}, {"target_type","questions"}, {"target_id",String.valueOf(id)}};
		inFavs = !URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		
		if(!inFavs) favsCount--;
		return !inFavs;
	}
	
	/**
	 * Возвращает URL вопроса
	 * @return URL вопроса
	 */
	public String getQuestURL() {
		return "http://habrahabr.ru/qa/" + String.valueOf(id) + "/";
	}
	
	/**
	 * Возвращает HTML код вопроса
	 * @return HTML код
	 */
	public String getDataAsHTML() {
		return "<div class=\"hentry question_hentry\" id=\"" + String.valueOf(id) 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getQuestURL() 
		+ "\" class=\"topic\">" + title + "</a></h2><div class=\"content\">" 
		+ text + "</div><ul class=\"tags\">" + tags 
		+ "</ul><div class=\"entry-info vote_holder answer-positive\">" 
		+ "<div class=\"corner tl\"></div><div class=\"corner tr\"></div>" 
		+ "<div class=\"entry-info-wrap\"><div class=\"mark\"><span>" 
		+ ( rating > 0 ? "+" : "" ) + String.valueOf(rating) 
		+ "</span></div><div class=\"informative\"><span>" 
		+ String.valueOf(answerCount) + " " + getAnswer() + "</span></div>" 
		+ "<div class=\"published\"><span>" + date + "</span></div>" 
		+ "<div class=\"favs_count\"><span>" + favsCount + "</span></div>" 
		+ "<div class=\"vcard author full\"><a href=\"http://" + author 
		+ ".habrahabr.ru/\" class=\"fn nickname url\"><span>" + author 
		+ "</span></a></div></div><div class=\"corner bl\"></div>" 
		+ "<div class=\"corner br\"></div></div></div>";
	}
	
	/**
	 * Возвращает HTML код ответа
	 * @param noContent скрыть контент
	 * @param noTags скрыть теги
	 * @param noMark скрыть оценку
	 * @param noAnswersCount скрыть кол-во ответов
	 * @param noDate скрыть дату
	 * @param noFavs скрыть кол-во добавлений в избранное
	 * @param noAuthor скрыть автора
	 * @return HTML код
	 */
	public String getDataAsHTML(boolean noContent, boolean noTags, boolean noMark, 
			boolean noAnswersCount, boolean noDate, boolean noFavs, boolean noAuthor) {
		return "<div class=\"hentry question_hentry\" id=\"" + String.valueOf(id) 
		+ "\"><h2 class=\"entry-title\"><a href=\"" + getQuestURL() 
		+ "\" class=\"topic\">" + title + "</a></h2>" 
		+ (noContent ? "" : "<div class=\"content\">" + text + "</div>")
		+ (noTags ? "" : "<ul class=\"tags\">" + tags + "</ul>")
		+ (noMark && noAnswersCount && noDate && noAuthor ? "" 
				: "<div class=\"entry-info vote_holder answer-positive\">" 
		+ "<div class=\"corner tl\"></div><div class=\"corner tr\"></div>" 
		+ "<div class=\"entry-info-wrap\">" 
		+ (noMark ? "" : "<div class=\"mark\"><span>" 
			+ ( rating > 0 ? "+" : "" ) + String.valueOf(rating) + "</span></div>") 
		+ (noAnswersCount ? "" : "<div class=\"informative\"><span>" 
			+ String.valueOf(answerCount) + " " + getAnswer() + "</span></div>") 
		+ (noDate ? "" : "<div class=\"published\"><span>" + date + "</span></div>")
		+ (noFavs ? "" : "<div class=\"favs_count\"><span>" + favsCount + "</span></div>")
		+ (noAuthor ? "" : "<div class=\"vcard author full\"><a href=\"http://" 
			+ author + ".habrahabr.ru/\" class=\"fn nickname url\"><span>" 
			+ author + "</span></a></div>") 
		+ "</div><div class=\"corner bl\"></div>" 
		+ "<div class=\"corner br\"></div></div>") + "</div>";
	}
	
	/**
	 * Вывод комментариев к вопросу
	 * @return HTML код комментариев
	 */
	public String getCommentsAsHTML() {
		String data = "";
		if(comments == null) return data;

		for(int i = 0; i < comments.length; i++) {
			data += comments[i].getDataAsHTML();
		}
			
		return data;
	}
	
	private String getAnswer() {
		int mod = answerCount / 10;
		
		if((answerCount >= 6 && answerCount <= 20) || mod == 0 || mod >= 5)
			return "ответов";
		else if(mod == 1) 
			return "ответ";
		else if(mod >= 2 && mod <= 4)
			return "ответа";
		
		return "ответов";
	}
}
