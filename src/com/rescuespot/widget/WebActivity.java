package com.rescuespot.widget;
import java.lang.reflect.InvocationTargetException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Button;

public class WebActivity extends Activity {
	private WebView webView;
	private String serverUrl;
	private int current_scale_level;
	private Point Scroll;	
	private Button btnRefresh;
	@SuppressWarnings({ "deprecation", "deprecation", "deprecation" })
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.webview);		
		this.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		webView = (WebView) findViewById(R.id.webview);
		webView.setBackgroundColor(Color.WHITE);	
		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);	
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
		String username= preferences.getString("liveusername", "").trim();		
		serverUrl="http://www.livetrack24.com/tracks/username/"+username;				
		  WebSettings webSettings = webView.getSettings();
		  webSettings.setJavaScriptEnabled (true);
		  webSettings.setSupportZoom (true);
		  webSettings.setBuiltInZoomControls(true);         
          webView.setWebChromeClient(new WebChromeClient());          
          webSettings.setPluginState(PluginState.ON);   
          webView.setWebViewClient(new Callback());  //HERE IS THE MAIN CHANGE
          webView.loadUrl(serverUrl);         
         
          btnRefresh = (Button) findViewById(R.id.btn_refresh);        
          btnRefresh.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {            	
            	  webView.loadUrl(serverUrl);       	
              }
          });
	}	
	
	@Override
	public void onPause() {
	    super.onPause();
	    try {
	        Class.forName("android.webkit.WebView")
	                .getMethod("onPause", (Class[]) null)
	                            .invoke(webView, (Object[]) null);

	    } catch(ClassNotFoundException cnfe) {	        
	    } catch(NoSuchMethodException nsme) {	       
	    } catch(InvocationTargetException ite) {	       
	    } catch (IllegalAccessException iae) {	       
	    }
	}
	private class Callback extends WebViewClient{  //HERE IS THE MAIN CHANGE. 

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
        	   return false;	                
        }	        

    }
	
}