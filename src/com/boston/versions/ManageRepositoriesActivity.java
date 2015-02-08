package com.boston.versions;

import com.boston.versions.R;
import com.boston.versions.WidgetProvider.UpdateOfflineService;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ManageRepositoriesActivity extends Activity {
	private Cursor mCursor;
	private SQLAgent sql_a;
	
	public void run() {
        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.managerepositories);

        // Query for all people contacts using the Contacts.People convenience class.
        // Put a managed wrapper around the retrieved cursor so we don't have to worry about
        // requerying or closing it as the activity changes state.
        sql_a = new SQLAgent(this);
        mCursor = sql_a.getCursor();
        //startManagingCursor(mCursor);

        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        ListAdapter adapter = new SimpleCursorAdapter(
                this, // Context.
                android.R.layout.simple_list_item_1,
                mCursor,                                              // Pass in the cursor to bind to.
                new String[] {SQLAgent.NAME},           // Array of cursor columns to bind to.
                new int[] {android.R.id.text1});  // Parallel array of which template objects to bind to those columns.
        
        // Bind to our new adapter.
        //setListAdapter(adapter);
        ImageView iv = (ImageView) findViewById(R.id.imageView1);
        iv.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
	            Intent myIntent = new Intent(ManageRepositoriesActivity.this, AddRepositoryActivity.class);
	            startActivity(myIntent);
				
			}});
        ListView lv = (ListView) findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Intent myIntent = new Intent(ManageRepositoriesActivity.this, BrowseRepository.class);
				myIntent.putExtra(BrowseRepository.IDENTIFIER, (int) arg3);
				myIntent.putExtra(BrowseRepository.PATH, "");
	            startActivity(myIntent);
				
			}
				
			});
        
        registerForContextMenu(lv);
	}
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.managerepositories_context, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.repEdit:  
	            Intent myIntent = new Intent(this, EditRepositoryActivity.class);
	            myIntent.putExtra("com.boston.versions.REV_ID", (int) (info.id));
	            startActivity(myIntent);
                return true;
            case R.id.repDelete:
            	SQLAgent a = new SQLAgent(this);
                a.delete(info.id);
                new RepositoryFactory(this).startWithoutUpdate();
                startService(new Intent(this, UpdateOfflineService.class));
	            mCursor.requery();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	run();
    }
    @Override
    protected void onPause() {
      try {
        super.onPause();
        mCursor.close();
        sql_a.closeDb();
        sql_a.close();
       
      } catch (Exception error) {
        /** Error Handler Code **/
      }// end try/catch (Exception error)
    }// end onStop
}
