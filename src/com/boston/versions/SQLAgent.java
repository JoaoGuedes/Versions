package com.boston.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLAgent extends SQLiteOpenHelper {
	
	private Context mContext;
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String ADDRESS = "address";
	public static final String TYPE = "type";
	public static final String LASTUPDATE = "lastupdate";
	public static final String REV_NUM = "headrevision";
	public static final String REV_AUTH = "headrevisionauthor";
	public static final String REV_MSG = "headrevisionmsg";
		
	public static final int ID_NUM = 0;
	public static final int NAME_NUM = 1;
	public static final int USER_NUM = 2;
	public static final int PASSWORD_NUM = 3;
	public static final int ADDRESS_NUM = 4;
	public static final int TYPE_NUM = 5;
	public static final int LASTUPDATE_NUM = 6;
	public static final int REV_NUM_NUM = 7;
	public static final int REV_AUTH_NUM = 8;
	public static final int REV_MSG_NUM = 9;
	
	private static int SVNDav = 0;
	private static int SVN = 1;
	private static int GIT = 2;
	
	private static final String COUNTER = "counter";
	
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "versions.db";
    private static final String TABLE_NAME = "versions";
    private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                		ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                		NAME + " TEXT NOT NULL, " +
                		USER + " TEXT, " +
                		PASSWORD + " TEXT, " +
                		ADDRESS + " TEXT NOT NULL, " +
                		TYPE + " INTEGER NOT NULL, " +
                		LASTUPDATE + " INTEGER, " +
                		REV_NUM + " INTEGER, " +
                		REV_AUTH + " TEXT, " +
                		REV_MSG + " TEXT );";
    
    private SQLiteDatabase DB_READABLE;
    private SQLiteDatabase DB_WRITABLE;

    public SQLAgent(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	mContext = context;
    }
        
    public boolean delete(long id) {
    	try {
        	DB_WRITABLE = this.getReadableDatabase();
    		DB_WRITABLE.delete(TABLE_NAME, ID + "=?", new String[] { Long.toString(id)});
    		DB_WRITABLE.close();
    	}
    	catch (SQLException e) { 
    		return false;
    	}
    	return true;
    }
    
	public int edit(int id, int type, String name,
			String user, String password, String address) {
		
    	ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(USER, user);
		values.put(PASSWORD, password);
		values.put(ADDRESS, address);
		values.put(TYPE, type);
		
		try {
			DB_WRITABLE = this.getWritableDatabase();
			int r = DB_WRITABLE.update(TABLE_NAME, values, ID + "=?", new String[] { Integer.toString(id)});
			DB_WRITABLE.close();
			return r;
		}
		catch (SQLException e) {}
		
		return 0;
	}


    /**
     * Adds single repository to database. Non-idempotent operation.
     * @param r
     * @return
     */
    public boolean add(Repository r) {
    	ContentValues values = new ContentValues();
		values.put(NAME, r.GetName());
		values.put(USER, r.GetUser());
		values.put(PASSWORD, r.GetPassword());
		values.put(ADDRESS, r.GetAddress());

		if (r instanceof SVNDav)
			values.put(TYPE, 0);
		else if (r instanceof SVN) {
			values.put(TYPE, 1);
		}
		/*else if (r instanceof Git) {
			values.put(TYPE, 2);
		}*/
		//TODO: implement Git and uncomment previous block
		
		try {
			DB_WRITABLE = this.getWritableDatabase();
			DB_WRITABLE.insertOrThrow(TABLE_NAME, null, values);
			DB_WRITABLE.close();
		}
		catch (SQLException e) {
			return false;
		}
		
		return true;
	}
    
    /**
     * Adds an entire Array to database. Idempotent operation.
     * @param myRepos
     * @return
     */
	public boolean add(ArrayList<Repository> myRepos) {

		try {
			
				DB_WRITABLE = this.getWritableDatabase();
				DB_WRITABLE.delete(TABLE_NAME, null, null);
				DB_WRITABLE.delete("sqlite_sequence", NAME + "=?", new String[] { TABLE_NAME });			
			
				Collections.sort(myRepos, new Comparator(){
					 
		            public int compare(Object o1, Object o2) {
		                Repository p1 = (Repository) o1;
		                Repository p2 = (Repository) o2;
		               if (p1.GetTime() > p2.GetTime())
		            	   return -1;
		               else if (p1.GetTime() < p2.GetTime())
		            	   return 1;
		               else 
		            	   return 0;
		            }
		 
		        });

				for (Repository r: myRepos) {
					
			    	ContentValues values = new ContentValues();
					values.put(NAME, r.GetName());
					values.put(USER, r.GetUser());
					values.put(PASSWORD, r.GetPassword());
					values.put(ADDRESS, r.GetAddress());
					values.put(TYPE, r.GetType());
					values.put(LASTUPDATE, r.GetTime());
					values.put(REV_NUM, r.GetHeadRevision());
					values.put(REV_AUTH, r.GetHeadRevisionAuthor());
					values.put(REV_MSG, r.GetHeadRevisionMsg());
					
					DB_WRITABLE.insertOrThrow(TABLE_NAME, null, values);	
					
				}
				
				DB_WRITABLE.close();
			
		}
		catch (SQLException e) {
			return false;
		}
			
		return true;
	}
	
	public long count() {
		try {
			DB_READABLE = this.getReadableDatabase();
			long ret = DatabaseUtils.queryNumEntries(DB_READABLE,TABLE_NAME);
			DB_READABLE.close();
			return ret;
		}
		catch (SQLException e) { return 0; }
	}
	
			
	/**
	 * Fetches specific repository from database
	 * @param id
	 * @return
	 */
	public static long getCircularId(long num, long length) {
		if (length == 0) 
			return 0;
		
		return (num % length==0) ? length : (num%length); 
	}
	
    public Repository get(int id) {
    	
    	if (count() > 0) {
    		long mod_id = getCircularId(id, count());
	    	try {
	    		DB_READABLE = this.getReadableDatabase();
	    		Cursor cursor = DB_READABLE.rawQuery("select * from " + TABLE_NAME + " where " + ID + "= ?", new String[] { Long.toString(mod_id) });
	    		if (!cursor.moveToFirst()) {
	    			DB_READABLE.close();
	    			cursor.close();
	    			return null;
	    		}   
	    		
	    		Repository rps = null;
	      		if (cursor.getInt(TYPE_NUM) == SVNDav) {
	      			rps = new SVNDav(mContext,
	      					cursor.getInt(ID_NUM),
	    					cursor.getString(NAME_NUM), 
	    					cursor.getString(USER_NUM), 
	    					cursor.getString(PASSWORD_NUM),
	    					cursor.getString(ADDRESS_NUM),
	    					cursor.getInt(TYPE_NUM),
							cursor.getLong(LASTUPDATE_NUM), 
							cursor.getLong(REV_NUM_NUM), 
							cursor.getString(REV_AUTH_NUM), 
							cursor.getString(REV_MSG_NUM));
	      		}
	      		else if (cursor.getInt(TYPE_NUM) == SVN) {
	      			rps = new SVN(mContext,
	      					cursor.getInt(ID_NUM),
	    					cursor.getString(NAME_NUM), 
	    					cursor.getString(USER_NUM), 
	    					cursor.getString(PASSWORD_NUM),
	    					cursor.getString(ADDRESS_NUM),
	    					cursor.getInt(TYPE_NUM),
							cursor.getLong(LASTUPDATE_NUM), 
							cursor.getLong(REV_NUM_NUM), 
							cursor.getString(REV_AUTH_NUM), 
							cursor.getString(REV_MSG_NUM));
	      		}	      		
	    			
	    			DB_READABLE.close();
	    			cursor.close();
	    			return rps;
	    		}
	      		//TODO: add rest of types
	    	
	    	catch (SQLException e) {
	    		System.out.println(e.getMessage());
	    		return null;
	    	}
    	} 
    	return null;

    }
    
    public Cursor getCursor() {
    	try {
    		DB_READABLE = this.getReadableDatabase();
    		Cursor cursor = DB_READABLE.rawQuery("select * from " + TABLE_NAME, null); 
    		return cursor;
    	}
    	catch (SQLException e) {
    		return null;
    	}    	
    }
    
    public void closeDb() {
    		if (DB_READABLE != null && DB_READABLE.isOpen())
    			DB_READABLE.close();
    		if (DB_WRITABLE != null && DB_WRITABLE.isOpen())
    			DB_WRITABLE.close();
    }
    /**
     * Fetches all repositories from database
     * @return
     */
	public ArrayList<Repository> get() {
		
		try {
    		DB_READABLE = this.getReadableDatabase();
    		Cursor cursor = DB_READABLE.rawQuery("select * from " + TABLE_NAME, null);
    		
    		if (!cursor.moveToFirst()) {
    			DB_READABLE.close();
    			cursor.close();
    			return null;
    		}    		
    		    		
    		ArrayList<Repository> reps = new ArrayList<Repository>();
    		do {
    			if (cursor.getInt(TYPE_NUM) == SVNDav)
	      			reps.add(new SVNDav(mContext,
	      					cursor.getInt(ID_NUM),
	    					cursor.getString(NAME_NUM), 
	    					cursor.getString(USER_NUM), 
	    					cursor.getString(PASSWORD_NUM),
	    					cursor.getString(ADDRESS_NUM),
	    					cursor.getInt(TYPE_NUM),
							cursor.getLong(LASTUPDATE_NUM), 
							cursor.getLong(REV_NUM_NUM), 
							cursor.getString(REV_AUTH_NUM), 
							cursor.getString(REV_MSG_NUM)));
    			else if (cursor.getInt(TYPE_NUM) == SVN)
	      			reps.add(new SVN(mContext,
	      					cursor.getInt(ID_NUM),
	    					cursor.getString(NAME_NUM), 
	    					cursor.getString(USER_NUM), 
	    					cursor.getString(PASSWORD_NUM),
	    					cursor.getString(ADDRESS_NUM),
	    					cursor.getInt(TYPE_NUM),
							cursor.getLong(LASTUPDATE_NUM), 
							cursor.getLong(REV_NUM_NUM), 
							cursor.getString(REV_AUTH_NUM), 
							cursor.getString(REV_MSG_NUM)));

    		} while (cursor.moveToNext());
    		
    		DB_READABLE.close();
    		
    		if (!cursor.isClosed())
    			cursor.close();
    		return reps;
    	}
    	catch (SQLException e) {
    		return null;
    	}
	}    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
		
	}

}
