package com.example.sniptease;

import java.io.BufferedReader;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class SnipViewerActivity extends Activity {
	String snipPath;
	ImageView snipImageView;
	VideoView snipVideoView;
	TextView snipQuestionView;
	TextView snipAnswerView;

	public class FlagTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				URI uri = new URI(
						"http://23.253.210.151/jsonrequests.php?action=flag&snip_id="
								+ getIntent().getExtras().getInt("snip_id")
								+ "&flag_option=" + params[0]);
				HttpPost httpPost = new HttpPost(uri);
				HttpResponse httpResponse = LoginActivity.client.execute(httpPost, LoginActivity.localContext);
				StringBuilder sb = new StringBuilder("");
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity().getContent()));
				sb.append(bReader.readLine());
				bReader.close();
				return sb.toString();
			} catch (  URISyntaxException |  IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String params) {
			if(!params.isEmpty()){
				Log.d("response", params);
				if(params.equalsIgnoreCase("null")){
					Toast.makeText(getBaseContext(), "Flag Sent", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void flagButtonOnClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Flag for what?");
		
		builder.setPositiveButton("Spam", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				flagTask.execute("spam");
				
			}
			
		});
		builder.setNegativeButton("Inappropriate",  new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				flagTask.execute("inappropriate");
				
			}
			
		});
		builder.setNeutralButton("Cancle", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
			
		});
		builder.create().show();
	}
	FlagTask flagTask;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snip_viewer);
		snipPath = "";
		snipImageView = (ImageView) findViewById(R.id.snip_viewer_image_holder);
		snipQuestionView = (TextView) findViewById(R.id.snip_viewer_question);
		snipAnswerView = (TextView) findViewById(R.id.snip_viewer_answer);
		snipVideoView = (VideoView) findViewById(R.id.snip_viewer_video_holder);
		flagTask = new FlagTask();
		int snipId = getIntent().getExtras().getInt("snip_id");
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		ViewSnipImageTask viewSnipImage = new ViewSnipImageTask();
		viewSnipImage.execute(snipId);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.snip_viewer, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_snip_viewer,
					container, false);
			return rootView;
		}
	}

	public class ViewSnipImageTask extends AsyncTask<Integer, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Integer... params) {
			Log.d("snip id", String.valueOf(params[0]));
			String link = "http://23.253.210.151/jsonrequests.php?action=view_snip_image&snipid="
					+ params[0];
			String line = "";
			StringBuilder sb = new StringBuilder("");
			Log.d("link", link);

			try {
				HttpGet httpget = new HttpGet();
				httpget.setURI(new URI(link));

				HttpResponse responce = LoginActivity.client.execute(httpget,
						LoginActivity.localContext);
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(responce.getEntity().getContent()));
				while ((line = bReader.readLine()) != null) {
					sb.append(line);
				}
				bReader.close();
				Log.d("result string", sb.toString());
				snipPath = sb.toString();
				if (sb.toString().contains(".jpg")) {

					return BitmapFactory.decodeStream(new URL(
							"http://23.253.210.151/uploads/" + sb.toString())
							.openConnection().getInputStream());

				}

			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bit) {

			if (bit != null) {

				snipImageView.setImageBitmap(bit);
				snipImageView.setVisibility(View.VISIBLE);
			} else {
				snipVideoView.setVideoPath("http://23.253.210.151/uploads/"
						+ snipPath);
				snipVideoView.setVisibility(View.VISIBLE);
				snipVideoView.start();
			}
			if (getIntent().hasExtra("question")
					|| getIntent().hasExtra("correctAnswer")) {
				snipQuestionView.setText(getIntent().getExtras().getString(
						"question"));
				snipAnswerView.setText(getIntent().getExtras().getString(
						"correctAnswer"));
				Log.d("question", getIntent().getExtras().getString("question"));
				snipQuestionView.setVisibility(View.VISIBLE);
				snipAnswerView.setVisibility(View.VISIBLE);
				snipQuestionView.bringToFront();
				snipAnswerView.bringToFront();

			}
		//	snipImageView.setTag(new String(getIntent().getStringExtra("snip_id")));
		}

	}

}
