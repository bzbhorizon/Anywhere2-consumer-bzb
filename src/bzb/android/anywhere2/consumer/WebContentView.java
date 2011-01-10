package bzb.android.anywhere2.consumer;

import java.net.URISyntaxException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import bzb.android.anywhere2.consumer.LocationService.CONTENTSTATUS;

public class WebContentView extends Activity {
	
	private Handler mHandler = new Handler();
	private WebView mWebView;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.webcontent);
		
		LocationService.setContentStatus(CONTENTSTATUS.VIEWING);
		
		Intent i = new Intent(); 
		i.setAction("android.intent.action.VIEW"); 
		i.addCategory("android.intent.category.BROWSABLE"); 
		Uri uri = Uri.parse(LocationService.getContentUrl()); 
		i.setData(uri); 
		startActivity(i); 
		
		/*mHandler.post(new Runnable() {
            public void run() {
            	mWebView = (WebView) findViewById(R.id.webview);
            	mWebView.setWebViewClient(new HelloWebViewClient());
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(LocationService.getContentUrl());
            }
		});*/
	}
	
	public void onDestroy () {
		super.onDestroy();
		LocationService.setContentStatus(CONTENTSTATUS.NO_LOCATION);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
	        mWebView.goBack();
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private class HelloWebViewClient extends WebViewClient {
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}

}
