package ru.client.habr;

import ru.client.habr.AsyncDataSender.OnSendDataFinish;

/**
 * @author WNeZRoS
 * ����� ������ �� ������
 */
public final class HabraAnswer extends HabraEntry {
	
	protected HabraEntryType type = HabraEntryType.ANSWER;
	public String avatar = null;
	public int rating = 0;
	public boolean isSolution = false;
	public HabraEntry[] comments = null;
	public int questionID = 0;
	
	
	public String getUrl() {
		return getUrl() + questionID + "/#answer_" + id;
	}
	
	/**
	 * @return ����� ��� HTML
	 */
	public String getDataAsHTML() {
		return getDataAsHTML(false);
	}
	
	/**
	 * @param noAvatar ������ ������
	 * @return ����� ��� HTML
	 */
	public String getDataAsHTML(boolean noAvatar) {
		return "<div class=\"comment_holder answer\" id=\"answer_" + id + "\">" 
		+ "<div class=\"msg-meta\"><ul class=\"menu info author hcard\">" 
		+ "<li class=\"avatar\">" + (noAvatar ? "" : "<a href=\"http://" + author.replace('_', '-') 
		+ ".habrahabr.ru/\"><img src=\"" + avatar + "\"/></a>") + "</li>" 
		+ "<li class=\"fn nickname username\"><a href=\"http://" + author.replace('_', '-') 
		+ ".habrahabr.ru/\" class=\"url\">" + author + "</a>,</li>" 
		+ "<li class=\"date\"><abbr class=\"published\">" + date 
		+ "</abbr></li><li class=\"correct\">" + (isSolution ? "<strong>Решение</strong>" : "") 
		+ "</li><li class=\"mark\" onClick=\"js.onClickRating(" 
		+ id + ", 'a', " + questionID + ");\"><span class=\"" + (rating > 0 ? "plus" : 
			(rating < 0 ? "minus" : "zero")) + "\">" + (rating > 0 ? "+" : "") + rating 
		+ "</span></li></ul></div><div class=\"entry-content\" onClick=\"js.onClickAnswer(" 
		+ id + ", " + questionID + ", '" + author + "');\">" + content + "</div></div>";
	}
	
	/**
	 * @return ����������� � ������ ��� HTML ���
	 */
	public String getCommentsAsHTML() {
		String data = "";
		if(comments == null) return data;

		for(int i = 0; i < comments.length; i++) {
			data += comments[i].getDataAsHTML();
		}
			
		return data;
	}
	
	public static void send(int questionID, String message, final OnSendFinish c) {
		/*
		 * Answer: http://habrahabr.ru/ajax/qa/answer
		 * question_id={ID}
		 * text={MSG}
		 */
		String post[][] = {{"question_id", String.valueOf(questionID)}, {"text", message}};
		
		new AsyncDataSender("http://habrahabr.ru/ajax/qa/answer", 
				"http://habrahabr.ru/qa/" + questionID, new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							if(c != null) c.onFinish(true, result);
						} else {
							if(c != null) c.onFinish(false, result);
						}
					}
		}).execute(post);
	}
}
