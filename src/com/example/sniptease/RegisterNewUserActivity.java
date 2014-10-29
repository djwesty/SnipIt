/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;
import com.google.android.gcm.GCMRegistrar;
import static com.example.sniptease.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.example.sniptease.CommonUtilities.EXTRA_MESSAGE;
import static com.example.sniptease.CommonUtilities.SENDER_ID;

public class RegisterNewUserActivity extends Activity {

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
					R.layout.fragment_register_new_user, container, false);
			return rootView;
		}
	}

	public class SetToken extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String serverString = "http://23.253.210.151/jsonrequests.php?action=send_regular_token_loggen&username="
					+ params[0]
					+ "&token="
					+ Secure.getString(getContentResolver(), Secure.ANDROID_ID);
			Log.d("android token",
					Secure.getString(getContentResolver(), Secure.ANDROID_ID));

			GCMRegistrar.checkDevice(getBaseContext());

			GCMRegistrar.checkManifest(getBaseContext());
			GCMRegistrar.register(getApplicationContext(), SENDER_ID);

			Log.d("GCM ID",
					GCMRegistrar.getRegistrationId(getApplicationContext()));
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(serverString);
			HttpResponse httpResponse;
			try {
				httpResponse = client.execute(httpPost);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}
				return sb.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		@Override
		protected void onPostExecute(String string) {
			Log.d("Token response", string);
		}
	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message depending upon your app
			 * requirement For now i am just displaying it on the screen
			 * */

			// Showing received message
			// lblMessage.append(newMessage + "\n");
			Toast.makeText(getApplicationContext(),
					"New Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	public class UserRegisterTask extends AsyncTask<String, Void, String> { // Task
																			// that
																			// communinicates
																			// with
																			// server.
		private Context context;
		private TextView statusField, roleField;

		public UserRegisterTask(Context context, TextView statusField,
				TextView roleField) {
			this.context = context;
			this.statusField = statusField;
			this.roleField = roleField;
		}

		@Override
		protected String doInBackground(String... arg0) {
			try {
				String userID = arg0[0];
				String password = arg0[1];
				String password2 = arg0[2];
				String eMail = arg0[3];
				String bDay = arg0[4];

				String link = "http://23.253.210.151/jsonrequests.php?action=register&user="
						+ userID
						+ "&pass="
						+ password
						+ "&pass2="
						+ password2
						+ "&email=" + eMail + "&birthday=" + bDay;

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));
				Log.d("Http Get request URI", request.getURI().toString());
				HttpResponse response = client.execute(request);
				Log.d("Http Responce", response.toString());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}
				Log.d("Responce from register", sb.toString());
				in.close();
				// return null;
				return sb.toString();
			} catch (Exception e) {
				Log.e("Exception", e.getMessage());
				return new String("Exception: " + e.getMessage());
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("Register On Post Execute", result);
			mAuthTask = null;
			showProgress(false);
			if (result.contains("Success")) {
				Intent intent = new Intent();
				intent.putExtra("USERNAME", mUserIDView.getText().toString());
				intent.putExtra("PASSWORD", mPasswordView.getText().toString());
				this.statusField.setText("Registration Successful");
				this.roleField.setText(result);
				setResult(2, intent);
				finish();// finishing activity
			}

			else {

				Log.w("Did not register", "Unable to register");
				// mPasswordView.setError(getString(R.string.error_incorrect_password));
				mUserIDView.requestFocus();
			}
		}

	}

	private UserRegisterTask mAuthTask = null;
	private String mBday;
	private DatePicker mBdayView;
	private String mEmail;

	private EditText mEmailView;
	private String mPassword;
	private String mPassword2;
	private EditText mPassword2View;
	private EditText mPasswordView;

	private TextView mRegisterStatusMessageView;
	private View mRegisterStatusView;

	private String mUserID;

	private EditText mUserIDView;

	final RegisterNewUserActivity register = this;

	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}
		// reset errors
		mUserIDView.setError(null);
		mPasswordView.setError(null);
		mPassword2View.setError(null);
		mEmailView.setError(null);
		// mBdayView.setError(null);

		// Store values at the time of the register attempt
		mUserID = mUserIDView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPassword2 = mPassword2View.getText().toString();
		mEmail = mEmailView.getText().toString();
		mBday = Integer.toString(mBdayView.getMonth()) + "/"
				+ Integer.toString(mBdayView.getDayOfMonth()) + "/"
				+ Integer.toString(mBdayView.getYear());

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mUserID)) {
			mUserIDView.setError(getString(R.string.error_field_required));
			focusView = mUserIDView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mUserIDView.setError(getString(R.string.error_invalid_email));
			focusView = mUserIDView;
			cancel = true;
		}
		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt register and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user register attempt.
			mRegisterStatusMessageView
					.setText(R.string.register_progress_registering);
			showProgress(true);
			mAuthTask = new UserRegisterTask(register,
					mRegisterStatusMessageView, mRegisterStatusMessageView);

			mAuthTask.execute(mUserID, mPassword, mPassword2, mEmail, mBday);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove activity
															// bar

		setContentView(R.layout.activity_register_new_user);

		if (savedInstanceState == null) {

		}
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		mUserIDView = (EditText) findViewById(R.id.new_Username);
		mPasswordView = (EditText) findViewById(R.id.new_Password);
		mPassword2View = (EditText) findViewById(R.id.new_Password2);
		mEmailView = (EditText) findViewById(R.id.new_Email);
		mBdayView = (DatePicker) findViewById(R.id.new_Birthdate);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);
		mRegisterStatusView = findViewById(R.id.register_status);
		final String regId = GCMRegistrar
				.getRegistrationId(this);
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// attemptRegister();
						// Get GCM registration id
						Log.d("is registered", String.valueOf(GCMRegistrar.isRegistered(getApplicationContext())));
						if (regId.equals("")) {
							// Registration is not present, register now with
							// GCM
							GCMRegistrar.register(getApplicationContext(), SENDER_ID);
						} else {
							Log.d("reg id", regId);
							// Device is already registered on GCM
							if (GCMRegistrar.isRegisteredOnServer(getApplicationContext())) {
								// Skips registration.
								Toast.makeText(getApplicationContext(),
										"Already registered with GCM",
										Toast.LENGTH_LONG).show();
							} else {
								// Try to register again, but not in the UI
								// thread.
								// It's also necessary to cancel the thread
								// onDestroy(),
								// hence the use of AsyncTask instead of a raw
								// thread.
								
								SetToken setToken = new SetToken();
								setToken.execute("android2", regId);

							}
						}
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_new_user, menu);
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
	 * Shows the progress UI and hides the register form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which
		// allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegisterStatusView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});
			/*
			 * mRegisterFormView.setVisibility(View.VISIBLE);
			 * mRegisterFormView.animate().setDuration(shortAnimTime)
			 * .alpha(show ? 0 : 1) .setListener(new AnimatorListenerAdapter() {
			 * 
			 * @Override public void onAnimationEnd(Animator animation) {
			 * mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			 * } });
			 */
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply
			// show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			// mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
