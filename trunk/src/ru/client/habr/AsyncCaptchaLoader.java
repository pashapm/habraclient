package ru.client.habr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author WNeZRoS
 * ����� ��� �������� ������
 */
public class AsyncCaptchaLoader extends AsyncTask<ImageView, Integer, Integer> {
	private File mFileCaptcha = null;
	private ImageView mImageCaptcha = null;
	
	protected void onProgressUpdate(Integer... progress) {
		
	}

	protected void onPostExecute(Integer result) {
		try {
			FileInputStream inputStream = new FileInputStream(mFileCaptcha);
			mImageCaptcha.setImageBitmap(BitmapFactory.decodeStream(inputStream));
			inputStream.close();
		} catch (FileNotFoundException e) {
			Log.e("HabraLoginForm.onStart", "FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("HabraLoginForm.onStart", "IOException: " + e.getMessage());
		}
	}
	
	@Override
	protected Integer doInBackground(ImageView... params) {
		mFileCaptcha = new File(getCaptcha());
		mImageCaptcha = params[0];
		return 0;
	}
	
	private String getCaptcha() {
		byte[] data =  URLClient.getUrlClient().getURLAsBytes("http://habrahabr.ru/core/captcha/");
		String fileCaptcha = ActivityMain.sCacheDir + "/captcha.png";
		
		File captcha = new File(fileCaptcha);
		try {
			if(!captcha.exists()) captcha.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(captcha);
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			Log.e("HabraLogin.getCaptcha", "IOException: " + e.getMessage());
		}
		
		return fileCaptcha;
	}
}