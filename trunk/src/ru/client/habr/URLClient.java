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

/**
 * @author WNeZRoS
 * Класс для отправки GET и POST запросов по протоколу HTTP
 */
public final class URLClient {
	
	public final static String USER_AGENT = "Mozilla/5.0 (Linux; Android) WebKit (KHTML, like Gecko) HabraClient 1.0";
	private static URLClient mUrlClient = null;
	
	private final int CONNECTION_TIMEOUT = 30000;
	private final int SOCKET_TIMEOUT = 90000;
	
	private DefaultHttpClient mHttpClient = null;
	private boolean mLocked = false;
	private int maxAttemptCount = 3;
	
	public URLClient() {
		Log.d("URLClient.URLClient", "construct");
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);
		
		mHttpClient = new DefaultHttpClient(httpParams);
		mHttpClient.getParams().setParameter("http.useragent", USER_AGENT);
	}
	
	public static URLClient getUrlClient() {
		Log.d("URLClient.getUrlClient", "called");
		
		if(mUrlClient == null) {
			Log.i("URLClient.getUrlClient", "creating new URLClient");
			mUrlClient = new URLClient();
		}
		
		return mUrlClient;
	}
	
	/**
	 * Добавляет куки к HttpClient'y
	 * @param cookies Массив печенек или null
	 */
	public void insertCookies(Cookie[] cookies) {
		Log.d("URLClient.insertCookies", "called with (" 
				+ (cookies == null ? "null" : "Cookie["+cookies.length+"]") + ")");
		
		if(cookies == null) return;

		for(int i = 0; i < cookies.length; i++) {
			this.insertCookie(cookies[i]);
		}
	}
	
	/**
	 * Добавляет куку к HttpClient'y
	 * @param cookie кука
	 */
	public void insertCookie(Cookie cookie) {
		Log.d("URLClient.insertCookie", "called witch cookie name " + cookie.getName());
		Log.d("URLClient.insertCookie", " + " + cookie.getValue() + "  | " 
				+ cookie.getDomain() + " | " + cookie.getPath());
		
		mHttpClient.getCookieStore().addCookie(cookie);
	}

	/**
	 * Отправляет GET запрос по URL'y url
	 * @param url URL Адрес страницы
	 * @return HTML Код страницы в строке
	 */
	public String getURL(String url) {
		return getURL(url, 0);
	}
	
	private String getURL(String url, int attempt) { 
		Log.d("URlClient.getURL", url);
		HttpGet httpGet = new HttpGet(url);
		
		try {
			while(mLocked) Thread.sleep(50);
		} catch (InterruptedException e1) {
			Log.e("URLClient.getURL", "InterruptedException: " + e1.getMessage());
		}
		
		try {
			mLocked = true;
			HttpResponse httpResponse = mHttpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			mLocked = false;
			
			if(httpEntity != null) {
				return EntityUtils.toString(httpEntity);
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.getURL", "ClientProtocolException: " + e.getMessage());
			if(attempt < maxAttemptCount) return getURL(url, ++attempt);
			return e.getLocalizedMessage();
		} catch (IOException e) {
			Log.e("URLClient.getURL", "IOException: " + e.getMessage());
			if(attempt < maxAttemptCount) return getURL(url, ++attempt);
			return e.getLocalizedMessage();
		}
		
		return null;
	}
	
	/**
	 * Отправаляет GET запрос поURL'y url
	 * @param url URL Адрес файла
	 * @return Содержимое файла в байтах
	 */
	public byte[] getURLAsBytes(String url) { 
		return getURLAsBytes(url, 0);
	}
	
	private byte[] getURLAsBytes(String url, int attempt) { 
		HttpGet httpGet = new HttpGet(url);
		
		try {
			while(mLocked) Thread.sleep(50);
		} catch (InterruptedException e1) {
			Log.e("URLClient.getURL", "InterruptedException: " + e1.getMessage());
		}
		
		try {
			mLocked = true;
			HttpResponse httpResponse = mHttpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			mLocked = false;
			
			if(httpEntity != null) {
				return EntityUtils.toByteArray(httpEntity);
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.getURLAsBytes", "ClientProtocolException: " + e.getMessage());
			if(attempt < maxAttemptCount) return getURLAsBytes(url, ++attempt);
		} catch (IOException e) {
			Log.e("URLClient.getURLAsBytes", "IOException: " + e.getMessage());
			if(attempt < maxAttemptCount) return getURLAsBytes(url, ++attempt);
		}
		
		return null;
	}
	
	/**
	 * Отправляет POST запрос по URL'y url
	 * @param url URL Адрес обработчика
	 * @param post POST Данные запроса или null
	 * @param referer URL Страница с которой отправляется запрос или  null
	 * @return HTML Ответ от сервера в строке
	 */
	public String postURL(String url, String[][] post, String referer) {
		return postURL(url, post, referer, 0);
	}
	
	private String postURL(String url, String[][] post, String referer, int attempt) { 
		HttpPost httpPost = new HttpPost(url);
		if(referer != null)	httpPost.addHeader("Referer", referer);
		
		if(post != null) {
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			for(int i = 0; i < post.length; i++) {
				nvps.add(new BasicNameValuePair(post[i][0], post[i][1]));
			}
			
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e1) {
				Log.e("URLClient.postURL", "UnsupportedEncodingException: " + e1.getMessage());
			}
		}
		
		try {
			while(mLocked) Thread.sleep(50);
		} catch (InterruptedException e1) {
			Log.e("URLClient.getURL", "InterruptedException: " + e1.getMessage());
		}
		
		try {
			Log.d("postURL", httpPost.getMethod() + " " + httpPost.getURI().toString() + " " + httpPost.getProtocolVersion());
			org.apache.http.Header[] hs = httpPost.getAllHeaders();
			for(int i = 0; i < hs.length; i++)
				Log.d("postURL", "Header: " + hs[i].getName() + " = " + hs[i].getValue());
			
			mLocked = true;
			HttpResponse httpResponse = mHttpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			mLocked = false;
			
			if(httpEntity != null) {
				String httpResult = EntityUtils.toString(httpEntity);
				return httpResult == null ? "null" : httpResult;
			}
		} catch (ClientProtocolException e) {
			Log.e("URLClient.postURL", "ClientProtocolException: " + e.getMessage());
			if(attempt < maxAttemptCount) return postURL(url, post, referer, ++attempt);
		} catch (IOException e) {
			Log.e("URLClient.postURL", "IOException: " + e.getMessage());
			if(attempt < maxAttemptCount) return postURL(url, post, referer, ++attempt);
		}

		return null;
	}

	/**
	 * Получает все когда-либо принятые куки от серверов
	 * @return Массив кук
	 */
	public Cookie[] getCookies() {
		return mHttpClient.getCookieStore().getCookies().toArray(new Cookie[0]);
	}

	/**
	 * Заменяет % # \ ? на %25 %23 %27 %3F
	 * @param code текст
	 * @return текст после замены
	 */
	public static String encode(String code) {
		return code.replace("%", "%25").replace("#", "%23").replace("\\", 
				"%27").replace("?", "%3F");
	}
}
