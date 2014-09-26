package com.ialert.activity;

import android.app.Activity;
import android.os.Bundle;

import com.ialert.R;


public class LockScreenActivity extends Activity {
	private static LockScreenActivity instance;
	
	static {
		LockScreenActivity.instance = null;
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
		LockScreenActivity.instance = this;
    }
    
    // Disable back button on lockscreen
    @Override
    public void onBackPressed() {
    }
    
    @Override
    public void onDestroy() {
		LockScreenActivity.instance = null;
		super.onDestroy();
    }
    
    public void exit() {
    	super.finish();
    }
    
    public static LockScreenActivity getInstance() {
    	return instance;
    }
}