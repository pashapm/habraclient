package ru.client.habr;

import android.util.Log;
import ru.client.habr.HabraTopic.HabraTopicType;

public class HabraTopicParser 
{
	String mData = null;		// Данные страницы
	int mStartPosition = 0;		// Конечная позиция последнего поиска
	
	/**
	 * Парсер топиков с главной страницы
	 * @param data Код страницы
	 */
	public HabraTopicParser(String data)
	{
		mData = data;
		mStartPosition = 0;
	}
	
	/**
	 * Получаем следующий топик из списка
	 * @return данные топика
	 */
	public HabraTopic parseTopicFromList()
	{
		if(mData == null || mStartPosition == -1) return null;
		
		Log.d("TopicParser", "Find post");
		
		// Находим начало поста
    	int startPosition = mData.indexOf("<div class=\"hentry ", mStartPosition);
    	if(startPosition == -1) return null;
    	
    	Log.d("TopicParser", "Find end");
    	
    	// Ищем конец
    	int endPosition = mData.indexOf("<div class=\"corner bl\"></div><div class=\"corner br\"></div>", startPosition);
    	if(endPosition == -1) return null;
    	
    	Log.d("TopicParser", "SubString");
    	// Вырезаем данные поста
    	String topicData = new String(mData.substring(startPosition, endPosition)) + "</div></div>";
    	HabraTopic topic = new HabraTopic();
    	
    	Log.d("TopicParser", "Parse data");
    	// Парсим данные поста
    	int lastIndex = 19;
    	
    	Log.d("TopicParser", "Parse Type");
    	String type = new String(topicData.substring(19, lastIndex = topicData.indexOf("\"", lastIndex)));
    	if(type != null)
    	{
    		Log.i("TopicData", type);
    		// blogs [XxX] | corporative [XxX]
    		// XxX: translation link
    		
    		if(type.charAt(0) == 'c') topic.isCorporativeBlog = true;
    		
    		int indexAfterSpace = type.indexOf(' ', 5) + 1;
    		if(indexAfterSpace == 0)
    			topic.type = HabraTopicType.Post;
    		else switch(type.charAt(indexAfterSpace))
    		{
    		case 't': topic.type = HabraTopicType.Translate; break;
    		case 'l': topic.type = HabraTopicType.Link; break;
    		case 'p': topic.type = HabraTopicType.Podcast; break;
    		}
    	}
    	
    	Log.d("TopicParser", "Parse Blog Data");
    	String blog = new String(topicData.substring(
    			lastIndex = topicData.indexOf("<a ", lastIndex), 
    			lastIndex = topicData.indexOf("</a>", lastIndex)));
    	
    	if(topic.isCorporativeBlog)
    	{
	    	int subIndex = blog.indexOf("/company/") + 9;
	    	topic.blogID = new String(blog.substring(subIndex, blog.indexOf('/', subIndex)));
    	}
    	else
    	{
    		int subIndex = blog.indexOf("/blogs/") + 7;
	    	topic.blogID = new String(blog.substring(subIndex, blog.indexOf('/', subIndex)));
    	}
    	topic.blogName = new String(blog.substring(blog.indexOf("\">") + 2));
    	
    	Log.i("TopicData", topic.blogID);
    	Log.i("TopicData", topic.blogName);
    	
    	Log.d("TopicGet", topic.getBlogURL());
    	
    	Log.d("TopicParser", "Parse ID and Title");
    	if(topic.type != HabraTopicType.Link)
    	{
	    	topic.id = Integer.valueOf(topicData.substring(
	    			lastIndex = (topicData.indexOf(topic.getBlogURL(), lastIndex) + topic.getBlogURL().length()), 
	    			lastIndex = topicData.indexOf('/', lastIndex)));
    	}
    	else
    	{
	    	topic.id = Integer.valueOf(topicData.substring(
	    			lastIndex = (topicData.indexOf("/linker/go/", lastIndex) + 11), 
	    			lastIndex = topicData.indexOf('/', lastIndex)));
    	}

    	topic.title = new String(topicData.substring(
    			lastIndex = (topicData.indexOf('>', lastIndex) + 1), 
    			lastIndex = topicData.indexOf("</a>", lastIndex)));
  
    	Log.i("TopicData", topic.title);
    	
    	Log.d("TopicParser", "Parse Content");
    	lastIndex = topicData.indexOf("<div class=\"content\">", lastIndex) + 21;
    	int endIndex = topicData.indexOf("<ul class=\"tags\">", lastIndex);
    	if(endIndex == -1) 
    	{
    		endIndex = topicData.indexOf("<div class=\"entry-info", lastIndex);
    		topic.content = new String(topicData.substring(lastIndex, endIndex));
    		topic.tags = "";
    	}
    	else
    	{
    		topic.content = new String(topicData.substring(lastIndex, endIndex));
    		Log.d("TopicParser", "Parse tags");
        	endIndex += 17;
        	topic.tags = new String(topicData.substring(endIndex, lastIndex = topicData.indexOf("</ul>", endIndex)));
    	}
    	

    	Log.d("TopicParser", "Parse rating");
    	topic.rating = new String(topicData.substring(
    			lastIndex = (topicData.indexOf('>', (topicData.indexOf("mark\">", lastIndex) + 6)) + 1), 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse date");
    	topic.date = new String(topicData.substring(
    			lastIndex = (topicData.indexOf("<span>", topicData.indexOf("<div class=\"published\">", lastIndex)) + 6),
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse favorites");
    	
    	topic.inFavs = topicData.indexOf("js-to_favs_remove", lastIndex) != -1;
    	
    	String favs = topicData.substring(
    			lastIndex = (topicData.indexOf('>', topicData.indexOf("<div class=\"favs", lastIndex)) + 1), 
    			lastIndex = topicData.indexOf('<', lastIndex));
    	if(favs.length() > 0) topic.favorites = Integer.valueOf(favs);
    	else topic.favorites = 0;
    	
    	if(topicData.indexOf("class=\"vcard", lastIndex) != -1)
    	{
	    	Log.d("TopicParser", "Parse author");
	    	topic.author = new String(topicData.substring(
	    			lastIndex = (topicData.indexOf("url\"><span>", lastIndex) + 11), 
	    			lastIndex = topicData.indexOf('<', lastIndex)));
    	}
    	else
    	{
    		topic.author = "";
    	}
    	
    	Log.d("TopicParser", "Parse comments");
    	String comments = topicData.substring(
    			lastIndex = (topicData.indexOf("<span class=\"all\">", lastIndex) + 18), 
    			lastIndex = topicData.indexOf('<', lastIndex));
    	Log.i("TopicData", comments + "(" + comments.codePointAt(0) + ")");
    	if(comments.codePointAt(0) != 1082) topic.commentsCount = Integer.valueOf(comments);
    	else topic.commentsCount = 0;
    	
    	lastIndex = topicData.indexOf("<span class=\"new\">", lastIndex);
    	if(lastIndex == -1) topic.commentsDiff = 0;
    	else topic.commentsDiff = Integer.valueOf(topicData.substring(
    			lastIndex += 19, 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Save position");
    	// Сохраняем конечную позицию
    	mStartPosition = endPosition;
		return topic;
	}
	
	/**
	 * Получаем полный топик
	 * @return данные топика
	 */
	public HabraTopic parseFullTopic()
	{
		if(mData == null) return null;
		
		Log.d("TopicParser", "Find post");
		
		// Находим начало поста
    	int startPosition = mData.indexOf("<div class=\"hentry ");
    	if(startPosition == -1) return null;
    	
    	HabraTopic topic = new HabraTopic();
    	
    	Log.d("TopicParser", "Parse data");
    	// Парсим данные поста
    	int lastIndex = startPosition + 19;
    	
    	Log.d("TopicParser", "Parse Type");
    	int endIndex = mData.indexOf("\"", lastIndex);
    	String type = (lastIndex == endIndex ? null : new String(mData.substring(lastIndex, endIndex)));
    	if(type != null)
    	{
    		Log.i("TopicData", type);
    		// blogs [XxX] | corporative [XxX]
    		// XxX: translation link
    		
    		if(type.charAt(0) == 'c') topic.isCorporativeBlog = true;
    		
    		int indexAfterSpace = type.indexOf(' ', 5) + 1;
    		if(indexAfterSpace == 0)
    			topic.type = HabraTopicType.Post;
    		else switch(type.charAt(indexAfterSpace))
    		{
    		case 't': topic.type = HabraTopicType.Translate; break;
    		case 'l': topic.type = HabraTopicType.Link; break;
    		case 'p': topic.type = HabraTopicType.Podcast; break;
    		}
    	}
    	
    	Log.d("TopicParser", "Parse Blog Data");
    	int blogIndex = mData.indexOf("class=\"blog-header");
    	if(blogIndex == -1) 
    	{
    		blogIndex = mData.indexOf("class=\"profile-header");
    		topic.isCorporativeBlog = true;
    	}
    	
    	String blog = new String(mData.substring(
    			blogIndex = mData.indexOf("<a ", blogIndex), 
    			blogIndex = mData.indexOf("</a>", blogIndex)));
    	
    	if(topic.isCorporativeBlog)
    	{
	    	int subIndex = blog.indexOf("/company/") + 9;
	    	topic.blogID = new String(blog.substring(subIndex, blog.indexOf('/', subIndex)));
    	}
    	else
    	{
    		int subIndex = blog.indexOf("/blogs/") + 7;
	    	topic.blogID = new String(blog.substring(subIndex, blog.indexOf('/', subIndex)));
    	}
    	topic.blogName = new String(blog.substring(blog.indexOf("\">") + 2));
    	
    	Log.i("TopicData", topic.blogID);
    	Log.i("TopicData", topic.blogName);
    	
    	Log.d("TopicGet", topic.getBlogURL());
    	
    	Log.d("TopicParser", "Parse ID and Title");
    	topic.id = Integer.valueOf(mData.substring(
    			lastIndex = (mData.indexOf(topic.getBlogURL(), lastIndex) + topic.getBlogURL().length()), 
    			lastIndex = mData.indexOf('/', lastIndex)));
    	
    	Log.i("TopicData", "ID: " + topic.id);
    	
    	topic.title = new String(mData.substring(
    			lastIndex = (mData.indexOf('>', lastIndex) + 1), 
    			lastIndex = mData.indexOf('<', lastIndex)));
    	
    	while(topic.title.length() == 0)
    	{
    		topic.title = new String(mData.substring(
        			lastIndex = (mData.indexOf('>', lastIndex) + 1), 
        			lastIndex = mData.indexOf('<', lastIndex)));
    	}
    	
    	Log.i("TopicData", "Title[" + topic.title.length() + "]: " + topic.title);
    	
    	Log.d("TopicParser", "Parse Content");
    	lastIndex = mData.indexOf("<div class=\"content\">", lastIndex) + 21;
    	endIndex = mData.indexOf("<ul class=\"tags\">", lastIndex);
    	if(endIndex == -1) 
    	{
    		endIndex = mData.indexOf("<div class=\"entry-info", lastIndex);
    		topic.content = new String(mData.substring(lastIndex, endIndex));
    		topic.tags = "";
    	}
    	else
    	{
    		topic.content = new String(mData.substring(lastIndex, endIndex));
    		Log.d("TopicParser", "Parse tags");
        	endIndex += 17;
        	topic.tags = new String(mData.substring(endIndex, lastIndex = mData.indexOf("</ul>", endIndex)));
    	}
    	
    	Log.d("TopicParser", "Parse rating");
    	topic.rating = new String(mData.substring(
    			lastIndex = (mData.indexOf('>', (mData.indexOf("mark\">", lastIndex) + 6)) + 1), 
    			lastIndex = mData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse date");
    	topic.date = new String(mData.substring(
    			lastIndex = (mData.indexOf("<span>", mData.indexOf("<div class=\"published\">", lastIndex)) + 6),
    			lastIndex = mData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse favorites");
    	
    	topic.inFavs = mData.indexOf("js-to_favs_remove", lastIndex) != -1;
    	
    	String favs = mData.substring(
    			lastIndex = (mData.indexOf('>', mData.indexOf("<div class=\"favs", lastIndex)) + 1), 
    			lastIndex = mData.indexOf('<', lastIndex));
    	if(favs.length() > 0) topic.favorites = Integer.valueOf(favs);
    	else topic.favorites = 0;
    	
    	if(mData.indexOf("class=\"vcard", lastIndex) != -1)
    	{
	    	Log.d("TopicParser", "Parse author");
	    	topic.author = new String(mData.substring(
	    			lastIndex = (mData.indexOf("url\"><span>", lastIndex) + 11), 
	    			lastIndex = mData.indexOf('<', lastIndex)));
    	}
    	else
    	{
    		topic.author = "";
    	}
    	
    	lastIndex = mData.indexOf("js-comments-count\">", lastIndex) + 19;
    	if(lastIndex != 18)
    	{
    		topic.commentsCount = Integer.valueOf(mData.substring(lastIndex, mData.indexOf('<', lastIndex)));
    	}
    	else topic.commentsCount = 0;
    	
    	topic.commentsDiff = 0;
    	
    	return topic;
	}
}
