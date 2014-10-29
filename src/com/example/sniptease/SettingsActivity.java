/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {
	public  class getEmailTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String link = "http://23.253.210.151/jsonrequests.php?action=";
			link += "get_email_add&username=" + LoginActivity.getUsername();
			Log.d("Connection String", link);
			BufferedReader in = null;
			String data = null;
			try {
	
				URI website = new URI(link);
				HttpGet request = new HttpGet();

	

				request.setURI(website);
				HttpResponse response = LoginActivity.client.execute(request,
						LoginActivity.localContext);
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String newLine = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + newLine);
				}
				in.close();
				data = sb.toString();
				Log.d("Data Value in 'doInBackground' ", data);
				return data;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// in.close();
				Log.d("Exception Caught", "");
				e.printStackTrace();
				return new String("Exception: " + e.getMessage());
			}

		}

		@Override
		protected void onPostExecute(String data) {
			buildEmailPopUp(data);
			
			Log.d("On Post execute email task", data);
		}

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
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			return rootView;
		}
	}

	public class setEmailTask extends AsyncTask<String, Void, String> {
		//TODO Change httpget to httppost
		@Override
		protected String doInBackground(String... params) {
			String link = "http://23.253.210.151/jsonrequests.php?action=";
			link += "update_email_add&username=" + LoginActivity.getUsername()
					+ "&new_email=" + params[0];
			BufferedReader in = null;
			try {
				URI website = new URI(link);
				HttpPost httppost = new HttpPost();

				httppost.setURI(website);
				HttpResponse response = LoginActivity.client.execute(httppost,
						LoginActivity.localContext);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// in.close();
				Log.d("Exception Caught", "");
				e.printStackTrace();
				return new String("Exception: " + e.getMessage());
			}
			return params[0];

		}

		@Override
		protected void onPostExecute(String result) {
		 Toast.makeText(getBaseContext(), "Save Sucessful",
					Toast.LENGTH_SHORT).show();
		 buildEmailPopUp(result);
		// emailTask.execute(LoginActivity.getUsername());
		}
	}

	static Button eMailButton;



	AlertDialog.Builder editEmailPopup;

	EditText editEmailText;

	getEmailTask emailTask;


	public void buildEmailPopUp(String email){
		eMailButton.setText(email);
		editEmailPopup = new AlertDialog.Builder(this);
		editEmailPopup.setMessage("Change E-mail");
		editEmailPopup.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setEmailTask updateEmail = new setEmailTask();
						updateEmail.execute(editEmailText.getText().toString());

					}
				});
		editEmailPopup.setNegativeButton("Cancle",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		
		editEmailText = new EditText(this);
		editEmailText.setText(email);
		editEmailPopup.setView(editEmailText);
	}
	
	public void editEmail(View v) {
		
		AlertDialog dialog = editEmailPopup.create();
		
		dialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TextView nameView = (TextView) findViewById(R.id.settings_username_view);
		nameView.setText(LoginActivity.getUsername());
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		eMailButton = (Button) findViewById(R.id.edit_email_button);
		 emailTask = new getEmailTask();
		emailTask.execute(LoginActivity.getUsername());
		


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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
