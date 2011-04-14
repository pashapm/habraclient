package ru.habrahabr;

public class HabraQuest 
{
	int id = 0;					// ID
	String title = null;		// Заголовок
	String text = null;			// Текст вопроса
	String tags = null;			// Тэги
	int rating = 0;				// Рейтинг
	String date = null;			// Дата публикации
	String author = null;		// Автор
	int favsCount = 0;			// Кол-во добавлений в избранное
	int answerCount = 0;		// Кол-во ответов
	boolean inFavs = false; 	// В избранном
	
	public boolean voteUp(URLClient url)
	{
		String[][] post = {{"action","vote"}, {"target_name","qa_question"}, 
				{"target_id",String.valueOf(id)}, {"mark", "1"}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean voteDown(URLClient url)
	{
		String[][] post = {{"action","vote"}, {"target_name","qa_question"}, 
				{"target_id",String.valueOf(id)}, {"mark", "-1"}};
		return url.postURL("http://habrahabr.ru/ajax/voting/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
	}
	
	public boolean addToFavorites(URLClient url)
	{
		String[][] post = {{"action","add"}, {"target_type","questions"}, {"target_id",String.valueOf(id)}};
		inFavs = url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		if(inFavs) favsCount++;
		return inFavs;
	}
	
	public boolean removeFromFavorites(URLClient url)
	{
		String[][] post = {{"action","remove"}, {"target_type","questions"}, {"target_id",String.valueOf(id)}};
		inFavs = !url.postURL("http://habrahabr.ru/ajax/favorites/", post, 
				"http://habrahabr.ru/qa/").contains("<message>ok</message>");
		if(!inFavs) favsCount--;
		return !inFavs;
	}
}
