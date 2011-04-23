package ru.client.habr;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncDataLoader
{
	public abstract static class LoaderData
	{
		protected String url = null;
		protected boolean force = false;
		
		public LoaderData(String url, boolean force) { this.url = url; this.force = force; }
		public abstract void start();
		public abstract void update(String data);
		public abstract void finish(String data);
	}
	
	public class AsyncLoader extends AsyncTask<Integer, Integer, Integer>
	{
		protected void onProgressUpdate(Integer... progress) 
	    {
	    	mLoaderData.update(mData);
	    }
	
	    protected void onPostExecute(Integer result) 
	    {
	    	mLastURL = mLoaderData.url;
	    	mLoaderData.finish(mData);
	    }
	    
		@Override
		protected Integer doInBackground(Integer... data) 
		{
			Log.d("AsyncLoader.doInBackground", "called");
			mData = URLClient.getUrlClient().getURL(mLoaderData.url);
			return 0;
		}
	}
	
	private static AsyncDataLoader asyncDataLoader = null;
	public static AsyncDataLoader getDataLoader()
	{
		if(asyncDataLoader == null)
		{
			Log.d("AsyncDataLoader.getDataLoader", "create new data loader");
			asyncDataLoader = new AsyncDataLoader();
		}
		return asyncDataLoader;
	}

	private String mLastURL = null;
	private String mData = null;
	private LoaderData mLoaderData = null;
	private AsyncTask<Integer, Integer, Integer> mAsyncLoader = null;
	
	/**
	 * 
	 * @param data
	 */
	public void execute(LoaderData data)
	{
		Log.d("AsyncDataLoader.execute", "called");
		
		mLoaderData = data;
		mLoaderData.start();
		
		if(mLoaderData.url == null) 
		{
			Log.d("AsyncDataLoader.execute", "url == null");
			mLoaderData.finish(null);
			return;
		}
		
		if(mLoaderData.url.equals(mLastURL) && !mLoaderData.force) 
		{
			Log.d("AsyncDataLoader.execute", "double load");
			mLoaderData.finish(mData);
			return;
		}
		
		mAsyncLoader = new AsyncLoader().execute();
	}
	
	public void reload()
	{
		mAsyncLoader = new AsyncLoader().execute();
	}
	
	/**
	 * see AsyncTask.cancel
	 * @param mayInterruptIfRunning
	 * @return AsyncTask.cancel(...)
	 */
	public final boolean cancel(boolean mayInterruptIfRunning)
	{
		return mAsyncLoader.cancel(mayInterruptIfRunning);
	}
}