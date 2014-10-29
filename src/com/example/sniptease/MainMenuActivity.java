/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ToggleButton;

public class MainMenuActivity extends Activity implements
		NewsFragment.OnFragmentInteractionListener,
		ProfileFragment.OnFragmentInteractionListener,
		FollowersFragment.OnFragmentInteractionListener,
		ChatFragment.OnFragmentInteractionListener {
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_news, container,
					false);
			return rootView;
		}
	}

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	boolean editMode = false;

	private void setSnipsHighLightable() {
		for (int i = 0; i < snipList.getChildCount(); i++) {

			// snipList.getChildAt(i).setClickable(false);
			snipList.getChildAt(i).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					JSONObject jsonObject = (JSONObject) view.getTag();
					ImageView imageView = (ImageView) view;
					int currentSnipId;
					try {
						currentSnipId = jsonObject.getInt("snip_id");
					} catch (JSONException e1) {
						currentSnipId = -1;
						e1.printStackTrace();
					}
					if (!selectedSnipsId.contains(currentSnipId)) {
						imageView.setColorFilter(Color.BLUE);
						editButton.setText("Delete");
						selectedSnipsId.add(currentSnipId);
					} else {
						imageView.setColorFilter(Color.TRANSPARENT);
						selectedSnipsId.remove((Object) currentSnipId);
						if (selectedSnipsId.isEmpty()) {
							editButton.setText("Cancel");
						}

					}

				}

			});
		}
	}

	private void setSnipsDefault() {
		for (int i = 0; i < snipList.getChildCount(); i++) {
			// Set not clickable, re-add onClickListener to each
			// snipList.getChildAt(i).setClickable(false);
			snipList.getChildAt(i).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					JSONObject snipInfoObject = (JSONObject) v.getTag();
					boolean isIncoming, hasQuestion, isRead;
					String question, correctAnswer;
					int snipId;
					try {
						isIncoming = snipInfoObject.getBoolean("isIncoming");
						hasQuestion = snipInfoObject.getBoolean("hasQuestion");
						isRead = snipInfoObject.getBoolean("isRead");
						if (hasQuestion) {
							question = snipInfoObject.getString("question");
							correctAnswer = snipInfoObject
									.getString("correctAnswer");
						} else {
							question = "";
							correctAnswer = "";
						}
						snipId = snipInfoObject.getInt("snip_id");
					} catch (JSONException e) {
						isIncoming = (Boolean) null;
						hasQuestion = (Boolean) null;
						isRead = (Boolean) null;
						question = "";
						correctAnswer = "";
						snipId = (Integer) null;
						e.printStackTrace();
					}
					Intent intent;

					if (isIncoming && hasQuestion && !isRead) {
						intent = new Intent(getBaseContext(),
								QuestionViewerActivity.class);

					} else {
						intent = new Intent(getBaseContext(),
								SnipViewerActivity.class);

					}
					if (hasQuestion) {

						intent.putExtra("question", question);
						intent.putExtra("correctAnswer", correctAnswer);
					} else {

					}
					intent.putExtra("snip_id", snipId);
					startActivity(intent);

				}

			});
		}
	}

	ArrayList<Integer> selectedSnipsId;
	LinearLayout snipList;
	Button editButton;

	public void editButtonOnClick(View v) {
		snipList = (LinearLayout) findViewById(R.id.news_table);

		editButton = (Button) v;
		if (editMode == false) {
			editMode = true;
		} else {
			editMode = false;
		}
		if (editMode) {

			// Remove all onClickListeners. Make Clickable, so snips are toggle
			// highlighted.
			setSnipsHighLightable();
			if (selectedSnipsId.isEmpty()) {
				editButton.setText("Cancel");
			} else {
				editButton.setText("Delete");
			}
		} else { // if Edit mode is turned off. Delete snips if any selected,
					// Set snips to normal after.
			setSnipsDefault();
			if (!selectedSnipsId.isEmpty()) {
				DeleteSnipTask deleteSnipTask = new DeleteSnipTask();
				deleteSnipTask.execute(selectedSnipsId);
			}
			editButton.setText("Edit");
		}

	}

	public class DeleteSnipTask extends
			AsyncTask<ArrayList<Integer>, Void, String> {

		@Override
		protected String doInBackground(ArrayList<Integer>... params) {
			Log.d("Delete snip task", params[0].toString());
			String server = "http://23.253.210.151/jsonrequests.php?action=delete_snips_new&username="+LoginActivity.getUsername()+"&ids=";
			String snips = params[0].toString();
			
			server += snips;
		
			server = server.replaceAll("\\[", "");
			server = server.replaceAll("\\]", "");
			server = server.replaceAll(" ", "");
			HttpPost httpPost = new HttpPost(server);
			HttpResponse httpResponse;
			Log.d("Server", server);
			BufferedReader in;
			try {
				 httpResponse = LoginActivity.client.execute(httpPost, LoginActivity.localContext);
				in = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				StringBuilder sb = new StringBuilder();
				sb.append(in.read());
				in.close();
				return sb.toString();
			} catch (IllegalStateException|IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} 
			
			
			
		}

		@Override
		protected void onPostExecute(String params) {
			Log.d("Http Response", params);
			//TODO refresh snip list news feed.
		}
	}

	public void editProfileButton(View v) {
		Intent intent = new Intent(this, EditProfileActivity.class);
		startActivity(intent);
	}

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_main_menu);
		selectedSnipsId = new ArrayList<Integer>();
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab mNewsTab = getActionBar().newTab().setText("News Feed");
		Tab mProfileTab = getActionBar().newTab().setText("Profile");
		Tab mFollowersTab = getActionBar().newTab().setText("Followers");
		Tab mChatTab = getActionBar().newTab().setText("Chat");

		Fragment mNewsFragment = new NewsFragment();
		Fragment mProfileFragment = new ProfileFragment();
		Fragment mFollowersFragment = new FollowersFragment();
		Fragment mChatFragment = new ChatFragment();

		mNewsTab.setTabListener(new MyTabsListener(mNewsFragment,
				getApplicationContext()));
		mProfileTab.setTabListener(new MyTabsListener(mProfileFragment,
				getApplicationContext()));
		mFollowersTab.setTabListener(new MyTabsListener(mFollowersFragment,
				getApplicationContext()));
		mChatTab.setTabListener(new MyTabsListener(mChatFragment,
				getApplicationContext()));

		getActionBar().addTab(mNewsTab);
		getActionBar().addTab(mProfileTab);
		getActionBar().addTab(mFollowersTab);
		getActionBar().addTab(mChatTab);

		getActionBar().show();
	}

	SearchView searchView;
	Menu thisMenu;

	public class SearchTextQueryPressTask extends
			AsyncTask<String, Void, JSONArray> {
		ArrayList<Bitmap> userBitmaps;
		ArrayList<String> usernameList;

		@Override
		protected JSONArray doInBackground(String... params) {
			try {
				usernameList = new ArrayList<String>();
				userBitmaps = new ArrayList<Bitmap>();
				HttpGet httpGet = new HttpGet(
						"http://23.253.210.151/jsonrequests.php?action=find_friend_ci&search_query="
								+ params[0]);
				HttpResponse httpResponse = LoginActivity.client.execute(
						httpGet, LoginActivity.localContext);
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity()
								.getContent(), "UTF-8"));
				StringBuilder sBuilder = new StringBuilder();
				sBuilder.append(bReader.readLine());

				JSONArray jsonArray = new JSONArray(sBuilder.toString());
				Log.d("Request info", jsonArray.toString(1));

				for (int i = 0; i < jsonArray.length(); i++) {
					userBitmaps.add(BitmapFactory.decodeResource(
							getResources(), R.drawable.profile_icon));
				}
				return jsonArray;

				/*
				 * 
				 * 
				 * 
				 * for (int i = 0; !jsonArray.isNull(i); i++) { URL url = new
				 * URL( "http://23.253.210.151/uploads/profile_pictures/thumbs/"
				 * + jsonArray.get(i) + ".jpg"); try {
				 * HttpURLConnection.setFollowRedirects(false); // note : you
				 * may also need //
				 * HttpURLConnection.setInstanceFollowRedirects(false)
				 * HttpURLConnection con = (HttpURLConnection) url
				 * .openConnection(); con.setRequestMethod("HEAD"); if
				 * (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				 * userBitmaps .add(Bitmap.createScaledBitmap(
				 * BitmapFactory.decodeStream(url .openStream()), 100, 100,
				 * false)); } } catch (Exception e) {
				 * 
				 * e.printStackTrace(); }
				 * 
				 * } return jsonArray; } catch (IOException | JSONException e) {
				 * e.printStackTrace(); return null; }
				 */
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONArray jsonArray) {

			try {

				if (searchView.getQuery().toString().isEmpty()) {
					findViewById(R.id.search_results_container).setVisibility(
							View.GONE);
					findViewById(R.id.following_toggle_group).setVisibility(
							View.VISIBLE);
					findViewById(R.id.following_me_scroll_view).setVisibility(
							View.VISIBLE);
					findViewById(R.id.im_following_container).setVisibility(
							View.VISIBLE);
				} else {
					findViewById(R.id.search_results_container).setVisibility(
							View.VISIBLE);
					findViewById(R.id.following_toggle_group).setVisibility(
							View.GONE);
					findViewById(R.id.following_me_container).setVisibility(
							View.GONE);
					findViewById(R.id.im_following_container).setVisibility(
							View.GONE);
					LinearLayout searchResults = (LinearLayout) findViewById(R.id.search_results_container);
					searchResults.setOrientation(LinearLayout.VERTICAL);
					searchResults.removeAllViews();
					for (int i = 0; i < jsonArray.length(); i++) {

						LinearLayout container = new LinearLayout(
								getBaseContext());
						ImageView thisImage = new ImageView(getBaseContext());
						thisImage.setImageBitmap(userBitmaps.get(i));
						container.addView(thisImage);
						TextView thisText = new TextView(getBaseContext());

						thisText.setText(jsonArray.get(i).toString());

						container.addView(thisText);
						container.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								LinearLayout linearLayout = (LinearLayout) v;
								TextView tv = (TextView) linearLayout
										.getChildAt(1);
								Intent intent = new Intent(getBaseContext(),
										ViewAProfileActivity.class);

								intent.putExtra("username", tv.getText()
										.toString());
								startActivity(intent);

							}

						});
						searchResults.addView(container);

					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		MenuItem searchMenuItem = menu.findItem(R.id.search_bar_friends);
		searchView = (SearchView) searchMenuItem.getActionView();

		searchView.setIconifiedByDefault(true);

		thisMenu = menu;
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				SearchTextQueryPressTask task = new SearchTextQueryPressTask();
				task.execute(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				SearchTextQueryPressTask task = new SearchTextQueryPressTask();
				task.execute(newText);
				return false;
			}

		});

		searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {

				thisMenu.setGroupVisible(R.id.search_bar_group, false);
				thisMenu.setGroupEnabled(R.id.search_bar_group, false);
				thisMenu.setGroupVisible(R.id.default_main_group, true);
				thisMenu.setGroupEnabled(R.id.default_main_group, true);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub
	}

	public void addFriendsButton(View V) {

		thisMenu.setGroupVisible(R.id.default_main_group, false);
		thisMenu.setGroupEnabled(R.id.default_main_group, false);
		searchView.setIconified(false);
		thisMenu.setGroupVisible(R.id.search_bar_group, true);
		thisMenu.setGroupEnabled(R.id.search_bar_group, true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_camera) {
			startActivity(new Intent(MainMenuActivity.this,
					CameraActivity.class));
			return true;
		} else if (id == R.id.action_logout) {
			finish();
			return true;
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(MainMenuActivity.this,
					SettingsActivity.class);

			startActivity(intent);
			return true;
		}

		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}
}
