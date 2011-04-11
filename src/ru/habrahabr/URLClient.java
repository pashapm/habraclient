package ru.habrahabr;

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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class URLClient 
{
	DefaultHttpClient httpClient = null;
    
	/**
	 * 
	 */
	public URLClient()
	{
		Log.d("loadURL", "void loadURL(String url)");
	    httpClient = new DefaultHttpClient();
	}
	
	/**
	 * 
	 * @param cookies Массив печенек или null
	 */
	public URLClient(Cookie[] cookies)
	{
		Log.d("loadURL", "void loadURL(String url)");
	    httpClient = new DefaultHttpClient();
	    
	    if(cookies != null)
		    for(int i = 0; i < cookies.length; i++)
		    	httpClient.getCookieStore().addCookie(cookies[i]);
	}
	
	/**
	 * 
	 * @param cookies Список печенек
	 */
	public URLClient(List<Cookie> cookies) 
	{
		Log.d("loadURL", "void loadURL(String url)");
	    httpClient = new DefaultHttpClient();
	    
	    if(cookies != null)
		    for(int i = 0; i < cookies.size(); i++)
		    	httpClient.getCookieStore().addCookie(cookies.get(i));
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
        	Log.d("loadURL", "try");
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Log.d("loadURL", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("loadURL", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("loadURL", "if(httpEntity != null)");
				String httpResult = EntityUtils.toString(httpEntity);				
				return httpResult == null ? "null" : httpResult;
			}
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
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
        	Log.d("loadURL", "try");
			HttpResponse httpResponse = httpClient.execute(httpGet);
			Log.d("loadURL", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("loadURL", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("loadURL", "if(httpEntity != null)");
				return EntityUtils.toByteArray(httpEntity);
			}
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
		}
		return null;
    }
	
	/**
	 * Запрос url методом POST
	 * @param url URL для запроса
	 * @param post POST параметры или null
	 * @return HTML код страницы
	 */
	public String postURL(String url, String[][] post)
    { 
		HttpPost httpPost = new HttpPost(url);
		
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
				Log.e("UnsupportedEncodingException", e1.getMessage());
			}
		}
		
        try {
        	Log.d("loadURL", "try");
			HttpResponse httpResponse = httpClient.execute(httpPost);
			Log.d("loadURL", "execute(httpGet)");
			HttpEntity httpEntity = httpResponse.getEntity();
			Log.d("loadURL", "httpEntity = getEntity()");
			if(httpEntity != null)
			{
				Log.d("loadURL", "if(httpEntity != null)");
				String httpResult = EntityUtils.toString(httpEntity);
				return httpResult == null ? "null" : httpResult;
			}
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
		}
		return null;
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
