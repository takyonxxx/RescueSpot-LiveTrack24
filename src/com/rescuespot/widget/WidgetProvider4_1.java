package com.rescuespot.widget;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetProvider4_1 extends AppWidgetProvider {		
	static RemoteViews remoteViews;		
	static PositionWriter liveWriter=null;
	static String username,password;
	static String serverUrl;
	static int vechiletype=1,loginterval=3,speedlimit=3,minacc=60;		
	static boolean loginLW=false,error=false,livetrackenabled=false,autoemergencymsg=false,
			livetracktestserver=false,servicestart=false,emergencysent=false;
	static String sendtoemail,sendtophone,usergmail,usergmailpass,pilotname,wingid,wingmodel,mailerror,
	sendtophone1,sendtophone2,sendtophone3,sendtophone4,sendtophone5;
	static int emergencytime;
	static int type=0;
	static int accuracy=0;
	static int notmoving=0;
	static double LWlatitude=0;
	static double LWlongitude=0;
	static double oldLWlatitude=0;
	static double oldLWlongitude=0;
	static double gpsSpeed=0;
	static int LWgroundSpeed=0;
	static int LWgpsaltitude=0;
	static int LWbearing=0;
	static long LWtime=0;	
	static long LWgpstime=0;	
	static int LWcount=0;
	static Context basecontext;
	static SimpleDateFormat sdf;
	static String stime;
	static String errorinfo,emergencynote;
	static LocationManager locationManager;
		
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
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		basecontext=context;
		  context.stopService(new Intent(context,GPSWidgetService.class));
		  context.startService(new Intent(context,WidgetService.class));		  	 
		  getUserInfo(context);	 		  
		  updateWidgetContent(context, appWidgetManager, appWidgetIds); 
		  locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}	
	@Override
    public void onEnabled(Context context) {	
		super.onEnabled(context);
		basecontext=context;
		 context.stopService(new Intent(context,GPSWidgetService.class));	
		 context.startService(new Intent(context,WidgetService.class));		 
		 getUserInfo(context);	
		 AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	 		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider4_1.class.getName());
	 		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
	 	    updateWidgetContent(context, appWidgetManager, appWidgetIds);	
	 	 locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }	
	@Override
    public void onDisabled(Context context) {       
        super.onDisabled(context);  
        context.stopService(new Intent(context,WidgetService.class));
        context.stopService(new Intent(context,GPSWidgetService.class));            
        if(livetrackenabled && loginLW)
		{        	
			setLivePos emitPos = new setLivePos();
			emitPos.execute(3);						
		}        
    }	
	static String gpsinfo,moveinfo="Not Moving";
	private double distance=0;	
	public static double getDistance(double targetlt,double targetlon,double oldlt,double oldlon)
	    {    	
	    	Location currentLocation = new Location("reverseGeocoded");
			currentLocation.setLatitude(oldlt);          
			currentLocation.setLongitude(oldlon);  			
			Location targetLocation = new Location("reverseGeocoded");
		    targetLocation.setLatitude(targetlt);           
		    targetLocation.setLongitude(targetlon); 	
	     	double distance = (int)currentLocation.distanceTo(targetLocation); 	     	
			return distance;  	     	
	    }
	@Override
	public void onReceive(Context context, Intent intent) {			
		super.onReceive(context, intent);			      
		
		basecontext=context;		
		remoteViews = new RemoteViews(context.getPackageName (), R.layout.widget_layout_4_1);		
		if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE_FROM_ACTIVITY)) {	
			String widgetText = intent.getExtras().getString(Constants.INTENT_EXTRA_WIDGET_TEXT);	
			if(widgetText.equals("start"))
			{	
				context.startService(new Intent(context,GPSWidgetService.class));
				servicestart=true;				
				emergencysent=false;				
				getUserInfo(context);
				notmoving=0;		
				LWcount=0;	
				remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_red);	
				remoteViews.setTextViewText(R.id.word_location, "Spot Service On\nLooking for GPS signal...");	
				locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);					
			}
			else if(widgetText.equals("stop"))
			{
				context.stopService(new Intent(context,GPSWidgetService.class));
				servicestart=false;																			
					setLivePos emitPos = new setLivePos();
					emitPos.execute(3);		
				remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_green);	
				remoteViews.setTextViewText(R.id.word_title, "Rescue Spot");	
				remoteViews.setTextViewText(R.id.word_location, "Spot Service Off");	
			}							 
			
        } 
		else if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE_FROM_ALARM)) {			
			if(servicestart && !GPSWidgetService.isRunning())				
				context.startService(new Intent(context,GPSWidgetService.class));	
			remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_red);
			sdf = new SimpleDateFormat("HH:mm:ss");   
			stime=sdf.format(new Date());   
			try{				
				
				String info=GPSWidgetService.getLocation();
				String[] parts = info.split(";");
				 try{
					    accuracy=(int) Double.parseDouble(parts[6].replace(",",".")); 	
					    LWlatitude =  Double.parseDouble(parts[0].replace(",",".")); 
					    LWlongitude =  Double.parseDouble(parts[1].replace(",",".")); 
					    LWgroundSpeed =  Integer.parseInt(parts[2]); 
					    LWgpsaltitude =Integer.parseInt(parts[3]);
					    LWbearing = Integer.parseInt(parts[4]);
					    LWgpstime=Long.parseLong(parts[5]);	
					    }
					    catch(Exception e){}			   
			   
				if(accuracy<=minacc && accuracy!=0)
            	 {
			        gpsinfo="Gps On";			        		        
			        if (LWgroundSpeed<=speedlimit){   
		            	if(autoemergencymsg)
		            	{	
		            		int max=emergencytime*60/loginterval;	
		            		if(!emergencysent && accuracy<=30)
		            		{
		            		notmoving++;
	            			moveinfo= notmoving + " / " + max;		            		            		
			            		if(notmoving>=max)
			            		{	            			
			            			if(!sendtoemail.trim().equals(""))            		
			                    		new SendMail().execute();        	
			            			if(!sendtophone.trim().equals(""))            		
			            				SendSms();
			            			emergencysent=true;
			            			moveinfo="Rescue Sent!";
			            			notmoving=0;
			            		}
		            		}
		            	}else
		            		moveinfo="Not Moving";		            	
		            }else
		            {
		            	moveinfo="Moving";
		            	notmoving=0;
		            }
			        if(livetrackenabled)
					{	
						if(!loginLW && servicestart)
						{	
							remoteViews.setTextViewText(R.id.word_title, gpsinfo + " - Trying to connect Live");
							setLivePos emitPos = new setLivePos();
							emitPos.execute(1);		
						}else if(loginLW && servicestart)
						{							
							setLivePos emitPos = new setLivePos();
							emitPos.execute(2);			            		
						}							
					}else
					{
						remoteViews.setTextViewText(R.id.word_title, gpsinfo + " - Live Off");	
					}
			        
			        remoteViews.setTextViewText(R.id.word_location,
							String.format("%.6f",LWlatitude).replace(",",".") +" " + getHemisphereLat(LWlatitude) + "   " +
							String.format("%.6f",LWlongitude).replace(",",".") +" " + getHemisphereLon(LWlongitude) +"\n" +
								LWgroundSpeed + " km   " +	LWgpsaltitude + " m   " + LWbearing  + " °  " + moveinfo
								);	 
				}else{	
				    gpsinfo="Gps Not ready";
				    if(accuracy!=0)
				    	 gpsinfo="Gps Not ready" + " Accuracy: " + String.valueOf(accuracy) + " m";
				    remoteViews.setTextViewText(R.id.word_title, gpsinfo);					    
				    remoteViews.setTextViewText(R.id.word_location, "Spot Service On\nLooking for GPS signal...");
				}
			}catch(Exception e){}			
		}else if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE_FROM_ICON)) {
			if(servicestart){
			getUserInfo(context);
			remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_green);	
			if(!sendtoemail.trim().equals(""))            		
        		new SendMail().execute();        	
			if(!sendtophone.trim().equals(""))            		
				SendSms();
			remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_red);	
			Toast.makeText(basecontext,"Emergency Request Sent!",Toast.LENGTH_LONG).show();
			}
		}	
		ComponentName cn = new ComponentName(context, WidgetProvider4_1.class);  
		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);	
	}	 
	 
	public String getHemisphereLat(double coord) {
		if (coord < 0)
			return "S";
		else if(coord > 0)
			return "N";
		else
			return "";
	}
	public String getHemisphereLon(double coord) {
		if (coord < 0) 
			return "W";
		else if(coord > 0)
			return "E";
		else
			return "";
	}
	
	public void getUserInfo(Context contex) {
		serverUrl="http://www.livetrack24.com/";			
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(contex);	
		livetrackenabled = preferences.getBoolean("livetrackenabled", false);	
		autoemergencymsg = preferences.getBoolean("sendemerency", false);	
		livetracktestserver=preferences.getBoolean("livetracktestserver", false);	
		username= preferences.getString("liveusername", "").trim();			
		password = preferences.getString("livepassword", "").trim();
		wingmodel= preferences.getString("wingmodel", "").trim();
		pilotname= preferences.getString("pilotname", "");
		wingid= preferences.getString("wingid", "");
		emergencynote= preferences.getString("emergencynote", "");
		String strlogtime=preferences.getString("loginterval", "3");		
		loginterval=Integer.parseInt(strlogtime);
		
		String strminacc=preferences.getString("minacc", "60");		
		minacc=Integer.parseInt(strminacc);
		
		String strwechiletype=preferences.getString("vehicletype", "1");		
		vechiletype=Integer.parseInt(strwechiletype);	
		
		String strspeedlimit=preferences.getString("speedlimit", "3");	
		speedlimit=Integer.parseInt(strspeedlimit);
		
		sendtophone= preferences.getString("sendtophone", "").trim();	
		sendtophone1 = preferences.getString("sendtophone1", "").trim();
		sendtophone2 = preferences.getString("sendtophone2", "").trim();
		sendtophone3 = preferences.getString("sendtophone3", "").trim();
		sendtophone4 = preferences.getString("sendtophone4", "").trim();
		sendtophone5 = preferences.getString("sendtophone5", "").trim();
		sendtoemail= preferences.getString("sendtoemail", "").trim();			
		this.usergmail="user@gmail.com";
		this.usergmailpass="userpassword";	
		String stremergencytime=preferences.getString("emergencytime", "10").trim();		
		emergencytime=Integer.parseInt(stremergencytime);
		if(livetracktestserver)
			serverUrl="http://test.livetrack24.com/";
			
	}	
	
	private class setLivePos extends AsyncTask<Object, Void, Boolean>{			
		 @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        getUserInfo(basecontext);		        	        
		        errorinfo="";
		        error=false;		       
		 }
		@Override
		protected Boolean doInBackground(Object... params)  {					
			try {				
				 type = (Integer) params[0];	
	             if(!loginLW && type==1)
				 {
	            	 liveWriter = new LeonardoLiveWriter(
					  		  basecontext,
					  		  serverUrl,
					  		  username,
					  		  password,
					  		  wingmodel,
					  		  vechiletype,
					  		  loginterval);
	            	 if(liveWriter!=null)
	           	        liveWriter.emitProlog();	           	      
				 }else if(loginLW && type==2)
				 {				 
					 liveWriter.emitPosition(LWgpstime, LWlatitude, LWlongitude, LWgpsaltitude, LWbearing, LWgroundSpeed);	
				 }else if(loginLW && type==3)
				 {	
					 liveWriter.emitEpilog();  
				 }	
	             return true;
	        }	      
	        catch (Exception e) {	            
	        	errorinfo="Connection Error";		
	        }
			return false;
		}			
		@Override
		protected void onPostExecute(Boolean result) {			
		        super.onPostExecute(result);  
				 if(result)
			        {	error=false;		        	
			        	if(!loginLW && type==1)
				        {
				        	loginLW=true;				        	
				        }	
			        	else if(loginLW && type==3)
			        	{			        		
			        		loginLW=false;
							type=0;	
							LWcount=0;	
							remoteViews.setImageViewResource(R.id.icon, R.drawable.icon_green);	
							remoteViews.setTextViewText(R.id.word_title, "Rescue Spot");	
							remoteViews.setTextViewText(R.id.word_location, "Spot Service Off");	
			        	}else if(loginLW && type==2)
			        	{
			        	   LWcount=liveWriter.getLWCount();	
			        	   remoteViews.setTextViewText(R.id.word_title, gpsinfo + " - Live On: "+ String.valueOf(LWcount-2));
			        	}		        	
			        	
			        }else
			        {
			        	error=true;	
			        	if(loginLW && type==3)
						 {	
							 liveWriter.emitEpilog();  
						 }	
			        	else if(loginLW && type==2)
			        	{
			        	   LWcount=liveWriter.getLWCount();	
			        	   remoteViews.setTextViewText(R.id.word_title, gpsinfo + " - Live On: "+ String.valueOf(LWcount-2));
			        	}	
			        	remoteViews.setTextViewText(R.id.word_title, errorinfo);			        	
			        }				    
				    ComponentName cn = new ComponentName(basecontext, WidgetProvider4_1.class);  
					AppWidgetManager.getInstance(basecontext).updateAppWidget(cn, remoteViews);	
		    }
	}	
	
	private String getUserLocation()
	{ 
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy' / 'HH:mm:ss"); 
		String lasttime;
		if(LWlatitude==0)
		{
			String locationProvider = LocationManager.GPS_PROVIDER;
			Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
			LWlatitude=lastKnownLocation.getLatitude();
			LWlongitude=lastKnownLocation.getLongitude();			
		    LWgpstime=lastKnownLocation.getTime();
		    if(LWgpstime!=0)  
				lasttime=sdf.format(LWgpstime);
			else
				lasttime=sdf.format(new Date());
		}else
			lasttime=sdf.format(LWgpstime);
		
		String link = new String("http://maps.google.com/?q="+ConvertDecimalToDegMinSec(LWlatitude)
				+ "+" + ConvertDecimalToDegMinSec(LWlongitude));
        String info = 
        		"Pilot: "+pilotname + "\n" +
        		"Competition id: "+wingid + "\n" +
        		"Lat:" + (String.format("%.6f",LWlatitude).replace(",",".") + "\n" +
        		"Lng:" + String.format("%.6f",LWlongitude).replace(",",".") + "\n" +
         		"Time: "+ lasttime + "\n" + link         		
          		);           
		return info;		
	}
	public static String ConvertDecimalToDegMinSec(double coord)
    {
    	String output, degrees, minutes, seconds;   
    	double mod = coord % 1;
    	int intPart = (int)coord;     
    	degrees = String.valueOf(intPart);
      	coord = mod * 60;
    	mod = coord % 1;
    	intPart = (int)coord;
            if (intPart < 0) {
               // Convert number to positive if it's negative.
               intPart *= -1;
            }     
    	minutes = String.format("%02d",intPart);     
    	coord = mod * 60;
    	intPart = (int)coord;
            if (intPart < 0) {
               // Convert number to positive if it's negative.
               intPart *= -1;
            }
    	seconds = String.format("%02d",intPart);     
    	//Standard output of D°M′S″
    	output = degrees + "+" + minutes + "+" + seconds;
    	return output;
    }
	private void SendSms(){
		try {				    
			    String phoneNumber="";//"+905418573787";
			    String message=getUserLocation();
			    SmsManager smsManager = SmsManager.getDefault();
			    if(!sendtophone.equals(""))
			    {	
				    phoneNumber = sendtophone;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null); 			
			    }
			    if(!sendtophone1.equals(""))
			    {
			    	phoneNumber = sendtophone1;
			    	smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null); 	
			    }
			    if(!sendtophone2.equals(""))
			    {			    
			    	phoneNumber = sendtophone2;
			    	smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null);   				
			    }
			    if(!sendtophone3.equals(""))
			    {			    
			    	phoneNumber = sendtophone3;
			    	smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null);   	  				
			    }
			    if(!sendtophone4.equals(""))
			    {			    
			    	phoneNumber = sendtophone4;
			    	smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null); 	 				
			    }
			    if(!sendtophone5.equals(""))
			    {			    
			    	phoneNumber = sendtophone5;
			    	smsManager.sendTextMessage(phoneNumber, null, message+"\nEmergency Case", null, null);    	  				
			    }
			    Toast.makeText(basecontext,"SMS sent to " + phoneNumber,Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(basecontext,"SMS failed, please try again later!\n"+e.toString(),Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	private class SendMail extends AsyncTask<Object, Void, Boolean>{	
		MailSender mailsender;			
		@Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        mailsender = new MailSender(usergmail,usergmailpass);		        
		 }
		@Override
		protected Boolean doInBackground(Object... params)  {				
			try {					
				 mailsender.sendMail("Rescue Spot Location Info for " + pilotname, getUserLocation()
						 +"\n"+emergencynote, sendtoemail);
	             return true;
	        }	      
	        catch (Exception e) {	
	        	mailerror=mailerror+e.toString();
	        }
			return false;
		}			
		@Override
		protected void onPostExecute(Boolean result) {
		        super.onPostExecute(result);
		        mailsender=null;	
		       if(!result)
		        {	
		        	Toast.makeText(basecontext,
		    				"Mail send failed! Check Internet Connection",Toast.LENGTH_LONG).show();
		        }			        
		    }
	 }
	
}
