package com.example.sniptease;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SnipRecipientsActivity extends Activity {
	LinearLayout followersContainer;
	CheckBox allFollowersCheckBox;
	final String TAG = "Snip Recipients Activity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snip_recipients);
		followersContainer = (LinearLayout) findViewById(R.id.sendSnipFollowersContainer);
		allFollowersCheckBox = (CheckBox) findViewById(R.id.allFollowersCheckBox);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		LoadFollowersTask loadFollowersTask = new LoadFollowersTask();
		loadFollowersTask.execute();
	}

	public void allFollowersCheckBoxOnClick(View v) {
		if (allFollowersCheckBox.isChecked()) {
			Log.d("V", "is selected");
			for (int i = 1; i < followersContainer.getChildCount(); i++) {
				Log.d("IN for loop", "Loop");

				CheckBox cb = (CheckBox) followersContainer.getChildAt(i);
				cb.setChecked(false);
			}
		}
	}

	public void uploadSnipButtonOnClick(View v) {
		SendSnipTask sendSnipTask = new SendSnipTask();
		sendSnipTask.execute();
	}

	private class SendSnipTask extends AsyncTask<String, Integer, Void> {
		ProgressBar progressBar;
		@Override
		protected void onPreExecute(){
			progressBar = (ProgressBar)findViewById(R.id.uploadSnipProgressBar);
			progressBar.setVisibility(View.VISIBLE);
		}
		
		File snipFile;
		@Override
		protected Void doInBackground(String... snipInfo) {
			String snipPath;
			if (getIntent().getBooleanExtra("snipIsAPic", true)) {
				// If snip is a pic
				snipPath = Environment.getExternalStorageDirectory()
						+ "/tempSnipPic.jpg";
				Log.d(TAG, "snip is a pic");
			} else {
				snipPath = Environment.getExternalStorageDirectory()
						+ "/tempSnipVideo.mp4";
				Log.d(TAG, "snip is video");
			}
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
					.create();
			entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			if (snipPath.contains("jpg")) {
				Bitmap bitmapOrg = BitmapFactory.decodeFile(snipPath);
				if (bitmapOrg != null) {
					snipFile = new File(snipPath);
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 50, bao);
					byte[] data = bao.toByteArray();
					entityBuilder.addPart("img", new ByteArrayBody(data, "snip.jpg"));
					
					Log.d(TAG, "entity added to builder");
					
				}
			} 
	
			
			
			else { // is video
				snipFile = new File(snipPath);
				
				entityBuilder.addPart("video", new FileBody(snipFile));
				
			}
			try {
				String serverQuestion = "http://23.253.210.151/jsonrequests.php?action="
						+ "upload_snip_question&user="
						+ LoginActivity.getUsername()
						+ "&question="
						+ getIntent().getStringExtra("snipQuestion")
						+ "&a1="
						+ getIntent().getStringExtra("correctAnswer")
						+ "&a2="
						+ getIntent().getStringExtra("firstFalseAnswer")
						+ "&a3="
						+ getIntent().getStringExtra("secondFalseAnswer")
						+ "&a4="
						+ getIntent().getStringExtra("thirdFalseAnswer") 
						+"&save="
						+getIntent().getStringExtra("saveQuestion")
					
					+"&hint="+getIntent().getStringExtra("hint");
						
				serverQuestion.replaceAll(" ", "%20");
				HttpPost httpPost = new HttpPost(new URI(serverQuestion));
				HttpResponse httpResponceQuestion = LoginActivity.client
						.execute(httpPost, LoginActivity.localContext);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpResponceQuestion.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				sb.append(br.readLine());
				Log.d(TAG, sb.toString());
				String snipId = sb.toString();
				br.close();
				// ***********Finalize Snip*************//

			
				HttpPost httpPostFinalize = new HttpPost();
				String serverFinalize;
				if (allFollowersCheckBox.isChecked()) {
					serverFinalize = "http://23.253.210.151/jsonrequests.php?action="
							+ "finalize_snip&userfrom="
							+ LoginActivity.getUsername()
							+ "&userto=all_followers"
							+"&snip_id="+snipId;}
				else{
					ArrayList<String> recipientsList = new ArrayList<String>();
					for (int i = 1; i < followersContainer.getChildCount(); i++) {
						CheckBox cb = (CheckBox) followersContainer
								.getChildAt(i);
						if(cb.isChecked()){
							recipientsList.add(cb.getText().toString());
						}
						
					}
					
					
					serverFinalize = "http://23.253.210.151/jsonrequests.php?action="
								+ "finalize_snip&userfrom="
								+ LoginActivity.getUsername()
								+ "&userto=" + recipientsList.get(0)
								+"&snip_id="+snipId;
						for(int i = 1; i<recipientsList.size();i++){
							serverFinalize+="&other_friends="+recipientsList.get(i);
						}
				}
					httpPostFinalize.setURI(new URI(serverFinalize));
					httpPostFinalize.setEntity(entityBuilder.build());
					HttpResponse response = LoginActivity.client.execute(
							httpPostFinalize, LoginActivity.localContext);
					
					
					//get the InputStream
					int count;
					FileBody fb = new FileBody(snipFile);
		            InputStream is=fb.getInputStream();

		            //create a buffer
		            byte data[] = new byte[1024];//1024

		            //this var updates the progress bar
		            long total=0;
		            while((count=is.read(data))!=-1){
		                total+=count;
		                publishProgress((int)(total*100/snipFile.length()));
		            }
					
					
					
					BufferedReader br1 = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					StringBuffer sb1 = new StringBuffer("");
					sb1.append(br1.readLine());
					Log.d(TAG, sb1.toString());
				}
			catch(IOException | URISyntaxException e){
				e.printStackTrace();
				return null;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
			//Log.d(TAG, Integer.toString(progress[0]));
		}

		@Override
		protected void onPostExecute(Void voids) {
			Log.d(TAG, "Done executing");
			progressBar.setVisibility(View.INVISIBLE);
			
			finish();
		}

	}

	private class LoadFollowersTask extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {
				URI uri = new URI(LoginActivity.getURL()
						+ "jsonrequests.php?action=get_followers&username="
						+ LoginActivity.getUsername());
				HttpGet httpGet = new HttpGet();
				httpGet.setURI(uri);
				HttpResponse httpResponce = LoginActivity.client.execute(
						httpGet, LoginActivity.localContext);
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(httpResponce.getEntity()
								.getContent()));
				StringBuffer sb = new StringBuffer("");
				sb.append(bReader.readLine());
				return new JSONArray(sb.toString());
			} catch (IOException | URISyntaxException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(JSONArray jsonArray) {
			try {
				Log.d("JSON Array", jsonArray.toString(1));
				for (int i = 0; !jsonArray.isNull(i); i++) {
					CheckBox nextFollowerCheckBox = new CheckBox(
							getBaseContext());
					nextFollowerCheckBox.setText(jsonArray.getJSONObject(i)
							.getString("username"));
					nextFollowerCheckBox.setTextColor(Color.BLACK);
					nextFollowerCheckBox
							.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									if (isChecked) {
										allFollowersCheckBox.setChecked(false);
									}

								}

							});
					followersContainer.addView(nextFollowerCheckBox);

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.snip_recipients, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_snip_recipients,
					container, false);
			return rootView;
		}
	}

}
