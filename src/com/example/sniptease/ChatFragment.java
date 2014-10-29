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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ChatFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class ChatFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

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
	 * @return A new instance of fragment ChatFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ChatFragment newInstance(String param1, String param2) {
		ChatFragment fragment = new ChatFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public ChatFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}

	}

	private LinearLayout chatListLayout;

	@Override
	public void onStart() {
		super.onStart();
		chatListLayout = (LinearLayout) this.getView().findViewById(
				R.id.chat_list_layout);
		LoadChatListTask loadChatList = new LoadChatListTask();
		loadChatList
				.execute("jsonrequests.php?action=generated_chat_list&user="
						+ LoginActivity.getUsername());
	}

	public class LoadChatListTask extends AsyncTask<String, Void, String> {
		List<Bitmap> chatsThumbs = new ArrayList<Bitmap>();
		ArrayList<String> nextFriend;
		@Override //Clear Layout first
		protected void onPreExecute(){
			chatListLayout.removeAllViews();
		}
		@Override
		protected String doInBackground(String... params) {
			try {
				String link = "http://23.253.210.151/" + params[0];
				URI website = new URI(link);
				BufferedReader in = null;
				Log.d("url", website.toString());
				HttpGet request = new HttpGet();
				request.setURI(website);
				URL newurl;
				Bitmap mIcon_val;
				HttpResponse response;

				response = LoginActivity.client.execute(request,
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
				Log.d("Echo from server", sb.toString());

				JSONArray arrayforthumbs = (JSONArray) new JSONArray(
						sb.toString());
				for (int i = 0; !arrayforthumbs.isNull(i); i++) {
					String nextFriendName = arrayforthumbs.getJSONObject(i)
							.getString("username");

					newurl = new URL(
							"http://23.253.210.151/uploads/profile_pictures/thumbs/"
									+ nextFriendName + ".jpg");
					mIcon_val = BitmapFactory.decodeStream(newurl
							.openConnection().getInputStream());

					chatsThumbs.add(mIcon_val);

				}

				return sb.toString();
			} catch (IOException | JSONException | URISyntaxException e) {
				e.printStackTrace();
				Log.d("error", e.toString());
				return e.toString();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("Echo from server", result);
			GridLayout container;

			try {
				JSONArray friendsArray = (JSONArray) new JSONArray(result);
				Log.d("Size of JSON array",
						Integer.toString(friendsArray.length()));
				nextFriend = new ArrayList<String>();
				for (int i = 0; friendsArray.getJSONObject(i) != null; i++) {
					container = new GridLayout(getActivity());

					nextFriend.add(friendsArray.getJSONObject(i).getString(
							"username"));
					String lastMessage = friendsArray.getJSONObject(i)
							.getString("last_message");

					TextView nextFriendTextView = new TextView(getActivity());
					

					ImageView nextFriendThumb = new ImageView(getActivity());
					nextFriendThumb.setImageBitmap(Bitmap.createScaledBitmap(
							chatsThumbs.get(i), 100, 100, false));

					nextFriendTextView.setText(nextFriend.get(i)+"\n"+lastMessage);
					
					nextFriendTextView.setTag(nextFriend);

					container.addView(nextFriendThumb, 0);
					container.addView(nextFriendTextView, 1);
					
					container.setTag(nextFriend.get(i));
					Log.d("the " + i + "st friend is ", nextFriend.get(i));
					final int index = i;

					container.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(),
									MessengerActivity.class);
							intent.putExtra("userfrom", nextFriend.get(index));
							startActivity(intent);
						}

					});

					chatListLayout.addView(container);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_chat, container, false);
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
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

}
