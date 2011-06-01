package ru.client.habr;

import java.util.ArrayList;
import java.util.List;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 * @author WNeZRoS
 * ������ �������
 */
public final class HabraAnswerParser {
	private int mListIndex = 0;
	private HtmlCleaner mParser = null;
	private TagNode mMainNode = null;
	private List<TagNode> mEntryNodeList = new ArrayList<TagNode>();
	
	/**
	 * ������ ������ �� �������
	 * @param data ������ HTML ��������
	 */
	@SuppressWarnings("unchecked")
	public HabraAnswerParser(String data) {
		if(data == null) return;
		
		mParser = new HtmlCleaner();
		mParser.getProperties().setUseEmptyElementTags(false);
		mMainNode = mParser.clean(data);

		mEntryNodeList = mMainNode.getElementListHavingAttribute("data-id", true);
	}
	
	public HabraAnswer parse() {
		if(mEntryNodeList.size() <= mListIndex || mMainNode == null) return null;
		
		HabraAnswer answer = new HabraAnswer();
		
		answer.id = Integer.valueOf(mEntryNodeList.get(mListIndex).getAttributeByName("data-id"));
		
		TagNode[] contentNodes = mEntryNodeList.get(mListIndex).getChildTags();
		TagNode[] titleNodes = contentNodes[0].findElementByName("ul", false).getChildTags();
		answer.author = titleNodes[0].findElementByName("a", false).getAttributeByName("title");
		answer.avatar = titleNodes[0].findElementByName("img", true).getAttributeByName("src");
		answer.date = titleNodes[2].getChildTags()[0].getText().toString();
		String ratings = titleNodes[6].findElementByAttValue("class", "mark", 
				true, true).findElementByName("span", false).getText().toString();
		
		answer.isSolution = titleNodes[4].getText().length() > 0;
		
		answer.rating = ratings.charAt(0) == '-' ? -1 : 1;
		try {
			answer.rating *= Integer.valueOf(ratings.substring(1));
		} catch(NumberFormatException e) {
			answer.rating = 0;
		}
		
		answer.content = mParser.getInnerHtml(contentNodes[1].findElementByName("div", false));
		
		TagNode[] comments = contentNodes[2].getChildTags();
		
		answer.comments = new HabraEntry[comments.length];
		for(int i = 0; i < comments.length; i++) {
			answer.comments[i] = parseComment(comments[i]);
		}
		
		mListIndex++;
		return answer;
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
