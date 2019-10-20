package net.tobano.quitsmoking.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignInActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

	private Context mContext;
	private FirebaseAuth mAuth;
	private FirebaseUser mCurrentUser;
	private GoogleApiClient mGoogleApiClient;
	public static final int RC_SIGN_IN = 9001;
	private static final String TAG = "SignInActivity";
	private DatabaseReference mDatabase;

	private SharedPreferences prefs;

	public ProgressDialog mProgressDialog;

	private RelativeLayout accountChoiceContainer;
	private LinearLayout authWithGoogle;
	private LinearLayout authWithCustomAccount;

	private LinearLayout customAccountContainer;
	private boolean onCustomAccountCustomer = false;
	private EditText mPseudoField;
	private TextInputLayout fieldPseudoContainer;
	private EditText mEmailField;
	private EditText mPasswordField;
	private Button signIn;
	private TextView signError;
	private TextView TCAndPrivacyPolicy;
	private TextView forgottenPwd;
	private Button signUp;
	private Button createAccount;
	private boolean onSignUp = false;
	private FirebaseAnalytics mFirebaseAnalytics;

	public final static String SIGN_IN_QUITTING_DATE = "SIGN_IN_QUITTING_DATE";
	public final static String SIGN_IN_NB_CIG_PER_DAY = "SIGN_IN_NB_CIG_PER_DAY";
	public final static String SIGN_IN_PRICE_OF_PACK = "SIGN_IN_PRICE_OF_PACK";
	public final static String SIGN_IN_NB_OF_CIG_PER_PACK = "SIGN_IN_NB_OF_CIG_PER_PACK";
	public final static String SIGN_IN_CURRENCY = "SIGN_IN_CURRENCY";
	private long quittingDate;
	private int numberOfCigarettePerDay;
	private double priceOfPack;
	private int numberOfCigarettePerPack;
	private String currency;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		mContext = this;
		Log.d(TAG, "OnCreate");

		Intent i = getIntent();
		quittingDate = i.getLongExtra(SIGN_IN_QUITTING_DATE, new Date().getTime());
		numberOfCigarettePerDay = i.getIntExtra(SIGN_IN_NB_CIG_PER_DAY,0);
		priceOfPack = i.getDoubleExtra(SIGN_IN_PRICE_OF_PACK, 0.0);
		numberOfCigarettePerPack = i.getIntExtra(SIGN_IN_NB_OF_CIG_PER_PACK, 0);
		currency = i.getStringExtra(SIGN_IN_CURRENCY);

		initFirebaseConnection();
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		initInterface();
	}

	@Override
	protected void onStop() {
		super.onStop();
		//if currently logged in as anonymous, delete that account to clean DB and then GSign in
		if(mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
			Log.d("ANONYMOUS", "delete");
			mAuth.getCurrentUser().delete();
		}
	}

	private void initInterface() {
		accountChoiceContainer = findViewById(R.id.signin);
		accountChoiceContainer.setVisibility(View.VISIBLE);
		customAccountContainer = findViewById(R.id.register);
		customAccountContainer.setVisibility(View.GONE);
		authWithGoogle = findViewById(R.id.auth_google);
		authWithGoogle.setOnClickListener(this);
		authWithCustomAccount = findViewById(R.id.auth_tobano);
		authWithCustomAccount.setOnClickListener(this);
		mPseudoField = findViewById(R.id.field_pseudo);
		fieldPseudoContainer = findViewById(R.id.field_pseudo_container);
		mEmailField = findViewById(R.id.field_email);
		mPasswordField = findViewById(R.id.field_password);
		TCAndPrivacyPolicy = findViewById(R.id.terms_and_privacy_policy);
		TCAndPrivacyPolicy.setOnClickListener(this);
		signIn = findViewById(R.id.button_sign_in);
		signIn.setOnClickListener(this);
		signError = findViewById(R.id.sign_error);
		forgottenPwd = findViewById(R.id.forgotten_password);
		forgottenPwd.setOnClickListener(this);
		signUp = findViewById(R.id.button_sign_up);
		signUp.setOnClickListener(this);
		createAccount = findViewById(R.id.button_create_account);
		createAccount.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.auth_google) {
			signInWithGoogle();
		}
		else if (i == R.id.auth_tobano) {
			// Log Firebase event for post
			Bundle bundle = new Bundle();
			bundle.putBoolean("email_auth_click", true);
			mFirebaseAnalytics.logEvent("email_auth_click", bundle);

			AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
			alertDialog.setTitle(getString(R.string.info));
			alertDialog.setMessage(getString(R.string.email_auth_coming_soon));
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.btnOK),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alertDialog.show();

			//TODO: uncomment once email/password auth ready
			//onCustomAccountCustomer = true;
			//accountChoiceContainer.setVisibility(View.GONE);
			//customAccountContainer.setVisibility(View.VISIBLE);
		}
		else if (i == R.id.button_sign_in) {
			signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
		}
		else if (i == R.id.button_sign_up) {
			signError.setVisibility(View.GONE);
			fieldPseudoContainer.setVisibility(View.VISIBLE);
			signIn.setVisibility(View.GONE);
			signUp.setVisibility(View.GONE);
			createAccount.setVisibility(View.VISIBLE);
			onSignUp = true;
		}
		else if (i == R.id.button_create_account) {
			createAccount(mPseudoField.getText().toString(), mEmailField.getText().toString(), mPasswordField.getText().toString());
		}
		else if (i == R.id.forgotten_password){
			sentResetPasswordEmail();
		} else if (i == R.id.terms_and_privacy_policy) {
			// Log Firebase event for post
			Bundle bundle = new Bundle();
			bundle.putBoolean("pp_tc_click", true);
			mFirebaseAnalytics.logEvent("pp_tc_click", bundle);
			Intent browserIntent;
			if (Locale.getDefault().getLanguage().toLowerCase().equals("fr")) {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/tobano-app/français"));
			} else {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/tobano-app"));
			}
			startActivity(browserIntent);
		}
	}

	private void sentResetPasswordEmail() {
		if (!validateEmailForm()) {
			Toast.makeText(getApplicationContext(), "Please enter email address to reset password",
					Toast.LENGTH_SHORT).show();
			return;
		}
		FirebaseAuth.getInstance().sendPasswordResetEmail(mEmailField.getText().toString())
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "Email sent.");
							Toast.makeText(getApplicationContext(), "Email sent.",
									Toast.LENGTH_SHORT).show();
						}
						else{
							Toast.makeText(getApplicationContext(), "Error. Email couldn't be sent. Please check again later",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (onSignUp) {
				fieldPseudoContainer.setVisibility(View.GONE);
				signIn.setVisibility(View.VISIBLE);
				signUp.setVisibility(View.VISIBLE);
				createAccount.setVisibility(View.GONE);
				onSignUp = false;
				return true;
			}
			else if (onCustomAccountCustomer) {
				accountChoiceContainer.setVisibility(View.VISIBLE);
				customAccountContainer.setVisibility(View.GONE);
				onCustomAccountCustomer = false;
				return true;
			}
			else {
				Intent intent = new Intent();
				setResult(1, intent);
				finish();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void initFirebaseConnection() {
		Log.d(TAG, "initfirebase connection.");
		initialize();
		if(mGoogleApiClient == null){
			// [START config_signin]
			// Configure Google Sign In
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();
			// [END config_signin]
			mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
					.enableAutoManage(this, 2, this)
					.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
					.build();

			// [START initialize_auth]
			mAuth = FirebaseAuth.getInstance();
			// [END initialize_auth]
		}
	}

	public void initialize() {
		Log.w(TAG, "Initialize.");
		mAuth = FirebaseAuth.getInstance();
		mCurrentUser = mAuth.getCurrentUser();
		// only logged in users can log out (not anonymous accounts)
		if(mCurrentUser != null && !mCurrentUser.isAnonymous()){
			User newUser = new User(mCurrentUser.getDisplayName(), mCurrentUser.getEmail(), false, false, mCurrentUser.getPhotoUrl().toString(), true,
					quittingDate, numberOfCigarettePerDay, priceOfPack, numberOfCigarettePerPack, currency);
			createUserIfFirstLogin(newUser);
		}
		else{
			updateUI(null);
		}
	}

	private void signInWithGoogle() {
		//if currently logged in as anonymous, delete that account to clean DB and then GSign in
		if(mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
			mAuth.getCurrentUser().delete();
		}
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.w("SignInActivity", "onConnectionFailed");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			Log.d(TAG, "firebaseAuthWithGoogle: status of result = " + result.getStatus());
			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			} else {
				// Google Sign In failed, update UI appropriately
				// [START_EXCLUDE]
				Log.d(TAG, "firebaseAuthWithGoogle: Google Sign In failed" + result.getStatus());
				updateUI(null);
				// [END_EXCLUDE]
			}
		}
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
		// [START_EXCLUDE silent]
		showProgressDialog();
		// [END_EXCLUDE]

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d(TAG, "signInWithCredential:success");
							FirebaseUser user = mAuth.getCurrentUser();
							Log.d(TAG, "signInWithCredential:user.getEmail() = " + user.getEmail());
							User newUser = new User(user.getDisplayName(), user.getEmail(), false, false, user.getPhotoUrl().toString(), true,
									quittingDate, numberOfCigarettePerDay, priceOfPack, numberOfCigarettePerPack, currency);
							hideProgressDialog();
							createUserIfFirstLogin(newUser);

						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "signInWithCredential:failure", task.getException());
							Toast.makeText(getApplicationContext(), "Authentication failed.",
									Toast.LENGTH_SHORT).show();
							updateUI(null);
							hideProgressDialog();
						}

						// [START_EXCLUDE]
						// [END_EXCLUDE]
					}
				});
	}

	private void createUserIfFirstLogin(final User newUser) {
		// check if user already exist in Firebase db. If not, create it
		final FirebaseUser fUser = mAuth.getCurrentUser();
		final String userId = fUser.getUid();
		Log.e("url", "URL =======>" + fUser.getPhotoUrl());
		mDatabase = FirebaseDatabase.getInstance().getReference();
		mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
				new ValueEventListener() {
					public Boolean alertReady = false;

					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						// Get user value
						User user = dataSnapshot.getValue(User.class);

						if (user == null) {
							// ask user to define username
							AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
							final EditText edittext = new EditText(mContext);
							edittext.setText(newUser.username);
							alert.setCancelable(false);
							//alert.setMessage("Choose your username");
							alert.setTitle(getString(R.string.choose_username_title));

							alert.setView(edittext);

							alert.setPositiveButton(getString(R.string.btnOK), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									//overriden by below code to prevent alert dialog to close on button click
								}
							});

							final AlertDialog dialog = alert.create();

							this.alertReady = false;
							dialog.setOnShowListener(new DialogInterface.OnShowListener() {
								@Override
								public void onShow(DialogInterface dialogOnShow) {
									if (alertReady == false) {
										Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
										button.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												final String userNameValue = edittext.getText().toString();
												// Validation
												if (userNameValue.length() <= 20) {
													String checkUsername = userNameValue.replaceAll("[a-zA-Z0-9]", "").replaceAll(" ", "");
													if (checkUsername.length() == 0) {
														// replace username with the one input by user
														newUser.username = userNameValue;

														// Create user in Firebase database
														Map<String, Object> childUpdates = new HashMap<>();
														childUpdates.put("/usernames/" + userNameValue, userId);
														childUpdates.put("/users/" + userId, newUser);
														mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
															@Override
															public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
																if (databaseError == null) {
																	Log.d(TAG, "createUser : onComplete: success");
																	// TO AVOID A BUG IN THE FIRST READING AFTER THE CONNECTION
//																	// set display name as pseudo in FirebaseUser object
//																	UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//																			.setDisplayName(userNameValue).build();
//																	mCurrentUser.updateProfile(profileUpdates);

																	dialog.dismiss();

																	//finish activity
																	updateUI(mAuth.getCurrentUser());
																} else {
																	Log.w(TAG, "createUser : onComplete: fail : " + databaseError.getMessage(), databaseError.toException());
																	// username exists, asking user to input a different one
																	Toast.makeText(SignInActivity.this, "An account with this username already exist",
																			Toast.LENGTH_SHORT).show();
																}

															}
														});
													} else {
														Toast.makeText(getApplicationContext(),
																getString(R.string.username_rules_chars),
																Toast.LENGTH_SHORT).show();
													}
												} else {
													Toast.makeText(getApplicationContext(),
															getString(R.string.username_rules_length),
															Toast.LENGTH_SHORT).show();
												}
											}
										});
										alertReady = true;
									}
								}
							});

							dialog.show();
						} else {
							// user already exist, so just finish the activity
							updateUI(mAuth.getCurrentUser());
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {
						Log.w(TAG, "getUser:onCancelled", databaseError.toException());
					}
				});
	}

	private void updateUI(FirebaseUser user) {
		hideProgressDialog();
		if (user != null) {
			finish();
		}
	}

	public void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getString(R.string.loading));
			mProgressDialog.setIndeterminate(true);
		}

		mProgressDialog.show();
	}

	public void hideProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	private void createAccount(final String username, String email, String password) {
		Log.d(TAG, "createAccount:" + email);
		if (!validateSignUpForm()) {
			return;
		}

		showProgressDialog();

		AuthCredential credential = EmailAuthProvider.getCredential(email, password);
		mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					mDatabase = FirebaseDatabase.getInstance().getReference();
					mDatabase.child("usernames").child(username).runTransaction(new Transaction.Handler() {
						@Override
						public Transaction.Result doTransaction(MutableData mutableData) {
							if (mutableData.getValue() == null) {
								mutableData.setValue(mAuth.getUid());
								return Transaction.success(mutableData);
							}

							return Transaction.abort();
						}

						@Override
						public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
							if (committed) {
								// username saved
								// Sign in success, update UI with the signed-in user's information
								Log.d(TAG, "createUserWithEmail:success");
								FirebaseUser user = mAuth.getCurrentUser();

								// set display name as pseudo in FirebaseUser object
								UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
										.setDisplayName(username).build();
								user.updateProfile(profileUpdates);

								// Create user in Firebase database
								Log.w(TAG, "createAccount:create new user = " + user.toString());
								User newUser = new User(username, user.getEmail(), false, false, null, true,
										quittingDate, numberOfCigarettePerDay, priceOfPack, numberOfCigarettePerPack, currency);
								createUserIfFirstLogin(newUser);

								// send confirmation email
								sendVerificationEmail();
							}
							else {
								// username exists
								Toast.makeText(SignInActivity.this, "An account with this username already exist",
										Toast.LENGTH_SHORT).show();

								//delete newly created Firebase User
								mAuth.signOut();
								mAuth.getCurrentUser().delete();

								//recreate new anonymous user
								mAuth.signInAnonymously()
										.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
											@Override
											public void onComplete(@NonNull Task<AuthResult> task) {
												if (task.isSuccessful()) {
													// Sign in success, update UI with the signed-in user's information
													Log.d(TAG, "signInAnonymously:success");
													FirebaseUser user = mAuth.getCurrentUser();
													Toast.makeText(getApplicationContext(), "Logged in anonymously",
															Toast.LENGTH_SHORT).show();
												} else {
													// If sign in fails, display a message to the user.
													Log.w(TAG, "signInAnonymously:failure", task.getException());
													Toast.makeText(getApplicationContext(), "Anonymous Authentication failed.",
															Toast.LENGTH_SHORT).show();
												}
											}
										});
							}
						}
					});
				} else {
					// If sign in fails, display a message to the user.
					Log.w(TAG, "linkWithCredential:failure", task.getException());
					Toast.makeText(SignInActivity.this, "Authentication failed.",
							Toast.LENGTH_SHORT).show();
					updateUI(null);
				}
				hideProgressDialog();
			}
		});
	}

	private void sendVerificationEmail() {
		final FirebaseUser user = mAuth.getCurrentUser();
		user.sendEmailVerification()
				.addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							// email sent
							Toast.makeText(SignInActivity.this,
									"Verification email sent to " + user.getEmail(),
									Toast.LENGTH_SHORT).show();
							// after email is sent just logout the user and finish this activity
							FirebaseAuth.getInstance().signOut();
						}
						else
						{
							// email not sent, so display message and restart the activity or do whatever you wish to do
							Log.e(TAG, "sendEmailVerification", task.getException());
							Toast.makeText(SignInActivity.this,
									"Failed to send verification email.",
									Toast.LENGTH_SHORT).show();
							//restart this activity
							overridePendingTransition(0, 0);
							finish();
							overridePendingTransition(0, 0);
							startActivity(getIntent());
						}
						updateUI(null);
					}
				});
	}

	//form with email and password
	private boolean validateSignInForm() {
		boolean valid = true;
		if(!validateEmailForm())
			valid = false;
		if(!validatePasswordForm())
			valid = false;
		return valid;
	}

	//form with email, password and pseudo
	private boolean validateSignUpForm() {
		boolean valid = true;
		if(!validateEmailForm())
			valid = false;
		if(!validatePasswordForm())
			valid = false;
		if(!validatePseudoForm())
			valid = false;
		return valid;
	}

	private boolean validateEmailForm() {
		boolean valid = true;
		String email = mEmailField.getText().toString();
		if (TextUtils.isEmpty(email)) {
			mEmailField.setError(getString(R.string.required));
			valid = false;
		} else {
			mEmailField.setError(null);
		}
		return valid;
	}

	private boolean validatePasswordForm() {
		boolean valid = true;
		String password = mPasswordField.getText().toString();
		if (TextUtils.isEmpty(password)) {
			mPasswordField.setError(getString(R.string.required));
			valid = false;
		} else {
			mPasswordField.setError(null);
		}
		return valid;
	}

	private boolean validatePseudoForm() {
		boolean valid = true;
		String pseudo = mPseudoField.getText().toString();
		if (TextUtils.isEmpty(pseudo)) {
			mPseudoField.setError(getString(R.string.required));
			valid = false;
		} else {
			mPseudoField.setError(null);
		}
		return valid;
	}

	private void signIn(String email, String password) {
		Log.d(TAG, "signIn:" + email);
		if (!validateSignInForm()) {
			return;
		}

		showProgressDialog();

		// [START sign_in_with_email]
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d(TAG, "signInWithEmail:success");
							FirebaseUser user = mAuth.getCurrentUser();
							if (user.isEmailVerified()) {
								updateUI(user);
							}
							else {
								Log.w(TAG, "signInWithEmail:noVerifiedEmail");
								Toast.makeText(SignInActivity.this, "Email not verified.",
										Toast.LENGTH_SHORT).show();
								mAuth.signOut();
								updateUI(null);
							}
						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "signInWithEmail:failure", task.getException());
							Toast.makeText(SignInActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
							updateUI(null);
						}
						// [START_EXCLUDE]
						hideProgressDialog();
						// [END_EXCLUDE]
					}
				});
		// [END sign_in_with_email]
	}
}