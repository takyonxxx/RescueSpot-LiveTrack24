package com.rescuespot.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
public class GPSWidgetService extends Service{
    private LocationManager manager = null;     
    static boolean isRunning=false;
   
    private LocationListener listener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {            	 
            }
            @Override
            public void onProviderEnabled(String provider) {               	 
            }
            @Override
            public void onProviderDisabled(String provider) {             	
            }            
            @Override
            public void onLocationChanged(Location location) {  
                    updateCoordinates(location);                     
            	   // stopSelf();
            }
    };
	  
    public static boolean isRunning() {    	
        return isRunning;
    } 
    public static String getLocation() {    	
        return info;
    } 
   
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void onCreate() {
            super.onCreate();       
            manager = (LocationManager)getSystemService(LOCATION_SERVICE);            
    }

    @Override
    public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);  
            isRunning=true;
            waitForGPSCoordinates();                  
    } 
    @Override
    public void onDestroy() {    		
            stopListening(); 
            isRunning=false;
            super.onDestroy();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
            waitForGPSCoordinates();   
            return super.onStartCommand(intent, flags, startId);
    }
     
    private void waitForGPSCoordinates() {
            startListening();
    }
     
    private void startListening(){        
    	    final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            final String bestProvider = manager.getBestProvider(criteria, true);
            if (bestProvider != null && bestProvider.length() > 0) {
                    manager.requestLocationUpdates(bestProvider, 1000, 0, listener);
            }
            else {
                    final List<String> providers = manager.getProviders(true);

                    for (final String provider : providers) {
                            manager.requestLocationUpdates(provider, 1000, 0, listener);
                    }
            }     
            isRunning=true;
    }
     
    public void stopListening(){
            try {
                    if (manager != null && listener != null) {
                            manager.removeUpdates(listener);
                    }

                    manager = null;                    
            }
            catch (final Exception ex) {}  
            isRunning=false;
    }
    static String info="0;0;0;0;0;0";    
    static double gpsSpeed=0;
    private void updateCoordinates(Location location){	
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy' / 'HH:mm:ss"); 
            String time=sdf.format(new Date());           
            if(location!=null)
            {
        	 		float kmPerHr = location.hasSpeed() ? (float) (location.getSpeed() * 3.6): Float.NaN;
        		    float altitude= location.hasAltitude() ? (float) location.getAltitude() : Float.NaN;
        		    float bearing= location.hasBearing() ? (float) location.getBearing() : Float.NaN;
        		   	Date date = new Date(location.getTime());		
            		sdf = new SimpleDateFormat("HH:mm:ss");     
                    info = (  location.getLatitude() + ";" 
                    		+ location.getLongitude() + ";" 
                    		+ (int) kmPerHr + ";"                     		
                    		+ (int) altitude + ";"
                    		+ (int) bearing + ";"
                    		+ location.getTime()+ ";"
                    		+ location.getAccuracy()
                    		);                      
            }
           /* RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout_4_1);                         
            views.setTextViewText(R.id.word_location, info);     
            ComponentName thisWidget = new ComponentName(this, WidgetProvider4_1.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, views);*/
            
    }   
}
