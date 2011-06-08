/**
 * 
 */
package ru.client.habr;

import android.net.Uri;

/**
 * @author WNeZRoS
 *
 */
public class HabraBlog {
	
	public String name = null;
	public String id_text = null;
	public boolean corporative = false;
	public boolean profiled = false;
	
	public int id = 0;
	public float index = 0.0f;
	public int readers = 0;
	public int posts = 0;
	public String category = null;
	public boolean isMember = false;
	
	public String getUrl() {
		return "http://habrahabr.ru/" + (corporative ? "company/" 
				+ id_text + "/blog/" : "blogs/" + id_text + "/");
	}
	
	public void parseIdFromUri(Uri uri) {
		corporative = uri.getPathSegments().get(0).equals("company");
		id_text = uri.getPathSegments().get(1);
	}
	
	public static HabraBlog parse(String data) {
		return null; // TODO
	}
	
	public static HabraBlog[] parseList(String data) {
		return null; // TODO
	}
	
	public void join() {
		join(id);
	}
	
	public static void join(int id) {
		/* TODO
		 * POST: http://habrahabr.ru/ajax/blogs/membership/join/
		 * DATA: action=join
		 * DATA: blog_id={ID}
		 */
	}
	
	public void leave() {
		leave(id);
	}
	
	public static void leave(int id) {
		/* TODO
		 * POST: http://habrahabr.ru/ajax/blogs/membership/leave/
		 * DATA: action=leave
		 * DATA: blog_id={ID}
		 */
	}
}
