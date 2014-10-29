/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ProfileFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class ProfileFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	static TextView usernameView;
	static TextView locationView;
	static TextView aboutView;
	static TextView orientationView;
	
	static ImageView profileImage;
	Bitmap coverBitmap;
	public static Drawable getProfileImage(){
		return profileImage.getDrawable();
	}
	public static String getLocation(){
		return locationView.getText().toString();
	}
	public static int getOrientation(){
		if(orientationView.getText().toString().equalsIgnoreCase("men")){
			return 1;
		}
		else if(orientationView.getText().toString().equalsIgnoreCase("women")){
			return 2;
		}
		else if(orientationView.getText().toString().equalsIgnoreCase("both")){
			return 3;
		}
		else{
			return 0;
		}
	}
	public static String getAboutInfo(){
		return aboutView.getText().toString();
	}

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment ProfileFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ProfileFragment newInstance(String param1, String param2) {
		ProfileFragment fragment = new ProfileFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public ProfileFragment() {
		// Required empty public constructor
	}
	private static final int SELECT_PICTURE = 1;
	public static final int RESULT_OK           = -1;
	private String selectedImagePath;
	// ADDED
	private String filemanagerstring;
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
					InputStream inputStream = getActivity().getContentResolver()
							.openInputStream(selectedImageUri);
					draw = Drawable.createFromStream(inputStream,
							selectedImageUri.toString());
				} catch (FileNotFoundException e) {
					draw = getResources().getDrawable(R.drawable.ic_launcher);
					e.printStackTrace();
				}
				
				//coverBitmap.
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
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
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
	public void coverImageOnClick(View v){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}



	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}
	@Override
	public void onStart(){
		super.onStart();
		locationView = (TextView) getActivity().findViewById(
				R.id.profile_location_view);
		orientationView = (TextView) getActivity().findViewById(
				R.id.profile_orientation_view);
		
		usernameView = (TextView) getActivity().findViewById(
				R.id.profile_username_view);
		aboutView = (TextView) getActivity().findViewById(
				R.id.profile_about_view);
		usernameView.setText(LoginActivity.getUsername());
		
		profileImage = (ImageView)getActivity().findViewById(R.id.profile_image);
		
		
		
		getProfileInfo getInfo = new getProfileInfo();
		getInfo.execute("jsonrequests.php?action=return_profile_data&user="
				+ LoginActivity.getUsername());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

	public static class getProfileInfo extends AsyncTask<String, Void, String> {
		
		protected void loadProfilePic(){
			try{
				String link = LoginActivity.getURL()+"uploads/profile_pictures/thumbs/"+LoginActivity.getUsername()+".jpg";
				URI website = new URI(link);
				HttpGet request = new HttpGet();
				request.setURI(website);
				
				profileImage.setImageBitmap(BitmapFactory.decodeStream(new URL(link).openConnection().getInputStream()));
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				// in.close();
				Log.d("Exception Caught", "");
				e.printStackTrace();
				
			}

		}
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String link;
			BufferedReader in = null;
			String data = null;
			link = LoginActivity.getURL() + params[0];
			Log.d("Profile get Info Url", link);
	
			try {
				loadProfilePic();
				URI website = new URI(link);
				HttpGet request = new HttpGet();
				request.setURI(website);

				HttpResponse response = LoginActivity.client.execute(request,
						LoginActivity.localContext);
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				// StringBuffer thisLine = new StringBuffer("");
				String line = "";
				String newLine = System.getProperty("line.separator");
				int i = 0;
				while ((line = in.readLine()) != null) {
					sb.append(line + newLine);

				}
				in.close();
				data = sb.toString();
				// Log.d("Data Value in 'doInBackground' ", data);

				return data;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// in.close();
				Log.d("Exception Caught", "");
				e.printStackTrace();
				return e.getMessage();
			}

		}

		@Override
		protected void onPostExecute(String Result) {
			// Log.d("Profile Frag post execute get server data result",
			// Result);
			// Parse data into Profile elements
			Log.d("Result String", Result);

			try {
				JSONObject obj = (JSONObject) new JSONObject(Result);
				Log.d("JSON STRING", obj.getString("location"));

				locationView.setText(obj.getString("location"));
				orientationView.setText(obj.getString("interested_in"));
				aboutView.setText(obj.getString("about_us"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 

		}
	}
}
