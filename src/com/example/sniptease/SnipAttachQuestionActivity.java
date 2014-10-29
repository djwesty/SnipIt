package com.example.sniptease;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SnipAttachQuestionActivity extends Activity {

	EditText enterQuestion, correctAnswer, firstFalseAnswer, secondFalseAnswer,
			thirdFalseAnswer;
	Button giveaHint, myFavorites, sendSnip;
	CheckBox saveCheckBox;
	EditText hintText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snip_attach_question);
		enterQuestion = (EditText) findViewById(R.id.enterQuestionText);
		correctAnswer = (EditText) findViewById(R.id.correctAnswerText);
		firstFalseAnswer = (EditText) findViewById(R.id.firstFalseAnswerText);
		secondFalseAnswer = (EditText) findViewById(R.id.secondFalseAnswerText);
		thirdFalseAnswer = (EditText) findViewById(R.id.thirdFalseAnswerText);
		giveaHint = (Button) findViewById(R.id.giveAHintButton);
		myFavorites = (Button) findViewById(R.id.myFavoriteHintButton);
		sendSnip = (Button) findViewById(R.id.uploadSnipButton);
		saveCheckBox = (CheckBox)findViewById(R.id.saveQuestionCheckBox);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	public void sendSnipOnClick(View v) {
		if (enterQuestion.getText().toString().matches("")
				|| correctAnswer.getText().toString().matches("")
				|| firstFalseAnswer.getText().toString().matches("")) {
			// TODO alert "Oops! you need to fill in the question and at least 2 answers" "dismiss: okay"
		}
		else{
			Intent intent = new Intent(this, SnipRecipientsActivity.class);
			intent.putExtra("snipQuestion", enterQuestion.getText().toString());
			intent.putExtra("correctAnswer", correctAnswer.getText().toString());
			intent.putExtra("firstFalseAnswer", firstFalseAnswer.getText().toString());
			intent.putExtra("secondFalseAnswer", secondFalseAnswer.getText().toString());
			intent.putExtra("thirdFalseAnswer", thirdFalseAnswer.getText().toString());
			intent.putExtra("snipIsAPic", getIntent().getBooleanExtra("snipIsAPic", true));
			
			intent.putExtra("hint", hint);
			if(saveCheckBox.isChecked()){
				intent.putExtra("saveQuestion", "YES");
			}
			else{
				intent.putExtra("saveQuestion", "NO");
			}
			startActivity(intent);
		}
		
	}
	String hint ="";
	public void giveAHintOnClick(View v) {
		new AlertDialog.Builder(this)
	    .setTitle("Enter Hint")
	    .setMessage("Enter a hint for the followers you send this to!")
	    .setView(hintText)
	    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	           hint = hintText.getText().toString(); 
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}

	public void myFavoritesOnClick(View v) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.snip_attach_question, menu);
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
	public void onDestroy(){
	super.onDestroy();
	finish();
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
			View rootView = inflater.inflate(
					R.layout.fragment_snip_attach_question, container, false);
			return rootView;
		}
	}

}
