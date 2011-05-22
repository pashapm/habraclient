package ru.client.habr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.http.cookie.Cookie;

import ru.client.habr.AsyncDataLoader.LoaderData;
import ru.client.habr.AsyncDataSender.OnSendDataFinish;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author WNeZRoS
 */
public final class HabraLogin {
	
	public static abstract class LoginListener {
		public abstract void onFinish(String message);
	}
	
	public static abstract class KarmaListener {
		public abstract void onFinish(HabraLogin login);
	}
	
	public static abstract class UserInfoListener {
		public abstract void onFinish(String userName);
	}
	
	private static HabraLogin habraLogin = null;

	private String mUserName = null;
	private int mUserID = 0;
	private float mUserKarma = 0.0f;
	private float mUserRating = 0.0f;
	private int mUserTopPosition = 0;
	
	public static HabraLogin getHabraLogin() {
		if(habraLogin == null) {
			Log.w("HabraLogin.getHabraLogin", "create new HabraLogin");
			habraLogin = new HabraLogin();
		}
		return habraLogin;
	}
	
	/**
	 * Проверка на логин
	 * @return true если вход на хабр произведён
	 */
	public boolean isLogged() {
		return mUserName != null;
	}
	
	/**
	 * Посылает запрос на логин. Использует новый поток.
	 * @param login Логин
	 * @param password Пароль
	 * @param captcha Каптча (В первую попытку можно не вводить)
	 * @param l класс для обработки результата
	 */
	public void login(String login, String password, String captcha, final LoginListener l) {
		final String[][] post = new String[][]{{"act","login"}, {"redirect_url","http://habrahabr.ru/"},
        		{"login",login},{"password",password},{"captcha",captcha},{"","true"}};
        
		new AsyncDataSender("http://habrahabr.ru/ajax/auth/", "http://habrahabr.ru/login/", new OnSendDataFinish() {
			@Override
			public void onFinish(String data) {
				Log.d("login", data);
		        
				if(data == null) l.onFinish(ActivityMain.getStringFromResource(R.string.cant_send_request));
				
		        int errorIndex = data.indexOf("<error");
		        if(errorIndex == -1) {
		        	if(l != null) l.onFinish(null);
		        }
		        
		        String result = data.substring(data.indexOf('>', errorIndex) + 1);
		        if(l != null) l.onFinish(result.substring(0, result.indexOf('<')));
			}
		}).execute(post);
	}
	
	/**
	 * Посылает запрос на выход с сайта. Работает в том же потоке, из которого был вызван.
	 * @return true если удалось разлогиниться
	 */
	public boolean logout() {
		if(mUserName == null || mUserID == 0) return false;
		
		URLClient.getUrlClient().getURL("http://habrahabr.ru/logout/" + mUserName + "/" + mUserID + "/");
		Cookie[] cooks = URLClient.getUrlClient().getCookies();
		
		for(int i = 0; i < cooks.length; i++) {
			Log.d("cooks", cooks[i].getName() + " expire for " + cooks[i].getExpiryDate().toGMTString());
			
			if(cooks[i].getName().equals("PHPSESSID")) {
				if(cooks[i].getExpiryDate().getTime() > new Date().getTime()) 
					return false;
			}
		}

		mUserID = 0;
		mUserName = null;
		return true;
	}
	
	/**
	 * Получает и парсит данные о пользователе. Используется как проверка на логин при старте. Использует новый поток.
	 */
	public void parseUserData(final UserInfoListener l) {
		
		AsyncDataLoader.getDataLoader().execute("http://habrahabr.ru/info/stats/", false, new LoaderData() {
			public void finish(String data) {
				parseUserData(data);
				if(l != null) l.onFinish(mUserName);
			}
		});
	}
	
	/**
	 * Парсит данные о пользователе. Парсит только имя и ид.
	 * @param data данные обычной страницы
	 */
	public void parseUserData(String data) {
		if(data == null) return;
		
		int logoutIndex = data.indexOf("http://habrahabr.ru/logout/") + 27;
		if(logoutIndex == 26) return;
		
		mUserName = new String(data.substring(logoutIndex, data.indexOf('/', logoutIndex)));
		logoutIndex += mUserName.length() + 1;
		mUserID = Integer.valueOf(data.substring(logoutIndex, data.indexOf('/', logoutIndex)));
		
		Log.i("HabraLogin.parseUserData", "http://habrahabr.ru/logout/" + mUserName + "/" + mUserID + "/");
	}
	
	/**
	 * Получет и парсит карму, рейтинг, позицию в топе. использует отдельный поток.
	 * @param l обработчик данных
	 */
	public void parseUserKarmaAndForce(final KarmaListener l) {
		if(mUserName == null) return;
		
		final HabraLogin _this = this;
		
		AsyncDataLoader.getDataLoader().execute("http://habrahabr.ru/api/profile/" 
				+ mUserName + "/", false, new LoaderData() {
			public void finish(String data) {
				if(l != null) l.onFinish(_this);
			}
			public String update(String data) {
				
				if(data == null) return null;
				
				int index = data.indexOf("<karma>") + 7;
				if(index == 6) return null;
				mUserKarma = Float.valueOf(data.substring(index, data.indexOf('<', index)));
				
				index = data.indexOf("<rating>") + 8;
				if(index == 7) return null;
				mUserRating = Float.valueOf(data.substring(index, data.indexOf('<', index)));
				
				index = data.indexOf("<ratingPosition>") + 16;
				if(index == 15) return null;
				mUserTopPosition = Integer.valueOf(data.substring(index, data.indexOf('<', index)));
				
				return data;
			}
		});
	}
	
	/**
	 * Загружает и сохраняет каптчу в кэш. Работает в отдельном потоке.
	 * @param captchaView Место для вывода каптчи
	 */
	public void loadCaptcha(final ImageView captchaView) {
		AsyncDataLoader.getDataLoader().execute("http://habrahabr.ru/core/captcha/", true, new AsyncDataLoader.LoaderData() {
			
			public void finish(byte[] data) {
				try {
					FileInputStream inputStream = new FileInputStream(new File(new String(data)));
					captchaView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
					inputStream.close();
				} catch (FileNotFoundException e) {
					Log.e("HabraLogin.loadCaptcha.finish", "FileNotFoundException: " + e.getMessage());
				} catch (IOException e) {
					Log.e("HabraLogin.loadCaptcha.finish", "IOException: " + e.getMessage());
				}
			}
			
			public byte[] update(byte[] data) {
				
				String fileCaptcha = ActivityMain.sCacheDir + "/captcha.png";
				
				File captcha = new File(fileCaptcha);
				try {
					if(!captcha.exists()) captcha.createNewFile();
					FileOutputStream outputStream = new FileOutputStream(captcha);
					outputStream.write(data);
					outputStream.close();
				} catch (IOException e) {
					Log.e("HabraLogin.loadCaptcha.update", "IOException: " + e.getMessage());
				}
				
				return fileCaptcha.getBytes();
			}
		});
	}
	
	public float getUserKarma() {
		return mUserKarma;
	}
	
	public float getUserRating() {
		return mUserRating;
	}
	
	public int getUserRatingPosition() {
		return mUserTopPosition;
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public String getProfileURL() {
		return "http://" + mUserName.replace('_', '-') + ".habrahabr.ru/";
	}
}
