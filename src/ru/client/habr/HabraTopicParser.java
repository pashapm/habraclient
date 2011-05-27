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
	private Uri mBlogURI = null;
	
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
			mBlogURI = Uri.parse(blogHeader.getAttributeByName("href"));
		} else {
			blogHeader = mMainNode.findElementByAttValue("class", "profile-header company-header", true, true);
			if(blogHeader != null) {
				blogHeader = blogHeader.findElementByName("a", true);
				mBlogName = "Блог компании " + blogHeader.getText().toString();
				mBlogURI = Uri.parse(blogHeader.getAttributeByName("href") + "blog/");
			}
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
		
		topic.postType = getTopicType(currentNode.getAttributeByName("class"));

		// Parse title: Blog URL, Blog Name, Post Title, Post ID
		topic.blog = new HabraBlog();
		TagNode[] titleNodes = currentNode.findElementByName("h2", false).getElementsByName("a", false);
		if(titleNodes.length < 2) {
			// Full topic
			titleNodes = currentNode.findElementByName("h2", false).getChildTags();
			
			topic.blog.parseIdFromUri(mBlogURI);
			topic.blog.name = mBlogName;
			
			TagNode titleUriTag = titleNodes[0].getParent().findElementByAttValue("class", "topic", true, true);
			topic.title = titleUriTag.getText().toString();
			
			try {
				topic.id = Integer.valueOf(Uri.parse(titleUriTag.getAttributeByName("href")).getLastPathSegment());
			} catch(NullPointerException e) {
				topic.id = Integer.valueOf(Uri.parse(titleUriTag.getAttributeByName("title")).getLastPathSegment());
			}
		} else {
			// Topic in list
			Uri blogURI = Uri.parse(titleNodes[0].getAttributeByName("href"));
			topic.blog.parseIdFromUri(blogURI);
			topic.blog.name = titleNodes[0].getText().toString();

			topic.title = titleNodes[titleNodes.length - 1].getText().toString();
			List<String> pathSegments = Uri.parse(
					titleNodes[titleNodes.length - 1].getAttributeByName("href")).getPathSegments();
			topic.id = Integer.valueOf(pathSegments.get(pathSegments.size() - 1));
		}
		
		Log.i("HabraTopicParser.parse", "ID: " + topic.id + "\nTitle: " + topic.title);
		
		// Parse content
		TagNode content = currentNode.findElementByAttValue("class", "content", false, true);
		if(mBlogURI != null) {
			// In full post habracut is '<a name="habracut" />', remove this for unlink text after it.
			content.removeChild(content.findElementByAttValue(
					"name", "habracut", false, true));
		}
		topic.content = mParser.getInnerHtml(content);
		
		// Parse tags
		try {
			TagNode[] tagsNodes = currentNode.findElementByAttValue("class", "tags", false, true).getChildTags();
			topic.tags = new String[tagsNodes.length];
			for(int i = 0; i < tagsNodes.length; i++)
				topic.tags[i] = tagsNodes[i].getText().toString();
		} catch(NullPointerException e) {
			Log.w("HabraTopicParser.parse", "No tags: " + e.getMessage());
		}
			
		// Parse information: Mark, Date, Favorites, Author, Comments count
		TagNode infoNode = currentNode.findElementByAttValue("class", 
				"entry-info-wrap", true, true);
		
		String ratings = "";
		try {
			ratings = infoNode.findElementByAttValue("class", "mark", 
					true, false).findElementByName("a", true).getText().toString();
		} catch(NullPointerException e) {
			ratings = infoNode.findElementByAttValue("class", "mark",
					true, false).findElementByName("span", true).getText().toString();
		}
		
		topic.rating = ratings.charAt(0) == '-' ? -1 : +1;
		ratings = "0" + ratings.substring(1);
		try {
			topic.rating *= Integer.valueOf(ratings);
		} catch(NumberFormatException e) {
			topic.rating = 99999;
		}
		
		topic.date = infoNode.findElementByAttValue("class", "published", false, 
				true).findElementByName("span", false).getText().toString();
		
		topic.inFavs = infoNode.findElementByAttValue("class", "js-to_favs_remove", true, true) != null;
		try {
			topic.favoritesCount = Integer.valueOf(infoNode.findElementByAttValue(
					"class", "favs_count", false, true).getText().toString());
		} catch(NumberFormatException e) {
			Log.w("HabraTopicParser.parse", "NumberFormatException: " + e.getMessage());
			topic.favoritesCount = 0;
		}
		
		TagNode additional = null;
		switch(topic.postType) {
		case LINK: additional = infoNode.findElementByAttValue("class", "link", 
					false, true).findElementByName("a", true); break;
		case TRANSLATE: additional = infoNode.findElementByAttValue("class", "original-author", 
				false, true).findElementByName("a", true); break;
		case PODCAST: break;
		}
		if(additional != null) {
			topic.additional = "<a href=\"" + additional.getAttributeByName("href") 
					+ "\">" + additional.getText().toString() + "</a>";
		}
		
		try {
			topic.author = infoNode.findElementByAttValue("class", "vcard author full", 
					false, true).findElementByName("span", true).getText().toString();
		} catch(NullPointerException e) {
			Log.w("HabraTopicParser.parse", "NullPointerException: " + e.getMessage());
			topic.author = "";
		}
		
		if(mBlogURI == null) {
			// Topic form list
			TagNode[] commentNodes = infoNode.findElementByAttValue("class", 
					"comments", false, true).getChildTags()[0].getChildTags();
			
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
