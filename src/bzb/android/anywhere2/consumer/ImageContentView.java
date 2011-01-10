package bzb.android.anywhere2.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import bzb.android.anywhere2.consumer.LocationService.CONTENTSTATUS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class ImageContentView extends Activity {
	
	private Handler mHandler = new Handler();
	private ProgressDialog dialog;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.imagecontent);
		
		dialog = ProgressDialog.show(this, "", 
                "Downloading image. Please wait...", true);
		
		LocationService.setContentStatus(CONTENTSTATUS.VIEWING);
		
		mHandler.post(new Runnable() {
            public void run() {
				try {
					InputStream is = (InputStream) ImageContentView.this.fetch(LocationService.getContentUrl());
					Drawable image = Drawable.createFromStream(is, "src");
					if (image != null) {
						ImageView imgView = new ImageView(ImageContentView.this);
						imgView = (ImageView)findViewById(R.id.imageview);
						imgView.setImageDrawable(image);
					} else {
						LocationService.clearContentUrl();
						finish();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					LocationService.clearContentUrl();
					finish();
				} catch (IOException e) {
					e.printStackTrace();
					LocationService.clearContentUrl();
					finish();
				} finally {
					dialog.dismiss();
				}
            }
		});
	}
	
	private Object fetch(String address) throws MalformedURLException,IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}
	
	public void onDestroy () {
		super.onDestroy();
		LocationService.setContentStatus(CONTENTSTATUS.NO_LOCATION);
	}

}
