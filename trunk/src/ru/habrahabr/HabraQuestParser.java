package ru.habrahabr;

import android.util.Log;

public class HabraQuestParser 
{
	String mData = null;		// Данные страницы
	int mStartPosition = 0;		// Конечная позиция последнего поиска
	
	/**
	 * Парсер топиков с главной страницы
	 * @param data Код страницы
	 */
	public HabraQuestParser(String data)
	{
		mData = data;
		mStartPosition = 0;
	}
	
	/**
	 * Получаем следующий вопрос из списка
	 * @return данные вопроса
	 */
	public HabraQuest parseQuestFromList()
	{
		if(mData == null || mStartPosition == -1) return null;
		
		Log.d("QuestParser", "Find quest");
		
		// Находим начало вопроса
    	int startPosition = mData.indexOf("<div class=\"hentry ", mStartPosition);
    	if(startPosition == -1) return null;
    	
    	Log.d("QuestParser", "Find end");
    	
    	// Ищем конец
    	int endPosition = mData.indexOf("<div class=\"corner bl\"></div><div class=\"corner br\"></div>", startPosition);
    	if(endPosition == -1) return null;
    	
    	Log.d("QuestParser", "SubString");
    	// Вырезаем данные поста
    	String questData = new String(mData.substring(startPosition, endPosition)) + "</div></div>";
    	HabraQuest quest = new HabraQuest();
    	
    	Log.d("QuestParser", "Parse data");
    	// Парсим данные
    	int lastIndex = 0;
    	
    	Log.d("QuestParser", "Parse Title");
    	quest.title = new String(questData.substring(
    			lastIndex = (questData.indexOf("class=\"topic\">", lastIndex) + 14), 
    			lastIndex = questData.indexOf('<', lastIndex)));
  
    	Log.i("QuestData", quest.title);
    	
    	Log.d("QuestParser", "Parse Content");
		quest.text = new String(questData.substring(
				lastIndex = questData.indexOf("<div class=\"content\">", lastIndex), 
				lastIndex = questData.indexOf("<ul class=\"tags\">", lastIndex)));
		Log.d("QuestParser", "Parse tags");
    	lastIndex += 17;
    	quest.tags = new String(questData.substring(lastIndex, lastIndex = questData.indexOf("</ul>", lastIndex)));   	

    	quest.accepted = questData.indexOf("answer-accepted", lastIndex) != -1;
    	
    	Log.d("QuestParser", "Parse ID");
    	quest.id = Integer.parseInt(questData.substring(
    			lastIndex = (questData.indexOf("id=\"infopanel", lastIndex) + 13), 
    			lastIndex = questData.indexOf('"', lastIndex)));
    	
    	Log.d("QuestParser", "Parse rating");
    	String rs = questData.substring(
    			lastIndex = (questData.indexOf('>', (questData.indexOf("mark\">", lastIndex) + 6)) + 1), 
    			lastIndex = questData.indexOf('<', lastIndex));
    	if(rs.charAt(0) == '-') quest.rating = -1; else quest.rating = 1;
    	rs = "0" + rs.substring(1);
    	quest.rating *= Integer.parseInt(rs);
    	
    	Log.d("QuestParser", "Parse answers count");
    	int aci = (questData.indexOf("#comments\">", lastIndex) + 11);
    	if(aci != 10)
    	{
	    	String acs = questData.substring(aci, lastIndex = questData.indexOf(' ', aci));
	    	if(acs.codePointAt(0) == 1073) quest.answerCount = 0;
	    	else quest.answerCount = Integer.parseInt(acs);
    	}
    	
    	
    	Log.d("QuestParser", "Parse date");
    	quest.date = new String(questData.substring(
    			lastIndex = (questData.indexOf("<span>", questData.indexOf("<div class=\"published\">", lastIndex)) + 6),
    			lastIndex = questData.indexOf('<', lastIndex)));
    	
    	Log.d("QuestParser", "Parse favorites");
    	
    	quest.inFavs = questData.indexOf("js-to_favs_remove", lastIndex) != -1;
    	
    	String favs = questData.substring(
    			lastIndex = (questData.indexOf('>', questData.indexOf("<div class=\"favs", lastIndex)) + 1), 
    			lastIndex = questData.indexOf('<', lastIndex));
    	if(favs.length() > 0) quest.favsCount = Integer.parseInt(favs);
    	else quest.favsCount = 0;
    	
    	Log.d("QuestParser", "Parse author");
    	quest.author = new String(questData.substring(
    			lastIndex = (questData.indexOf("url\"><span>", lastIndex) + 11), 
    			lastIndex = questData.indexOf('<', lastIndex)));
    	//js-comments-count">
    	
    	Log.d("QuestParser", "Save position");
    	// Сохраняем конечную позицию
    	mStartPosition = endPosition;
		return quest;
	}
	
	/**
	 * Получаем полный вопрос
	 * @return данные вопроса
	 */
	public HabraQuest parseFullQuest()
	{
		if(mData == null) return null;
		
		Log.d("QuestParser", "Find quest");
		
		// Находим начало вопроса
    	int lastIndex = mData.indexOf("<div class=\"hentry ", mStartPosition);
    	if(lastIndex == -1) return null;
    	
    	HabraQuest quest = new HabraQuest();
    	
    	Log.d("QuestParser", "Parse data");
    	// Парсим данные
    	
    	Log.d("QuestParser", "Parse Title");
    	quest.title = new String(mData.substring(
    			lastIndex = (mData.indexOf("class=\"topic\">", lastIndex) + 14), 
    			lastIndex = mData.indexOf('<', lastIndex)));
  
    	Log.i("QuestData", quest.title);
    	
    	Log.d("QuestParser", "Parse Content");
		quest.text = new String(mData.substring(
				lastIndex = mData.indexOf("<div class=\"content\">", lastIndex), 
				lastIndex = mData.indexOf("<ul class=\"tags\">", lastIndex)));
		Log.d("QuestParser", "Parse tags");
    	lastIndex += 17;
    	quest.tags = new String(mData.substring(lastIndex, lastIndex = mData.indexOf("</ul>", lastIndex)));   	

    	quest.accepted = mData.indexOf("answer-accepted", lastIndex) != -1;
    	
    	Log.d("QuestParser", "Parse ID");
    	quest.id = Integer.parseInt(mData.substring(
    			lastIndex = (mData.indexOf("id=\"infopanel", lastIndex) + 13), 
    			lastIndex = mData.indexOf('"', lastIndex)));
    	
    	Log.d("QuestParser", "Parse rating");
    	quest.rating = Integer.parseInt(mData.substring(
    			lastIndex = (mData.indexOf('>', (mData.indexOf("mark\">", lastIndex) + 6)) + 1), 
    			lastIndex = mData.indexOf('<', lastIndex)));
    	
    	Log.d("QuestParser", "Parse answers count");
    	int aci = (mData.indexOf("#comments\">", lastIndex) + 11);
    	if(aci != 10)
    	{
	    	String acs = mData.substring(aci, lastIndex = mData.indexOf(' ', aci));
	    	Log.d("acs", String.valueOf(acs.codePointAt(0)));
	    	quest.answerCount = Integer.parseInt(acs);
    	}
    	
    	Log.d("QuestParser", "Parse date");
    	quest.date = new String(mData.substring(
    			lastIndex = (mData.indexOf("<span>", mData.indexOf("<div class=\"published\">", lastIndex)) + 6),
    			lastIndex = mData.indexOf('<', lastIndex)));
    	
    	Log.d("QuestParser", "Parse favorites");
    	
    	quest.inFavs = mData.indexOf("js-to_favs_remove", lastIndex) != -1;
    	
    	String favs = mData.substring(
    			lastIndex = (mData.indexOf('>', mData.indexOf("<div class=\"favs", lastIndex)) + 1), 
    			lastIndex = mData.indexOf('<', lastIndex));
    	if(favs.length() > 0) quest.favsCount = Integer.parseInt(favs);
    	else quest.favsCount = 0;
    	
    	Log.d("QuestParser", "Parse author");
    	quest.author = new String(mData.substring(
    			lastIndex = (mData.indexOf("url\"><span>", lastIndex) + 11), 
    			lastIndex = mData.indexOf('<', lastIndex)));
    	    	
    	lastIndex = mData.indexOf("js-comments-count\">", lastIndex) + 19;
    	if(lastIndex != 18)
    	{
    		quest.answerCount = Integer.parseInt(mData.substring(lastIndex, mData.indexOf('<', lastIndex)));
    	}
    	else quest.answerCount = 0;
    	
    	return quest;
	}
}
