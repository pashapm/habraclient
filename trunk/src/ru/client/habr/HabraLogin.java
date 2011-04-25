package ru.client.habr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.http.cookie.Cookie;

import android.util.Log;

public class HabraLogin 
{
	private String mCacheDir = null;
	private String mUserName = null;
	private int mUserID = 0;
	private float mUserKarma = 0.0f;
	private float mUserRating = 0.0f;
	private int mUserTopPosition = 0;
	
	private static HabraLogin habraLogin = null;
	
	/**
	 * Получает общий HabraLogin класс. Внимание! Перед получением его надо где-то создать
	 * @return экземпляр HabraLogin
	 */
	public static HabraLogin getHabraLogin()
	{
		if(habraLogin == null)
		{
			Log.w("HabraLogin.getHabraLogin", "create new HabraLogin");
			habraLogin = new HabraLogin();
		}
		return habraLogin;
	}
	
	/**
	 * Класс авторизации на хабр
	 */
	public HabraLogin()
	{
		
	}
	
	public void setCacheDir(String cacheDir)
	{
		mCacheDir = cacheDir;
	}
	
	/**
	 * Проверяет залогинен пользователь или нет
	 * @return быть или не быть
	 */
	public boolean isLogged()
	{
		return mUserName != null;
	}
	
	/**
	 * Вход в аккаунт на хабре
	 * @param login Логин
	 * @param password Пароль
	 * @param captcha Каптча
	 * @return null или описание ошибки
	 */
	public String login(String login, String password, String captcha)
	{
		String[][] post = new String[][]{{"act","login"}, {"redirect_url","http://habrahabr.ru/"},
        		{"login",login},{"password",password},{"captcha",captcha},{"","true"}};
        
        String data = URLClient.getUrlClient().postURL("http://habrahabr.ru/ajax/auth/", post, "http://habrahabr.ru/login/");
        
        Log.d("login", data);
        
        int errorIndex = data.indexOf("<error");
        if(errorIndex == -1) 
        {
        	return null;
        }
        
        String result = data.substring(data.indexOf('>', errorIndex) + 1);
        return result.substring(0, result.indexOf('<'));
	}
	
	/**
	 * Выход из аккаунта
	 * @return успешность выхода
	 */
	public boolean logout()
	{
		if(mUserName == null || mUserID == 0) return false;
		
		URLClient.getUrlClient().getURL("http://habrahabr.ru/logout/" + mUserName + "/" + mUserID + "/");
		Cookie[] cooks = URLClient.getUrlClient().getCookies();
		
		for(int i = 0; i < cooks.length; i++)
		{
			Log.d("cooks", cooks[i].getName() + " expire for " + cooks[i].getExpiryDate().toGMTString());
			if(cooks[i].getName().equals("PHPSESSID")) 
			{
				if(cooks[i].getExpiryDate().getTime() > new Date().getTime()) return false;
			}
		}

		mUserID = 0;
		mUserName = null;
		return true;
	}
	
	/**
	 * Получает каптчу
	 * @return путь до файла каптчи
	 */
	public String getCaptcha()
	{
		byte[] data =  URLClient.getUrlClient().getURLAsBytes("http://habrahabr.ru/core/captcha/");
		String fileCaptcha = mCacheDir + "/captcha.png";
		
		File captcha = new File(fileCaptcha);
		try {
			if(!captcha.exists()) captcha.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(captcha);
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			Log.e("HabraLogin.getCaptcha", "IOException: " + e.getMessage());
		}
		
		return fileCaptcha;
	}
	
	/**
	 * Получает данные от сервера и проверяет логин
	 * @return this.isLogged()
	 */
	public boolean parseUserData()
	{
		String data = URLClient.getUrlClient().getURL("http://habrahabr.ru/info/stats/");
		int logoutIndex = data.indexOf("http://habrahabr.ru/logout/") + 27;
		if(logoutIndex == 26) return false;
		
		mUserName = new String(data.substring(logoutIndex, data.indexOf('/', logoutIndex)));
		logoutIndex += mUserName.length() + 1;
		mUserID = Integer.valueOf(data.substring(logoutIndex, data.indexOf('/', logoutIndex)));
		
		return isLogged();
	}
	
	public void parseUserKarmaAndForce()
	{
		if(mUserName == null) return;
		
		String data = URLClient.getUrlClient().getURL("http://habrahabr.ru/api/profile/" + mUserName + "/");
		
		int index = data.indexOf("<karma>") + 7;
		if(index == 6) return;
		mUserKarma = Float.valueOf(data.substring(index, data.indexOf('<', index)));
		
		index = data.indexOf("<rating>") + 8;
		if(index == 7) return;
		mUserRating = Float.valueOf(data.substring(index, data.indexOf('<', index)));
		
		index = data.indexOf("<ratingPosition>") + 16;
		if(index == 15) return;
		mUserTopPosition = Integer.valueOf(data.substring(index, data.indexOf('<', index)));
	}
	
	public float getUserKarma()
	{
		return mUserKarma;
	}
	
	public float getUserRating()
	{
		return mUserRating;
	}
	
	public int getUserRatingPosition()
	{
		return mUserTopPosition;
	}
	
	public String getUserName()
	{
		return mUserName;
	}
	
	public String getProfileURL()
	{
		return "http://" + mUserName + ".habrahabr.ru/";
	}
}
