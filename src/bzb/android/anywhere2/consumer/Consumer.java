package bzb.android.anywhere2.consumer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Consumer extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	Log.i(getClass().getName(),"GPS enabled");
        	
        	final Intent locationServiceIntent = new Intent();
		    locationServiceIntent.setAction("bzb.android.anywhere2.consumer.STARTLOCATION");
		            
        	final Button serviceButton = (Button) findViewById(R.id.serviceButton);
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
        	Log.i(getClass().getName(),"GPS not enabled - enable it in the Settings menu");
        }
    }
}