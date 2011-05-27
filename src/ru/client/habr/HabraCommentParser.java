package ru.client.habr;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 * @author WNeZRoS
 * ������ ������������
 */
public final class HabraCommentParser {
	private int mListIndex = 0;
	private HtmlCleaner mParser = null;
	private TagNode mMainNode = null;
	private TagNode mEntryNodes[] = null;
	
	/**
	 * ������ ����������� �� ������
	 * @param data ������ HTML �������� �����
	 */
	//@SuppressWarnings("unchecked")
	public HabraCommentParser(String data) {
		if(data == null) return;
		
		mParser = new HtmlCleaner();
		mMainNode = mParser.clean(data);

		try {
		mEntryNodes = mMainNode.findElementByAttValue("id", "comments", true, false)
				.findElementByName("ul", false)
				.getElementsByAttValue("class", "comment_holder vote_holder", false, false);
		} catch(NullPointerException e) {
			mMainNode = null;
		}
	}
	
	public HabraComment parse() {
		return parse(0);
	}
	
	public HabraComment parse(int postID) {
		if(mEntryNodes.length <= mListIndex || mMainNode == null) return null;
		mListIndex++;
		return parse(mEntryNodes[mListIndex - 1], postID);
	}
	
	private HabraComment parse(TagNode commentNode, int postID) {
		HabraComment comment = new HabraComment();
		comment.postID = postID;
		
		comment.id = Integer.valueOf(commentNode.getAttributeByName("id").substring(8));
		
		TagNode[] contentNodes = commentNode.getChildTags();
		
		comment.newReply = contentNodes[0].getAttributeByName("class").contains("new-reply");
		
		TagNode[] titleNodes = contentNodes[0].findElementByName("ul", false).getChildTags();
		TagNode avatarNode = titleNodes[0].findElementByName("img", true);
		comment.author = avatarNode.getAttributeByName("alt");
		comment.avatar = avatarNode.getAttributeByName("src");
		comment.date = titleNodes[2].findElementByName("abbr", false).getText().toString();
		
		comment.inFavs = titleNodes[4].findElementByName("a", false)
				.getAttributeByName("class").contains("js-to_favs_add");
		
		String ratings = titleNodes[titleNodes.length - 1].findElementByName(
				"span", true).getText().toString();
		comment.rating = ratings.charAt(0) == '–' ? -1 : +1;
		try {
			comment.rating *= Integer.valueOf(ratings.substring(1));
		} catch(NumberFormatException e) {
			comment.rating = 0;
		}
		
		comment.content = mParser.getInnerHtml(contentNodes[1].findElementByName("div", false));
		
		if(!contentNodes[contentNodes.length - 1].hasAttribute("id")) {
			TagNode[] childNodes = contentNodes[contentNodes.length - 1].getChildTags();
			comment.childs = new HabraComment[childNodes.length];
			
			for(int i = 0; i < childNodes.length; i++) {
				comment.childs[i] = parse(childNodes[i], postID);
			}
		}
		
		return comment;
	}
}
