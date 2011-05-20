package ru.client.habr;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author WNeZRoS
 * ����� ��� �������� ������ (������ GET) � ��������� ������
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
	
	public abstract static class LoaderData {
		protected String url = null;
		protected PageType pageType = null;
		
		public LoaderData(String url) { 
			this.url = url; 
			pageType = getPageTypeByURI(Uri.parse(url));
		}
		
		public LoaderData() {
		}
		
		public void setUrl(String url) {
			this.url = url;
			pageType = getPageTypeByURI(Uri.parse(url));
		}
		
		public void start() { }
		public String update(String data) { 
			return data; 
		}
		public void finish(String data) { }
	}
	
	private class AsyncLoader extends AsyncTask<Integer, Integer, Integer> {
		private String mUpdateData = null;
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
	
		protected void onPostExecute(Integer result) {
			mHistory.add(mLoaderData.url);
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
	
	private String mData = null;
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
	
	public void setLoaderData(LoaderData loaderData) {
		Log.i("AsyncDataLoader.setLoaderData", "Change loaderData to " 
				+ (loaderData == null ? "null" : loaderData.toString()));
		mLoaderData = loaderData;
	}

	public void execute(String url) {
		if(url == null) return;
		Log.d("AsyncDataLoader.execute", "called");
		
		mLoaderData.setUrl(url);
		mLoaderData.start();
		
		if(mLoaderData.url == null) {
			Log.d("AsyncDataLoader.execute", "url == null");
			mLoaderData.finish(null);
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
		if(mHistory.size() == 0) return false;
		
		mLoaderData.setUrl(mHistory.get(mHistory.size() - 1));
		mAsyncLoader = new AsyncLoader().execute();
		return true;
	}
	
	public boolean isFinished() {
		return mIsFinished;
	}
	
	public void back() {
		if(mLoaderData == null) return;
		if(mHistory.size() == 0) return;
		
		mLoaderData.setUrl(mHistory.get(mHistory.size() - 1));
		mHistory.remove(mHistory.size() - 1);
		mAsyncLoader = new AsyncLoader().execute();
	}
	
	/**
	 * see AsyncTask.cancel
	 * @param mayInterruptIfRunning
	 * @return AsyncTask.cancel(...)
	 */
	public final boolean cancel(boolean mayInterruptIfRunning) {
		// TODO: correct cancel ??? WTF ???
		if(mAsyncLoader == null) return false;
		return mAsyncLoader.cancel(mayInterruptIfRunning);
	}
	
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
			switch(ps.get(0).charAt(0)) {
			case 'q': 
				if(ps.get(1).equals("new") || ps.get(1).equals("unhabred")) 
					return PageType.QUEST_LIST;
				else return PageType.QUEST;
			case 'b': 
				if(ps.get(0).length() > 5) return PageType.BLOG_LIST;
				else return PageType.POST_LIST;
			case 'c': return PageType.COMPANY;
			}
			break;
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
}