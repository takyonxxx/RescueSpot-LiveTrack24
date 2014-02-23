package com.rescuespot.widget;
import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;
public class WidgetService extends Service {
    private static final String TAG = "WidgetService";
    static boolean isRunning=false;
    private static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    private static Context mContext;   

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public static boolean isRunning() {    	
        return isRunning;
    } 
    @Override
    public void onCreate() {      
        mContext = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BCAST_CONFIGCHANGED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
    	isRunning=false;
    }

    @Override
    public void onStart(Intent intent, int startid) {   
    	isRunning=true;
    	 Timer timer=new Timer();
         TimerTask tt=new TimerTask(){
             @Override
             public void run() {
            	 refreshScreen();            	 
             }
         };
         timer.schedule(tt,0,1000);
    }

    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {

            if ( myIntent.getAction().equals( BCAST_CONFIGCHANGED ) ) {
            	refreshScreen();              	
            }
        }
    };  
    static GPSWidgetService service;
    static RemoteViews remoteViews;
    public void refreshScreen()
    {    	
    	 try { 
    		    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
    	 		ComponentName thisAppWidget = new ComponentName(mContext.getPackageName(), WidgetProvider4_1.class.getName());
    	 		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		 	    updateWidgetContent(mContext, appWidgetManager, appWidgetIds);			   
		        } catch (Exception e) {}  	
    }
   
    public static void updateWidgetContent(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	    remoteViews = new RemoteViews(context.getPackageName (), R.layout.widget_layout_4_1);  
	 
	    Intent intent = new Intent(context, WidgetProvider4_1.class);
	    intent.setAction(Constants.ACTION_WIDGET_UPDATE_FROM_ICON);
	    PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context,1, intent, 0);
	    remoteViews.setOnClickPendingIntent(R.id.icon,actionPendingIntent);
		  
        // When we click the widget, we want to open our main activity.
        Intent defineIntent = new Intent(context,RescueSpotActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,1, defineIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);  
       
	    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);	    
	}
    
}

    	