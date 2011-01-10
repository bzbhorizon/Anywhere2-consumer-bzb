package bzb.android.anywhere2.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import bzb.android.anywhere2.consumer.LocationService.CONTENTSTATUS;

public class VideoContentView extends Activity {
	
	private Handler mHandler = new Handler();
	private ProgressDialog dialog;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.videocontent);
		
		Button back = (Button) findViewById(R.id.backbutton);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		dialog = ProgressDialog.show(this, "", 
                "Downloading video. Please wait...", true);
		
		LocationService.setContentStatus(CONTENTSTATUS.VIEWING);
		
		mHandler.post(new Runnable() {
            public void run() {
            	try {
        			URLConnection cn = new URL(LocationService.getContentUrl()).openConnection();
        			InputStream is = cn.getInputStream();
        			File videoMediaFile = new File(Environment.getExternalStorageDirectory().toString(),"mediafile.3gp");
        			FileOutputStream fos = new FileOutputStream(videoMediaFile);   
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
        			
        			Uri uri = Uri.parse("file://" + videoMediaFile.getPath());
        			Intent intent = new Intent(Intent.ACTION_VIEW);
        			String type = "video/mp4";
        			intent.setDataAndType(uri, type);
        			startActivity(intent); 
        			
        		} catch(Exception e){
        			Log.e("DownloadVideo", e.getMessage());
        			//LocationService.clearContentUrl();
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
		
	}

}
