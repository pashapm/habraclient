package ru.habrahabr;

import android.util.Log;

public class HabraLogin 
{
	URLClient mUrlClient = null;
	String mUserName = null;
	int mUserID = 0;
	
	public HabraLogin(URLClient url)
	{
		mUrlClient = url;
	}
	
	public boolean isLogged()
	{
		return mUserName != null;
	}
	
	public int login(String login, String password, String captcha)
	{
		String[][] post = new String[][]{{"act","login"}, {"redirect_url","http://habrahabr.ru/"},
        		{"login",login},{"password",password},{"captcha",captcha},{"","true"}};
        
        String data = mUrlClient.postURL("http://habrahabr.ru/ajax/auth/", post, "http://habrahabr.ru/login/");
        
        Log.d("login", data);
        
		return 1;
	}
	
	public boolean logout()
	{
		return true;
	}
	
	public byte[] getCaptcha()
	{
		return mUrlClient.getURLAsBytes("http://habrahabr.ru/core/captcha/");
	}
}
