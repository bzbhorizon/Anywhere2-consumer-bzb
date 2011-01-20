package com.jcolley.anywere2.android.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.jcolley.anywere2.android.consumer.LocationService.CONTENTSTATUS;

public class ImageContentView extends Activity {
	
	private Handler mHandler = new Handler();
	private ProgressDialog dialog;
	private Uri uri;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.imagecontent);
		
		LocationService.setContentStatus(CONTENTSTATUS.VIEWING);
		
		mHandler.post(new Runnable() {
            public void run() {
            	uri = null;
            	try {
            		dialog = ProgressDialog.show(ImageContentView.this, "", 
                            "Downloading image. Please wait...", true);
            		
        			URLConnection cn = new URL(LocationService.getContentUrl()).openConnection();
        			InputStream is = cn.getInputStream();
        			File imageMediaFile = new File(Environment.getExternalStorageDirectory().toString(),"mediafile");
        			FileOutputStream fos = new FileOutputStream(imageMediaFile);   
        			byte buf[] = new byte[16 * 1024];
        			do {
        				int numread = is.read(buf);   
        				if (numread <= 0)  
        					break;
        				fos.write(buf, 0, numread);
        			} while (true);
        			fos.flush();
        			fos.close();
        			Log.i("FileOutputStream", "Saved");
        			
        			dialog.dismiss();
        			
        			uri = Uri.parse("file://" + imageMediaFile.getPath());
        			Drawable image = Drawable.createFromPath(uri.getPath());
					if (image != null) {
						ImageView imgView = new ImageView(ImageContentView.this);
						imgView = (ImageView)findViewById(R.id.imageview);
						imgView.setImageDrawable(image);
					} else {
						LocationService.clearContentUrl();
						finish();
					}
        			
        		} catch(Exception e){
        			Log.e("DownloadImage", e.getMessage());
        			finish();
        		} finally {
        			dialog.dismiss();
        		}
            }
		});
	}
	
	public void onDestroy () {
		super.onDestroy();
		LocationService.setContentStatus(CONTENTSTATUS.NO_LOCATION);
		if (uri != null) {
			new File(uri.getPath()).delete();
			uri = null;
		}
	}

}
