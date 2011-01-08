package bzb.android.anywhere2.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service implements Listener, LocationListener {
	
	private static boolean running = false;
	public static enum GPSSTATUS {OFF, STOPPED, STARTED_NOFIX, STARTED_FIX, STARTED_LOC};
	private static GPSSTATUS gpsStatus = GPSSTATUS.STOPPED;
	public static enum CONTENTSTATUS {NO_LOCATION, NEAR, DOWNLOADING, STALE};
	private static CONTENTSTATUS contentStatus = CONTENTSTATUS.NO_LOCATION;
	private static int distance = -1;

	private static final int NOTIFICATION_ID = 1;
	
	public void onCreate() {
		super.onCreate();
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence tickerText = "Anywhere2 started";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.ic_stat_notify, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Anywhere2 started";
		CharSequence contentText = "Searching for nearby content";
		Intent notificationIntent = new Intent(this, Consumer.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		
		setRunning(true);
		
		Log.i(getClass().getName(),"Started service");
		
		try {
			startTracking();
		} catch (IOException e1) {
			e1.printStackTrace();
			stopSelf();
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
			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
			Log.i(getClass().getName(),"Started GPS tracking");
		} else {
			setGpsStatus(GPSSTATUS.OFF);
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
	
	private boolean gettingURL = false;
	
	public void onLocationChanged(final Location location) {
		setGpsStatus(GPSSTATUS.STARTED_LOC);
		if (!gettingURL) {
			gettingURL = true;
			
			Thread t = new Thread() {
				public void run() {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost("http://anywhere2gae.appspot.com/getcontent");
					List <NameValuePair> parameters = new ArrayList <NameValuePair>();
					parameters.add(new BasicNameValuePair("x",String.valueOf(location.getLatitude())));
					parameters.add(new BasicNameValuePair("y",String.valueOf(location.getLongitude())));
					UrlEncodedFormEntity sendentity;
					try {
						sendentity = new UrlEncodedFormEntity(parameters, HTTP.UTF_8);
						httpPost.setEntity(sendentity);
						HttpResponse response = httpClient.execute(httpPost);
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);

						String resp = new String(ostream.toString());
						try {
							double distance = Double.parseDouble(resp);
							setDistance(distance * 1000.0);
							setContentStatus(CONTENTSTATUS.NEAR);
						} catch (NumberFormatException e) {
							String [] params = resp.split("@");
							if(params.length > 1) {
								setContentStatus(CONTENTSTATUS.DOWNLOADING);
								//deal
								//type = params[0];
								//currentContentUrl = params[1];
							} else {
								System.out.println("Who knows");
							}
						}
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						Log.d("catch1",e.getMessage());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						Log.d("catch2",e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						Log.d("catch3",e.getMessage());
					} finally {
						gettingURL = false;
					}
				}
			};
			t.start();
		}
		
		Log.i(getClass().getName(), "GPS location changed: lat="+location.getLatitude()+", lon="+location.getLongitude());
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	public void onDestroy() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
		
		if (locationManager != null) {
			locationManager.removeGpsStatusListener(this);
			locationManager.removeUpdates(this);
			Log.i(getClass().getName(), "Unregistered GPS listeners");
		}
		
		setGpsStatus(GPSSTATUS.STOPPED);
	
		super.onDestroy();

		setRunning(false);
		
		Log.i(getClass().getName(),"Destroyed service");
	}
	
	public IBinder onBind(Intent i) {return null;}

	private void setGpsStatus(GPSSTATUS gpsStatus) {
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

	public static void setDistance(double distance) {
		LocationService.distance = (int) distance;
	}

	public static int getDistance() {
		return distance;
	}

	public static void setContentStatus(CONTENTSTATUS contentStatus) {
		LocationService.contentStatus = contentStatus;
	}

	public static CONTENTSTATUS getContentStatus() {
		return contentStatus;
	}

	

}
