package ru.client.habr;

import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import ru.client.habr.AsyncDataSender.OnSendDataFinish;

import android.net.Uri;
import android.util.Log;

/**
 * @author WNeZRoS
 *
 */
public class HabraUser {
	protected static class Have {
		public String description = null;
		public String data = null;
		public String url = null; // Can be null
	}
	
	// profile header
	public int id = 0;
	public String username = null;
	public String avatar = null;
	public float karma = 0.0f;
	public int karmaMarks = 0;
	public float force = 0.0f;
	
	// profile info
	public String name = null;
	public int ratingPlace = 0;
	public String note = null;
	public String birthday = null;
	public String fromCountry = null;
	public String fromRegion = null;
	public String fromCity = null;
	public String[] tags = null;
	public String summary = null;
	public Have[] have = null;
	public String[] interest = null;
	public HabraBlog[] admin = null;
	public HabraCompany[] favoriteCompanies = null;
	public HabraCompany[] workIn = null;
	public String[] friends = null;
	public HabraBlog[] memberIn = null;
	public String registration = null;
	public String[] invite = null;
	public String lastVisit = null;
	
	public static HabraUser parse(String data) {
		if(data == null) return null;
		
		HtmlCleaner parser = new HtmlCleaner();
		TagNode mainNode = parser.clean(data);
		TagNode mainContentNode = mainNode.findElementByAttValue("id", "main-content", true, false);
		if(mainContentNode == null) return null;
		
		TagNode headerNode = mainContentNode.getChildTags()[0];
		TagNode userinfoNode = mainContentNode.getChildTags()[2];
		if(headerNode == null || userinfoNode == null) return null;
		
		HabraUser user = new HabraUser();
		
		TagNode avaname = headerNode.findElementByName("img", true);
		user.username = avaname.getAttributeByName("alt");
		user.avatar = avaname.getAttributeByName("src");
		
		TagNode[] karmaAndForce = headerNode.getChildTags();
		if(karmaAndForce.length == 4) karmaAndForce = karmaAndForce[2].getChildTags();
		else karmaAndForce = karmaAndForce[4].getChildTags();
		
		String karmaStr = karmaAndForce[0].findElementByName("span", 
				true).findElementByName("span", false).getText().toString();
		
		user.karma = Float.valueOf(karmaStr.replace(' ', '0').replace(',', '.'));
		String karmaMarks = karmaAndForce[0].findElementByAttValue("class", 
				"total", true, false).getChildTags()[0].getText().toString();
		user.karmaMarks = Integer.valueOf(karmaMarks.substring(0, karmaMarks.indexOf(' ')));
		user.id = Integer.valueOf(karmaAndForce[0].getChildTags()[1].getAttributeByName("id").substring(4));
		user.force = Float.valueOf(karmaAndForce[1].getChildTags()[1].getText().toString().replace(',', '.'));
		
		// catch(NullPointerException e) для того, чтобы не было ошибок при не заполненом профиле
		
		try {
			user.name = userinfoNode.findElementByAttValue("class", "fn", true, 
					true).getText().toString();
		} catch(NullPointerException e) { }
		
		String ratingPlaceStr = userinfoNode.findElementByAttValue("class", 
				"rating-place", true, true).getText().toString();
		
		try {
			user.ratingPlace = Integer.valueOf(ratingPlaceStr.substring(0, ratingPlaceStr.indexOf('-')));
		} catch(NumberFormatException e) {
			Log.w("HabraUser.parse", "NumberFormatException: " + e.getMessage());
			user.ratingPlace = 0;
		} catch(StringIndexOutOfBoundsException e) {
			user.ratingPlace = 0;
		}
		
		try {
			TagNode noteNode = userinfoNode.findElementByAttValue("class", 
					"current_note", true, true);
			noteNode.getParent().getParent().setAttribute("class", "class");
			user.note = noteNode.getText().toString();
		} catch(NullPointerException e) { }
		
		try {
			TagNode birthdayNode = userinfoNode.findElementByAttValue("class", 
					"bday", true, true);
			birthdayNode.getParent().setAttribute("class", "class");
			Log.i("bday", birthdayNode.getText().toString());
			user.birthday = birthdayNode.getText().toString();
		} catch(NullPointerException e) { }
		
		try {
			TagNode countryNameNode = userinfoNode.findElementByAttValue("class", 
					"country-name", true, true);
			
			user.fromCountry = countryNameNode.getText().toString();
			countryNameNode.getParent().getParent().setAttribute("class", "class");
			
			user.fromRegion = userinfoNode.findElementByAttValue("class", 
					"region", true, true).getText().toString();
			user.fromCity = userinfoNode.findElementByAttValue("class", 
					"city", true, true).getText().toString();
		} catch(NullPointerException e) { }
		
		try {
			TagNode tagNode = userinfoNode.findElementByAttValue("id", "people-tags", true, true);
			tagNode.getParent().getParent().setAttribute("class", "class");
			TagNode[] tagsNode = tagNode.getChildTags();
			
			if(tagsNode.length > 0) {
				user.tags = new String[tagsNode.length];
				for(int i = 0; i < tagsNode.length; i++) {
					Log.d("tags", "" + i);
					user.tags[i] = tagsNode[i].findElementByName("span", true).getText().toString();
					Log.d("tags", user.tags[i]);
				}
			}
		} catch(NullPointerException e) { }
		
		try {
			TagNode summaryNode = userinfoNode.findElementByAttValue("class", "summary", true, true);
			if(summaryNode != null) {
				user.summary = parser.getInnerHtml(summaryNode);
				summaryNode.getParent().setAttribute("class", "class");
			}
		} catch(NullPointerException e) { }
		
		TagNode[] childNodes = userinfoNode.getChildTags();
		
		try {
			boolean endGrab = false;
			List<Have> iHave = new ArrayList<Have>();
			for(int i = 0; i < childNodes.length; i++) {
				if(childNodes[i].getAttributeByName("class") == null) {
					endGrab = true;
					Have have = new Have();
					have.description = childNodes[i].findElementByName("dt", false).getText().toString();
					TagNode dd = childNodes[i].findElementByName("dd", false);
					TagNode a = dd.findElementByName("a", false);
					if(a != null) {
						have.url = a.getAttributeByName("href");
						have.data = a.getText().toString();
					} else {
						have.data = dd.getText().toString();
					}
					iHave.add(have);
				}
				else if(endGrab) break;
			}
			
			user.have = iHave.toArray(new Have[0]);
		} catch(NullPointerException e) { }
		
		TagNode[] logicWrap = userinfoNode.getElementsByAttValue("class", "dl_logic_wrap", false, true);
		
		try {
			TagNode[] interests = logicWrap[0].findElementByName("ul", true).getElementsByName("a", true);
			user.interest = new String[interests.length];
			for(int i = 0; i < interests.length; i++) {
				user.interest[i] = interests[i].getText().toString();
			}
		} catch(NullPointerException e) { }
			
		try {
			TagNode[] admin = logicWrap[1].findElementByName("ul", true).getElementsByName("a", true);
			user.admin = new HabraBlog[admin.length];
			for(int i = 0; i < admin.length; i++) {
				user.admin[i] = new HabraBlog();
				user.admin[i].parseIdFromUri(Uri.parse(admin[i].getAttributeByName("href")));
				user.admin[i].name = admin[i].getText().toString();
			}
		} catch(NullPointerException e) { }
			
		try {
			TagNode[] companies = logicWrap[2].findElementByAttValue("id", 
					"working_in", false, true).findElementByName("ul", true).getElementsByName("a", true);
			user.workIn = new HabraCompany[companies.length];
			for(int i = 0; i < companies.length; i++) {
				user.workIn[i] = new HabraCompany();
				user.workIn[i].id = Uri.parse(
						companies[i].getAttributeByName("href")).getPathSegments().get(1);
				user.workIn[i].name = companies[i].getText().toString();
			}
		} catch(NullPointerException e) { }
		
		try {
			TagNode[] companies = logicWrap[2].findElementByAttValue("id", 
					"favorite_companies_list", false, true).findElementByName("ul", true).getElementsByName("a", true);
			
			user.favoriteCompanies = new HabraCompany[companies.length];
			for(int i = 0; i < companies.length; i++) {
				user.favoriteCompanies[i] = new HabraCompany();
				user.favoriteCompanies[i].id = Uri.parse(
						companies[i].getAttributeByName("href")).getPathSegments().get(1);
				user.favoriteCompanies[i].name = companies[i].getText().toString();
			}
		} catch(NullPointerException e) { Log.w("HabraUser.parse", "favoriteCompanies exception"); }
		
		try {
			TagNode[] friends = logicWrap[3].findElementByName("ul", true).getElementsByName("a", true);
			user.friends = new String[friends.length];
			for(int i = 0; i < friends.length; i++) {
				user.friends[i] = friends[i].getText().toString();
			}
		} catch(NullPointerException e) { }
		
		try {
			TagNode[] member = logicWrap[4].findElementByName("ul", true).getElementsByName("a", true);
			user.memberIn = new HabraBlog[member.length];
			Log.d("parse", "" + member.length);
			for(int i = 0; i < member.length; i++) {
				user.memberIn[i] = new HabraBlog();
				user.memberIn[i].parseIdFromUri(Uri.parse(member[i].getAttributeByName("href")));
				user.memberIn[i].name = member[i].getText().toString();
			}
		} catch(NullPointerException e) { }
			
		int regDateOffset = 1;
		
		try {
			user.lastVisit = childNodes[childNodes.length - 1].findElementByName("dd", false).getText().toString();
			regDateOffset++;
		} catch(NullPointerException e) { }
		
		try {
			TagNode[] invited = childNodes[childNodes.length - 2].findElementByName("ul", true).getChildTags();
			user.invite = new String[invited.length];
			for(int i = 0; i < invited.length; i++) {
				user.invite[i] = invited[i].getText().toString();
			}
			regDateOffset++;
		} catch(NullPointerException e) { }
		
		user.registration = parser.getInnerHtml(
				childNodes[childNodes.length - regDateOffset].findElementByName("dd", false));

		return user;
	}
	
	public String getDataAsHTML() {
		return getHeaderAsHTML() + getInfoAsHTML();
	}
	
	private String getHeaderAsHTML() {
		return "<div class=\"profile-header\"><h1 class=\"habrauserava\"><img src=\"" 
		+ avatar + "\"></h1><dl class=\"profile-actions\"><dt><a class=\"habrauser silentlink\" href=\"http://" 
		+ username + ".habrahabr.ru\">" + username + "</a></dt></dl><div class=\"profile-karma-holder\">" 
		+ "<dl class=\"karma\" onClick=\"js.onClickKarma('" + username + "', " 
		+ id + ");\"><dt>карма</dt><dd class=\"vote vote_holder\"><span class=\"mark\"><span>" 
		+ karma + "</span></span></dd><dd class=\"total\"><em>" + getTotal() 
		+ "</em></dd></dl><dl class=\"habraforce\"><dt>рейтинг</dt><dd class=\"number\">" 
		+ force + "</dd></dl></div></div>";
	}
	
	private String getTotal() {
		int mod = karmaMarks - (karmaMarks / 10) * 10;
		Log.d("HabraUser.getTotal", "mod is " + mod);
		
		if((karmaMarks >= 6 && karmaMarks <= 20) || mod == 0 || mod >= 5)
			return karmaMarks + " голосов";
		else if(mod == 1) 
			return karmaMarks + " голос";
		else if(mod >= 2 && mod <= 4)
			return karmaMarks + " голоса";
		
		return karmaMarks + " голосов";
	}
	
	private String getInfoAsHTML() {
		return "<div class=\"userinfo vcard\">"
		+ (name != null ? "<dl class=\"user-name\"><dt class=\"fn\">" + name + "</dt><dd class=\"rating-place\">" : "")
		+ (ratingPlace == 0 ? "Не участвует в рейтинге хабралюдей" : ratingPlace + "-й в рейтинге хабралюдей</dd></dl>")
		+ (!username.equals(HabraLogin.getHabraLogin().getUserName()) && note != null && note.length() > 0 ? 
				"<dl><dt>Заметка:</dt><dd class=\"note\"><span class=\"current_note\">" + note + "</span></dd></dl>" : "")
		+ (birthday != null ? "<dl><dt>Дата рождения:</dt><dd class=\"bday\">" + birthday + "</dd></dl>" : "")
		+ (fromCountry != null ?
				"<dl><dt>Откуда:</dt><dd>" 
				+ "<a class=\"country-name\">" + fromCountry + "</a>"
				+ (fromRegion != null ? ", <a class=\"region\">" + fromRegion + "</a>" : "")
				+ (fromCity != null ? ", <a class=\"city\">" + fromCity + "</a>" : "")
				+ "</dd></dl>" : "")
		+ (tags != null && tags.length > 0 ? "<dl><dt>Значки:</dt><dd>" + getList(tags) + "</dd></dl>" : "")
		+ (summary != null ? "<dl><dt>О себе:</dt><dd class=\"summary\">" + summary + "</dd></dl>" : "")
		+ getContacts()
		+ (interest != null ? "<div class=\"dl_logic_wrap\"><dl class=\"interests\"><dt>Интересы:</dt><dd>"
				+ getList(interest) + "</dd></dl></div>" : "")
		+ (admin != null ? "<div class=\"dl_logic_wrap\"><dl class=\"blogs_list\"><dt>Администрирует:</dt><dd>" 
				+ getBlogsList(admin) + "</dd></dl></div>" : "")
		+ (favoriteCompanies != null ? "<div class=\"dl_logic_wrap\"><dl id=\"favorite_companies_list\"><dt>Нравится:</dt><dd>" 
				+ getCompamiesList(favoriteCompanies) + "</dd></dl></div>" : "")
		+ (friends != null ? "<div class=\"dl_logic_wrap\"><dl class=\"friends_list\"><dt>Друзья:</dt><dd>" 
				+ getUsersList(friends) + "</dd></dl></div>" : "")
		+ (memberIn != null ? "<div class=\"dl_logic_wrap\"><dl class=\"blogs_list\"><dt>Состоит в:</dt><dd>" 
				+ getBlogsList(memberIn) + "</dd></dl></div>" : "")
		+ "<dl><dt>Зарегистрирован:</dt><dd>" + registration + "</dd></dl>" 
		+ (invite != null && invite.length > 0 ? "<dl class=\"friends_list\"><dt>Пригласил на сайт:</dt><dd>" 
				+ getUsersList(invite) + "</dd></dl>" : "")
		+ (lastVisit != null ? "<dl><dt>Активность:</dt><dd>" + lastVisit + "</dd></dl>" : "")
		+ "</div>";
	}
	
	private String getContacts() {
		if(have == null) return "";
		String result = "";
		
		for(int i = 0; i < have.length; i++) {
			result += "<dl><dt>" + have[i].description + "</dt><dd><a href=\"" 
			+ have[i].url + "\">" + have[i].data + "</a></dd></dl>";
		}
		
		return result;
	}
	
	private String getUsersList(String[] users) {
		if(users == null) return "";
		String result = "<ul>";
		for(int i = 0; i < users.length; i++) {
			result += "<li><a href=\"http://" + users[i] + ".habrahabr.ru/\">" + users[i] + "</a></li>";
		}
		result += "</ul>";
		return result;
	}
	
	private String getList(String[] list) {
		if(list == null) return "";
		String result = "<ul>";
		for(int i = 0; i < list.length; i++) {
			result += "<li>" + list[i] + ", </li>";
		}
		result += "</ul>";
		return result;
	}
	
	private String getBlogsList(HabraBlog[] blogs) {
		if(blogs == null) return "";
		String result = "<ul>";
		for(int i = 0; i < blogs.length; i++) {
			result += "<li><a href=\"" + blogs[i].getUrl() + "\">" + blogs[i].name + "</a></li>";
		}
		result += "</ul>";
		return result;
	}
	
	private String getCompamiesList(HabraCompany[] companies) {
		if(companies == null) return "";
		String result = "<ul>";
		for(int i = 0; i < companies.length; i++) {
			result += "<li><a href=\"" + companies[i].getUrl() + "\">" + companies[i].name + "</a></li>";
		}
		result += "</ul>";
		return result;
	}
	
	public boolean karmaUpdate(int mark) {
		return karmaUpdate(mark, username, mark);
	}
	
	public static boolean karmaUpdate(int id, String username, int mark) {
		String post[][] = {{"action", "vote"}, {"target_name", "user"}, 
				{"target_id", String.valueOf(id)}, {"mark", String.valueOf(mark)}};
		
		new AsyncDataSender("http://" + username + ".habrahabr.ru/ajax/voting/", 
				"http://" + username + ".habrahabr.ru/", new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							Dialogs.getDialogs().showToast(R.string.vote_ok);
						} else {
							Dialogs.getDialogs().showToast(R.string.vote_failed);
						}
					}
		}).execute(post);
		
		//  URL: http://{USERNAME}.habrahabr.ru/ajax/voting/
		// DATA: action=vote
		// DATA: target_name=user
		// DATA: target_id={ID}
		// DATA: mark={MARK={-1;1}}
		// ANSW: <message>ok</message> 
		return false;
	}
	
	public boolean addFriend(String message) {
		return addFriend(id, username, message);
	}
	
	public static boolean addFriend(int id, String username, String message) {
		String post[][] = {{"action", "friend"}, {"friendId", String.valueOf(id)}, {"msg", message}};
		
		new AsyncDataSender("http://" + username + ".habrahabr.ru/ajax/users/friends/", 
				"http://" + username + ".habrahabr.ru/", new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							Dialogs.getDialogs().showToast(R.string.friend_added);
						} else {
							Dialogs.getDialogs().showToast(R.string.friend_failed);
						}
					}
		}).execute(post);
		
		//  URL: http://{USERNAME}.habrahabr.ru/ajax/users/friends/
		// DATA: action=friend
		// DATA: friendId={ID}
		// DATA: msg={MSG}
		// ANSW: <message>ok</message> 
		return false;
	}
	
	public boolean removeFriend(String message) {
		return removeFriend(id, username, message);
	}
	
	public static boolean removeFriend(int id, String username, String message) {
		String post[][] = {{"action", "unfriend"}, {"friendId", String.valueOf(id)}, {"msg", message}};
		
		new AsyncDataSender("http://" + username + ".habrahabr.ru/ajax/users/friends/", 
				"http://" + username + ".habrahabr.ru/", new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							Dialogs.getDialogs().showToast(R.string.friend_removed);
						} else {
							Dialogs.getDialogs().showToast(R.string.friend_failed);
						}
					}
		}).execute(post);
		
		//  URL: http://{USERNAME}.habrahabr.ru/ajax/users/friends/
		// DATA: action=unfriend
		// DATA: friendId={ID}
		// DATA: msg={MSG}
		// ANSW: <message>ok</message> 
		return false;
	}
	
	public boolean sendMessage(String to, String title, String content) {
		return HabraUser.sendMessage(username, to, title, content);
	}
	
	public static boolean sendMessage(String from, String to, String title, String content) {
		String post[][] = {{"message[recipients]", to}, {"message[title]", title}, {"message[text]", content}};
		
		new AsyncDataSender("http://" + from + ".habrahabr.ru/ajax/messages/add/", 
				"http://" + from + ".habrahabr.ru/", new OnSendDataFinish() {
					@Override
					public void onFinish(String result) {
						if(result.contains("<message>ok</message>")) {
							Dialogs.getDialogs().showToast(R.string.message_sent);
						} else {
							Dialogs.getDialogs().showToast(R.string.message_failed);
						}
					}
		}).execute(post);
		
		// URL: http://{MY_USERNAME}.habrahabr.ru/ajax/messages/add/
		// DATA: message[recipients]={TO}
		// DATA: message[title]={TITLE}
		// DATA: message[text]={CONTEXT}
		// ANSW: <message>ok</message> 
		return false;
	}
}
