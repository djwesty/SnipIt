/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MessengerActivity extends Activity {
	
	LinearLayout chatLayout;
	Bitmap clientBitmap;
	ScrollView messagesScroll;

	TextView displayName;

	JSONArray messagesListArray;
	EditText nextLine;
	String nextMessage;
	String nextMessageUserFrom;
	RefreshMessageTask refreshMessageTask;

	SendAMessageTask sendAMessage;

	String userFrom;

	Bitmap userFromBitmap;

	private void initializeObjects() {
		
		messagesScroll = (ScrollView) findViewById(R.id.message_scroll);
		userFrom = getIntent().getStringExtra("userFrom");
		chatLayout = (LinearLayout) findViewById(R.id.messenger_layout);
		messagesListArray = new JSONArray();
		nextLine = (EditText) findViewById(R.id.messenger_next_line);
		nextLine.setSelected(false);
		refreshMessageTask = new RefreshMessageTask();
		sendAMessage = new SendAMessageTask();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messenger);
		initializeObjects();
		
		
	
		getActionBar().setTitle(userFrom);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		refreshMessageTask.execute("new_load");

		nextLine.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendAMessage = new SendAMessageTask();
					sendAMessage.execute(nextLine.getText().toString());
					nextLine.setText("");
					handled = true;
				}
				return false;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.messenger, menu);
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

	@Override
	public void onStop() {
		super.onStop();
		finish();
	}

	public void refreshMessages(View v) {
		refreshMessageTask = new RefreshMessageTask();
		refreshMessageTask.execute("new_load");

	}

	private void updateLayout() {
		chatLayout.removeAllViews();
		for (int i = 0; !messagesListArray.isNull(i); i++) {

			LinearLayout containerForNextMessage = new LinearLayout(this);
			TextView aLineView = new TextView(this);
			ImageView nextIcon = new ImageView(this);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			nextMessage = "";
			nextMessageUserFrom = "";
			aLineView.setText("");

			try {
				nextMessage = messagesListArray.getJSONObject(i).getString(
						"message");
				nextMessageUserFrom = messagesListArray.getJSONObject(i)
						.getString("userfrom");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			aLineView.setText(nextMessage);
			if (nextMessageUserFrom.equalsIgnoreCase(LoginActivity
					.getUsername())) {
				lp.gravity = Gravity.RIGHT;
				nextIcon.setImageBitmap(Bitmap.createScaledBitmap(clientBitmap,
						100, 100, false));

				containerForNextMessage.addView(aLineView);
				containerForNextMessage.addView(nextIcon);

			} else {
				lp.gravity = Gravity.LEFT;
				nextIcon.setImageBitmap(Bitmap.createScaledBitmap(
						userFromBitmap, 100, 100, false));
				containerForNextMessage.addView(nextIcon);
				containerForNextMessage.addView(aLineView);
			}
			chatLayout.addView(containerForNextMessage, lp);
			chatLayout.getChildAt(i).setLayoutParams(lp);

		}
	}

	public class RefreshMessageTask extends AsyncTask<String, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(String... params) {
			StringBuffer sb = new StringBuffer("");
			try {
				URI server = new URI(
						"http://23.253.210.151/jsonrequests.php?action=load_chats_by_person"
								+ "&user=" + LoginActivity.getUsername()
								+ "&userfrom=" + userFrom + "&n=" + params[0]);
				HttpGet httpget = new HttpGet();
				httpget.setURI(server);
				HttpResponse httpresponse = LoginActivity.client.execute(
						httpget, LoginActivity.localContext);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpresponse.getEntity().getContent()));

				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}

				// If first load, deal with icons
				if (params[0].equalsIgnoreCase("new_load")) {
					URL clientBitmapUrl = new URL(
							"http://23.253.210.151/uploads/profile_pictures/thumbs/"
									+ LoginActivity.getUsername() + ".jpg");
					clientBitmap = BitmapFactory.decodeStream(clientBitmapUrl
							.openConnection().getInputStream());
					
					URL userFromBitmapUrl = new URL(
							"http://23.253.210.151/uploads/profile_pictures/thumbs/"
									+ userFrom + ".jpg");
					userFromBitmap = BitmapFactory
							.decodeStream(userFromBitmapUrl.openConnection()
									.getInputStream());

				}

				return new JSONArray(sb.toString());

			} catch (URISyntaxException | IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(JSONArray thisArray) {
			Log.d("JSONArray", thisArray.toString());
			messagesListArray = thisArray;
			updateLayout();
			messagesScroll.post(new Runnable() {
			    @Override
			    public void run() {
			    	messagesScroll.fullScroll(ScrollView.FOCUS_DOWN);
			    }
			});
			
		}

	}

	public class SendAMessageTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String link = "http://23.253.210.151/jsonrequests.php?action=send_message&user="
					+ LoginActivity.getUsername()
					+ "&userto="
					+ userFrom
					+ "&message=" + params[0];
			StringBuffer sb = new StringBuffer("");
			try {
				URI website = new URI(link.replaceAll(" ", "%20"));
				HttpPost httppost = new HttpPost();
				httppost.setURI(website);
				HttpResponse response = LoginActivity.client.execute(httppost,
						LoginActivity.localContext);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}

			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
			return sb.toString();

		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equalsIgnoreCase("success")) {
				// Log.d("Message status", result.toString());

			} else {
				Toast.makeText(getBaseContext(), "Message Failed",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_messenger,
					container, false);
			return rootView;
		}

		public PlaceholderFragment() {
		}
	}

}
