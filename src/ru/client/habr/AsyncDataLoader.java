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
		public void start() { }
		public String update(String data) { return data; }
		public void finish(String data) { }
	}
	
	private class AsyncLoader extends AsyncTask<Integer, Integer, Integer>
	{
		private String mUpdateData = null;
		protected void onProgressUpdate(Integer... progress) 
	    {
	    	
	    }
	
	    protected void onPostExecute(Integer result) 
	    {
	    	mLastURL = mLoaderData.url;
	    	mIsFinished = true;
	    	mLoaderData.finish(mUpdateData);
	    }
	    
		@Override
		protected Integer doInBackground(Integer... data) 
		{
			Log.d("AsyncLoader.doInBackground", "called");
			mData = URLClient.getUrlClient().getURL(mLoaderData.url);
			mUpdateData = mLoaderData.update(mData);
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

	/****************************************************************************************/
	
	private String mLastURL = null;
	private String mData = null;
	private LoaderData mLoaderData = null;
	private AsyncTask<Integer, Integer, Integer> mAsyncLoader = null;
	private boolean mIsFinished = true;
	
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
			mLoaderData.finish(mLoaderData.update(mData));
			return;
		}
		
		mIsFinished = false;
		mAsyncLoader = new AsyncLoader().execute();
	}
	
	public boolean reload()
	{
		if(mLoaderData == null) return false;
		if(mLastURL == null) return false;
		
		mAsyncLoader = new AsyncLoader().execute();
		return true;
	}
	
	public void repeat(LoaderData data)
	{
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null)
		{
			execute(data);
		}
		else
		{
			data.finish(mLoaderData.update(mData));
		}
	}
	
	public void repeat(LoaderData data, boolean notUpdate)
	{
		Log.d("AsyncDataLoader.repeat", "called");
		
		if(mLastURL == null)
		{
			execute(data);
		}
		else
		{
			if(notUpdate) data.finish(mData);
			else data.finish(mLoaderData.update(mData));
		}
	}
	
	public boolean isFinished()
	{
		return mIsFinished;
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