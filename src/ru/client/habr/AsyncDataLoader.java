package ru.client.habr;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author WNeZRoS
 * Отправка GET запросов в отдельном потоке
 */
public final class AsyncDataLoader {
	public static enum PageType {
		UNKNOWN,
		POST_LIST,
		POST,
		QUEST_LIST,
		QUEST,
		USER_LIST,
		USER,
		BLOG_LIST,
		BLOG,
		COMPANY_LIST,
		COMPANY
	}
	
	/**
	 * @author WNeZRoS
	 * Класс для обработки
	 */
	public abstract static class LoaderData {
		protected String url = null;
		protected PageType pageType = null;
		protected boolean binary = false;
		
		public LoaderData(String url) { 
			setUrl(url);
		}
		
		public LoaderData() { }
		
		/**
		 * Устанавливает URL запроса
		 * @param url Страница для загрузки
		 */
		public void setUrl(String url) {
			this.url = url;
			pageType = getPageTypeByURI(Uri.parse(url));
			Log.i("LoaderData.setUrl", "URL is '" + url + "' and type = " + pageType);
		}
		
		/**
		 * Устанавливает режим чтения страницы
		 * @param bin включает бинарный режим
		 */
		public void setBinary(boolean bin) {
			this.binary = bin;
		}
		
		/**
		 * Выполняется в UI потоке перед началом.
		 */
		public void start() { }
		
		/**
		 * Выполняется в отдельном потоке. Используется для обработки полученных данных.
		 * @param data полученные данные
		 * @return данные после обработки
		 */
		public String update(String data) { 
			return data; 
		}
		
		/**
		 * Выполняется в отдельном потоке. Используется для обработки полученных данных.
		 * @param data полученные данные
		 * @return данные после обработки
		 */
		public byte[] update(byte[] data) {
			return data;
		}
		
		/**
		 * Выполняется в UI потоке после получения и обработки.
		 * @param data обработанные данные
		 */
		public void finish(String data) { }
		
		/**
		 * Выполняется в UI потоке после получения и обработки.
		 * @param data обработанные данные
		 */
		public void finish(byte[] data) { }
	}
	
	private class AsyncLoader extends AsyncTask<Integer, Integer, Integer> {
		private String mUpdateData = null;
		private byte[] mUpdateBinData = null;
		private LoaderData mLoaderData = null;
		
		public AsyncLoader(LoaderData loaderData) {
			mLoaderData = loaderData;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
	
		protected void onPostExecute(Integer result) {
			if(mLoaderData == null || mHistory == null) return;
			if(mLoaderData.pageType != PageType.UNKNOWN && !mLoaderData.binary) mHistory.add(mLoaderData.url);
			
			mIsFinished = true;
			
			if(mLoaderData.binary) mLoaderData.finish(mUpdateBinData);
			else mLoaderData.finish(mUpdateData);
		}
		
		@Override
		protected Integer doInBackground(Integer... data) {
			Log.d("AsyncLoader.doInBackground", "called");
			
			if(mLoaderData.binary) {
				mUpdateBinData = mLoaderData.update(URLClient.getUrlClient().getURLAsBytes(mLoaderData.url));
			} else {
				mUpdateData = mLoaderData.update(URLClient.getUrlClient().getURL(mLoaderData.url));
			}
			
			return 0;
		}
	}
	
	private static AsyncDataLoader mAsyncDataLoader = null;
	
	private LoaderData mLoaderData = null;
	private AsyncTask<Integer, Integer, Integer> mAsyncLoader = null;
	private boolean mIsFinished = true;
	private List<String> mHistory = new ArrayList<String>();
	
	public static AsyncDataLoader getDataLoader() {
		if(mAsyncDataLoader == null) {
			Log.d("AsyncDataLoader.getDataLoader", "create new data loader");
			mAsyncDataLoader = new AsyncDataLoader();
		}
		
		return mAsyncDataLoader;
	}
	
	/**
	 * Устанавливает обработчик
	 * @param loaderData обработчик
	 */
	public void setLoaderData(LoaderData loaderData) {
		Log.i("AsyncDataLoader.setLoaderData", "Change loaderData to " 
				+ (loaderData == null ? "null" : loaderData.toString()));
		mLoaderData = loaderData;
	}
	
	/**
	 * Выполняет запрос в отдельном потоке
	 * @param url URL запрашиваемой страницы
	 * @param binary Нужен ли бинарный режим чтения
	 * @param loaderData Собственный обработчик.
	 * Если null, то будет использован обработчик установленный через setLoaderData.
	 */
	public void execute(String url, boolean binary, LoaderData loaderData) {
		if(url == null) return;
		Log.d("AsyncDataLoader.execute", "called");
		
		if(loaderData == null) loaderData = mLoaderData;
		
		loaderData.setUrl(url);
		loaderData.setBinary(binary);
		loaderData.start();
		
		if(loaderData.url == null) {
			Log.d("AsyncDataLoader.execute", "url == null");
			if(loaderData.binary) loaderData.finish((byte[]) null);
			else loaderData.finish((String) null);
			return;
		}
		
		mIsFinished = false;
		mAsyncLoader = new AsyncLoader(loaderData).execute();
	}
	
	/**
	 * Выполняет запрос в отдельном потоке
	 * @param url URL запрашиваемой страницы
	 * @param binary Нужен ли бинарный режим чтения
	 */
	public void execute(String url, boolean binary) {
		execute(url, binary, null);
	}
	
	/**
	 * Выполняет запрос в отдельном потоке
	 * @param url URL запрашиваемой страницы
	 */
	public void execute(String url) {
		execute(url, false, null);
	}
	
	/**
	 * Перезагружает последнюю загруженную страницу
	 * @return false если нет обработчика или истории загрузок
	 */
	public boolean reload() {
		if(mLoaderData == null) return false;
		if(mHistory.size() == 0) return false;
		
		mLoaderData.setUrl(mHistory.get(mHistory.size() - 1));
		mAsyncLoader = new AsyncLoader(mLoaderData).execute();
		return true;
	}
	
	/**
	 * @return Завершено ли последнее скачивание
	 */
	public boolean isFinished() {
		return mIsFinished;
	}
	
	/**
	 * Загружает предыдущую страницу
	 */
	public void back() {
		if(mLoaderData == null) return;
		if(mHistory.size() == 0) return;
		
		mLoaderData.setUrl(mHistory.get(mHistory.size() - 1));
		mHistory.remove(mHistory.size() - 1);
		mAsyncLoader = new AsyncLoader(mLoaderData).execute();
	}
	
	/**
	 * see AsyncTask.cancel
	 * @param mayInterruptIfRunning
	 * @return AsyncTask.cancel(...)
	 */
	public final boolean cancel(boolean mayInterruptIfRunning) {
		if(mAsyncLoader == null) return false;
		return mAsyncLoader.cancel(mayInterruptIfRunning);
	}
	
	/**
	 * Получает тип страницы по URI
	 * @param uri URI страницы
	 * @return тип страницы
	 */
	public static PageType getPageTypeByURI(Uri uri) {
		if(uri.getHost().indexOf("habrahabr.ru") < 0) return PageType.UNKNOWN;
		if(uri.getHost().indexOf("habrahabr.ru") > 0) return PageType.USER;
		
		List<String> ps = uri.getPathSegments();
		boolean pagination = false;
		
		if(ps.size() > 0) {
			Log.d("HabraView.getPageTypeByURI", "::" + ps.size() + " " + ps.get(ps.size() - 1));
			if(ps.get(ps.size() - 1).startsWith("page"))
				pagination = true;
		}
		
		switch(ps.size() - (pagination ? 1 : 0)) {
		case 0: return PageType.POST_LIST;
		case 1:
			switch(ps.get(0).charAt(0)) {
			case 'n': return PageType.POST_LIST;
			case 'q': return PageType.QUEST_LIST;
			case 's': return PageType.POST_LIST;
			case 'b': return PageType.BLOG_LIST;
			case 'p': return PageType.USER_LIST;
			case 'c': return PageType.COMPANY_LIST;
			}
			break;
		case 2:
			String pss = ps.get(0);
			if(pss.equalsIgnoreCase("qa")) { 
				if(ps.get(1).equals("new") || ps.get(1).equals("unhabred")) 
					return PageType.QUEST_LIST;
				else return PageType.QUEST;
			} else if(pss.equalsIgnoreCase("blogs")) { 
				if(ps.get(0).length() > 5) return PageType.BLOG_LIST;
				else return PageType.POST_LIST;
			} else if(pss.equalsIgnoreCase("company")) { 
				return PageType.COMPANY;
			}
			return PageType.UNKNOWN;
		case 3:
			switch(ps.get(0).charAt(0)) {
			case 'b': 
				if(ps.get(2).equals("new") || ps.get(2).equals("unhabred")) 
					return PageType.POST_LIST;
				else return PageType.POST;
			case 'c': 
				switch(ps.get(2).charAt(0)) {
				case 'b': return PageType.POST_LIST;
				case 'f': return PageType.USER_LIST;
				}
			}
			break;
		case 4:
			switch(ps.get(0).charAt(0)) {
			case 'c': 
				switch(ps.get(3).charAt(0)) {
				case 'n': return PageType.POST_LIST;
				case 'u': return PageType.POST_LIST;
				default: return PageType.POST;
				}
			}
			break;
		}
		
		return PageType.UNKNOWN;
	}

	/**
	 * Удаляет из истории последний запрошенный URL
	 */
	public void removeLastRequestFromHistory() {
		if(mHistory.size() == 0) return;
		
		mLoaderData.setUrl(mHistory.get(mHistory.size() - 1));
		mHistory.remove(mHistory.size() - 1);
	}
}