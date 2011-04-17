package ru.habrahabr;

public class HabraQAComment 
{
	int id;
	String text;
	String author;
	String date;
	
	public String getDataAsHTML()
	{
		return "<div id=\"comment_" + String.valueOf(id) + "\" class=\"comment_holder vote_holder\"><div class=\"entry-content\"><div class=\"entry-content-only\">" + text + 
          "&nbsp;<span class=\"fn comm\"><a href=\"http://" + author + ".habrahabr.ru/\">" + author + "</a>,&nbsp;<abbr class=\"published\">" + date + "</abbr></span></div></div></div>";
	}
}
