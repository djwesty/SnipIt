package com.example.sniptease;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ViewAProfileActivity extends Activity {
	ReportUserTask reportUserTask;

	public void reportUserOnClick(View v) {
		reportUserTask.execute(getIntent().getStringExtra("username"));
	}

	public class ReportUserTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				URI uri = new URI(
						"http://23.253.210.151/jsonrequests.php?action=flag_user&");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String params) {

		}
	}

	public class LoadProfileTask extends AsyncTask<String, Void, JSONObject> {
		Bitmap profilePicBitmap, coverBitmap;
		LinearLayout viewProfileLayout;

		@Override
		protected JSONObject doInBackground(String... params) {
			try {

				HttpGet httpGet = new HttpGet(
						"http://23.253.210.151/jsonrequests.php?action=return_profile_data_new&user="
								+ params[0] + "&user_from="
								+ LoginActivity.getUsername());
				HttpResponse httpResponse = LoginActivity.client.execute(
						httpGet, LoginActivity.localContext);
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity()
								.getContent(), "UTF-8"));
				StringBuilder sBuilder = new StringBuilder();
				sBuilder.append(bReader.readLine());

				JSONObject jsonObject = new JSONObject(sBuilder.toString());
				Log.d("JSON", jsonObject.toString(1));
				// Load profile pic and Cover pic
				URL profilePicUrl = new URL(
						"http://23.253.210.151/uploads/profile_pictures/thumbs/"
								+ getIntent().getStringExtra("username")
								+ ".jpg");
				try {
					profilePicBitmap = Bitmap.createScaledBitmap(BitmapFactory
							.decodeStream(profilePicUrl.openStream()), 100,
							100, false);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				URL coverPhotoUrl = new URL(
						"http://23.253.210.151/uploads/cover-photos/"
								+ getIntent().getStringExtra("username")
								+ ".jpg");
				try {
					coverBitmap = Bitmap.createScaledBitmap(BitmapFactory
							.decodeStream(coverPhotoUrl.openStream()), 200,
							500, false);
				} catch (FileNotFoundException e) {
					return jsonObject;
				}
				return jsonObject;
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			try {
				TextView usernameText = (TextView) findViewById(R.id.view_profile_username);
				usernameText.setText("@"
						+ getIntent().getStringExtra("username"));

				TextView locationText = (TextView) findViewById(R.id.view_profile_location_text);
				locationText.setText(jsonObject.getString("location"));

				TextView followersCountersText = (TextView) findViewById(R.id.view_profile_followers_counter);
				followersCountersText.setText(jsonObject.getString("followers")
						+ "\nFOLLOWERS");

				TextView followingCountersText = (TextView) findViewById(R.id.view_profile_following_counter);
				followingCountersText.setText(jsonObject.getString("following")
						+ "\nFOLLOWING");

				TextView aboutMeTextView = (TextView) findViewById(R.id.view_profile_about_me);
				aboutMeTextView.setText(jsonObject.getString("about_us"));

				ImageView profileImageView = (ImageView) findViewById(R.id.view_profile_pro_pic);
				profileImageView.setImageBitmap(profilePicBitmap);
				viewProfileLayout = (LinearLayout) findViewById(R.id.view_profile_background_container);
				viewProfileLayout.setBackgroundDrawable(new BitmapDrawable(
						getResources(), coverBitmap));
				ToggleButton tb = (ToggleButton) findViewById(R.id.view_profile_following_button_tog);
				if (jsonObject.getString("currently_friends").equalsIgnoreCase(
						"yes")) {

					tb.setChecked(true);
				} else {
					tb.setChecked(false);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public class FollowUserTask extends AsyncTask<Boolean, Void, String> {
		@Override
		protected String doInBackground(Boolean... params) {
			try {
				String uriString = "http://23.253.210.151/jsonrequests.php?action=";
				if (params[0] == true) {// request to follow
					uriString += "find_friend_new&from_user="
							+ LoginActivity.getUsername() + "&friend="
							+ getIntent().getStringExtra("username");
				} else { // end follow/rescind request
					uriString += "remove_friend_new&user="
							+ LoginActivity.getUsername() + "&friend="
							+ getIntent().getStringExtra("username");
				}

				HttpPost httpPost = new HttpPost(uriString);
				HttpResponse httpResponse = LoginActivity.client.execute(
						httpPost, LoginActivity.localContext);
				StringBuffer sb = new StringBuffer("");
				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				sb.append(in.readLine());
				return sb.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String params) {
			Log.d("Toggle following results", params);
		}
	}

	public void startChatOnClick(View v) {
		Intent intent = new Intent(this, MessengerActivity.class);
		Log.d("Username", getIntent().getStringExtra("username"));
		intent.putExtra("userFrom", getIntent().getStringExtra("username"));
		startActivity(intent);
		// finish();
	}

	public void toggleFollowingOnClick(View v) {
		FollowUserTask followUserTask = new FollowUserTask();
		ToggleButton toggleButton = (ToggleButton) v;
		if (toggleButton.isChecked()) {
			followUserTask.execute(true);
		} else {
			followUserTask.execute(false);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_aprofile);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		LoadProfileTask loadProfileTask = new LoadProfileTask();
		loadProfileTask.execute(getIntent().getStringExtra("username"));
		reportUserTask = new ReportUserTask();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_aprofile, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_aprofile,
					container, false);

			return rootView;
		}
	}

}
