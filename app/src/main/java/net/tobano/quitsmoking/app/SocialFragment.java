package net.tobano.quitsmoking.app;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.tobano.quitsmoking.app.activities.NewPostActivity;
import net.tobano.quitsmoking.app.activities.PostDetailActivity;
import net.tobano.quitsmoking.app.activities.SignInActivity;
import net.tobano.quitsmoking.app.models.Comment;
import net.tobano.quitsmoking.app.models.Language;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.util.OnGetDataListener;
import net.tobano.quitsmoking.app.util.Theme;
import net.tobano.quitsmoking.app.viewholder.PostViewHolder;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.tobano.quitsmoking.app.Start.RC_SIGN_IN;
import static net.tobano.quitsmoking.app.Start.TAG_FORUM_MAINTENANCE;
import static net.tobano.quitsmoking.app.Start.TAG_GOOGLE;
import static net.tobano.quitsmoking.app.Start.appVersionCode;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_CURRENCY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_CIG_PER_DAY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_OF_CIG_PER_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_PRICE_OF_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_QUITTING_DATE;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class SocialFragment extends Fragment implements
        View.OnClickListener, OnGetDataListener {

    private static final int ACTIVITY_SIGN_IN = 9004;
    private static final int PAGINATION_NUMBER = 5;
    protected Language mLanguage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseUser mCurrentUser;
    protected HashMap<String, Comment> postComments;

    private View v;
    private FloatingActionButton newPostBtn;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private RelativeLayout lError;
    private TextView tvError;
    private ImageView ivUnhappy;
    private ImageView ivAppIcon;
    private Button btnRetry;
    private Button btnDownload;
    private RelativeLayout layout;

    private FirebaseAnalytics mFirebaseAnalytics;

    private Intent intent;
    private boolean mFlagLike;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = inflater.inflate(R.layout.activity_google, container, false);

        layout = v.findViewById(R.id.secondary_layout);
        lError = v.findViewById(R.id.error_layout);
        tvError = v.findViewById(R.id.tvMessage);
        btnRetry = v.findViewById(R.id.retry_button);
        btnDownload = v.findViewById(R.id.download_button);
        ivUnhappy = v.findViewById(R.id.ivUnhappy);
        ivAppIcon = v.findViewById(R.id.ivTobanoIcon);

        btnDownload.setOnClickListener(this);
        ivAppIcon.setOnClickListener(this);
        btnRetry.setOnClickListener(this);

        allowSocialAccess();
        updateStartVariable();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        // Button launches NewPostActivity
        newPostBtn = v.findViewById(R.id.fab_new_post);
        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser != null && !mCurrentUser.isAnonymous()) {
                    if (allowSocialAccess()) {
                        Intent intent = new Intent(getActivity(), NewPostActivity.class);
                        startActivity(intent);
                    }
                } else {
                    if (openLoginActivityNeeded())
                        return;
                }
            }
        });

        manageTheme();

        return v;
    }

    private boolean allowSocialAccess() {
        boolean hasInternet = ((Start) getActivity()).hasInternetConnection();
        if (!hasInternet) {
            lError.setVisibility(VISIBLE);
            btnRetry.setVisibility(VISIBLE);
            btnDownload.setVisibility(GONE);
            ivUnhappy.setVisibility(VISIBLE);
            ivAppIcon.setVisibility(GONE);
            layout.setVisibility(GONE);
            tvError.setText(getString(R.string.check_internet_connection));
            return false;
        } else if (appVersionCode < ((Start) getActivity()).minVersion && ((Start) getActivity()).minVersion != TAG_FORUM_MAINTENANCE) {
            lError.setVisibility(VISIBLE);
            btnRetry.setVisibility(GONE);
            btnDownload.setVisibility(VISIBLE);
            ivUnhappy.setVisibility(GONE);
            ivAppIcon.setVisibility(VISIBLE);
            layout.setVisibility(GONE);
            tvError.setText(getString(R.string.update_app_for_forum));
            return false;
        } else if (((Start) getActivity()).minVersion == TAG_FORUM_MAINTENANCE) {
            lError.setVisibility(VISIBLE);
            btnRetry.setVisibility(GONE);
            ivUnhappy.setVisibility(VISIBLE);
            ivAppIcon.setVisibility(GONE);
            btnDownload.setVisibility(GONE);
            layout.setVisibility(GONE);
            tvError.setText(getString(R.string.forum_under_maintenance));
            return false;
        } else { // no issue to display the social page
            lError.setVisibility(GONE);
            layout.setVisibility(VISIBLE);
            ((Start) getActivity()).showProgressDialog();
            return true;
        }
    }

    private void updateStartVariable() {
        mDatabase = ((Start) getActivity()).mDatabase;
        mGoogleApiClient = ((Start) getActivity()).mGoogleApiClient;
        mAuth = ((Start) getActivity()).mAuth;
        mCurrentUser = ((Start) getActivity()).mCurrentUser;
        mLanguage = ((Start) getActivity()).mLanguage;
    }

    public Map<String, Object> suspendUser(String uid) {
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users-banned/" + uid, true);
        return childUpdates;
    }

    private void manageTheme() {
        if (Start.theme == Theme.BLUE) {
            updateBtnShare(ContextCompat.getColorStateList(getActivity(), R.color.primary_dark));
        } else {
            updateBtnShare(ContextCompat.getColorStateList(getActivity(), R.color.kwit_dark));
        }
    }

    private void updateBtnShare(ColorStateList color) {
        try {
            newPostBtn.setBackgroundTintList(color);
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // [START post_stars_transaction]
    private void onStarClicked(final DatabaseReference postRef) {
        // ensure user is not banned
        mDatabase.child("users-banned/" + mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // perform star operation
                    postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Post p = dataSnapshot.getValue(Post.class);

                            if (p == null) {
                                // just there is no post in the database
                            } else {
                                if (p.stars.containsKey(getUid())) {
                                    // Unstar the post and remove self from stars
                                    p.starCount = p.starCount - 1;
                                    p.stars.remove(getUid());
                                    mFlagLike = false;
                                } else {
                                    // Star the post and add self to stars
                                    p.starCount = p.starCount + 1;
                                    p.stars.put(getUid(), true);
                                    mFlagLike = true;
                                }

                                final String postKey = postRef.getKey();

                                Map<String, Object> requests = new HashMap<>();
                                requests.put("/posts/" + postKey, p);
                                requests.put("/user-posts/" + p.uid + "/" + postKey, p);
                                requests.put("/forum-posts/" + p.idForum.toString() + "/" + postKey, p);
                                requests.put("/language-posts/" + mLanguage.idLanguage.toString() + "/" + postKey, p);
                                mDatabase.updateChildren(requests, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Log.d(TAG_GOOGLE, "SocialFragment.star onComplete: success");
                                            // Log Firebase event for post
                                            Bundle bundle = new Bundle();
                                            bundle.putString("post_id", postKey);
                                            bundle.putString("user_id", getUid());
                                            if (mFlagLike)
                                                mFirebaseAnalytics.logEvent("like_post", bundle);
                                            else
                                                mFirebaseAnalytics.logEvent("unlike_post", bundle);
                                        } else {
                                            Log.w(TAG_GOOGLE, "SocialFragment.start onComplete: fail", databaseError.toException());
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //
                        }
                    });
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.error_banned_user),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    // [END post_stars_transaction]

    private void onPostReportClicked(final DatabaseReference postRef) {
        // ensure user is not banned
        mDatabase.child("users-banned/" + mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // check if post already reported by user
                    postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                            final Post p = dataSnapshot.getValue(Post.class);

                            if (p == null) {
                                // there is no existing post
                            } else {
                                if (p.reports.containsKey(mAuth.getCurrentUser().getUid())) {
                                    //pop-up confirmation
                                    builder.setCancelable(true);
                                    builder.setTitle(getString(R.string.thanks));
                                    builder.setMessage(getString(R.string.content_already_reported));
                                    builder.setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // post already reported, do nothing..
                                                }
                                            });
                                } else {
                                    builder.setTitle(getString(R.string.confirmation));
                                    builder.setMessage(getString(R.string.report_content_inappropriate));
                                    builder.setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    final String postKey = postRef.getKey();

                                                    // if confirmation given and post not already reported, report the post
                                                    Map<String, Object> requests = new HashMap<>();
                                                    // Report the post and add self to reports
                                                    p.reportCount = p.reportCount + 1;
                                                    p.reports.put(getUid(), true);
                                                    requests.put("/posts/" + postKey, p);
                                                    requests.put("/user-posts/" + p.uid + "/" + postKey, p);
                                                    requests.put("/forum-posts/" + p.idForum.toString() + "/" + postKey, p);
                                                    requests.put("/language-posts/" + mLanguage.idLanguage.toString() + "/" + postKey, p);
                                                    mDatabase.updateChildren(requests, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null) {
                                                                Log.d(TAG_GOOGLE, "SocialFragment.report onComplete: success");
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("post_id", postKey);
                                                                bundle.putString("user_id", getUid());
                                                                mFirebaseAnalytics.logEvent("report_post", bundle);
                                                                Toast.makeText(getActivity(), getString(R.string.post_reported),
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Log.w(TAG_GOOGLE, "SocialFragment.report onComplete: fail", databaseError.toException());
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
                    Toast.makeText(getActivity(),
                            getString(R.string.error_banned_user),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        if (((Start) getActivity()).hasInternetConnection())
            ((Start) getActivity()).startSocial(this);
    }
    // [END on_start_check_user]

    @Override
    public void onResume() {
        super.onResume();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }
        //updateUI(mCurrentUser);
        if (((Start) getActivity()).hasInternetConnection()) {
            ((Start) getActivity()).startSocial(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            ((Start) getActivity()).startSocial(this);
        }
        else if (requestCode == ACTIVITY_SIGN_IN) {
            switch (resultCode) {
                case 0:
                    if (((Start) getActivity()).hasInternetConnection())
                        ((Start) getActivity()).startSocial(this);
                    break;
                case 1:
                    // from SignInActivity on back pressed
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {
        RelativeLayout llSec = v.findViewById(R.id.secondary_layout);
        llSec.setVisibility(View.VISIBLE);

        mRecycler = v.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        // execute only on activity launch
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        retrievePosts();
    }

    private void retrievePosts() {
        if (mLanguage == null) {
            if (((Start)getActivity()).mUser == null) {
                ((Start)getActivity()).getUser(this);
            } else {
                ((Start)getActivity()).getUserLanguage(this);
            }
        } else {
            // get forum for your language (the language of the user's phone)
            queryForPosts();
        }
    }

    private void queryForPosts() {
        // get and display the post
        Query postsQuery = mDatabase.child("language-posts").child(mLanguage.idLanguage.toString());
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);
                // Set click listener for the whole post view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allowSocialAccess()) {
                            ((Start) getActivity()).hideProgressDialog();
                            // Launch PostDetailActivity
                            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra(PostDetailActivity.EXTRA_POST_LANGUAGE_ID, mLanguage.idLanguage.toString());
                            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postRef.getKey());
                            startActivity(intent);
                        }
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (FirebaseAuth.getInstance().getCurrentUser() != null && model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                // Set up post color indicator
                if (model.uid.equals(getUid())) { // urgency forum
                    viewHolder.llCategoryIndicator.setBackgroundColor(getResources().getColor(R.color.my_posts_indicator));
                } else { // progress
                    viewHolder.llCategoryIndicator.setBackgroundColor(getResources().getColor(R.color.others_post_indicator));
                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        if (openLoginActivityNeeded()) {
                            return;
                        }
                        if (allowSocialAccess()) {
                            // on star click
                            DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                            onStarClicked(globalPostRef);
                            ((Start) getActivity()).hideProgressDialog();
                        }
                    }
                }, new View.OnClickListener() {
                    // reportClickListener
                    @Override
                    public void onClick(View reportView) {
                        if (openLoginActivityNeeded())
                            return;

                        if (allowSocialAccess()) {
                            createListenerToBanUser();

                            // on report click
                            DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                            onPostReportClicked(globalPostRef);
                            ((Start) getActivity()).hideProgressDialog();
                        }
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
        ((Start) getActivity()).hideProgressDialog();
    }

    private void createListenerToBanUser() {
        // if post reported 4 times, move it to post-removed
        mDatabase.child("posts").orderByChild("reportCount").startAt(4.0).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post p = postSnapshot.getValue(Post.class);
                    removePostAndLinkedComments(postSnapshot.getKey(), p);
                    // and ban user (done in the previous function)
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void removePostAndLinkedComments(final String postId, final Post post) {
        mDatabase.child("post-comments").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Comment> comments = new HashMap<>();
                for (DataSnapshot c : dataSnapshot.getChildren()) {
                    comments.put(c.getKey(), c.getValue(Comment.class));
                }

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.putAll(writeNewPostRemoved(post, postId));
                childUpdates.putAll(removeReportedPost(post, postId));
                childUpdates.putAll(writeNewPostsCommentRemoved(postId, comments));
                childUpdates.putAll(removeComments(postId, comments));
                childUpdates.putAll(suspendUser(post.uid));
                mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Log.d(TAG_GOOGLE, "writeNewPostCommentsRemoved : onComplete: success");
                        } else {
                            Log.w(TAG_GOOGLE, "writeNewPostCommentsRemoved : onComplete: fail : " + databaseError.getMessage(), databaseError.toException());
                        }
                    }
                });

                postComments = comments;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG_GOOGLE, "writeNewPostCommentsRemoved : onCancelled: success");
            }
        });
    }

    private boolean openLoginActivityNeeded() {
        if (mCurrentUser == null || mCurrentUser.isAnonymous()) {
            intent = new Intent(getActivity(), SignInActivity.class);
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

    public String getUid() {
        return mCurrentUser.getUid();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    private Map<String, Object> writeNewPostRemoved(final Post post, final String key) {
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts-removed/" + key, postValues);
        return childUpdates;
    }

    private Map<String, Object> removeReportedPost(Post post, String key) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-posts/" + post.uid + "/" + key, null);
        childUpdates.put("/forum-posts/" + post.idForum.toString() + "/" + key, null);
        childUpdates.put("/posts/" + key, null);
        childUpdates.put("/language-posts/" + mLanguage.idLanguage.toString() + "/" + key, null);
        return childUpdates;
    }

    private Map<String, Object> writeNewPostsCommentRemoved(String postId, Map<String, Comment> postComments) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/post-comments-removed/" + postId, postComments);
        return childUpdates;
    }

    private Map<String, Object> removeComments(String postId, Map<String, Comment> postComments) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (String idComment : postComments.keySet()) {
            childUpdates.put("/post-comments/" + postId + "/" + idComment, null);
            childUpdates.put("/user-comments/" + postComments.get(idComment).uid + "/" + idComment, null);
        }
        return childUpdates;
    }

    @Override
    public void onFirebaseStart() {
        if (((Start) getActivity()).hasInternetConnection())
            ((Start) getActivity()).showProgressDialog();
    }

    @Override
    public void onSuccess(String type) {
        if (allowSocialAccess()) {
            switch (type) {
                case SIGN_IN:
                    updateStartVariable();
                    initView();
                    break;
                case TYPE_FORUM:
                    if (mLanguage == null) {
                        ((Start) getActivity()).getUserLanguage(this);
                    } else {
                        updateStartVariable();
                        initView();
                    }
                    break;
                case TYPE_USER_LANGUAGE:
                    updateStartVariable();
                    initView();
                    break;
                default:
                    break;
            }
        }
        ((Start) getActivity()).hideProgressDialog();
    }

    @Override
    public void onFailed(DatabaseError databaseError) {
        ((Start) getActivity()).hideProgressDialog();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.retry_button) {
            allowSocialAccess();
            if (((Start) getActivity()).hasInternetConnection()) {
                ((Start) getActivity()).startSocial(this);
            } else {
                ((Start) getActivity()).showProgressDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((Start) getActivity()).hideProgressDialog();
                    }
                }, 1000);
            }
            ((Start) getActivity()).hideProgressDialog();
        } else if (i == R.id.download_button || i == R.id.ivTobanoIcon) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.tobano.quitsmoking.app"));
            startActivity(intent);
        }
    }
}
