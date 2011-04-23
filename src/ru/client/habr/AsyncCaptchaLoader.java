package ru.client.habr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class AsyncCaptchaLoader extends AsyncTask<ImageView, Integer, Integer>
{
	File mFileCaptcha = null;
	ImageView mImageCaptcha = null;
	
	protected void onProgressUpdate(Integer... progress) 
    {
    	
    }

    protected void onPostExecute(Integer result) 
    {
    	try
    	{
			FileInputStream inputStream = new FileInputStream(mFileCaptcha);
			mImageCaptcha.setImageBitmap(BitmapFactory.decodeStream(inputStream));
			inputStream.close();
		} 
    	catch (FileNotFoundException e) 
    	{
			Log.e("HabraLoginForm.onStart", "FileNotFoundException: " + e.getMessage());
		} 
    	catch (IOException e) 
    	{
			Log.e("HabraLoginForm.onStart", "IOException: " + e.getMessage());
		}
    }
    
	@Override
	protected Integer doInBackground(ImageView... params) 
	{
		mFileCaptcha = new File(HabraLogin.getHabraLogin().getCaptcha());
		mImageCaptcha = params[0];
		return 0;
	}
}
