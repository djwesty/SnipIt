package com.example.sniptease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class QuestionViewerActivity extends Activity {
	RadioButton optionA, optionB, optionC, optionD;
	RadioGroup answersGroup;
	TextView theQuestion;
	String actualAnswer;
	public class LoadQuestionDetailsTask extends AsyncTask<Integer, Void, JSONObject>{

		@Override
		protected JSONObject doInBackground(Integer... params) {
			String link = "http://23.253.210.151/jsonrequests.php?action=get_snip_challenge&snipid="+params[0];
			Log.d("link", link);
			try{
			
			HttpGet httpget = new HttpGet(new URI(link));
		
			HttpResponse httpresponce = LoginActivity.client.execute(httpget, LoginActivity.localContext);
			StringBuilder stringbuilder = new StringBuilder();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					httpresponce.getEntity().getContent()));
			stringbuilder.append(in.readLine());
			in.close();
			Log.d("string builder",stringbuilder.toString());
			JSONObject jsonObject = new JSONObject(stringbuilder.toString());
			return jsonObject;
			}
			catch(IOException | URISyntaxException | JSONException e){
				e.printStackTrace();
				return null;
			}
			
		}
		@Override
		protected void onPostExecute(JSONObject jsonObject){
			try {
				Log.d("JsonObject", jsonObject.toString(1));
				optionA.setText(jsonObject.getString("answer1"));
				optionB.setText(jsonObject.getString("answer2"));
				optionC.setText(jsonObject.getString("answer3"));
				optionD.setText(jsonObject.getString("answer4"));
				theQuestion.setText(jsonObject.getString("question"));
				actualAnswer = jsonObject.getString("actual_answer");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void submitButtonOnClick(View v){
		int id = answersGroup.getCheckedRadioButtonId();
		if(id != -1){
			 RadioButton radioButton = (RadioButton) answersGroup.findViewById(id);
			 if(radioButton.getText().toString().equalsIgnoreCase(actualAnswer)){
				 Log.d("Correct", "You selected " +radioButton.getText().toString() +" actual answer: "+ actualAnswer);
				 
			 }
			 else{
				 Log.d("Incorrect", "You selected " +radioButton.getText().toString() +" actual answer: "+ actualAnswer);
			 }
		}
		else{
			Log.d("nothing selected", "null");
		}
			
	}
	public void hintButtonOnClick(View v){
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_viewer);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		LoadQuestionDetailsTask loadQuestionDetails = new LoadQuestionDetailsTask();
		loadQuestionDetails.execute(getIntent().getIntExtra("snip_id", -1));
		optionA = (RadioButton)findViewById(R.id.question_option_a);
		optionB = (RadioButton)findViewById(R.id.question_option_b);
		optionC = (RadioButton)findViewById(R.id.question_option_c);
		optionD = (RadioButton)findViewById(R.id.question_option_d);
		answersGroup = (RadioGroup)findViewById(R.id.question_radio_group);
		theQuestion = (TextView)findViewById(R.id.question_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question_viewer, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_question_viewer,
					container, false);
			return rootView;
		}
	}

}
