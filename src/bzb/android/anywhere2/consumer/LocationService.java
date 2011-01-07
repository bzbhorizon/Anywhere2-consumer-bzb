package bzb.android.anywhere2.consumer;

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service implements Listener, LocationListener {
	
	private static boolean running = false;
	private static double lat = 0;
	private static double lon = 0;
	private static enum GPSSTATUS {STOPPED, STARTED_NOFIX, STARTED_FIX, STARTED_LOC};
	private static GPSSTATUS gpsStatus = GPSSTATUS.STOPPED;

	public void onCreate() {
		super.onCreate();
		
		setRunning(true);
		
		Log.i(getClass().getName(),"Started service");
		
		try {
			startTracking();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private LocationManager locationManager;
	
	private void startTracking () throws IOException {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);
		Log.i(getClass().getName(),"Listed providers");
		
		for (String provider : providers) {
			Log.i(getClass().getName(),"Enabled provider " + provider);
		}
		
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.addGpsStatusListener(this);
			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
			Log.i(getClass().getName(),"Started GPS tracking");
		} else {
			Log.i(getClass().getName(),"GPS is not enabled; no GPS tracking");
		}
	}
	
	private int satellitesSeen = -1;
	
	public void onGpsStatusChanged(int event) {
		switch (event) {
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			GpsStatus status = locationManager.getGpsStatus(null);
			int i = 0;
			for (GpsSatellite gps : status.getSatellites()) {
				i++;
			}
			if (i != satellitesSeen) {
				satellitesSeen = i;
				Log.i(getClass().getName(), satellitesSeen + " satellites seen");
			}
			break;
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			setGpsStatus(GPSSTATUS.STARTED_FIX);
			Log.i(getClass().getName(), "GPS fix acquired");
			break;
		case GpsStatus.GPS_EVENT_STARTED:
			setGpsStatus(GPSSTATUS.STARTED_NOFIX);
			Log.i(getClass().getName(), "GPS fired up");
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			setGpsStatus(GPSSTATUS.STOPPED);
			Log.i(getClass().getName(), "GPS powered down");
			break;
		}
	}
	
	public void onLocationChanged(Location location) {
		if (getGpsStatus() != GPSSTATUS.STARTED_LOC) {
			setGpsStatus(GPSSTATUS.STARTED_LOC);
		}
		setLat(location.getLatitude());
		setLon(location.getLongitude());
		Log.i(getClass().getName(), "GPS location changed: lat="+location.getLatitude()+", lon="+location.getLongitude());
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	public void onDestroy() {
		if (locationManager != null) {
			locationManager.removeGpsStatusListener(this);
			locationManager.removeUpdates(this);
			Log.i(getClass().getName(), "Unregistered GPS listeners");
		}
	
		super.onDestroy();

		setRunning(false);
		
		Log.i(getClass().getName(),"Destroyed service");
	}
	
	public IBinder onBind(Intent i) {return null;}

	public static void setLat(double lat) {
		LocationService.lat = lat;
	}

	public static double getLat() {
		return lat;
	}

	public static void setLon(double lon) {
		LocationService.lon = lon;
	}

	public static double getLon() {
		return lon;
	}

	public static void setGpsStatus(GPSSTATUS gpsStatus) {
		LocationService.gpsStatus = gpsStatus;
	}

	public static GPSSTATUS getGpsStatus() {
		return gpsStatus;
	}

	public static void setRunning(boolean running) {
		LocationService.running = running;
	}

	public static boolean isRunning() {
		return running;
	}

	

}
