/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Context;
import android.app.Fragment;
import android.util.Log;

public class MyTabsListener implements TabListener {
	public Context context;
	public Fragment fragment;

	public MyTabsListener(Fragment fragment, Context context) {
		this.fragment = fragment;
		this.context = context;

		Log.d("My Tabs Listener Constructor", "");
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

		Log.d("New Tab Selected ", tab.toString() + " " + ft.toString());
		ft.replace(R.id.fragment_container, fragment);

	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Log.d("On Tab Unselected", "Unselect");
		ft.remove(fragment);

	}

}
