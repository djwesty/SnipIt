/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */

public class LoginActivity extends Activity {

	public static DefaultHttpClient client;
	public static CookieStore cookieStore;
	public static HttpContext localContext;
	
	public static HttpGet request = new HttpGet();
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "email@example.com";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for user Name and password at the time of the login attempt.
	private String mUserName;
	private String mPassword;

	public static String globalUserName;
	
	public static String getURL() {
		return "http://23.253.210.151/";
	}

	// UI references.
	private EditText mUserNameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	final LoginActivity login = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove activity
															// bar
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUserName = getIntent().getStringExtra(EXTRA_EMAIL);
		mUserNameView = (EditText) findViewById(R.id.userName);
		mUserNameView.setText(mUserName);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.log_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent registerIntent = new Intent(LoginActivity.this,
								RegisterNewUserActivity.class);
						
						LoginActivity.this.startActivityForResult(registerIntent, 2);
					}
				});

	}
	 @Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data)  
     {  
               super.onActivityResult(requestCode, resultCode, data);  
                   
                // check if the request code is same as what is passed  here it is 2  
                 if(requestCode==2)  
                       {  
                	 mUserNameView.setText(data.getStringExtra("USERNAME"));
                	 mPasswordView.setText(data.getStringExtra("PASSWORD"));
               
                       }  
   
   }  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	static String getUsername(){
		return globalUserName;
	}
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}
		// Reset errors.
		mUserNameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUserName = mUserNameView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		

		boolean cancel = false;
		View focusView = null;

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
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			globalUserName = mUserNameView.getText().toString();
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(login, mLoginStatusMessageView,
					mLoginStatusMessageView);
			
			mAuthTask.execute(mUserName, mPassword);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, String> {
		private TextView statusField, roleField;
		private Context context;
		 
		public UserLoginTask(Context context, TextView statusField,
				TextView roleField) {
			this.context = context;
			this.statusField = statusField;
			this.roleField = roleField;

		}

		protected String doInBackground(String... arg0) {

			try {
				Log.d("Async Task", "Do In Background (String)");
				String username = (String) arg0[0];
				globalUserName = username;
				Log.d("Username", username);
				String password = (String) arg0[1];
				Log.d("Password", password);
				String link = "http://23.253.210.151/jsonrequests.php?action=login&user="
						+ username + "&pass=" + password;
				
				
				cookieStore = new BasicCookieStore();
			    localContext = new BasicHttpContext();
				localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				HttpGet request = new HttpGet();
				LoginActivity.client = new DefaultHttpClient();
				Log.d("Connection manager",client.getConnectionManager().toString());
			
				request.setURI(new URI(link));

				Log.d("Http Get request URI", request.getURI().toString());
				HttpResponse response = LoginActivity.client.execute(request, localContext);
				HttpEntity entity = response.getEntity();
			       Header[] headers1 = response.getAllHeaders();
			        for (int i=0; i < headers1.length; i++) {
			            Header h = headers1[i];
			            Log.d("Header names: ",h.getName());
			            Log.d("Header Value: ",h.getValue());
			        }
			        Log.d("responce status line",response.getStatusLine().toString());
			        Log.d("http entity", entity.toString());
			        if (entity != null) {
			            Log.d("Response content length: " , String.valueOf(entity.getContentLength()));
			            
			        }
			        List<Cookie> cookies = cookieStore.getCookies();
			        for (int i = 0; i < cookies.size(); i++) {
			            Log.d("Local cookie: " , cookies.get(i).toString());
			        }
				Log.d("Http Responce", response.toString());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}
				Log.d("String Buffer after Loop", sb.toString());
				in.close();

				return sb.toString();
			} catch (Exception e) {
				Log.d("Exception", e.getMessage());
				return new String("Exception: " + e.getMessage());
			}

		}

		protected void onPostExecute(String result) {

			Log.d("Async Task", "On Post Execute result string:" + result);
			mAuthTask = null;
			showProgress(false);

			if (result.contains("Success")) {
				Intent intent = new Intent(context, MainMenuActivity.class);
				
				startActivity(intent);
				this.statusField.setText("Login Successful");
				this.roleField.setText(result);
			}

			else {

				Log.w("Bad Password", "Incorrect Password");
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
			//finish();

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

		protected void onPreExecute() {
			Log.d("Async Task", "On Pre Execute");
		}

		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Log.d("Async Task", "Do In Background (Boolean)");
			return null;
		}
	}

}
