package ru.client.habr;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HabraLoginForm extends Activity 
{
	EditText textUserName, textPassword, textCaptcha;
	Button buttonLogin, buttonSkip;
	WebView viewCaptcha;
	TextView titleError;
	LinearLayout layoutCaptcha;
	CheckBox checkSavePassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.first_login);
        
        Log.d("HabraLoginForm.onCreate", "called");
        
        titleError = (TextView) findViewById(R.id.titleError);
        textUserName = (EditText) findViewById(R.id.textUserName);
        textPassword = (EditText) findViewById(R.id.textPassword);
        layoutCaptcha = (LinearLayout) findViewById(R.id.layoutCaptcha);
        viewCaptcha = (WebView) findViewById(R.id.viewCaptcha);
        textCaptcha = (EditText) findViewById(R.id.textCaptcha);
        checkSavePassword = (CheckBox) findViewById(R.id.checkSavePassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonSkip = (Button) findViewById(R.id.buttonSkip);
	}
	
	public void onStart()
	{
		super.onStart();
		
		Log.d("HabraLoginForm.onStart", "called");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
    	textUserName.setText(preferences.getString("prefUserName", ""));
    	textPassword.setText(preferences.getString("prefPassword", ""));
    	checkSavePassword.setChecked(preferences.getBoolean("prefSavePassword", false));
		
    	viewCaptcha.getSettings().setAllowFileAccess(true);
		viewCaptcha.loadUrl("file://" + HabraLogin.getHabraLogin().getCaptcha());
	}
	
	public void onClickLogin(View v)
	{	
		Log.d("HabraLoginForm.onClickLogin", "called");
		
		String username = textUserName.getText().toString();
		String password = textPassword.getText().toString();
		String captcha = textCaptcha.getText().toString();
		String result = null;
		
		if((result = HabraLogin.getHabraLogin().login(username, password, captcha)) == null)
		{	
			Log.d("HabraLoginForm.onClickLogin", "logged");
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor preferencesEditor = preferences.edit();
			
			preferencesEditor.putString("prefUserName", username);
			
			if(checkSavePassword.isChecked())
			{
				preferencesEditor.putString("prefPassword", password);
			}
			
			preferencesEditor.putBoolean("prefSavePassword", checkSavePassword.isChecked());
			preferencesEditor.commit();
			
			onBackPressed();
		}
		else
		{
			Log.d("HabraLoginForm.onClickLogin", "fail");
			titleError.setText(result);
			
			textCaptcha.setText("");
			textCaptcha.requestFocus();
			
			viewCaptcha.getSettings().setAllowFileAccess(true);
			viewCaptcha.loadUrl("file://" + HabraLogin.getHabraLogin().getCaptcha());
		}
		Log.d("HabraLoginForm.onClickLogin", "end");
	}
	
	public void onClickSkip(View v)
	{
		Log.d("HabraLoginForm.onClickSkip", "called");
		onBackPressed();
	}
	
	public void onBackPressed()
	{
		SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		preferencesEditor.putBoolean("prefFirstStart", false);
		preferencesEditor.commit();
		
		finish();
	}
}
