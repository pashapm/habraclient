package ru.client.habr;

import android.net.Uri;
import android.util.Log;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WNeZRoS
 * ����� ��� �������� ������
 */
public final class HabraTopicParser {
	private int mListIndex = 0;
	private HtmlCleaner mParser = null;
	private TagNode mMainNode = null;
	private List<TagNode> mEntryNodeList = new ArrayList<TagNode>();
	private String mBlogName = null;
	private String mBlogURL = null;
	
	/**
	 * ������ ������
	 * @param data ��� ��������
	 */
	public HabraTopicParser(String data) {	
		if(data == null) return;
		
		mParser = new HtmlCleaner();
		mMainNode = mParser.clean(data);
		
		TagNode blogHeader = mMainNode.findElementByAttValue("class", "blog-header", true, true);
		if(blogHeader != null) {
			blogHeader = blogHeader.getChildTags()[0];
			mBlogName = blogHeader.getText().toString();
			mBlogURL = blogHeader.getAttributeByName("href");
		}
		
		TagNode[] divNodes = mMainNode.getElementsByName("div", true);	
		for(int i = 0; i < divNodes.length; i++) {
			if(divNodes[i].getAttributeByName("class") != null && divNodes[i].getAttributeByName("id") == null) {
				Log.d("HabraTopicParser.construct", "checking node... " + divNodes[i].getAttributeByName("class"));
				if(divNodes[i].getAttributeByName("class").startsWith("hentry")) {
					mEntryNodeList.add(divNodes[i]);
					Log.d("HabraTopicParser.construct", "ok ... lis size is " + mEntryNodeList.size());
				}
			}
		}
	}
	
	public HabraTopic parse() {
		if(mMainNode == null) return null;
		if(mEntryNodeList.size() <=  mListIndex) return null;
		
		HabraTopic topic = new HabraTopic();
		
		TagNode currentNode = mEntryNodeList.get(mListIndex);
		TagNode[] contentNodes = currentNode.getChildTags();
		
		topic.postType = getTopicType(currentNode.getAttributeByName("class"));

		// Parse title: Blog URL, Blog Name, Post Title, Post ID
		TagNode[] titleNodes = contentNodes[0].getChildTags();
		if(titleNodes.length == 1) {
			// Full topic
			topic.blogURL = mBlogURL;
			topic.blogName = mBlogName;
			topic.title = titleNodes[0].getText().toString();
			List<String> pathSegments = Uri.parse(
					titleNodes[0].getAttributeByName("title")).getPathSegments();
			topic.isCorporativeBlog = pathSegments.get(0).equals("company");
			topic.id = Integer.valueOf(pathSegments.get(pathSegments.size() - 1));
		} else {
			// Topic in list
			topic.blogURL = titleNodes[0].getAttributeByName("href");
			topic.blogName = titleNodes[0].getText().toString();
			topic.title = titleNodes[titleNodes.length - 1].getText().toString();
			List<String> pathSegments = Uri.parse(
					titleNodes[titleNodes.length - 1].getAttributeByName("href")).getPathSegments();
			topic.isCorporativeBlog = pathSegments.get(0).equals("company");
			topic.id = Integer.valueOf(pathSegments.get(pathSegments.size() - 1));
		}
		
		Log.i("HabraTopicParser.parse", "ID: " + topic.id + "\nTitle: " + topic.title);
		
		// Parse content
		topic.content = mParser.getInnerHtml(contentNodes[1]);
		
		// Parse tags
		TagNode[] tagsNodes = contentNodes[2].getChildTags();
		topic.tags = new String[tagsNodes.length];
		for(int i = 0; i < tagsNodes.length; i++)
			topic.tags[i] = tagsNodes[i].getText().toString();
		
		// Parse information: Mark, Date, Favorites, Author, Comments count
		TagNode[] infoNodes = contentNodes[3].findElementByAttValue("class", 
				"entry-info-wrap", false, true).getChildTags();
		
		topic.rating = infoNodes[0].findElementByAttValue("class", "mark", false, true).findElementByName("a", true).getText().toString();
		topic.date = infoNodes[1].findElementByName("span", false).getText().toString();
		
		topic.inFavs = infoNodes[2].getAttributeByName("class").equals("js-to_favs_remove");
		try {
			topic.favoritesCount = Integer.valueOf(infoNodes[3].getText().toString());
		} catch(NumberFormatException e) {
			Log.w("HabraTopicParser.parse", "NumberFormatException: " + e.getMessage());
			topic.favoritesCount = 0;
		}
		
		if(infoNodes.length > 8 || (infoNodes.length > 7 && mBlogURL != null)) {
			// Additional information: original link
			TagNode additional = infoNodes[6].findElementByName("a", true);
			topic.additional = "<a href=\"" + additional.getAttributeByName("href") 
					+ "\">" + additional.getText().toString() + "</a>";
			topic.author = infoNodes[7].findElementByName("span", true).getText().toString();
		} else {
			topic.additional = "";
			topic.author = infoNodes[6].findElementByName("span", true).getText().toString();
		}
		
		if(mBlogURL == null) {
			// Topic form list
			TagNode[] commentNodes = infoNodes[infoNodes.length - 1].getChildTags()[0].getChildTags();
			
			try {
				topic.commentsCount = Integer.valueOf(commentNodes[0].getText().toString());
			} catch(NumberFormatException e) {
				Log.w("HabraTopicParser.parse", "NumberFormatException: " + e.getMessage());
				topic.commentsCount = 0;
			}
			
			if(commentNodes.length == 2)
				topic.commentsDiff = Integer.valueOf(commentNodes[1].getText().toString().substring(1));
		} else {
			// Full topic
			topic.commentsDiff = 0;
			
			try {
				topic.commentsCount = Integer.valueOf(mMainNode
						.findElementByAttValue("class", "js-comments-count", 
								true, true).getText().toString());
			} catch(NumberFormatException e) {
				Log.w("HabraTopicParser.parse", "NumberFormatException: " + e.getMessage());
				topic.commentsCount = 0;
			}
		}
		
		mListIndex++;
		return topic;
	}
	
	private HabraTopic.HabraTopicType getTopicType(String classString) {
		if(classString.contains("link")) return HabraTopic.HabraTopicType.LINK;
		if(classString.contains("translate")) return HabraTopic.HabraTopicType.TRANSLATE;
		if(classString.contains("podcast")) return HabraTopic.HabraTopicType.PODCAST;
		return HabraTopic.HabraTopicType.POST;
	}
}
