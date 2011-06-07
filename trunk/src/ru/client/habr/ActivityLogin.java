package ru.client.habr;

import ru.client.habr.R;
import ru.client.habr.HabraLogin.LoginListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author WNeZRoS
 * ����� ������
 */
public final class ActivityLogin extends Activity {
	
	private EditText mTextUserName;
	private EditText mTextPassword;
	private EditText mTextCaptcha;
	private ImageView mImageCaptcha;
	private TextView mTitleError;
	private CheckBox mCheckSavePassword;
	private Button mButtonLogin;
	private Button mButtonSkip;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_login);
		
		//Dialogs.getDialogs().setContext(this);
		
		Log.d("HabraLoginForm.onCreate", "called");
		
		mTitleError = (TextView) findViewById(R.id.titleError);
		mTextUserName = (EditText) findViewById(R.id.textUserName);
		mTextPassword = (EditText) findViewById(R.id.textPassword);
		mImageCaptcha = (ImageView) findViewById(R.id.imageCaptcha);
		mTextCaptcha = (EditText) findViewById(R.id.textCaptcha);
		mCheckSavePassword = (CheckBox) findViewById(R.id.checkSavePassword);
		mButtonLogin = (Button) findViewById(R.id.buttonLogin);
		mButtonSkip = (Button) findViewById(R.id.buttonSkip);
		
		mImageCaptcha.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HabraLogin.getHabraLogin().loadCaptcha(mImageCaptcha);
			}
		});
	}
	
	public void onStart() {
		super.onStart();
		
		Log.d("HabraLoginForm.onStart", "called");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mTextUserName.setText(preferences.getString("prefUserName", ""));
		mTextPassword.setText(preferences.getString("prefPassword", ""));
		mCheckSavePassword.setChecked(preferences.getBoolean("prefSavePassword", false));
		
		Dialogs.setContext(this);
		
		HabraLogin.getHabraLogin().loadCaptcha(mImageCaptcha);
	}
	
	/**
	 * �������� �� ������� ������ "�����"
	 * @param v ������
	 */
	public void onClickLogin(View v) {	
		Log.d("HabraLoginForm.onClickLogin", "called");
		
		final String username = mTextUserName.getText().toString();
		final String password = mTextPassword.getText().toString();
		final String captcha = mTextCaptcha.getText().toString();
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mButtonLogin.setEnabled(false);
		mButtonSkip.setEnabled(false);
		
		HabraLogin.getHabraLogin().login(username, password, captcha, new LoginListener() {

			@Override
			public void onFinish(String message) {
				
				mButtonLogin.setEnabled(true);
				mButtonSkip.setEnabled(true);
				
				if(message == null) {	
					Log.d("HabraLoginForm.onClickLogin", "logged");
					
					SharedPreferences.Editor preferencesEditor = preferences.edit();
					
					preferencesEditor.putString("prefUserName", username);
					
					if(mCheckSavePassword.isChecked()) {
						preferencesEditor.putString("prefPassword", password);
					}
					
					preferencesEditor.putBoolean("prefSavePassword", mCheckSavePassword.isChecked());
					preferencesEditor.commit();
					
					Dialogs.showToast(R.string.logged);
					
					onBackPressed();
				} else {
					Log.d("HabraLoginForm.onClickLogin", "fail");
					mTitleError.setText(message);
					
					mTextCaptcha.setText("");
					mTextCaptcha.requestFocus();
					
					HabraLogin.getHabraLogin().loadCaptcha(mImageCaptcha);
				}
			}
			
		});
		
		Log.d("HabraLoginForm.onClickLogin", "end");
	}
	
	/**
	 * �������� �� ������� ������ "����������"
	 * @param v ������
	 */
	public void onClickSkip(View v) {
		Log.d("HabraLoginForm.onClickSkip", "called");
		Dialogs.showToast(R.string.skip_login_text);
		onBackPressed();
	}
	
	public void onBackPressed() {
		SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		preferencesEditor.putBoolean("prefFirstStart", false);
		preferencesEditor.commit();
		
		setResult(RESULT_OK);
		finish();
	}
}
