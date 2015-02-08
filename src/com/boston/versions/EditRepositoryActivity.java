package com.boston.versions;

import com.boston.versions.R;
import com.boston.versions.WidgetProvider.UpdateService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;


public class EditRepositoryActivity extends Activity {
	private int SVN = R.id.svn;
	private int SVNDav = R.id.svndav;
	private int GIT = R.id.git;
	private int TYPE;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.addrepositories);
 
		EditText name = (EditText) findViewById(R.id.editName);
		EditText user = (EditText) findViewById(R.id.editUser);				
		EditText password = (EditText) findViewById(R.id.editPassword);
		EditText address = (EditText) findViewById(R.id.editAddress);
		
		SQLAgent sql_a = new SQLAgent(EditRepositoryActivity.this);		
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		int id = extras.getInt("com.boston.versions.REV_ID");
		
		Repository rep = sql_a.get(id);
		
		if (rep == null) {
			finish();
			return;
		}
		
		name.setText(rep.GetName());
		user.setText(rep.GetUser());
		password.setText(rep.GetPassword());
		address.setText(rep.GetAddress());
        Button create = (Button) findViewById(R.id.btnCreate);
        
        RadioGroup protocol = (RadioGroup) findViewById(R.id.editProtocol);

        if (rep.GetType() == 0)
        	protocol.check(SVNDav);
        if (rep.GetType() == 1)
        	protocol.check(SVN);
        if (rep.GetType() == 2)
            protocol.check(GIT);   
   
        create.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				final EditText name = (EditText) findViewById(R.id.editName);
				final EditText user = (EditText) findViewById(R.id.editUser);				
				final EditText password = (EditText) findViewById(R.id.editPassword);
				final EditText address = (EditText) findViewById(R.id.editAddress);
				RadioGroup protocol = (RadioGroup) findViewById(R.id.editProtocol);
				
				if (protocol.getCheckedRadioButtonId() == SVNDav)
					TYPE = 0;
				if (protocol.getCheckedRadioButtonId() == SVN)
					TYPE = 1;
				if (protocol.getCheckedRadioButtonId() == GIT)
					TYPE = 2;
				
				if (name.getText().toString().equals(""))
					Toast.makeText(getApplicationContext(), "Name of the repository must not be null.", Toast.LENGTH_SHORT).show();
				else if (address.getText().toString().equals(""))
					Toast.makeText(getApplicationContext(), "Address of the repository must not be null.", Toast.LENGTH_SHORT).show();
				else {
						//TODO: add regex validation of Host URL
						  new Thread(new Runnable() {
							    public void run() {
									Looper.prepare();	
									Bundle extras = getIntent().getExtras();
									if (extras == null) {
										return;
									}
									int id = extras.getInt("com.boston.versions.REV_ID");									
									SQLAgent sql_a = new SQLAgent(EditRepositoryActivity.this);
									sql_a.edit(id, TYPE, name.getText().toString(), user.getText().toString(), password.getText().toString(), address.getText().toString());								
					                startService(new Intent(EditRepositoryActivity.this, UpdateService.class));
									Looper.loop();
							    }
							  }).start();	
						  
						  finish();
				}
			}});
    }
}
