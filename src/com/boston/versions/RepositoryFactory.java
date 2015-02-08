package com.boston.versions;

import java.util.ArrayList;

import android.content.Context;

public class RepositoryFactory {
	public ArrayList<Repository> myRepositories;
	private Context mContext;
	private SQLAgent sql_a;
	
	/**
	 * Initializes constants
	 * @param context
	 */
	public RepositoryFactory(Context context) {
		myRepositories = new ArrayList<Repository>();
		mContext = context;
		sql_a = new SQLAgent(mContext);
	}
	
	/**
	 * Reads repositories from disk, updates and writes back
	 * @return 
	 */
	public boolean start() {
		readAll();
		if (myRepositories != null) {
			updateAll();
			return writeAll();
		}
		return false;
	}
	
	public boolean startWithoutUpdate() {
		readAll();
		if (myRepositories != null)
			return writeAll();		
		return false;
	}
	/**
	 * Runs update() method of every repository stored
	 */
	private void updateAll() {
			for (Repository t: myRepositories)
					t.Update();

	}
	
	/**
	 * Writes changes to disk
	 */
	private boolean writeAll() {
			return sql_a.add(myRepositories);
	}	
	/**
	 * Reads all repositories from disk, and fills repository buffer myRepositories
	 */
	private void readAll() {
		myRepositories = sql_a.get();		
	}
}
