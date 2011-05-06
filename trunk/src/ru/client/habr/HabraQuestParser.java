package ru.client.habr;

import android.util.Log;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WNeZRoS
 * ����� ��� �������� ��������
 */
public final class HabraQuestParser {
	private int mListIndex = 0;
	private HtmlCleaner mParser = null;
	private TagNode mMainNode = null;
	private List<TagNode> mEntryNodeList = new ArrayList<TagNode>();
	
	/**
	 * ������ ������� � ������� ��������
	 * @param data ��� ��������
	 */
	public HabraQuestParser(String data) {
		if(data == null) return;
		
		mParser = new HtmlCleaner();
		mMainNode = mParser.clean(data);
		
		TagNode[] divNodes = mMainNode.getElementsByName("div", true);	
		for(int i = 0; i < divNodes.length; i++) {
			if(divNodes[i].getAttributeByName("class") != null && divNodes[i].getAttributeByName("id") == null) {
				Log.d("HabraQuestParser.construct", "checking node... " + divNodes[i].getAttributeByName("class"));
				if(divNodes[i].getAttributeByName("class").startsWith("hentry")) {
					mEntryNodeList.add(divNodes[i]);
					Log.d("HabraQuestParser.construct", "ok ... list size is " + mEntryNodeList.size());
				}
			}
		}
	}
	
	public HabraQuest parse() {
		if(mMainNode == null) return null;
		if(mEntryNodeList.size() <=  mListIndex) return null;
		
		HabraQuest quest = new HabraQuest();
		
		TagNode currentNode = mEntryNodeList.get(mListIndex);
		TagNode[] contentNodes = currentNode.getChildTags();
		
		quest.title = contentNodes[0]
		                           .getChildTags()[0]
		                                             .getText().toString();
		quest.content = mParser.getInnerHtml(contentNodes[1]);
		
		// Parse tags
		TagNode[] tagsNodes = contentNodes[2].getChildTags();
		quest.tags = new String[tagsNodes.length];
		for(int i = 0; i < tagsNodes.length; i++)
			quest.tags[i] = tagsNodes[i].getText().toString();
		
		// Parse information: Mark, Date, Favorites, Author, Answer count
		TagNode[] infoNodes = contentNodes[3].findElementByAttValue("class", 
				"entry-info-wrap", false, true).getChildTags();
		
		String ids = contentNodes[3].getAttributeByName("id");
		quest.id = Integer.valueOf(ids.substring(9));
	
		Log.i("HabraQuestParser.parse", "ID: " + quest.id + "\nTitle: " + quest.title);
		
		String ratings = infoNodes[0].findElementByAttValue("class", "mark", false, true)
				.findElementByName("span", true).getText().toString();
		quest.rating = ratings.charAt(0) == '-' ? -1 : 1;
		ratings = ratings.substring(1);
		try {
			quest.rating *= Integer.valueOf(ratings);
		} catch(NumberFormatException e) {
			quest.rating = 0;
		}
		
		if(infoNodes.length == 8) {
			String answers = infoNodes[1].findElementByName("a", true).getText().toString();
			try {
				quest.answerCount = Integer.valueOf(answers.substring(0, answers.indexOf(' ')));
			} catch(NumberFormatException e) {
				Log.w("HabraQuestParser.parse", "NumberFormatException: " + e.getMessage());
				quest.answerCount = 0;
			}
		} else {
			try {
				quest.answerCount = Integer.valueOf(mMainNode
						.findElementByAttValue("class", "js-comments-count", 
								true, true).getText().toString());
			} catch(NumberFormatException e) {
				Log.w("HabraQuestParser.parse", "NumberFormatException: " + e.getMessage());
				quest.answerCount = 0;
			}
		}
		
		quest.date = infoNodes[infoNodes.length - 6].findElementByName("span", false).getText().toString();
		quest.inFavs = infoNodes[infoNodes.length - 5].getAttributeByName("class").equals("js-to_favs_remove");
		try {
			quest.favoritesCount = Integer.valueOf(infoNodes[4].getText().toString());
		} catch(NumberFormatException e) {
			Log.w("HabraQuestParser.parse", "NumberFormatException: " + e.getMessage());
			quest.favoritesCount = 0;
		}
		
		quest.author = infoNodes[infoNodes.length - 1].findElementByName("span", true).getText().toString();
		
		if(mEntryNodeList.size() == 1) {
			TagNode[] comments = mEntryNodeList.get(0).getParent().findElementByAttValue("class", 
					"post-comments", false, true).findElementByName("ul", false).getChildTags();
			
			quest.comments = new HabraEntry[comments.length];
			for(int i = 0; i < comments.length; i++) {
				quest.comments[i] = parseComment(comments[i]);
			}
		}
		
		mListIndex++;
		return quest;
	}
	
	private HabraEntry parseComment(TagNode commentNode) {
		HabraEntry entry = new HabraEntry();
		
		entry.id = Integer.valueOf(commentNode.getAttributeByName("id").substring(8));
		
		TagNode contentNode = commentNode.getChildTags()[0].getChildTags()[0];
		entry.content = mParser.getInnerHtml(contentNode);
		entry.content = entry.content.substring(0, entry.content.indexOf("<span class=\"fn comm"));
		
		TagNode infoNode = contentNode.findElementByAttValue("class", "fn comm", false, true);
		entry.author = infoNode.findElementByName("a", false).getText().toString();
		entry.date = infoNode.findElementByName("abbr", false).getText().toString();
		
		return entry;
	}
}