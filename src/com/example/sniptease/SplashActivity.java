/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SplashActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
	    //this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove activity bar
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Forces phone to portrait mode

		mSplashThread = new Thread() {	// The thread to wait for splash screen events
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(1000); 	// Wait given period of time or exit on touch
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();				
				}

				finish();

				// Run next activity
				Intent intent = new Intent();
				intent.setClass(sPlashScreen, LoginActivity.class);
				startActivity(intent);
				// stop();
			}
		};

		mSplashThread.start();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_splash,
					container, false);
			return rootView;
		}
	}
	protected boolean _active = true;
	protected int _splashTime = 1000; // time to display the splash screen in ms
	final SplashActivity sPlashScreen = this; // create instance of splash screen
	/**
	 * The thread to process splash screen events
	 */
	private Thread mSplashThread;



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
