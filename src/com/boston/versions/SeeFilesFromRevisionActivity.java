package com.boston.versions;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SeeFilesFromRevisionActivity extends ListActivity {

	private SQLAgent sql_a;
	private Repository rd;
	private int id;
	private long revision;
	private ArrayList<HashMap<String, Object>> files;
	private Context mContext;

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.managerepositories, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.add_rep:
	            Intent myIntent = new Intent(ManageRepositoriesActivity.this, AddRepositoryActivity.class);
	            startActivity(myIntent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}*/
	
	public void run() {

        setContentView(R.layout.simplelist);        

        if (!isOnline()) {
        	Toast.makeText(this, "error: no network connection found.", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }  
        
        final ProgressDialog dialog = ProgressDialog.show(mContext, "", "Downloading. Please wait...", true);
		
        final Handler handler = new Handler() {
        	
		   public void handleMessage(Message msg) {
			   
			   if (msg.what == 1 && files != null)
			   {
			   
			        ListAdapter adapter = new SimpleAdapter(mContext, 
			        		files, //TODO: tratar de excepções
			        		android.R.layout.simple_list_item_2, 
			        		new String[] { Repository.PATH, Repository.TYPE },
			        		new int[] { android.R.id.text1, android.R.id.text2 });
	
			        setListAdapter(adapter);
			        
			        ListView lv = (ListView) findViewById(android.R.id.list);
			        
			        lv.setOnItemClickListener(new OnItemClickListener() {
	
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
								long arg3) {
							
							Intent myIntent = new Intent(arg1.getContext(), DiffFileActivity.class);
				            myIntent.putExtra(DiffFileActivity.ID, id);                    	
			            	myIntent.putExtra(DiffFileActivity.FILE, (String) files.get((int) arg3).get(Repository.PATH));
			            	myIntent.putExtra(DiffFileActivity.FILETYPE, (String) files.get((int) arg3).get(Repository.TYPE)); 
				            myIntent.putExtra(DiffFileActivity.NEW_REVISION, revision);
				            myIntent.putExtra(DiffFileActivity.OLD_REVISION, revision-1);
				            startActivity(myIntent);
							
						}});
			        
			        registerForContextMenu(lv);
			        dialog.dismiss();
			   }
			   else if (msg.what == 0) {
				   dialog.dismiss();
				   Toast.makeText(SeeFilesFromRevisionActivity.this, "error: invalid repository.", Toast.LENGTH_SHORT).show();
		        	finish();
		        	return;
			   }
				   
    		      
	      }
		   
	   };
        
		
		//TODO: add regex validation of Host URL
		  new Thread(new Runnable() {
			    public void run() {
					
					Bundle extras = getIntent().getExtras();
					if (extras == null) {
						finish();
						return;
					}
					
					id = extras.getInt("com.boston.versions.PEEK_ID");
					revision = extras.getLong("com.boston.versions.REV_NUM");
					sql_a = new SQLAgent(mContext); 
			        rd = sql_a.get(id);			    
			        	
			        if (rd != null && (files = rd.GetRevisionChanges(revision)) != null) 		        
			        	handler.sendEmptyMessage(1);			        
			        else
			        	handler.sendEmptyMessage(0);
			    }
			  }).start();	
	    
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        run();
    }
    
    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.managerepositories_context, menu);
    }*/
    
    /*@Override
    public void onPause() {
    	super.onPause();
    	finish();
    }*/
    
    /*@Override
    
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.repEdit:  
	            Intent myIntent = new Intent(this, EditRepositoryActivity.class);
	            myIntent.putExtra("id", (int) (info.id-1));
	            startActivity(myIntent);
                return true;
            case R.id.repDelete:
            	SQLAgent a = new SQLAgent(this);
                a.delete(info.id);
                RepositoryFactory rf = new RepositoryFactory(this);
                rf.startWithoutUpdate();
				Intent intnt = new Intent(this, WidgetProvider.class);  
				intnt.setAction(WidgetProvider.ACTION_REFRESH);
	            PendingIntent.getBroadcast(this, 0, intnt, 0);
	            mCursor.requery();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/
    

}

