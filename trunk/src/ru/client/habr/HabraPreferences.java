package ru.client.habr;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class HabraPreferences extends PreferenceActivity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference prefLoginForm = findPreference("prefLoginForm");
            prefLoginForm.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) 
				{
					startActivity(new Intent(getBaseContext(),HabraLoginForm.class));
					return false;
				}
            });
    }
}
