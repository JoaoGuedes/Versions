package com.boston.versions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public abstract class Repository {	
	
	public static final String TYPE="Type";
	public static final String PATH="Path";
	public static final String REVISION="Revision";
	
	public static final String BROWSER_NODE_NAME="Name";
	public static final String BROWSER_NODE_PATH="Path";
	public static final String BROWSER_NODE_TYPE="Type";
	public static final String BROWSER_TYPEICON="Icon";
	
	private String name=null;
	private String user=null;
	private String password=null;
	private String address=null;
	private long lastUpdate;
	private int type;
	private int id;
	
	private long headRevision=0;
	private String headRevisionAuthor=null;
	private String headRevisionMsg=null;
	
	public Repository(String name, String user, String password, String address) {
		this.name = name;
		this.user = user;
		this.password = password;
		this.address = address;
	}
	
	public Repository(String name, String user, String password, String address, int type, long lastUpdate, long headRevision,
			String headRevisionAuthor, String headRevisionMsg) {
		this.name = name;
		this.user = user;
		this.password = password;
		this.address = address;
		this.type = type;
		this.lastUpdate = lastUpdate;
		this.headRevision = headRevision;
		this.headRevisionAuthor = headRevisionAuthor;
		this.headRevisionMsg = headRevisionMsg;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public  void SetType(int type) {
		this.type = type;
	}
	
	public int GetType() {
		return type;
	}
	
	public long GetTime() {
		return lastUpdate;
	}

	public void SetTime(long time) {
		lastUpdate = time;
	}
	public String GetName() {
		return name;
	}
	
	public void SetName(String name) {
		this.name = name;
	}	

	public String GetUser() {
		return user;
	}
	
	public void SetUser(String user) {
		this.user = user;
	}
	
	public String GetPassword() {
		return password;
	}
	
	public void SetPassword(String password) {
		this.password = password;
	}
	
	public String GetAddress() {
		return address;
	}
	
	public void SetAddress(String address) {
		this.address = address;
	}
	
	public long GetHeadRevision() {
		return headRevision;
	}
	
	public void SetHeadRevision(long n) {
		this.headRevision = n;
	}
	
	public String GetHeadRevisionAuthor() {
		return headRevisionAuthor;
	}
	
	public void SetHeadRevisionAuthor(String hra) {
		this.headRevisionAuthor = hra;
	}
	
	public String GetHeadRevisionMsg() {
		return headRevisionMsg;
	}
	
	public void SetHeadRevisionMsg(String msg) {
		this.headRevisionMsg = msg;
	}
	
	public abstract void UpdateTime(Date time);	
		
	public abstract void Update();	
	
	public abstract ArrayList<HashMap<String, Object>> GetRevisionChanges(long revnum);

	public abstract ByteArrayOutputStream GetFile(String path, long rev);

	public abstract ByteArrayOutputStream GetDiff(String path, long newRev, long oldRev);

	public abstract ArrayList<HashMap<String, Object>> GetBrowsePath(String path);

}
