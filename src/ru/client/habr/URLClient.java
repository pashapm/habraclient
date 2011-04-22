package ru.client.habr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class URLClient 
{
	private DefaultHttpClient mHttpClient = null;
	
	private static URLClient mUrlClient = null;
	public static URLClient getUrlClient()
	{
		Log.d("URLClient.getUrlClient", "called");
		if(mUrlClient == null)
		{
			Log.i("URLClient.getUrlClient", "creating new URLClient");
			mUrlClient = new URLClient();
			Log.d("URLClient.getUrlClient", "mUrlClient " + (mUrlClient == null ? "=" : "!") + "= null");
		}
		return mUrlClient;
	}
    
	/**
	 * 
	 */
	public URLClient()
	{
		Log.d("URLClient.URLClient", "construct");
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 25000);
	    mHttpClient = new DefaultHttpClient(httpParams);
	    mHttpClient.getParams().setParameter("http.useragent", "Mozilla/5.0 (Linux; Android) AppleWebKit (KHTML, like Gecko) HabraClient 1.0");
	}
	
	/**
	 * Добавляет куки в клиент
	 * @param cookies Массив печенек или null
	 */
	public void insertCookies(Cookie[] cookies)
	{
		Log.d("URLClient.insertCookies", "called with (" + (cookies == null ? "null" : "Cookie["+cookies.length+"]") + ")");
		
	    if(cookies != null)
		    for(int i = 0; i < cookies.length; i++)
		    	this.insertCookie(cookies[i]);
	}
	
	/**
	 * Добавляет куку в клиент
	 * @param cookie Кука
	 */
	public void insertCookie(Cookie cookie) 
	{
		Log.d("URLClient.insertCookie", "called witch cookie name " + cookie.getName());
		Log.d("URLClient.insertCookie", " + " + cookie.getValue() + "  | " + cookie.getDomain() + " | " + cookie.getPath());
		mHttpClient.getCookieStore().addCookie(cookie);
	}

	/**
	 * Запрос url методом GET
	 * @param url URL для запроса
	 * @return HTML код страницы
	 */
	public String getURL(String url)
    { 
		HttpGet httpGet = new HttpGet(url);
		
        try {
        	Log.d("URLClient.getURL", "try");
			HttpResponse httpResponse = mHttpClient.execute(httpGet);
			Log.d("URLClient.getURL", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("URLClient.getURL", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("URLClient.getURL", "if(httpEntity != null)");
				return EntityUtils.toString(httpEntity);
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.getURL", "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("URLClient.getURL", "IOException: " + e.getMessage());
		}
		return null;
    }
	
	/**
	 * Запрос url методом GET
	 * @param url URL для запроса
	 * @return HTML код страницы
	 */
	public byte[] getURLAsBytes(String url)
    { 
		HttpGet httpGet = new HttpGet(url);
		
        try {
        	Log.d("URLClient.getURLAsBytes", "try");
			HttpResponse httpResponse = mHttpClient.execute(httpGet);
			Log.d("URLClient.getURLAsBytes", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("URLClient.getURLAsBytes", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("URLClient.getURLAsBytes", "if(httpEntity != null)");
				return EntityUtils.toByteArray(httpEntity);
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.getURLAsBytes", "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("URLClient.getURLAsBytes", "IOException: " + e.getMessage());
		}
		return null;
    }
	
	/**
	 * Запрос url методом POST
	 * @param url URL для запроса
	 * @param post POST параметры или null
	 * @return HTML код страницы
	 */
	public String postURL(String url, String[][] post, String referer)
    { 
		HttpPost httpPost = new HttpPost(url);
		if(referer != null)	httpPost.addHeader("Referer", referer);
		
		if(post != null)
		{
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			for(int i = 0; i < post.length; i++)
			{
				nvps.add(new BasicNameValuePair(post[i][0], post[i][1]));
			}
			
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e1) {
				Log.e("URLClient.postURL", "UnsupportedEncodingException: " + e1.getMessage());
			}
		}
		
        try {
        	Log.d("URLClient.postURL", "try");
			HttpResponse httpResponse = mHttpClient.execute(httpPost);
			Log.d("URLClient.postURL", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("URLClient.postURL", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("loadURL", "if(httpEntity != null)");
				String httpResult = EntityUtils.toString(httpEntity);
				return httpResult == null ? "null" : httpResult;
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.postURL", "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("URLClient.postURL", "IOException: " + e.getMessage());
		}
		return null;
    }
	
	public Cookie[] getCookies()
	{
		return mHttpClient.getCookieStore().getCookies().toArray(new Cookie[0]);
	}
	
	/**
	 * Кодирование строки для загрузки в WebView
	 * @param code инпут
	 * @return аутпут
	 */
	public static String encode(String code)
    {
    	return code.replace("%", "%25").replace("#", "%23").replace("\\", "%27").replace("?", "%3F");
    }
}
