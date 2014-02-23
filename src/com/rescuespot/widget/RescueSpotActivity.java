/* Copyright (C) Türkay Biliyor 
   turkaybiliyor@hotmail.com */
package com.rescuespot.widget;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Spanned;
public class RescueSpotActivity extends Activity implements OnSharedPreferenceChangeListener{
	private Button btnStart,btnSettings,btnExit,btnLive24Track,btnSendSms,btnSendEmail;
	private TextView txtUsername,txttitle;
	private AlarmManager alarmManager;
	private SharedPreferences prefs;		
	private String liveusername,sendtoemail,sendtophone,
	sendtophone1,sendtophone2,sendtophone3,sendtophone4,sendtophone5,
	usergmail,usergmailpass,devices,pilotname,wingid;
	private int loginterval=3;
	static  LocationManager locationManager;	
	static boolean requestlive24login=false,gpsenabled=false,smsenabled=false,
			mailemergency=false,smsemergency=false,usemygmail=false,sendemergency=false;
	private String ourVersion;
	public String error;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendBroadcast(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
        setContentView(R.layout.main);        
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.prefs.registerOnSharedPreferenceChangeListener(this);
        this.alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);  
        txtUsername=(TextView)findViewById(R.id.username);
        txttitle=(TextView)findViewById(R.id.txttitle);
        btnStart = (Button) findViewById(R.id.btnStart);           
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);     
        smsenabled=getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	    gpsenabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);	    
	    PackageManager pm = getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(getPackageName(), 0);

			ourVersion = pi.versionName;
			txttitle.setText("Rescue Spot V" + ourVersion);
		} catch (NameNotFoundException eNnf) {
			throw new RuntimeException(eNnf); 
		}
        if (!gpsenabled)
        	 showGPSDisabledAlertToUser();
        togglebtnStartText(); 	 
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
	            	if(requestlive24login)
	        		{
	        			if(liveusername.equals(""))
	        			{
	        				Toast.makeText(getBaseContext(), "Livetrack24 username not defined", Toast.LENGTH_SHORT).show();
	        			}else
	        				toggleAlarm();
	        				
	        		}else
	        		{	        			
	        			toggleAlarm();
	        		}               
            }
        });
        btnSettings = (Button) findViewById(R.id.btnSettings);        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {       			
    			startActivity(new Intent(RescueSpotActivity.this, EditPreferences.class));       			
            }
        });
        btnExit = (Button) findViewById(R.id.btnExit);        
        btnExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        }); 
        btnLive24Track = (Button) findViewById(R.id.btnLive24Track);        
        btnLive24Track.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(getApplicationContext(),WebActivity.class);					
         		startActivity(i);
            }
        }); 
        btnSendSms = (Button) findViewById(R.id.btnSendSms);        
        btnSendSms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {            	
            	if(smsenabled)
            		SendSms();
            	else
            		Toast.makeText(getBaseContext(), "Your Device Does Not Support SMS", Toast.LENGTH_LONG).show();
            }
        }); 
        btnSendEmail = (Button) findViewById(R.id.btnSendEmail);        
        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {      
            	getUserInfo();
            	if(!sendtoemail.equals(""))            		
            		new SendMail().execute();
            	else
            		Toast.makeText(getBaseContext(), "Please enter an email for sending!", Toast.LENGTH_LONG).show();
            }
        });        
	}
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {		 
		 switch (keyCode) {
		    case KeyEvent.KEYCODE_BACK:		    		 
		    	finish();
		        return true;		    
		    default:
		        return false;
		    }
		}
	private Location getLastLocation()
	{
		Location currentLocation;	
		Location bestLocation = null;
		for (String provider : locationManager.getProviders(true)) {
			currentLocation = locationManager.getLastKnownLocation(provider);
			if (currentLocation == null)
				continue;
			if (bestLocation == null
					|| currentLocation.getAccuracy() < bestLocation.getAccuracy()) {
				bestLocation = currentLocation;
			}
		}
		return bestLocation;
	}
	private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        .setCancelable(false)
        .setPositiveButton("Goto Settings Page To Enable GPS",
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                Intent callGPSSettingIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
	@Override
	protected void onStart() {		
	    super.onStart();	    
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.prefs.unregisterOnSharedPreferenceChangeListener(this);		 
	} 
	@Override
	protected void onResume() {
	    super.onResume();	 
	    getUserInfo();	   
	}
	public void sendServiceStart(Context context) {			
		Intent uiIntent2 = new Intent(Constants.ACTION_WIDGET_UPDATE_FROM_ACTIVITY);
		uiIntent2.putExtra(Constants.INTENT_EXTRA_WIDGET_TEXT,"start");
		context.sendBroadcast(uiIntent2);		
	}   
	public void sendServiceStop(Context context) {
		Intent uiIntent2 = new Intent(Constants.ACTION_WIDGET_UPDATE_FROM_ACTIVITY);
		uiIntent2.putExtra(Constants.INTENT_EXTRA_WIDGET_TEXT,"stop");
		context.sendBroadcast(uiIntent2);	
	}  
    public void startspot() {    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.add(Calendar.SECOND, 10);
    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*loginterval, getPendingIntentForAlarm());
    	final Editor edit = prefs.edit();
		edit.putBoolean(Constants.ALARM_STATUS, true);
		edit.commit();				
    }    
	private void toggleAlarm() {
		if (isAlarmEnabled()) {			
			stopspot();				
		} else {			
			startspot();				
		}		
	}	
	private boolean isAlarmEnabled() {
		return this.prefs.getBoolean(Constants.ALARM_STATUS, false);
	}    
    private PendingIntent getPendingIntentForAlarm() {   
    	Intent intent = new Intent(Constants.ACTION_WIDGET_UPDATE_FROM_ALARM);        	
    	return PendingIntent.getBroadcast(this, 0, intent, 0);
    }    
    private void stopspot() {    	
    	alarmManager.cancel(getPendingIntentForAlarm());
    	final Editor edit = prefs.edit();
		edit.putBoolean(Constants.ALARM_STATUS, false);
		edit.commit();   			
    }
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		if (Constants.ALARM_STATUS.equals(key)) {
			togglebtnStartText();
		}
	}
	private void togglebtnStartText() {		
		if (isAlarmEnabled()) {
			this.btnStart.setText("Stop");
			if(!GPSWidgetService.isRunning())
				sendServiceStart(this);
		} else {
			this.btnStart.setText("Start");	
			if(GPSWidgetService.isRunning())
				sendServiceStop(this);
		}
	}
	public void getUserInfo() {				
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());	
		requestlive24login = preferences.getBoolean("cBlive24", false);			
		sendemergency= preferences.getBoolean("sendemergency", false);	
		liveusername= preferences.getString("liveusername", "").trim();	
		sendtophone= preferences.getString("sendtophone", "").trim();	
		sendtophone1 = preferences.getString("sendtophone1", "").trim();
		sendtophone2 = preferences.getString("sendtophone2", "").trim();
		sendtophone3 = preferences.getString("sendtophone3", "").trim();
		sendtophone4 = preferences.getString("sendtophone4", "").trim();
		sendtophone5 = preferences.getString("sendtophone5", "").trim();
		sendtoemail= preferences.getString("sendtoemail", "").trim();			
		this.usergmail="user@gmail.com";
		this.usergmailpass="userpassword";				
		pilotname= preferences.getString("pilotname", "");
		wingid= preferences.getString("wingid", "");
		String strlogtime=preferences.getString("loginterval", "3").trim();		
		loginterval=Integer.parseInt(strlogtime);
			
		txtUsername.setText(
				"Pilot Name: " + pilotname 
				+"\nCompetition id: " + wingid 			
				);			
	}
	private String getUserLocation()
	{
		Location location=getLastLocation();
		String lasttime;
		Spanned infohtml;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy' / 'HH:mm:ss"); 
		if(location.getTime()!=0)  
			lasttime=sdf.format(location.getTime());
		else
			lasttime=sdf.format(new Date());
		
		String link = new String("http://maps.google.com/?q="+ConvertDecimalToDegMinSec(location.getLatitude())
				+ "+" + ConvertDecimalToDegMinSec(location.getLongitude()));
        String info = 
        		"Pilot: "+pilotname + "\n" +
        		"Competition id: "+wingid + "\n" +
        		"Lat: " + (String.format("%.6f",location.getLatitude()).replace(",",".") + "\n" +
        		"Lng: " + String.format("%.6f",location.getLongitude()).replace(",",".") + "\n" +
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
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   				
			    }
			    if(!sendtophone1.equals(""))
			    {
			    	phoneNumber = sendtophone1;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   		
			    }
			    if(!sendtophone2.equals(""))
			    {			    
			    	phoneNumber = sendtophone2;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   				
			    }
			    if(!sendtophone3.equals(""))
			    {			    
			    	phoneNumber = sendtophone3;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   	  				
			    }
			    if(!sendtophone4.equals(""))
			    {			    
			    	phoneNumber = sendtophone4;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   	 				
			    }
			    if(!sendtophone5.equals(""))
			    {			    
			    	phoneNumber = sendtophone5;
				    smsManager.sendTextMessage(phoneNumber, null, message+"\nNo Problem", null, null);   	  				
			    }			    
			    Toast.makeText(getApplicationContext(),"SMS sent to " + phoneNumber,Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
				"SMS failed, please try again later!\n"+e.toString(),Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	private class SendMail extends AsyncTask<Void, Void, Boolean>{	
		MailSender mailsender;
		 @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        mailsender = new MailSender(usergmail,usergmailpass);
		 }
		@Override
		protected Boolean doInBackground(Void... params)  {					
			try {	
				 mailsender.sendMail("Rescue Spot Location Info for " + pilotname, getUserLocation(), sendtoemail);
	             return true;
	        }	      
	        catch (Exception e) {	
	        	error=error+e.toString();
	        }
			return false;
		}			
		@Override
		protected void onPostExecute(Boolean result) {
		        super.onPostExecute(result);
		        mailsender=null;	
		       if(result)
		        {	
		        	Toast.makeText(getApplicationContext(),
		    				"Mail sent to " + sendtoemail,Toast.LENGTH_LONG).show();
		        }else
		        {	
		        	Toast.makeText(getApplicationContext(),
		    				"Mail send failed!\n"+error,Toast.LENGTH_LONG).show();
		        }			        
		    }
	 }
	
}