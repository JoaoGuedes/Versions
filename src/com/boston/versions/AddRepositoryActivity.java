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


public class AddRepositoryActivity extends Activity {

    private int SVNDav = R.id.svndav;
    private int SVN = R.id.svn;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.addrepositories);
        
        Button create = (Button) findViewById(R.id.btnCreate);
        
        create.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				final EditText name = (EditText) findViewById(R.id.editName);
				final EditText user = (EditText) findViewById(R.id.editUser);				
				final EditText password = (EditText) findViewById(R.id.editPassword);
				final EditText address = (EditText) findViewById(R.id.editAddress);
				final RadioGroup protocol = (RadioGroup) findViewById(R.id.editProtocol);
				
				if (name.getText().toString().equals(""))
					Toast.makeText(getApplicationContext(), "Name of the repository must not be null.", Toast.LENGTH_SHORT).show();
				else if (address.getText().toString().equals(""))
					Toast.makeText(getApplicationContext(), "Address of the repository must not be null.", Toast.LENGTH_SHORT).show();
				else {
						//TODO: add regex validation of Host URL
						  new Thread(new Runnable() {
							    public void run() {
									Looper.prepare();	
									
									Repository rep = null;
									if (protocol.getCheckedRadioButtonId() == SVNDav)
										rep = new SVNDav(getApplicationContext(), name.getText().toString(), user.getText().toString(), password.getText().toString(), address.getText().toString());
									if (protocol.getCheckedRadioButtonId() == SVN)
										rep = new SVN(getApplicationContext(), name.getText().toString(), user.getText().toString(), password.getText().toString(), address.getText().toString());
									
									SQLAgent sql_a = new SQLAgent(AddRepositoryActivity.this);
									sql_a.add(rep);								
					                startService(new Intent(AddRepositoryActivity.this, UpdateService.class));
									Looper.loop();
							    }
							  }).start();	
						  
						  finish();
					
				}
			}});
    }
}
