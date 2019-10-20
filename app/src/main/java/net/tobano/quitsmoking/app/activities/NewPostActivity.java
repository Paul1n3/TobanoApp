package net.tobano.quitsmoking.app.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import net.tobano.quitsmoking.app.BuildConfig;
import net.tobano.quitsmoking.app.ForumArrayAdapter;
import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.Start;
import net.tobano.quitsmoking.app.models.Forum;
import net.tobano.quitsmoking.app.models.Language;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.models.User;
import net.tobano.quitsmoking.app.util.Theme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static net.tobano.quitsmoking.app.Start.context;
import static net.tobano.quitsmoking.app.Util.customErrorMessage;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";

    // [START declare_database_ref]
    public DatabaseReference mDatabase;
    // [END declare_database_ref]
    private Language mLanguage;
    private EditText mTitleField;
    private EditText mBodyField;
    private TextView mBodyCharCounter;
    private Spinner mForumSpinner;
    private TreeMap<String, Forum> mForums;
    private FloatingActionButton mSubmitButton;
    private long mTimeBetwPosts;
    private FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    private FirebaseAnalytics mFirebaseAnalytics;
    private User mUser;

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            mBodyCharCounter.setText(String.valueOf(s.length())+"/1500");
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);
        mBodyCharCounter = (TextView) findViewById(R.id.char_counter);
        mBodyField.addTextChangedListener(mTextEditorWatcher);

        initSpinner();

        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_post);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        manageTheme();

        initTimeBetweenPosts();
    }

    private void manageTheme() {
        if (theme == Theme.BLUE) {
            updateBtnShare(ContextCompat.getColorStateList(this, R.color.primary_dark));
        }
        else {
            updateBtnShare(ContextCompat.getColorStateList(this, R.color.kwit_dark));
        }
    }

    private void updateBtnShare(ColorStateList color) {
        try {
            mSubmitButton.setBackgroundTintList(color);
        }
        catch (Exception e){
            //
        }
    }

    private void initSpinner() {
        // get language id of user device
        mDatabase.child("language").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentLanguage = Locale.getDefault().getISO3Language();
                for (DataSnapshot languageSnapshot: dataSnapshot.getChildren()) {
                    if(currentLanguage.equalsIgnoreCase(languageSnapshot.child("nameAbbrev").getValue(String.class))) {
                        mLanguage = new Language(languageSnapshot.child("idLanguage").getValue(Long.class),
                                languageSnapshot.child("name").getValue(String.class), languageSnapshot.child("nameAbbrev").getValue(String.class));
                        break;
                    }
                }
                getForumForSpecificLanguage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getForumForSpecificLanguage() {
        mDatabase.child("forum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Is better to use a List, because you don't know the size
                // of the iterator returned by dataSnapshot.getChildren() to
                // initialize the array
                mForums = new TreeMap<>();

                //add default choice:
                Forum forum = new Forum(0L, 0L, getResources().getString(R.string.topic_post));
                mForums.put("0", forum);

                for (DataSnapshot forumSnapshot: dataSnapshot.getChildren()) {
                    Forum f = forumSnapshot.getValue(Forum.class);

                    if(f.idLanguage.equals(mLanguage.idLanguage))
                    {
                        mForums.put(forumSnapshot.getKey(), f);
                    }
                }

                mForumSpinner = (Spinner) findViewById(R.id.field_forum);
                mForumSpinner.setOnItemSelectedListener((new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ColorStateList colorStateList = ColorStateList.valueOf(Color.BLACK);
                        ViewCompat.setBackgroundTintList(mForumSpinner, colorStateList);
                        // If user change the default selection
                        // First item is disable and it is used for hint
                        if (position > 0) {
                            // Notify the selected item text
                            mBodyField.requestFocus();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // sometimes you need nothing here
                    }
                }));
                List<Forum> forumList = new ArrayList<>(mForums.values());
                ArrayAdapter<Forum> forumsAdapter = new ForumArrayAdapter(NewPostActivity.this, android.R.layout.simple_spinner_item, forumList);
                forumsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mForumSpinner.setAdapter(forumsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        final String nameForum = mForumSpinner.getSelectedItem().toString();
        Long idForum0 = null;
        for (String forumId : mForums.keySet()) {
            if (mForums.get(forumId).name.equals(nameForum)) {
                idForum0 = Long.valueOf(forumId);
            }
        }
        final Long idForum = idForum0;

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.required)));
            return;
        }

        if (title.length() < 6) {
            mTitleField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.title_too_short)));
            return;
        }

        if (title.length() > 60) {
            mTitleField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.title_too_long)));
            return;
        }

        // Forum is required
        if(mForumSpinner.getSelectedItemPosition() == 0){ // throw error if no forum selected
            ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);
            ViewCompat.setBackgroundTintList(mForumSpinner, colorStateList);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.required)));
            return;
        }

        if (body.length() < 20) {
            mBodyField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.body_too_short)));
            return;
        }

        if (body.length() > 1500) {
            mBodyField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.body_too_long)));
            return;
        }

        if (!hasInternetConnection()) {
            Toast.makeText(context, getString(R.string.check_your_internet_connection),
                    Toast.LENGTH_SHORT).show();
        }
        else {

            // Disable button so there are no multi-posts
            setEditingEnabled(false);
            Toast.makeText(this, getResources().getString(R.string.posting), Toast.LENGTH_SHORT).show();

            // [START single_value_read]
            final String userId = getUid();

            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            mUser = dataSnapshot.getValue(User.class);
                            // [START_EXCLUDE]
                            if (mUser == null) {
                                // User is null, error out
                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                Toast.makeText(NewPostActivity.this,
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // ensure user is not banned
                                mDatabase.child("users-banned/" + userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            // Write new post
                                            writeNewPost(mUser, userId, title, body, idForum);
                                        } else {
                                            Toast.makeText(NewPostActivity.this,
                                                    getString(R.string.error_banned_user),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            // [END_EXCLUDE]
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // [START_EXCLUDE]
                            setEditingEnabled(true);
                            // [END_EXCLUDE]
                        }
                    });
            // [END single_value_read]
        }
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewPost(User user, final String userId, String title, String body, Long idForum) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously

        final String key = mDatabase.child("posts").push().getKey();
        final Post post = new Post(userId, user.username, title, body, user.photoURL, user.displayedPhotoURL, idForum, Start.quittingDate.getTime());
        Map<String, Object> postValues = post.toMap();

        //check last post of user and block on client side
        if(user.getDateLastPost() + mTimeBetwPosts > Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis()){
            Toast.makeText(context, getString(R.string.wait_before_posting_again),
                    Toast.LENGTH_LONG).show();
            setEditingEnabled(true);
            return;
        }

        // update date lastpost of user
        user.dateLastPost = ServerValue.TIMESTAMP;
        final Map<String, Object> userValues = user.toMap();

        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
        childUpdates.put("/forum-posts/" + idForum + "/" + key, postValues);
        childUpdates.put("/language-posts/" + mLanguage.idLanguage + "/" + key, postValues);
        childUpdates.put("/users/" + userId, userValues);

        // request post creation with completion listener
        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "onComplete: success");

                    // Log Firebase event for post
                    Bundle bundle = new Bundle();
                    bundle.putString("post_id", key);
                    bundle.putString("user_id", userId);
                    mFirebaseAnalytics.logEvent("create_post", bundle);

                    // Finish this Activity, back to the stream
                    finish();
                } else {
                    Log.w(TAG, "onComplete: fail ", databaseError.toException());
                    Toast.makeText(NewPostActivity.this,
                            getString(R.string.wait_before_posting_again),
                            Toast.LENGTH_SHORT).show();
                    setEditingEnabled(true);
                }
            }
        });
    }
    // [END write_fan_out]

    private void initTimeBetweenPosts() {
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build());

        mTimeBetwPosts = 300000l;
        HashMap<String, Object> defaults =  new HashMap<>();
        defaults.put("time_min_between_posts", 300000); // by default 5 minutes betw posts
        remoteConfig.setDefaults(defaults);

        Task<Void> fetch = remoteConfig.fetch(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12));
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                // update time needed between comments
                mTimeBetwPosts = (Long) remoteConfig.getLong("time_min_between_posts");
            }
        });
    }

    protected boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}

