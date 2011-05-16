/**
 * 
 */
package ru.client.habr;

/**
 * @author WNeZRoS
 *
 */
public class HabraCompany {
	public String name = null;
	public String id = null;
	
	public String getUrl() {
		return "http://habrahabr.ru/company/" + id + "/";
	}
}
