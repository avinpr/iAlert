package com.ialert.activity;

import com.ialert.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class SplashScreenActivity extends Activity implements AnimationListener {

	RelativeLayout SpalshScreen_rLayout;
	Animation animZoomIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// setting screen on always while application running time
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Initializing all utilities
		SpalshScreen_rLayout = (RelativeLayout) findViewById(R.id.rLayout_Spalsh);
		animZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
		// listeners for animation effects..
		animZoomIn.setAnimationListener(this);
		
		// Setting RelativeLayout to zoom animation at starting
		SpalshScreen_rLayout.startAnimation(animZoomIn);
		
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// Starting Main_activity on click of the button NEXT
		Intent intent = new Intent(SplashScreenActivity.this,
				MainActivity.class);
		startActivity(intent);
		finish();		
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {		
	}

	@Override
	public void onAnimationStart(Animation arg0) {		
	}
	
}
