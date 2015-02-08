package com.boston.versions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import difflib.DiffRow;
import difflib.DiffRowGenerator;

import android.content.Context;
import android.widget.Toast;

public class SVNDav extends Repository {
		
	private Context mContext;
	public SVNDav(Context context, String name, String user, String password, String address) {
		super(name, user, password, address);
		mContext = context;
		SetType(0);
	}
	
	public SVNDav(Context context, int id, String name, String user, String password, String address, int type, long lastUpdate, long headRevision,
			String headRevisionAuthor, String headRevisionMsg) {
		super(name, user, password, address, type, lastUpdate, headRevision, headRevisionAuthor, headRevisionMsg);
		SetType(0);
		setId(id);
		mContext = context;
	}
	
	@Override
	public ByteArrayOutputStream GetDiff(String path, long newRev, long oldRev) {
		
		ByteArrayOutputStream nRev = GetFile(path, newRev);
		ByteArrayOutputStream oRev = GetFile(path, oldRev);
		
		if (nRev != null && oRev != null) { 
		    List<String> newRevList = DiffTasks.stringToLines(nRev);
		    List<String> oldRevList = DiffTasks.stringToLines(oRev);				  
		    
		    DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();             
		    boolean sideBySide = true;  //default -> inline
		    builder.showInlineDiffs(!sideBySide);
		    builder.ignoreBlankLines(true);
		    builder.columnWidth(120);
		    DiffRowGenerator dfg = builder.build();                
		    List<DiffRow> rows = dfg.generateDiffRows(oldRevList, newRevList);   
		    		    
	    	ByteArrayOutputStream strBuf = new ByteArrayOutputStream();
		    for (DiffRow t: rows) {
		    	
		    	//System.out.println(t.getNewLine());
		    	String o = "";
		    	if (t.getTag().equals(DiffRow.Tag.EQUAL))
		    		o = t.getNewLine() + "<br/>";
		    	
		    	if (t.getTag().equals(DiffRow.Tag.CHANGE)) {
		    		if (!t.getOldLine().equals("") && !t.getNewLine().equals("")) {
		    			o = "<span class=\"delete\">Ñ " + t.getOldLine() + "</span>" +
		    					"<span class=\"insert\">+ " + t.getNewLine() + "</span>";
		    		}
		    	}
		    	if (t.getTag().equals(DiffRow.Tag.INSERT)) {
		    		if (!t.getNewLine().equals(""))
		    			o = "<span class=\"insert\">+ " + t.getNewLine() + "</span>";
		    	}
		    	if (t.getTag().equals(DiffRow.Tag.DELETE)) {
		    		if (!t.getOldLine().equals(""))
		    			o = "<span class=\"delete\">Ñ " + t.getOldLine().replaceAll("\\*", " ") + "</span>";	
		    	}
		    	
		    	try {
					strBuf.write(o.getBytes("utf-8"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    
		    return strBuf;
		    
		}
		
		return null;
		
	}
	
	@Override
	public ByteArrayOutputStream GetFile(String path, long rev) {
		
		DAVRepositoryFactory.setup();
        
        SVNRepository repository = null;
        try {
        	repository = SVNRepositoryFactory.create( SVNURL.parseURIDecoded( this.GetAddress() ) );
        	ISVNAuthenticationManager authManager = new BasicAuthenticationManager( this.GetUser() , this.GetPassword() );
        	repository.setAuthenticationManager( authManager );
        	SVNNodeKind nodeKind = repository.checkPath( path , rev );
        	
        	if ( nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR) {
               return null;
           }
        	else if ( nodeKind == SVNNodeKind.FILE ) {
	        	SVNProperties fileProperties = new SVNProperties();
	        	
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream( );
	            repository.getFile( path , rev , fileProperties , baos );
	            String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
		        boolean isTextType = SVNProperty.isTextMimeType( mimeType );
		            
		        if (isTextType)
		            	return baos;
	            }
        	
        } catch (SVNException e) {}
        
        return null;
	}
	//TODO: add Map<ArrayList<FileChange>, rep
	@Override
	public ArrayList<HashMap<String, Object>> GetBrowsePath(String path) {
		
	
		DAVRepositoryFactory.setup();        
        SVNRepository repository = null;
    
        try {
	    	repository = SVNRepositoryFactory.create( SVNURL.parseURIDecoded( this.GetAddress() ) );
	    	ISVNAuthenticationManager authManager = new BasicAuthenticationManager( this.GetUser() , this.GetPassword() );
	    	repository.setAuthenticationManager( authManager );
	           /*
             * Checks up if the specified path/to/repository part of the URL
             * really corresponds to a directory. If doesn't the program exits.
             * SVNNodeKind is that one who says what is located at a path in a
             * revision. -1 means the latest revision.
             */
            SVNNodeKind nodeKind = repository.checkPath("", -1);
            if (nodeKind == SVNNodeKind.NONE) {
                //System.err.println("There is no entry at '" + url + "'.");
                return null;
            } else if (nodeKind == SVNNodeKind.FILE) {
               // System.err.println("The entry at '" + url + "' is a file while a directory was expected.");
                return null;
            }

            /*
             * Displays the repository tree at the current path - "" (what means
             * the path/to/repository directory)
             */
            return listEntries(repository, path);
        } catch (SVNException svne) {
        	//Toast.makeText(mContext, svne.getErrorMessage().toString(), Toast.LENGTH_SHORT).show();
        	return null;
        }
	}
	
    /*
     * Called recursively to obtain all entries that make up the repository tree
     * repository - an SVNRepository which interface is used to carry out the
     * request, in this case it's a request to get all entries in the directory
     * located at the path parameter;
     * 
     * path is a directory path relative to the repository location path (that
     * is a part of the URL used to create an SVNRepository instance);
     *  
     */
	
    public ArrayList<HashMap<String, Object>> listEntries( SVNRepository repository, String path) {
        /*
         * Gets the contents of the directory specified by path at the latest
         * revision (for this purpose -1 is used here as the revision number to
         * mean HEAD-revision) getDir returns a Collection of SVNDirEntry
         * elements. SVNDirEntry represents information about the directory
         * entry. Here this information is used to get the entry name, the name
         * of the person who last changed this entry, the number of the revision
         * when it was last changed and the entry type to determine whether it's
         * a directory or a file. If it's a directory listEntries steps into a
         * next recursion to display the contents of this directory. The third
         * parameter of getDir is null and means that a user is not interested
         * in directory properties. The fourth one is null, too - the user
         * doesn't provide its own Collection instance and uses the one returned
         * by getDir.
         */
        Collection entries;
		try {
			entries = repository.getDir(path, -1, null,
			        (Collection) null);
		} catch (SVNException e) {
			return null;
		}
        Iterator iterator = entries.iterator();
        
        ArrayList<HashMap<String, Object>> dircontent = new ArrayList<HashMap<String, Object>>();
        while (iterator.hasNext()) {
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            
            /*
             * Checking up if the entry is a directory.
             */
            
            if (entry.getKind() == SVNNodeKind.DIR) {
            	
            	HashMap<String, Object> Item = new HashMap<String, Object>();            	
            	Item.put(Repository.BROWSER_NODE_NAME, entry.getName());
            	Item.put(Repository.BROWSER_NODE_PATH, (path.equals("")? "/" + entry.getName() : path + "/" + entry.getName()));
            	Item.put(Repository.BROWSER_NODE_TYPE, entry.getKind());
            	Item.put(Repository.BROWSER_TYPEICON, R.raw.folder);
            	dircontent.add(Item);                         
            }
            else if (entry.getKind() == SVNNodeKind.FILE) {
            	HashMap<String, Object> Item = new HashMap<String, Object>();            	
            	Item.put(Repository.BROWSER_NODE_NAME, entry.getName());
            	Item.put(Repository.BROWSER_NODE_PATH, (path.equals("")? "/" + entry.getName() : path + "/" + entry.getName()));
            	Item.put(Repository.BROWSER_NODE_TYPE, entry.getKind());
            	Item.put(Repository.BROWSER_TYPEICON, R.raw.file);
            	dircontent.add(Item);                  
            }
        }
        
        return dircontent;
        
    }
	
	@Override
	public ArrayList<HashMap<String, Object>> GetRevisionChanges(long revnum) {
		DAVRepositoryFactory.setup();
        
        SVNRepository repository = null;
    
        try {
	    	repository = SVNRepositoryFactory.create( SVNURL.parseURIDecoded( this.GetAddress() ) );
	    	ISVNAuthenticationManager authManager = new BasicAuthenticationManager( this.GetUser() , this.GetPassword() );
	    	repository.setAuthenticationManager( authManager );
			Collection logEntries = null;
			logEntries = repository.log( new String[] { "" } , null , revnum, revnum, true, true );
			SVNLogEntry head = ( SVNLogEntry ) logEntries.iterator().next( );
			
			ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
			if (head.getChangedPaths().size() > 0) {

                /*
                 * keys are changed paths
                 */
                Set changedPathsSet = head.getChangedPaths().keySet();

                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths
                        .hasNext();) {
                    /*
                     * obtains a next SVNLogEntryPath
                     */
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) head
                            .getChangedPaths().get(changedPaths.next());
                    
                    
                    /*
                     * SVNLogEntryPath.getPath returns the changed path itself;
                     * 
                     * SVNLogEntryPath.getType returns a charecter describing
                     * how the path was changed ('A' - added, 'D' - deleted or
                     * 'M' - modified);
                     * 
                     * If the path was copied from another one (branched) then
                     * SVNLogEntryPath.getCopyPath &
                     * SVNLogEntryPath.getCopyRevision tells where it was copied
                     * from and what revision the origin path was at.
                     */
                    HashMap<String, Object> fc = new HashMap<String, Object>();
                    
                    if (entryPath.getType() == 'A')
                    	fc.put(TYPE, "Added");
                    else if (entryPath.getType() == 'D')
                    	fc.put(TYPE, "Deleted");
                    else if (entryPath.getType() == 'M')
                    	fc.put(TYPE, "Modified");
                    else
                    	fc.put(TYPE, "Unknown");
                    
                    fc.put(PATH, entryPath.getPath());
                    ret.add(fc);
                }
                
                return ret;
            }
			
		} catch (SVNException e) {
			return null;
		}
		return null;
	}
	
	@Override
	public void Update() {
		
		DAVRepositoryFactory.setup();
        
        SVNRepository repository = null;
        try {
        	repository = SVNRepositoryFactory.create( SVNURL.parseURIDecoded( this.GetAddress() ) );
        	ISVNAuthenticationManager authManager = new BasicAuthenticationManager( this.GetUser() , this.GetPassword() );
        	repository.setAuthenticationManager( authManager );

        	long latestRevision = repository.getLatestRevision( );
        	Collection logEntries = null;
        	logEntries = repository.log( new String[] { "" } , null , latestRevision, latestRevision , false, false );
        	
        	if (this.GetHeadRevision() > 0 && this.GetHeadRevision() < latestRevision)
        		Utils.displayNotification("New commit...", "on " + this.GetName() + " (revision " + latestRevision + ")", this.getId(), latestRevision, mContext);
        	
        	this.SetHeadRevision(latestRevision);
	        SVNLogEntry head = ( SVNLogEntry ) logEntries.iterator().next( );	            
	        this.SetHeadRevisionAuthor(head.getAuthor());
	      	this.SetHeadRevisionMsg(head.getMessage());        	
        	UpdateTime(head.getDate());

            
        } catch (SVNException e) {
        	Toast.makeText(mContext, e.getErrorMessage().toString(), Toast.LENGTH_SHORT).show();
        	this.SetHeadRevision(0);           
	        this.SetHeadRevisionAuthor("");
	      	this.SetHeadRevisionMsg(""); 
        	UpdateTime(new Date(System.currentTimeMillis()));
        	//System.out.println(e.getErrorMessage());
        }
			
	}

	@Override
	public void UpdateTime(Date time) {
		SetTime(time.getTime());		
	}
	
	
}
