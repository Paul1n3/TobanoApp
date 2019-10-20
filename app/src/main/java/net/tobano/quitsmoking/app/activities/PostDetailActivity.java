package net.tobano.quitsmoking.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Picasso;

import net.tobano.quitsmoking.app.BuildConfig;
import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.Start;
import net.tobano.quitsmoking.app.Util;
import net.tobano.quitsmoking.app.models.Comment;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.models.User;
import net.tobano.quitsmoking.app.util.CircleTransform;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static net.tobano.quitsmoking.app.Application.getContext;
import static net.tobano.quitsmoking.app.Start.context;
import static net.tobano.quitsmoking.app.Util.customErrorMessage;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_CURRENCY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_CIG_PER_DAY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_OF_CIG_PER_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_PRICE_OF_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_QUITTING_DATE;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int ACTIVITY_SIGN_IN = 9003;
    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_POST_LANGUAGE_ID = "post_language_id";

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private Post mPost;
    private String mLanguageId;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mCommentCharCounter;
    private EditText mCommentField;
    //private Button mCommentButton;
    private ImageView mCommentSubmit;
    private RecyclerView mCommentsRecycler;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TextView mCategoryTextView;

    private ImageView mAuthorImage;
    private TextView mAuthorBadge;

    private long mTimeBetwComments;

    public ProgressDialog mProgressDialog;
    private DatabaseReference mDatabase;
    String mForumName;

    private String mCommentId;

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            mCommentCharCounter.setText(String.valueOf(s.length())+"/500");
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initTimeBetweenComments();

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        mLanguageId = getIntent().getStringExtra(EXTRA_POST_LANGUAGE_ID);
        if (mLanguageId == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_LANGUAGE_ID");
        }

        // Init Users
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // Initialize Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mPostReference = mDatabase.child("posts").child(mPostKey);
        mCommentsReference = mDatabase.child("post-comments").child(mPostKey);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_author);
        mTitleView = (TextView) findViewById(R.id.post_title);
        mCategoryTextView = (TextView) findViewById(R.id.categoryLabel);
        mBodyView = (TextView) findViewById(R.id.post_body);
        mCommentField = (EditText) findViewById(R.id.field_comment_text);
        mCommentCharCounter = (TextView) findViewById(R.id.char_counter);
        //mCommentButton = (Button) findViewById(R.id.button_post_comment);
        mCommentSubmit = (ImageView) findViewById(R.id.img_post_comment);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);
        mCommentsRecycler.setNestedScrollingEnabled(false);

        mAuthorImage = (ImageView) findViewById(R.id.post_author_photo);
        mAuthorBadge = (TextView) findViewById(R.id.social_level);

        //mCommentButton.setOnClickListener(this);
        mCommentSubmit.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentField.addTextChangedListener(mTextEditorWatcher);
    }

    private void initTimeBetweenComments() {
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build());

        mTimeBetwComments = 20000l;
        HashMap<String, Object> defaults =  new HashMap<>();
        defaults.put("time_min_between_comments", 20000); // by default 20 sec between posts
        remoteConfig.setDefaults(defaults);

        Task<Void> fetch = remoteConfig.fetch(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12));
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                // update time needed between comments
                mTimeBetwComments = (Long) remoteConfig.getLong("time_min_between_comments");
            }
        });
    }

    public void initPostCategory(final Long idForum){
        mDatabase.child("forum/" + idForum.toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Is better to use a List, because you don't know the size
                // of the iterator returned by dataSnapshot.getChildren() to
                // initialize the array
                for (DataSnapshot forumSnapshot: dataSnapshot.getChildren()) {
                    if(forumSnapshot.getKey().equals("name")){
                        mForumName = forumSnapshot.getValue(String.class);
                        mCategoryTextView.setText(getString(R.string.posted_in, mForumName));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Forum failed, log a message
                Log.w(TAG, "loadForumName:onCancelled", databaseError.toException());
//                Toast.makeText(PostDetailActivity.this, "Failed to load forum information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void init() {
        Log.d(TAG, "initfirebase connection.");
        // if user does not logged with google or password
        // create an anonymous user
        // because the anonymous user from Start will be deleted by Start.onStop
        if (mCurrentUser == null || mCurrentUser.isAnonymous()) {
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("PostDetailActivity", "isAnonymous : " + mCurrentUser.isAnonymous());
                        initFirebaseConnection();
                        initListeners();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("PostDetailActivity", "signInAnonymously:failure", task.getException());
                    }
                }
            });
        }
        else {
            initListeners();
        }
    }

    private void initListeners() {
        // Add value event listener to the post
        // [START post_value_event_listener]
        // listener on URI /posts/mPostKey
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                mPost = dataSnapshot.getValue(Post.class);
                // [START_EXCLUDE]
                mAuthorView.setText(mPost.author);
                mTitleView.setText(mPost.title);
                mBodyView.setText(mPost.body);

                if (mPost.displayedAuthorPhotoURL) {
                    Picasso.with(getContext()).load(mPost.authorPhotoURL).transform(new CircleTransform()).into(mAuthorImage);
                } else {
                    Picasso.with(getContext()).load(R.drawable.ic_action_account_circle_40).transform(new CircleTransform()).into(mAuthorImage);
                }

                Date now = new Date();
                int days = (int)( (now.getTime() - (long)mPost.authorQuittingDate) / (1000 * 60 * 60 * 24));
                if (days < 31) {
                    mAuthorBadge.setBackground(getContext().getResources().getDrawable(R.drawable.badge_square));
                    mAuthorBadge.setText(String.valueOf(days));
                }
                else if (days < 365) {
                    mAuthorBadge.setBackground(getContext().getResources().getDrawable(R.drawable.badge_circle));
                    mAuthorBadge.setText(String.valueOf((long)days/30));
                }
                else {
                    mAuthorBadge.setBackground(getContext().getResources().getDrawable(R.drawable.badge_star));
                    mAuthorBadge.setText(String.valueOf((long)days/365));
                }
                // id forum needed to retrieve forum name displayed
                initPostCategory(mPost.idForum);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//                Toast.makeText(PostDetailActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    private void initFirebaseConnection() {
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
        }
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("PostDetailActivity", "onConnectionFailed");
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();

        //if currently logged in as anonymous, delete that account to clean DB and then GSign in
        if(mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
            Log.d("ANONYMOUS", "delete");
            mAuth.getCurrentUser().delete();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.img_post_comment) {
            postComment();
        }
    }

    private void postComment() {
        if(mCurrentUser == null || mCurrentUser.isAnonymous()){
            if (openLoginActivityNeeded())
                return;
        }

        final String comment = mCommentField.getText().toString();

        // comment is required
        if (TextUtils.isEmpty(comment)) {
            mCommentField.setError(Util.customErrorMessage(getApplicationContext(), getResources().getString(R.string.required)));
            return;
        }

        if (comment.length() > 1000) {
            mCommentField.setError(customErrorMessage(getApplicationContext(), getResources().getString(R.string.comment_too_long)));
            return;
        }

        if (!hasInternetConnection()) {
            Toast.makeText(context, getString(R.string.check_your_internet_connection),
                    Toast.LENGTH_SHORT).show();
        }

        // ensure user is not banned
        mDatabase.child("users-banned/" + getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Write new post
                    sendCommentOnFirebase(getUid());
                }
                else{
                    Toast.makeText(PostDetailActivity.this,
                            getString(R.string.error_banned_user),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Comment failed, log a message
                Log.w(TAG, "getUserBanned from post comment :onCancelled", databaseError.toException());
            }
        });
    }

    private void sendCommentOnFirebase(final String uid) {
        mDatabase.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        final User user = dataSnapshot.getValue(User.class);

                        //check last comment date of user and block on client side
                        if(user.getDateLastComment()+mTimeBetwComments > Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis()){
                            Toast.makeText(context, getString(R.string.wait_before_posting_again),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // update date last comment of user
                        user.dateLastComment = ServerValue.TIMESTAMP;
                        final Map<String, Object> userValues = user.toMap();

                        // Push the comment, it will appear in the post-comments list
                        mCommentId = mCommentsReference.push().getKey();

                        // update Post
                        Post p = new Post(mPost);
                        p.commentCount = p.commentCount + 1;
                        p.comments.put(mCommentId, true);
                        String postId = mPostReference.getKey();

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        final Comment comment = new Comment(uid, user.username, commentText, user.photoURL, user.displayedPhotoURL, postId, Start.quittingDate.getTime());

                        // build all the request to send
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/posts/" + mPostReference.getKey(), p);
                        childUpdates.put("/user-posts/" + p.uid + "/" + mPostReference.getKey(), p);
                        childUpdates.put("/forum-posts/" + p.idForum.toString() + "/" + mPostReference.getKey(), p);
                        childUpdates.put("/language-posts/" + mLanguageId + "/" + mPostReference.getKey(), p);
                        // insert comment in the table post/user-comments
                        childUpdates.put("/post-comments/" + postId + "/" + mCommentId, comment);
                        childUpdates.put("/user-comments/" + uid + "/" + mCommentId, comment);
                        // update user dateLastComment
                        childUpdates.put("/users/" + uid, userValues);
                        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: success");
                                } else {
                                    Log.d(TAG,  "onComplete: fail : " + databaseError.toException());
                                }
                                hideProgressDialog();
                            }
                        });

                        // Log Firebase event for post
                        Bundle bundle = new Bundle();
                        bundle.putString("comment_id", mCommentId);
                        bundle.putString("post_id", mPostKey);
                        bundle.putString("user_id", getUid());
                        mFirebaseAnalytics.logEvent("create_comment", bundle);

                        // clean edittext once comment successfully posted
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if(databaseError != null){
                            Toast.makeText(context, getString(R.string.wait_before_posting_again),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public ImageView authorImageView;
        public TextView authorBadgeView;
        public TextView buttonOptions;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
            authorImageView = itemView.findViewById(R.id.comment_photo);
            authorBadgeView = itemView.findViewById(R.id.comment_badge);

            buttonOptions = itemView.findViewById(R.id.buttonOptions);

            TypedValue outValue = new TypedValue();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // If we're running on Lollipop or newer, then we can use the Theme's
                // selectableItemBackground to ensure that the View has a pressed state
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            }
            else{
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            }
            buttonOptions.setBackgroundResource(outValue.resourceId);
        }
    }

    private void onCommentReportClicked(final DatabaseReference commentRef) {
        if (mCurrentUser == null || mCurrentUser.isAnonymous()) {
            if (openLoginActivityNeeded())
                return;
        }

        // ensure user is not banned
        mDatabase.child("users-banned/" + mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                            final Comment c = dataSnapshot.getValue(Comment.class);

                            if (c == null) {
                                // there is no comment in the database
                            } else {
                                if (c.reports.containsKey(mAuth.getCurrentUser().getUid())) {
                                    //pop-up confirmation
                                    builder.setCancelable(true);
                                    builder.setTitle(getString(R.string.thanks));
                                    builder.setMessage(getString(R.string.content_already_reported));
                                    builder.setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // comment already reported, do nothing..
                                                }
                                            });
                                } else {
                                    builder.setTitle(getString(R.string.confirmation));
                                    builder.setMessage(getString(R.string.report_content_inappropriate));
                                    builder.setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // if confirmation given and post not already reported, report the post
                                                    final String commentKey = commentRef.getKey();
                                                    // if confirmation given and comment not already reported, report it
                                                    Map<String, Object> requests = new HashMap<>();
                                                    // Report the comment and add self to reports
                                                    c.reportCount = c.reportCount + 1;
                                                    c.reports.put(getUid(), true);
                                                    requests.put("/post-comments/" + c.postId + "/" + commentKey, c);
                                                    requests.put("/user-comments/" + c.uid + "/" + commentKey, c);
                                                    mDatabase.updateChildren(requests, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null) {
                                                                Log.d(TAG, "SocialFragment.report onComplete: success");
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("comment_id", commentKey);
                                                                bundle.putString("user_id", getUid());
                                                                mFirebaseAnalytics.logEvent("report_comment", bundle);
                                                                Toast.makeText(getBaseContext(), getString(R.string.comment_reported),
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Log.w(TAG, "SocialFragment.report onComplete: fail", databaseError.toException());
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                }

                                builder.show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_banned_user),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "getUserBanned:onCancelled", databaseError.toException());
            }
        });
    }

    private boolean openLoginActivityNeeded() {
        if (mCurrentUser == null || mCurrentUser.isAnonymous()) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra(SIGN_IN_QUITTING_DATE, Start.quittingDate.getTime());
            intent.putExtra(SIGN_IN_NB_CIG_PER_DAY, Start.cigarettesPerDay);
            intent.putExtra(SIGN_IN_PRICE_OF_PACK, Start.priceOfAPack);
            intent.putExtra(SIGN_IN_NB_OF_CIG_PER_PACK, Start.cigarettesPerPack);
            intent.putExtra(SIGN_IN_CURRENCY, Start.currency);
            startActivityForResult(intent, ACTIVITY_SIGN_IN);
            return true;
        }
        return false;
    }

    // when we come from an other activity (here it is from SignInActivity)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SIGN_IN) {
            showProgressDialog();
            switch (resultCode) {
                case 0:
                    getUser();
                    break;
                case 1:
                    // on back pressed
                    break;
                default:
                    break;
            }
            hideProgressDialog();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getUser() {
        mCurrentUser = mAuth.getCurrentUser();
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
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        catch (Exception e) {
            System.out.println("hideProgressDialog : isNull=" + (mProgressDialog != null) + " / isShowing=" + mProgressDialog.isShowing());
        }
    }

    private void createListenerToBanUser() {
        // if comment reported 4 times, move it to post-comments-removed
        mDatabase.child("post-comments").child(mPostKey).orderByChild("reportCount").startAt(4.0)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                            Comment c = commentSnapshot.getValue(Comment.class);
                            removeReportedPostComment(c, commentSnapshot.getKey(), mPostKey, c.uid);
                            // and ban user (done in the next function)
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void removeReportedPostComment(Comment comment, String commentKey, String postKey, String uid) {
        Map<String, Object> childUpdates = new HashMap<>();
        mPost.commentCount--;
        mPost.comments.remove(commentKey);
        childUpdates.put("/post-comments-removed/" + postKey + "/" + commentKey, comment);
        childUpdates.put("/posts/" + postKey + "/", mPost);
        childUpdates.put("/user-posts/" + uid + "/" + postKey + "/", mPost);
        childUpdates.put("/forum-posts/" + mPost.idForum + "/" + postKey + "/", mPost);
        childUpdates.put("/language-posts/" + mLanguageId + "/" + postKey + "/", mPost);
        childUpdates.put("/post-comments/" + postKey + "/" + commentKey, null);
        childUpdates.put("/user-comments/" + uid + "/" + commentKey, null);
        //temporary suspend user's account
        childUpdates.put("/users-banned/" + uid, true);
        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "removeReportedPostComment : onComplete: success");
                } else {
                    Log.w(TAG, "removeReportedPostComment : onComplete: fail : " + databaseError.getMessage(), databaseError.toException());
                }
            }
        });
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // only retrieve comments with less than 4 reports
                    if(comment.reportCount > 3) {
                        return;
                    }

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
//                    Toast.makeText(mContext, "Failed to load comments.", Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CommentViewHolder holder, int position) {
            final Comment comment = mComments.get(position);
            final String commentId = mCommentIds.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);

            if (comment.displayedAuthorPhotoURL) {
                Picasso.with(getContext()).load(comment.authorPhotoURL).transform(new CircleTransform()).into(holder.authorImageView);
            } else {
                Picasso.with(getContext()).load(R.drawable.ic_action_account_circle_40).transform(new CircleTransform()).into(holder.authorImageView);
            }

            Date now = new Date();
            int days = (int)( (now.getTime() - (long)comment.authorQuittingDate) / (1000 * 60 * 60 * 24));
            if (days < 31) {
                holder.authorBadgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_square));
                holder.authorBadgeView.setText(String.valueOf(days));
            }
            else if (days < 365) {
                holder.authorBadgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_circle));
                holder.authorBadgeView.setText(String.valueOf((long)days/30));
            }
            else {
                holder.authorBadgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_star));
                holder.authorBadgeView.setText(String.valueOf((long)days/365));
            }

            holder.buttonOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, holder.buttonOptions);
                    popup.inflate(R.menu.options_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.report:
                                    // Need to write to three places the post is stored
                                    DatabaseReference commentRef = mDatabase.child("post-comments")
                                                                            .child(mCommentsReference.getKey())
                                                                            .child(commentId);

                                    // On Comment reported
                                    createListenerToBanUser();
                                    onCommentReportClicked(commentRef);

                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
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
