package ru.habrahabr;

import android.util.Log;
import ru.habrahabr.HabraTopic.HabraTopicType;

public class HabraCutTopicParser 
{
	String mData = null;		// ������ ��������
	int mStartPosition = 0;		// �������� ������� ���������� ������
	
	/**
	 * ������ ������� � ������� ��������
	 * @param data ��� ��������
	 */
	public HabraCutTopicParser(String data)
	{
		mData = data;
		mStartPosition = 0;
	}
	
	/**
	 * �������� ��������� �����
	 * @return ������ ������
	 */
	public HabraTopic parseTopic()
	{
		if(mData == null || mStartPosition == -1) return null;
		
		Log.d("TopicParser", "Find post");
		
		// ������� ������ �����
    	int startPosition = mData.indexOf("<div class=\"hentry ", mStartPosition);
    	if(startPosition == -1) return null;
    	
    	Log.d("TopicParser", "Find end");
    	
    	// ���� �����
    	int endPosition = mData.indexOf("<div class=\"corner bl\"></div><div class=\"corner br\"></div>", startPosition);
    	if(endPosition == -1) return null;
    	
    	Log.d("TopicParser", "SubString");
    	// �������� ������ �����
    	String topicData = new String(mData.substring(startPosition, endPosition)) + "</div></div>";
    	HabraTopic topic = new HabraTopic();
    	
    	Log.d("TopicParser", "Parse data");
    	// ������ ������ �����
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
    	String ids = topicData.substring(
    			lastIndex = (topicData.indexOf(topic.getBlogURL(), lastIndex) + topic.getBlogURL().length()), 
    			lastIndex = topicData.indexOf('/', lastIndex));
    	Log.i("TopicData", ids == null ? "null" : ids);
    	topic.id = Integer.parseInt(ids);

    	topic.title = new String(topicData.substring(
    			lastIndex = (topicData.indexOf('>', lastIndex) + 1), 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	Log.i("TopicData", topic.title);
    	
    	Log.d("TopicParser", "Parse Content");
    	lastIndex = topicData.indexOf("<div class=\"content\">", lastIndex) + 21;
    	topic.content = new String(topicData.substring(lastIndex, lastIndex = topicData.indexOf("<ul class=\"tags\">", lastIndex)));
    	
    	Log.d("TopicParser", "Parse tags");
    	lastIndex += 17;
    	topic.tags = new String(topicData.substring(lastIndex, lastIndex = topicData.indexOf("</ul>", lastIndex)));
    	
    	Log.d("TopicParser", "Parse rating");
    	topic.rating = new String(topicData.substring(
    			lastIndex = (topicData.indexOf('>', (topicData.indexOf("mark\">", lastIndex) + 6)) + 1), 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse date");
    	topic.date = new String(topicData.substring(
    			lastIndex = (topicData.indexOf("<span>", topicData.indexOf("<div class=\"published\">", lastIndex)) + 6),
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse favorites");
    	String favs = topicData.substring(
    			lastIndex = (topicData.indexOf('>', topicData.indexOf("<div class=\"favs", lastIndex)) + 1), 
    			lastIndex = topicData.indexOf('<', lastIndex));
    	if(favs.length() > 0) topic.favorites = Integer.parseInt(favs);
    	else topic.favorites = 0;
    	
    	Log.d("TopicParser", "Parse author");
    	topic.author = new String(topicData.substring(
    			lastIndex = (topicData.indexOf("url\"><span>", lastIndex) + 11), 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Parse comments");
    	String comments = topicData.substring(
    			lastIndex = (topicData.indexOf("<span class=\"all\">", lastIndex) + 18), 
    			lastIndex = topicData.indexOf('<', lastIndex));
    	Log.i("TopicData", comments + "(" + comments.codePointAt(0) + ")");
    	if(comments.codePointAt(0) != 1082) topic.commentsCount = Integer.parseInt(comments);
    	else topic.commentsCount = 0;
    	
    	lastIndex = topicData.indexOf("<span class=\"new\">", lastIndex);
    	if(lastIndex == -1) topic.commentsDiff = 0;
    	else topic.commentsDiff = Integer.parseInt(topicData.substring(
    			lastIndex += 18, 
    			lastIndex = topicData.indexOf('<', lastIndex)));
    	
    	Log.d("TopicParser", "Save position");
    	// ��������� �������� �������
    	mStartPosition = endPosition;
		return topic;
	}
	
	
}
