/**
 * 
 */
package ru.client.habr;

import android.os.AsyncTask;

/**
 * @author WNeZRoS
 * Класс для отправки POST запросов в отдельном потоке
 */
public class AsyncDataSender extends AsyncTask<String[], Integer, Integer> {
	
	/**
	 * @author WNeZRoS
	 * Абстрактный класс для обработки результата запроса
	 */
	public static abstract class OnSendDataFinish {
		public abstract void onFinish(String result);
	}
	
	private String mUrl = null;
	private String mReferer = null;
	private OnSendDataFinish mCallback = null;
	private String mResult = null;
	
	/**
	 * @param url Адрес скрипта
	 * @param referer Страница с которой отправлен запрос 
	 * @param callback класс обработки результата
	 */
	public AsyncDataSender(String url, String referer, OnSendDataFinish callback) {
		mUrl = url;
		mReferer = referer;
		mCallback = callback;
	}
	
	protected void onProgressUpdate(Integer... progress) {
		
	}

	protected void onPostExecute(Integer result) {
		if(mCallback != null) {
			mCallback.onFinish(mResult);
		}
	}
	
	@Override
	protected Integer doInBackground(String[]... params) {
		mResult = URLClient.getUrlClient().postURL(mUrl, params, mReferer);
		return 0;
	}
}