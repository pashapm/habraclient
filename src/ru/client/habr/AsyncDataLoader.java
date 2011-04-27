package ru.client.habr;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author WNeZRoS
 * Класс для загрузки данных (только GET) в отдельном потоке
 */
public final class AsyncDataLoader {
	
	/**
	 * @author WNeZRoS
	 *
	 */
	public abstract static class LoaderData {
		protected String url = null;
		protected boolean force = false;
		
		/**
		 * @param url URL для запроса
		 * @param force выполнить запрос даже если есть данные с этого URL'a
		 */
		public LoaderData(String url, boolean force) { 
			this.url = url; 
			this.force = force; 
		}
		
		/**
		 * Выполняется перед началом работы
		 */
		public void start() { }
		
		/**
		 * Выполняется в отдельном потоке в процессе работы. Предназначен для обработки даннных
		 * @param data исходные данные
		 * @return обработанные данные
		 */
		public String update(String data) { 
			return data; 
		}
		
		/**
		 * Выполняется после завершения работы потока
		 * @param data обработанные данные
		 */
		public void finish(String data) { }
	}
	
	private class AsyncLoader extends AsyncTask<Integer, Integer, Integer> {
		private String mUpdateData = null;
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
	
		protected void onPostExecute(Integer result) {
			mLastURL = mLoaderData.url;
			mIsFinished = true;
			mLoaderData.finish(mUpdateData);
		}
		
		@Override
		protected Integer doInBackground(Integer... data) {
			Log.d("AsyncLoader.doInBackground", "called");
			
			mData = URLClient.getUrlClient().getURL(mLoaderData.url);
			mUpdateData = mLoaderData.update(mData);
			
			return 0;
		}
	}
	
	private static AsyncDataLoader mAsyncDataLoader = null;
	
	private String mLastURL = null;
	private String mData = null;
	private LoaderData mLoaderData = null;
	private AsyncTask<Integer, Integer, Integer> mAsyncLoader = null;
	private boolean mIsFinished = true;
	
	/**
	 * @return экземпляр AsyncDataLoader
	 */
	public static AsyncDataLoader getDataLoader() {
		if(mAsyncDataLoader == null) {
			Log.d("AsyncDataLoader.getDataLoader", "create new data loader");
			mAsyncDataLoader = new AsyncDataLoader();
		}
		
		return mAsyncDataLoader;
	}

	
	/**
	 * Выполняет запрос в отдельном потоке
	 * @param data класс информации о запросе и обработки данных
	 */
	public void execute(LoaderData data) {
		if(data == null) return;
		Log.d("AsyncDataLoader.execute", "called");
		
		mLoaderData = data;
		mLoaderData.start();
		
		if(mLoaderData.url == null) {
			Log.d("AsyncDataLoader.execute", "url == null");
			mLoaderData.finish(null);
			return;
		}
		
		if(mLoaderData.url.equals(mLastURL) && !mLoaderData.force) {
			Log.d("AsyncDataLoader.execute", "double load");
			mLoaderData.finish(mLoaderData.update(mData));
			return;
		}
		
		mIsFinished = false;
		mAsyncLoader = new AsyncLoader().execute();
	}
	
	/**
	 * Перезагружает данные использую класс-обработчик предыдущего запроса
	 * @return false если это первый запрос
	 */
	public boolean reload() {
		if(mLoaderData == null) return false;
		if(mLastURL == null) return false;
		
		mAsyncLoader = new AsyncLoader().execute();
		return true;
	}
	
	/**
	 * Обновляет данные по предыдущему URL спользуя новый класс-обработчик
	 * @param data класс-обработчик
	 */
	public void repeat(LoaderData data) {
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null) {
			execute(data);
		} else {
			data.finish(mLoaderData.update(mData));
		}
	}
	
	/**
	 * Обновляет данные по предыдущему URL спользуя новый класс-обработчик
	 * @param data класс-обработчик
	 * @param notUpdate не выполнять обработку данных, только получение
	 */
	public void repeat(LoaderData data, boolean notUpdate) {
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null) {
			execute(data);
		} else {
			if(notUpdate) data.finish(mData);
			else data.finish(mLoaderData.update(mData));
		}
	}
	
	/**
	 * @return завершён ли предыдущий запрос
	 */
	public boolean isFinished() {
		return mIsFinished;
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
}