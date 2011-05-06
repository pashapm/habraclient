package ru.client.habr;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author WNeZRoS
 * ����� ��� �������� ������ (������ GET) � ��������� ������
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
		 * @param url URL ��� �������
		 * @param force ��������� ������ ���� ���� ���� ������ � ����� URL'a
		 */
		public LoaderData(String url, boolean force) { 
			this.url = url; 
			this.force = force; 
		}
		
		public LoaderData() {
			
		}
		
		public LoaderData setUrl(String url) {
			this.url = url;
			return this;
		}
		
		/**
		 * ����������� ����� ������� ������
		 */
		public void start() { }
		
		/**
		 * ����������� � ��������� ������ � �������� ������. ������������ ��� ��������� �������
		 * @param data �������� ������
		 * @return ������������ ������
		 */
		public String update(String data) { 
			return data; 
		}
		
		/**
		 * ����������� ����� ���������� ������ ������
		 * @param data ������������ ������
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
			if(mData == null) {
				mUpdateData = "";
				return 1;
			}
			
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
	 * @return ��������� AsyncDataLoader
	 */
	public static AsyncDataLoader getDataLoader() {
		if(mAsyncDataLoader == null) {
			Log.d("AsyncDataLoader.getDataLoader", "create new data loader");
			mAsyncDataLoader = new AsyncDataLoader();
		}
		
		return mAsyncDataLoader;
	}

	
	/**
	 * ��������� ������ � ��������� ������
	 * @param data ����� ���������� � ������� � ��������� ������
	 */
	public void execute(LoaderData data, boolean noExecute) {
		if(data == null) return;
		Log.d("AsyncDataLoader.execute", "called");
		
		mLoaderData = data;
		
		if(noExecute) return;
		
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
	 * ������������� ������ ��������� �����-���������� ����������� �������
	 * @return false ���� ��� ������ ������
	 */
	public boolean reload() {
		if(mLoaderData == null) return false;
		if(mLastURL == null) return false;
		
		mAsyncLoader = new AsyncLoader().execute();
		return true;
	}
	
	/**
	 * ��������� ������ �� ����������� URL �������� ����� �����-����������
	 * @param data �����-����������
	 */
	public void repeat(LoaderData data) {
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null) {
			execute(data, false);
		} else {
			data.finish(mLoaderData.update(mData));
		}
	}
	
	/**
	 * ��������� ������ �� ����������� URL �������� ����� �����-����������
	 * @param data �����-����������
	 * @param notUpdate �� ��������� ��������� ������, ������ ���������
	 */
	public void repeat(LoaderData data, boolean notUpdate) {
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null) {
			execute(data, false);
		} else {
			if(notUpdate) data.finish(mData);
			else data.finish(mLoaderData.update(mData));
		}
	}
	
	/**
	 * @return ������� �� ���������� ������
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
		// TODO: correct cancel
		if(mAsyncLoader == null) return false;
		return mAsyncLoader.cancel(mayInterruptIfRunning);
	}
}