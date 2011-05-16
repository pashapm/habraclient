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
	public String id = null;
	public boolean corporative = false;
	
	public String getUrl() {
		return "http://habrahabr.ru/" + (corporative ? "company/" 
				+ id + "/blog/" : "blogs/" + id + "/");
	}
	
	public void parseIdFromUri(Uri uri) {
		corporative = uri.getPathSegments().get(0).equals("company");
		id = uri.getPathSegments().get(1);
	}
}
