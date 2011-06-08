/**
 * 
 */
package ru.client.habr;

/**
 * @author WNeZRoS
 *
 */
public class HabraCompany {
	public class Indystry {
		public String id = null;
		public String title = null;
		
		public Indystry(String id, String title) {
			this.id = id;
			this.title = title;
		}
	}
	
	public int id = 0;
	public String name = null;
	public String id_text = null;
	
	public String iconURL = null;
	public float index = 0.0f;
	public int fans = 0;
	
	public String lastPostName = null;
	public String lastPostURL = null;
	
	public String creationDate = null;
	public String site = null;
	public Indystry indystries[] = null;
	public String number = null;
	public String about = null;
	public String leadership = null;
	public String stages = null;
	public String staff[] = null;
	public String registrationDate = null;
	
	public boolean isWorker = false;
	public boolean isFan = false;
	
	public static HabraCompany parse(String data) {
		return null; // TODO
	}
	
	public static HabraCompany[] parseList(String data) {
		return null; // TODO
	}
	
	public String getDataAsHTML() {
		if(lastPostName != null && lastPostURL != null)
			return getListElementAsHTML();
		return getFullInfo();
	}
	
	private String getFullInfo() {
		return null; // TODO
	}
	
	private String getListElementAsHTML() {
		return null; // TODO
	}
	
	public String getUrl() {
		return "http://habrahabr.ru/company/" + id_text + "/";
	}
	
	public void worker() {
		worker(id, !isWorker);
	}
	
	public static void worker(int id, boolean add) {
		/* TODO
		 * POST: http://habrahabr.ru/ajax/company/set/
		 * DATA: action=worker_add | action=worker_del
		 * DATA: company_id={ID}
		 */
	}
	
	public void fan() {
		fan(id, !isFan);
	}
	
	public static void fan(int id, boolean add) {
		/* TODO
		 * POST: http://habrahabr.ru/ajax/company/set/
		 * DATA: action=fan_add | action=fan_del
		 * DATA: company_id={ID}
		 */
	}
}
