package com.boston.versions;

import com.boston.versions.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

 
public class WidgetProvider extends AppWidgetProvider { //Isto Ž um Broadcast Receiver
	
		public static final String ACTION_CYCLE = "com.boston.versions.CYCLE";
		public static final String ACTION_NONE = "com.boston.versions,NONE";
		public static final String ACTION_PEEK = "com.boston.versions.PEEK";
		public static final String ACTION_REFRESH = "com.boston.versions.REFRESH";
		
		private static int FIRST_REPOSITORY = 1;
		private static long DAY = 60*60*24;
		private static final String COUNTER = "counter";
		private static String PREFS = "Versions";
		
		private static String NOREP_REPNAMETXT = "Repository not found";
		private static String NOREP_COMMITAUTHTXT = "Open main application to add.";
		private static String NOREP_MSGTXT = "Magnifying glass: see changes\nRefresh icon: update repositories.";
		
		public static boolean isOnline(Context context) {
		    ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}
		
		
        @Override
        public void onDeleted(Context context, int[] appWidgetIds) {
                //called when widgets are deleted
                //see that you get an array of widgetIds which are deleted
                //so handle the delete of multiple widgets in an iteration
                super.onDeleted(context, appWidgetIds);
        }
 
        @Override
        public void onDisabled(Context context) {
                super.onDisabled(context);
                //runs when all of the instances of the widget are deleted from
                //the home screen
                //here you can do some setup
        }
 
        @Override
        public void onEnabled(Context context) {
                super.onEnabled(context);
	                context.startService(new Intent(context, WidgetProvider.class)); 
	                Intent intnt = new Intent(); 
					intnt.setAction(ACTION_CYCLE);
		            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intnt, 0);
		            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		            views.setOnClickPendingIntent(R.id.data, pendingIntent); 
		         
		        	Intent update = new Intent(context, WidgetProvider.class);  
					update.setAction(ACTION_REFRESH);
		            PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, update, 0);
		            views.setOnClickPendingIntent(R.id.refresh, pendingIntentUpdate);            		         
			        
		            //context.startService(new Intent(context, UpdateService.class));
		            
	                //appWidgetManager.updateAppWidget(appWidgetId, views);
	                ComponentName cn = new ComponentName(context, WidgetProvider.class);  
	                AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
        }

        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) { 
                super.onUpdate(context, appWidgetManager, appWidgetIds);
                
                
        }
        
               
        @Override
        public void onReceive(Context context, Intent intent) { 
        		super.onReceive(context, intent);
        		
        		if (intent.getAction().equals(ACTION_CYCLE)) 
        			context.startService(new Intent(context, FetchService.class));     			          
        		else if (intent.getAction().equals(ACTION_REFRESH)) 
                	context.startService(new Intent(context, UpdateService.class)); 
        		else {
        			
                    /*final int N = appWidgetIds.length;
                    
                    for (int i=0; i<N; i++) {
                        int appWidgetId = appWidgetIds[i];*/

    	                context.startService(new Intent(context, WidgetProvider.class)); 
    	                Intent intnt = new Intent(); 
    					intnt.setAction(ACTION_CYCLE);
    		            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intnt, 0);
    		            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
    		            views.setOnClickPendingIntent(R.id.data, pendingIntent); 
    		         
    		        	Intent update = new Intent(context, WidgetProvider.class);  
    					update.setAction(ACTION_REFRESH);
    		            PendingIntent pendingIntentUpdate = PendingIntent.getBroadcast(context, 0, update, 0);
    		            views.setOnClickPendingIntent(R.id.refresh, pendingIntentUpdate);            		         
    			        
    		            //context.startService(new Intent(context, UpdateService.class));
    		            
    	                //appWidgetManager.updateAppWidget(appWidgetId, views);
    	                ComponentName cn = new ComponentName(context, WidgetProvider.class);  
    	                AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
                    //}
        		}
                	
       }
        
        public static class FetchService extends IntentService {

			public FetchService() {
				super("FETCHSERVICE");
			}

			@Override
			public IBinder onBind(Intent arg0) {
				
				
				return null;
			}


			@Override
			protected void onHandleIntent(Intent intent) {
				SharedPreferences widgetPrefs = this.getSharedPreferences(PREFS, 0);
				int counter = widgetPrefs.getInt(COUNTER, 1);
				
				SQLAgent sql_a = new SQLAgent(this);
				Repository rpst = sql_a.get(++counter);
				
				SharedPreferences.Editor editor = widgetPrefs.edit();
				editor.putInt(COUNTER, (int) SQLAgent.getCircularId(counter, sql_a.count()));
				editor.commit();
				
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
				
				if (rpst != null) {
	                views.setTextViewText(R.id.rep_name, rpst.GetName() + " (" + rpst.GetHeadRevision() + ")");
	                views.setTextViewText(R.id.commit_author, rpst.GetHeadRevisionAuthor());
	                views.setTextViewText(R.id.message, rpst.GetHeadRevisionMsg());
	                
	                long elapsedTime = (System.currentTimeMillis()-rpst.GetTime())/1000;	               
	                String elapsedTimeFormatted;
	                if (elapsedTime > DAY)
	                	elapsedTimeFormatted = (elapsedTime / DAY) + " day(s)";
	                else	
	                	elapsedTimeFormatted = DateUtils.formatElapsedTime(elapsedTime);
	                
	                views.setTextViewText(R.id.update_time, elapsedTimeFormatted); 
	                
		        	Intent peek = new Intent(getApplicationContext(), SeeFilesFromRevisionActivity.class);  
		            peek.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					peek.putExtra("com.boston.versions.PEEK_ID", counter);
					peek.putExtra("com.boston.versions.REV_NUM", rpst.GetHeadRevision());

		            PendingIntent pendingIntentPeek = PendingIntent.getActivity(getApplicationContext(), 0, peek, PendingIntent.FLAG_UPDATE_CURRENT);
			        views.setOnClickPendingIntent(R.id.view, pendingIntentPeek);   			      
			        
				} else {
					views.setTextViewText(R.id.rep_name, NOREP_REPNAMETXT);
					views.setTextViewText(R.id.commit_author, NOREP_COMMITAUTHTXT);
					views.setTextViewText(R.id.message, NOREP_MSGTXT);
	                views.setTextViewText(R.id.update_time, "");
	               				
		        }
                ComponentName cn = new ComponentName(this, WidgetProvider.class);  
                AppWidgetManager.getInstance(this).updateAppWidget(cn, views);
				
			}
        }

        public static class UpdateOfflineService extends IntentService {
        	
 			public UpdateOfflineService() {
 				super("UPDATEOFFLINESERVICE");
 			}
 			
 			@Override
 			public IBinder onBind(Intent arg0) {
 			
 				
 				return null;
 			}
 			
 			@Override
 			protected void onHandleIntent(Intent intent) {
 				
 				RepositoryFactory rf = new RepositoryFactory(this);
     			
         		rf.startWithoutUpdate();
     			

 					SharedPreferences widgetPrefs = this.getSharedPreferences(PREFS, 0);            	
 					
 					SQLAgent sql_a = new SQLAgent(this);
 					Repository rpst = sql_a.get(FIRST_REPOSITORY) ;
 					
 					SharedPreferences.Editor editor = widgetPrefs.edit();
 					editor.putInt(COUNTER, FIRST_REPOSITORY);
 					editor.commit();
 					
 	                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
 					
 					if (rpst != null) {
 		                views.setTextViewText(R.id.rep_name, rpst.GetName() + " (" + rpst.GetHeadRevision() + ")");
 		                views.setTextViewText(R.id.commit_author, rpst.GetHeadRevisionAuthor());
 		                views.setTextViewText(R.id.message, rpst.GetHeadRevisionMsg());
 		                
 		                long elapsedTime = (System.currentTimeMillis()-rpst.GetTime())/1000;
 		                
 		                String elapsedTimeFormatted;
 		                if (elapsedTime > DAY)
 		                	elapsedTimeFormatted = (elapsedTime / DAY) + " day(s)";
 		                else	
 		                	elapsedTimeFormatted = DateUtils.formatElapsedTime(elapsedTime);
 		                
 		                views.setTextViewText(R.id.update_time, elapsedTimeFormatted);
 		                
 			        	Intent peek = new Intent(getApplicationContext(), SeeFilesFromRevisionActivity.class);  
 			            peek.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
 						peek.putExtra("com.boston.versions.PEEK_ID", FIRST_REPOSITORY);
 						peek.putExtra("com.boston.versions.REV_NUM", rpst.GetHeadRevision());

 			            PendingIntent pendingIntentPeek = PendingIntent.getActivity(getApplicationContext(), 0, peek, PendingIntent.FLAG_UPDATE_CURRENT);
 				        views.setOnClickPendingIntent(R.id.view, pendingIntentPeek);
 	 
 					} else {
 						views.setTextViewText(R.id.rep_name, NOREP_REPNAMETXT);
 						views.setTextViewText(R.id.commit_author, NOREP_COMMITAUTHTXT);
 						views.setTextViewText(R.id.message, NOREP_MSGTXT);
 		                views.setTextViewText(R.id.update_time, "");	            
 	
 					}
 	                ComponentName cn = new ComponentName(this, WidgetProvider.class);  
 	                AppWidgetManager.getInstance(this).updateAppWidget(cn, views);
     			}
 			}
         

        public static class UpdateService extends IntentService {
        	
			public UpdateService() {
				super("UPDATESERVICE");
			}
			
			@Override
			public IBinder onBind(Intent arg0) {
			
				
				return null;
			}
			
			@Override
			protected void onHandleIntent(Intent intent) {
				
				RepositoryFactory rf = new RepositoryFactory(this);
    			
				if (!isOnline(this)) {
        				rf.startWithoutUpdate();
    			}
    			else {    				
					Utils.displayOGNotification("Update service...", "Running...", this);
					rf.start();   	
    			}
					SharedPreferences widgetPrefs = this.getSharedPreferences(PREFS, 0);            	
					
					SQLAgent sql_a = new SQLAgent(this);
					Repository rpst = sql_a.get(FIRST_REPOSITORY) ;
					
					SharedPreferences.Editor editor = widgetPrefs.edit();
					editor.putInt(COUNTER, FIRST_REPOSITORY);
					editor.commit();
					
	                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
					
					if (rpst != null) {
		                views.setTextViewText(R.id.rep_name, rpst.GetName() + " (" + rpst.GetHeadRevision() + ")");
		                views.setTextViewText(R.id.commit_author, rpst.GetHeadRevisionAuthor());
		                views.setTextViewText(R.id.message, rpst.GetHeadRevisionMsg());
		                
		                long elapsedTime = (System.currentTimeMillis()-rpst.GetTime())/1000;
		                
		                String elapsedTimeFormatted;
		                if (elapsedTime > DAY)
		                	elapsedTimeFormatted = (elapsedTime / DAY) + " day(s)";
		                else	
		                	elapsedTimeFormatted = DateUtils.formatElapsedTime(elapsedTime);
		                
		                views.setTextViewText(R.id.update_time, elapsedTimeFormatted);
		                
			        	Intent peek = new Intent(getApplicationContext(), SeeFilesFromRevisionActivity.class);  
			            peek.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
						peek.putExtra("com.boston.versions.PEEK_ID", FIRST_REPOSITORY);
						peek.putExtra("com.boston.versions.REV_NUM", rpst.GetHeadRevision());

			            PendingIntent pendingIntentPeek = PendingIntent.getActivity(getApplicationContext(), 0, peek, PendingIntent.FLAG_UPDATE_CURRENT);
				        views.setOnClickPendingIntent(R.id.view, pendingIntentPeek);
	 
					} else {
						views.setTextViewText(R.id.rep_name, NOREP_REPNAMETXT);
						views.setTextViewText(R.id.commit_author, NOREP_COMMITAUTHTXT);
						views.setTextViewText(R.id.message, NOREP_MSGTXT);
		                views.setTextViewText(R.id.update_time, "");	            
	
					}
	                ComponentName cn = new ComponentName(this, WidgetProvider.class);  
	                AppWidgetManager.getInstance(this).updateAppWidget(cn, views);
	        		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        		manager.cancel(Notification.FLAG_ONGOING_EVENT);
    			}
			}
        }
 
