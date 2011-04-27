package ru.client.habr;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author WNeZRoS
 * Форма логина
 */
public final class HabraLoginForm extends Activity {
	
	private EditText mTextUserName;
	private EditText mTextPassword;
	private EditText mTextCaptcha;
	private ImageView mImageCaptcha;
	private TextView mTitleError;
	private CheckBox mCheckSavePassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_login);
		
		Log.d("HabraLoginForm.onCreate", "called");
		
		mTitleError = (TextView) findViewById(R.id.titleError);
		mTextUserName = (EditText) findViewById(R.id.textUserName);
		mTextPassword = (EditText) findViewById(R.id.textPassword);
		mImageCaptcha = (ImageView) findViewById(R.id.imageCaptcha);
		mTextCaptcha = (EditText) findViewById(R.id.textCaptcha);
		mCheckSavePassword = (CheckBox) findViewById(R.id.checkSavePassword);
	}
	
	public void onStart() {
		super.onStart();
		
		Log.d("HabraLoginForm.onStart", "called");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mTextUserName.setText(preferences.getString("prefUserName", ""));
		mTextPassword.setText(preferences.getString("prefPassword", ""));
		mCheckSavePassword.setChecked(preferences.getBoolean("prefSavePassword", false));
		
		updateCaptcha();
	}
	
	/**
	 * Действие по нажатию кнопки "Логин"
	 * @param v кнопка
	 */
	public void onClickLogin(View v) {	
		Log.d("HabraLoginForm.onClickLogin", "called");
		
		String username = mTextUserName.getText().toString();
		String password = mTextPassword.getText().toString();
		String captcha = mTextCaptcha.getText().toString();
		String result = null;
		
		if((result = HabraLogin.getHabraLogin().login(username, password, captcha)) == null) {	
			Log.d("HabraLoginForm.onClickLogin", "logged");
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor preferencesEditor = preferences.edit();
			
			preferencesEditor.putString("prefUserName", username);
			
			if(mCheckSavePassword.isChecked()) {
				preferencesEditor.putString("prefPassword", password);
			}
			
			preferencesEditor.putBoolean("prefSavePassword", mCheckSavePassword.isChecked());
			preferencesEditor.commit();
			
			HabraLogin.getHabraLogin().parseUserData();
			Toast.makeText(getApplicationContext(), getString(R.string.logged), Toast.LENGTH_LONG).show();
			
			onBackPressed();
		} else {
			Log.d("HabraLoginForm.onClickLogin", "fail");
			mTitleError.setText(result);
			
			mTextCaptcha.setText("");
			mTextCaptcha.requestFocus();
			
			updateCaptcha();
		}
		Log.d("HabraLoginForm.onClickLogin", "end");
	}
	
	/**
	 * Действие по нажатию кнопки "Пропустить"
	 * @param v кнопка
	 */
	public void onClickSkip(View v) {
		Log.d("HabraLoginForm.onClickSkip", "called");
		Toast.makeText(getApplicationContext(), getString(R.string.skip_login_text), Toast.LENGTH_LONG).show();
		onBackPressed();
	}
	
	public void onBackPressed() {
		SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		preferencesEditor.putBoolean("prefFirstStart", false);
		preferencesEditor.commit();
		
		finish();
	}
	
	private void updateCaptcha() {
		new AsyncCaptchaLoader().execute(mImageCaptcha);
	}
}
