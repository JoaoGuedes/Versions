package com.boston.versions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	String REFRESH = "pref_autorefresh";
	String REFRESH_RATE = "pref_refreshrate";
	int MIN = 1000*60;
	String DEFAULT_MIN = String.valueOf(1440);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		
		if (key.equals(REFRESH)) {
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			boolean val = sp.getBoolean(key, false);
			Intent update = new Intent(this, WidgetProvider.class);  
			update.setAction(WidgetProvider.ACTION_REFRESH);
            PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(this, 0, update, 0);   
            
			if (val == true)  
				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Integer.parseInt(sp.getString(REFRESH_RATE, DEFAULT_MIN))*MIN, pendingIntentUpdate);	
			else 
				am.cancel(pendingIntentUpdate);
			
		}
		else if (key.equals(REFRESH_RATE)) {
			  CheckBoxPreference pref = (CheckBoxPreference) findPreference(REFRESH);
	          pref.setChecked(false);
	          pref.setChecked(true);
		}
		
	}
    
    
}
