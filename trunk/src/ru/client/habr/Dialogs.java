/**
 * 
 */
package ru.client.habr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

/**
 * @author WNeZRoS
 *
 */
public class Dialogs {
	
	public static abstract class OnClickMenuItem {
		public abstract void onClick(int item, String itemText);
	}
	
	public static abstract class OnClickMessage {
		public abstract void onClick(int rel);
	}
	
	private static Context sApplicationContext = null;
	
	public static void setContext(Context applicationContext) {
		sApplicationContext = applicationContext;
	}
	
	public static void showDialogMenu(String title, final String[] items, final OnClickMenuItem l) {
		AlertDialog.Builder builder = new AlertDialog.Builder(sApplicationContext);
		builder.setTitle(title);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if(l != null) l.onClick(item, items[item]);
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public static void showDialogMessage(String message, String neg, String neu, String pos, final OnClickMessage l) {
		AlertDialog.Builder builder = new AlertDialog.Builder(sApplicationContext);
		builder.setMessage(message);
		
		if(neg != null) builder.setNegativeButton(neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(l != null) l.onClick(-1);
				}
			});
		
		if(neu != null) builder.setNeutralButton(neu, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(l != null) l.onClick(0);
				}
			});
		
		if(pos != null) builder.setPositiveButton(pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(l != null) l.onClick(1);
				}
			});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public static void showToast(CharSequence text, int duration) {
		Toast.makeText(sApplicationContext, text, duration).show();
	}
	
	public static void showToast(CharSequence text) {
		showToast(text, Toast.LENGTH_LONG);
	}
	
	public static void showToast(int resId, int duration) {
		Toast.makeText(sApplicationContext, resId, duration).show();
	}
	
	public static void showToast(int resId) {
		showToast(resId, Toast.LENGTH_LONG);
	}
}
