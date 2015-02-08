package com.boston.versions;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;

public class PeekFileActivity extends Activity {

	private ByteArrayOutputStream datastream;
	private WebView webview;
	private Context mContext;
	private String summary;
	private String path;
	private Repository rep;
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public String returnCodeString(String input) {
		return "<html>" +
				"<head>" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-16le\">" +
				"<meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />" +
				"<style>" +
				"pre {" +
				"width: device-width;" +                          /* specify width  */
				"white-space: pre;" +           /* CSS 2.0 */
				"white-space: pre-wrap;" +      /* CSS 2.1 */
				"white-space: pre-line;" +     /* CSS 3.0 */
				"white-space: -pre-wrap;" +    /* Opera 4-6 */
				"white-space: -o-pre-wrap;" +  /* Opera 7 */
				"white-space: -moz-pre-wrap;" + /* Mozilla */
				"white-space: -hp-pre-wrap;" +  /* HP Printers */
				"word-wrap: break-word;" +      /* IE 5+ */
				"}" +
				"</style>" +	
				"<link rel=\"stylesheet\" href=\"http://yandex.st/highlightjs/6.1/styles/magula.min.css\">" +
				"<script src=\"http://yandex.st/highlightjs/6.1/highlight.min.js\"></script>" +
				"<script>" +
				"hljs.tabReplace = \'  \';" +
				"hljs.initHighlightingOnLoad();" +
				"</script>" +				
				"</head>" +
				"<body><pre><code>"
				+ input + "</code></pre></body></html>";
	}
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peekfile);
        mContext = this;
        
        if (!isOnline()) {
        	Toast.makeText(this, "error: no network connection found.", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }  
        
        final ProgressDialog dialog = ProgressDialog.show(this, "", "Downloading. Please wait...", true);
        
        final Handler handler = new Handler() {
        	
		   public void handleMessage(Message msg) {
			   
				dialog.dismiss();	
				
				if (datastream != null) {		
					webview.loadData(summary, "text/html", "UTF8");
					
				}
				else {
					Toast.makeText(PeekFileActivity.this, "error: unable to open file.", Toast.LENGTH_SHORT).show();
		        	finish();
		        }

	      }
		   
		   
	   };
        
	   new Thread(new Runnable () {

		public void run() {
			webview = (WebView) findViewById(R.id.webview);
	        webview.getSettings().setJavaScriptEnabled(true);
	        registerForContextMenu(webview);
			Bundle extras = getIntent().getExtras();
			if (extras == null) {
				finish();
				return;
			}
			
			int id = extras.getInt("com.boston.versions.PEEK_REPID");
			path = extras.getString("com.boston.versions.PEEK_FILE");
			long peek_rev = extras.getLong("com.boston.versions.PEEK_REVISION");
			
			SQLAgent sql_a = new SQLAgent(mContext);
			
			rep = sql_a.get(id);
			
			
			
			if (rep != null) {	
				
				datastream = rep.GetFile(path, peek_rev);
				
					if (datastream != null) {
					try {
						summary = returnCodeString(Uri.encode(TextUtils.htmlEncode(datastream.toString("utf-8"))));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS); 
					webview.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
					webview.getSettings().setJavaScriptEnabled(true);
				
				}
			}
			
			handler.sendEmptyMessage(0);
		}}).start();
        
    }
    
	
}
