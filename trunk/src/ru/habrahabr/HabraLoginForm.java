package ru.habrahabr;

import android.app.Activity;
import android.os.Bundle;

public class HabraLoginForm extends Activity 
{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.first_login);
	}
	
	public void onStart()
	{
		// Insert saved user name
		// Insert {saved password}
		// Load captcha
		// Set checkbox state
	}
}
