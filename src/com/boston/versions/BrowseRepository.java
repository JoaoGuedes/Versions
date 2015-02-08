package com.boston.versions;

import java.util.ArrayList;
import java.util.HashMap;

import org.tmatesoft.svn.core.SVNNodeKind;

import android.app.Activity;
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
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class BrowseRepository extends Activity {

	private String path;
	private SQLAgent sql_a;
	private Repository rd;
	private int id;
	private long revision;
	private ArrayList<HashMap<String, Object>> tree;
	private Context mContext;
	
	public static final String IDENTIFIER = "com.boston.versions.BROWSE_ID";
	public static final String PATH = "com.boston.versions.BROWSE_PATH";

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

    /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.peekfile, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.fileDownload:
        	//Downloader.getFile(rep.GetAddress() + path);
        	SVNNodeKind filetype = (SVNNodeKind) tree.get((int) info.position).get(Repository.BROWSER_NODE_TYPE);
			String ppath = (String) tree.get((int) info.position).get(Repository.BROWSER_NODE_PATH);
			String filename = (String) tree.get((int) info.position).get(Repository.BROWSER_NODE_NAME);	
			if ( SVNNodeKind.FILE.equals(filetype)) {
				System.out.println("You CAN download me!");
    		}
			else if (SVNNodeKind.DIR.equals(filetype)) {
				System.out.println("You CANNOT download me!");
			}
			        	
            return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/
	
	public void run() {

        setContentView(R.layout.gridview);        

        if (!isOnline()) {
        	Toast.makeText(this, "error: no network connection found.", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }  
        
        final ProgressDialog dialog = ProgressDialog.show(mContext, "", "Downloading. Please wait...", true);
		
        final Handler handler = new Handler() {
        	
		   public void handleMessage(Message msg) {
			   
			   if (msg.what == 1 && tree != null)
			   {
			   
			        ListAdapter adapter = new SimpleAdapter(mContext, 
			        		tree, //TODO: tratar de excepções
			        		R.layout.gridrow, 
			        		new String[] { Repository.BROWSER_NODE_NAME, Repository.BROWSER_TYPEICON },
			        		new int[] { R.id.itemName, R.id.itemIcon });

			        GridView gv = (GridView) findViewById(R.id.grid);
			        gv.setAdapter(adapter);    
			        
			        gv.setOnItemClickListener(new OnItemClickListener() {
	
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
								long pos) {
							
							SVNNodeKind filetype = (SVNNodeKind) tree.get((int) pos).get(Repository.BROWSER_NODE_TYPE);
							String ppath = (String) tree.get((int) pos).get(Repository.BROWSER_NODE_PATH);
							String filename = (String) tree.get((int) pos).get(Repository.BROWSER_NODE_NAME);	
							if ( SVNNodeKind.FILE.equals(filetype)) {
							
								Intent myIntent = new Intent(BrowseRepository.this, DiffFileActivity.class);
								myIntent.putExtra(DiffFileActivity.ID, id);				                     	
				            	myIntent.putExtra(DiffFileActivity.FILE, path + '/' + filename );
				            	myIntent.putExtra(DiffFileActivity.FILETYPE, DiffFileActivity.TYPE_VIEWFILE);            					            
					            myIntent.putExtra(DiffFileActivity.NEW_REVISION, revision);
					            myIntent.putExtra(DiffFileActivity.OLD_REVISION, revision);
					            
					            startActivity(myIntent);
			        		}
							else if (SVNNodeKind.DIR.equals(filetype)) {
								Intent myIntent = new Intent(BrowseRepository.this, BrowseRepository.class);
								myIntent.putExtra(IDENTIFIER, rd.getId());
								myIntent.putExtra(PATH, ppath);
								
								System.out.println(ppath);
					            startActivity(myIntent);
							}
							
						}});
			        
			        registerForContextMenu(gv);
			        dialog.dismiss();
			   }
			   else if (msg.what == 0) {
				   dialog.dismiss();
				   Toast.makeText(BrowseRepository.this, "error: invalid repository.", Toast.LENGTH_SHORT).show();
		        	finish();
		        	return;
			   }
				   
    		      
	      }
		   
	   };
        
		  new Thread(new Runnable() {
			    public void run() {
					
					Bundle extras = getIntent().getExtras();
					if (extras == null) {
						finish();
						return;
					}
					
					id = extras.getInt(IDENTIFIER);
					path = extras.getString(PATH);
					sql_a = new SQLAgent(mContext); 
			        rd = sql_a.get(id);		
			        revision = rd.GetHeadRevision();
			        	
			        if (rd != null && (tree = rd.GetBrowsePath(path)) != null) 		        
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
    public void onPause() {
    	super.onPause();
    	finish();
    }*/

}

