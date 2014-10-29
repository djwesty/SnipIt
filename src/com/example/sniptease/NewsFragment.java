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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link NewsFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link NewsFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class NewsFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	JSONArray snipsArray;
	LinearLayout snipContainer;
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
	 * @return A new instance of fragment NewsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static NewsFragment newInstance(String param1, String param2) {
		NewsFragment fragment = new NewsFragment();

		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public NewsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("News Fragment", "On Create");
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
		snipsArray = new JSONArray();
		

	}
	@Override
	public void onStart(){
		super.onStart();
		snipContainer = (LinearLayout) this.getView().findViewById(R.id.news_table);

		ReturnSnipsTask returnSnips = new ReturnSnipsTask();
		returnSnips.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_news, container, false);
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

	public void refreashSnips() {

	}

	private void updateLayout(){
		Log.d("Update Layout method","");
		

		boolean isIncoming = false;
		boolean isRead = false;
		boolean hasQuestion = false;
		boolean isCorrect = false;
		String withUser;
		String snipUrl;
		String question;
		String correctAnswer;
		SnipItemView nextSnipItemView;
		int snipId;
		
		snipContainer.removeAllViews();
		for(int i = 0; !snipsArray.isNull(i); i++){

			try{
				if(snipsArray.getJSONObject(i).getString("user_to_username").equalsIgnoreCase(snipsArray.getJSONObject(i).getString("username")) ){
					isIncoming = true;
				}
				else{
					isIncoming = false;
				}

				if(snipsArray.getJSONObject(i).getString("read").equalsIgnoreCase("YES")){
					isRead = true;
				}
				else{
					isRead = false;
				}
				if(snipsArray.getJSONObject(i).getString("question").length()>0){
					hasQuestion = true;
				}
				else{
					hasQuestion = false;
				}
				if(snipsArray.getJSONObject(i).getString("right").equalsIgnoreCase("RIGHT")){
					isCorrect = true;
				}
				else{
					isCorrect = false;
				}
				if(isIncoming){
					withUser = snipsArray.getJSONObject(i).getString("user_from_username");
				}
				else{
					withUser = snipsArray.getJSONObject(i).getString("user_to_username");
				}
				snipId = snipsArray.getJSONObject(i).getInt("snip_id");
				snipUrl = snipsArray.getJSONObject(i).getString("snip_url");
				question = snipsArray.getJSONObject(i).getString("question");
				correctAnswer = snipsArray.getJSONObject(i).getString("correct_answer");
				nextSnipItemView = new SnipItemView(getActivity(),isIncoming,isRead,hasQuestion,isCorrect,withUser,snipUrl,question,correctAnswer,snipId);
				
				snipContainer.addView(nextSnipItemView,0);
			}
			
			catch(JSONException e){
				e.printStackTrace();
			}
		
		}
		//snipContainer.bringChildToFront(getView().findViewById(R.id.news_feed_edit_button));
	}

	
	public class ReturnSnipsTask extends AsyncTask<Void, Void, JSONArray> {
		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				URI server = new URI(
						"http://23.253.210.151/jsonrequests.php?action=return_snips_new&username="
								+ LoginActivity.getUsername());
				HttpGet httpget = new HttpGet();
				httpget.setURI(server);
				HttpResponse response = LoginActivity.client.execute(httpget,
						LoginActivity.localContext);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String newLine = System.getProperty("line.separator");
				int i = 0;
				while ((line = in.readLine()) != null) {
					sb.append(line + newLine);
				}
				in.close();
				return new JSONArray(sb.toString());
			} catch (URISyntaxException | IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(JSONArray thisArray) {
			try {
				Log.d("JSON Array", thisArray.toString(1));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			snipsArray = thisArray;
			updateLayout();
		}
	}
}
