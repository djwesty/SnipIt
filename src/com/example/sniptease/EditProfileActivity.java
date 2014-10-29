/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileActivity extends Activity {

	ImageButton profileImage;
	TextView locationText;
	RadioGroup orientationGroup;
	RadioButton menButton;
	RadioButton womenButton;
	RadioButton bothButton;
	TextView aboutText;
	UpdateProfileInfo updateProfileInfoTask;
	// YOU CAN EDIT THIS TO WHATEVER YOU WANT
	private static final int SELECT_PICTURE = 1;

	private String selectedImagePath;
	// ADDED
	private String filemanagerstring;

	public class UpdateProfileInfo extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String link = "http://23.253.210.151/jsonrequests.php?action=edit_profile&user="
					+ LoginActivity.getUsername()
					+ "&location="
					+ params[0]
					+ "&interested_in=" + params[1] + "&about_us=" + params[2];

			try {
				URI website = new URI(link.replaceAll(" ", "%20"));
				HttpPost httppost = new HttpPost();
				httppost.setURI(website);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
						.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				profileImage.buildDrawingCache();
				if (profileImage.getDrawingCache() == null)
					Log.d("bitmap is null", "");
				
				Bitmap bitmapOrg = BitmapFactory.decodeFile(selectedImagePath);
				if(bitmapOrg != null){
					
				
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
				byte[] data = bao.toByteArray();
				entityBuilder.addPart("img", new ByteArrayBody(data, "img",
						"img"));
				httppost.setEntity(entityBuilder.build());
				}
				HttpResponse response = LoginActivity.client.execute(httppost,
						LoginActivity.localContext);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}
				if (sb.length() == 0)
					Log.d("null", "");
				Log.d("HTTP POst responce", sb.toString());
				in.close();

			} catch (URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getBaseContext(), "Save Sucessful",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				// OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();

				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);

				// DEBUG PURPOSE - you can delete this if you want
				if (selectedImagePath != null)
					System.out.println(selectedImagePath);
				else
					System.out.println("selectedImagePath is null");
				if (filemanagerstring != null)
					System.out.println(filemanagerstring);
				else
					System.out.println("filemanagerstring is null");

				// NOW WE HAVE OUR WANTED STRING
				Drawable draw;
				try {
					InputStream inputStream = getContentResolver()
							.openInputStream(selectedImageUri);
					draw = Drawable.createFromStream(inputStream,
							selectedImageUri.toString());
				} catch (FileNotFoundException e) {
					draw = getResources().getDrawable(R.drawable.ic_launcher);
					e.printStackTrace();
				}
				profileImage.setImageDrawable(draw);
				if (selectedImagePath != null)
					System.out
							.println("selectedImagePath is the right one for you!");
				else
					System.out
							.println("filemanagerstring is the right one for you!");
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public void onImageClick(View v) {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}

	private void bindViews() {
		profileImage = (ImageButton) findViewById(R.id.edit_profile_upload_pic);
		locationText = (TextView) findViewById(R.id.edit_profile_location);
		orientationGroup = (RadioGroup) findViewById(R.id.edit_profile_orientation_group);
		menButton = (RadioButton) findViewById(R.id.edit_profile_likes_men);
		womenButton = (RadioButton) findViewById(R.id.edit_profile_likes_women);
		bothButton = (RadioButton) findViewById(R.id.edit_profile_likes_both);
		aboutText = (TextView) findViewById(R.id.edit_profile_about_me);
		profileImage.setImageDrawable(ProfileFragment.getProfileImage());
		locationText.setText(ProfileFragment.getLocation());
		if (ProfileFragment.getOrientation() == 1) {
			menButton.toggle();
		} else if (ProfileFragment.getOrientation() == 2) {
			womenButton.toggle();
		} else {
			bothButton.toggle();
		}
		aboutText.setText(ProfileFragment.getAboutInfo());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		bindViews();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_profile, menu);
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
		} else if (id == R.id.edit_profile_save) {
			updateProfileInfoTask = new UpdateProfileInfo();
			String[] parameters = new String[3];
			parameters[0] = locationText.getText().toString();
			if (orientationGroup.getCheckedRadioButtonId() == menButton.getId()) {
				parameters[1] = "Men";
			} else if (orientationGroup.getCheckedRadioButtonId() == womenButton
					.getId()) {
				parameters[1] = "Women";
			} else {
				parameters[1] = "Both";
			}
			parameters[2] = aboutText.getText().toString();
			updateProfileInfoTask.execute(parameters);

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
			View rootView = inflater.inflate(R.layout.fragment_edit_profile,
					container, false);
			return rootView;
		}
	}

}
