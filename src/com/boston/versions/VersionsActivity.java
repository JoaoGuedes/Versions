package com.boston.versions;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class VersionsActivity extends TabActivity {
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ManageRepositoriesActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("repositories").setIndicator("My Repositories",
	                      res.getDrawable(android.R.drawable.ic_menu_agenda))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    spec = tabHost.newTabSpec("Preferences").setIndicator("Preferences",
                res.getDrawable(android.R.drawable.ic_menu_preferences))
            .setContent(new Intent().setClass(this, PreferencesActivity.class));
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);        
        Intent myIntent = new Intent(VersionsActivity.this, ManageRepositoriesActivity.class);
        startActivity(myIntent);

    }   */ 
    
}

