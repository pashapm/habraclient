package ru.habrahabr;

public class HabraAnswer 
{
	int id = 0;
	String avatar = null;
	String author = null;
	String date = null;
	String text = null;
	int rating = 0;
	boolean isSolution = false;
	HabraQAComment[] comments = null;
	
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
	
	public String getAnswerURL(int questionID)
	{
		return "http://habrahabr.ru/qa/" + questionID + "/#answer_" + id;
	}
	
	public String getDataAsHTML()
	{
		return "<div class=\"comment_holder vote_holder answer\"><div class=\"msg-meta\"><ul class=\"menu info author hcard\"><li class=\"avatar\"><a href=\"http://" + 
		author + ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a></li><li class=\"fn nickname username\"><a href=\"http://" + author + 
		".habrahabr.ru/\" class=\"url\">" + author + "</a>,</li><li class=\"date\"><abbr class=\"published\">" + date + 
		"</abbr></li><li class=\"mark\"><span>" + String.valueOf(rating) + "</span></li></ul></div>" + text + "</div>";
	}
	
	public String getCommentsAsHTML()
	{
		String data = "";
		if(comments == null) return data;

        for(int i = 0; i < comments.length; i++)
        {
          data += comments[i].getDataAsHTML();
        }
			
		return data;
	}
}
