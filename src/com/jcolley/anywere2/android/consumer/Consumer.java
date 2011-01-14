package com.jcolley.anywere2.android.consumer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jcolley.anywere2.android.consumer.LocationService.GPSSTATUS;

public class Consumer extends Activity {
	
	private Handler mHandler = new Handler();
	private Button serviceButton;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        serviceButton = (Button) findViewById(R.id.serviceButton);
        final Button hideButton = (Button) findViewById(R.id.hideButton);
        hideButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        
        TextView gpsStatus = (TextView) findViewById(R.id.gpsStatus);
    	gpsStatus.setTextColor(Color.BLACK);
    	gpsStatus.setPadding(2, 2, 2, 2);
    	TextView contentStatus = (TextView) findViewById(R.id.contentStatus);
    	contentStatus.setTextColor(Color.BLACK);
    	contentStatus.setPadding(2, 2, 2, 2);
    	TextView locationStatus = (TextView) findViewById(R.id.locationStatus);
 	   	locationStatus.setTextColor(Color.BLACK);
 	   	locationStatus.setPadding(2, 2, 2, 2);
        
        if (((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	Log.i(getClass().getName(),"GPS enabled");
        	
        	final Intent locationServiceIntent = new Intent();
		    locationServiceIntent.setAction(getString(R.string.locationServiceName));
		            
        	serviceButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
		            if (LocationService.isRunning()) {
		            	Log.i(getClass().getName(),"Service already running");
		            	stopService(locationServiceIntent);
		            	serviceButton.setText(R.string.serviceOn);
		            } else {
		            	startService(locationServiceIntent);
		            	Log.i(getClass().getName(),"Service starting");
		            	serviceButton.setText(R.string.serviceOff);
		            }
				}
			});
        } else {
        	LocationService.setGpsStatus(GPSSTATUS.OFF);
        }
        
        new Thread(new Runnable() {
			public void run() {
				while (true) {
					mHandler.post(new Runnable() {
			            public void run() {
			            	updateGPSStatusBlurb();
			            	updateContentStatusBlurb();
			            	updateLocationStatusBlurb();
			            }
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
        }).start();
    }
    
    private void updateGPSStatusBlurb () {
    	TextView gpsStatus = (TextView) findViewById(R.id.gpsStatus);
    	serviceButton.setEnabled(true);
    	switch (LocationService.getGpsStatus()) {
    	case OFF:
    		gpsStatus.setText(R.string.gpsOffBlurb);
    		gpsStatus.setBackgroundColor(Color.RED);
    		serviceButton.setEnabled(false);
    		break;
    	case STOPPED:
    		gpsStatus.setText(R.string.gpsStoppedBlurb);
    		gpsStatus.setBackgroundColor(Color.RED);
    		break;
    	case STARTED_NOFIX:
    		String text = getString(R.string.gpsStartedNoFixBlurb) + " " + LocationService.getSatellitesSeen() + " satellites; try to find a clearer view to the sky, or be patient if this is the first time you've tried to use GPS here";
    		gpsStatus.setText(text);
    		gpsStatus.setBackgroundColor(Color.YELLOW);
    		break;
    	case STARTED_FIX:
    		gpsStatus.setText(R.string.gpsStartedFixBlurb);
    		gpsStatus.setBackgroundColor(Color.YELLOW);
    		break;
    	case STARTED_LOC:
    		gpsStatus.setText(R.string.gpsStartedLocBlurb);
    		gpsStatus.setBackgroundColor(Color.GREEN);
    		break;
    	}
    }
    
   private void updateContentStatusBlurb () {
    	TextView contentStatus = (TextView) findViewById(R.id.contentStatus);
    	switch (LocationService.getContentStatus()) {
    	case NO_LOCATION:
    		contentStatus.setText(R.string.notSearchingForContentBlurb);
    		contentStatus.setBackgroundColor(Color.RED);
    		break;
    	case NEAR:
    		String text = getString(R.string.nearContentBlurb) + " " + LocationService.getDistance() + " metres";
    		contentStatus.setText(text);
    		contentStatus.setBackgroundColor(Color.GREEN);
    		break;
    	case DOWNLOADING:
    		contentStatus.setText(R.string.downloadingContentBlurb);
    		contentStatus.setBackgroundColor(Color.GREEN);
    		break;
    	case STALE:
    		contentStatus.setText(R.string.sameContentBlurb);
    		contentStatus.setBackgroundColor(Color.YELLOW);
    		break;
    	}
    }
   
   private void updateLocationStatusBlurb () {
	   TextView locationStatus = (TextView) findViewById(R.id.locationStatus);
	   if (LocationService.isRunning()) {
       		serviceButton.setText(R.string.serviceOff);
       		locationStatus.setText(R.string.serviceOnStatus);
       		locationStatus.setBackgroundColor(Color.GREEN);
       } else {
       		serviceButton.setText(R.string.serviceOn);
       		locationStatus.setText(R.string.serviceOffStatus);
       		locationStatus.setBackgroundColor(Color.RED);
       }
   }
    
}