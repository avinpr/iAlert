package com.ialert.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ialert.R;
import com.ialert.applink.AppLinkActivity;
import com.ialert.applink.AppLinkApplication;

public class MainActivity extends AppLinkActivity {

	protected Button mConnectButton;
	
	protected View.OnClickListener mConnectButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Toast.makeText(getBaseContext(),R.string.dialog_text, Toast.LENGTH_LONG).show();
			startSyncProxy();
		}
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectButton = (Button)findViewById(R.id.btnConnect);
        mConnectButton.setOnClickListener(mConnectButtonListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @SuppressWarnings("static-access")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.reset:
	        	startSyncProxy();
	            return true;
	        case R.id.about:
	        	AppLinkApplication.getInstance().showAppVersion(this);
	            return true;
	        case R.id.tdk:
	        	Toast.makeText(this, R.string.menu_tdk_toast_message, Toast.LENGTH_LONG).show();
	        	AppLinkApplication.getInstance().setRunInTdk(true);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    
    private void startSyncProxy(){
    	AppLinkApplication.getInstance().endSyncProxyInstance();
    	AppLinkApplication.getInstance().startSyncProxyService();
    }

	@Override
	protected void onDestroy() {
		Log.v(AppLinkApplication.TAG, "onDestroy main");
		super.onDestroy();
	}
}
