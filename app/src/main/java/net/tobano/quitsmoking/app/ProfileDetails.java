package net.tobano.quitsmoking.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import net.tobano.quitsmoking.app.activities.SignInActivity;
import net.tobano.quitsmoking.app.models.Comment;
import net.tobano.quitsmoking.app.models.Language;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.models.User;
import net.tobano.quitsmoking.app.util.CircleTransform;
import net.tobano.quitsmoking.app.util.OnGetDataListener;
import net.tobano.quitsmoking.app.util.Theme;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static net.tobano.quitsmoking.app.Start.TAG_FORUM_MAINTENANCE;
import static net.tobano.quitsmoking.app.Start.TAG_GOOGLE;
import static net.tobano.quitsmoking.app.Start.appVersionCode;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_CURRENCY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_CIG_PER_DAY;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_NB_OF_CIG_PER_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_PRICE_OF_PACK;
import static net.tobano.quitsmoking.app.activities.SignInActivity.SIGN_IN_QUITTING_DATE;

/**
 * This Activity contains all the quitting information as they have been set at
 * the beginning and the quitting date. It's the configuration page
 *
 * @author Nicolas Lett
 */

public class ProfileDetails extends Fragment implements OnGetDataListener, View.OnClickListener {

    private static final int ACTIVITY_SIGN_IN = 9002;
    public ProgressDialog mProgressDialog;
    protected Language mLanguage;
    private View v;
    private RelativeLayout lError;
    private TextView tvError;
    private RelativeLayout layout;
    private View mSignIn;
    private Button mSignOut;
    private View wallpaper;
    private RelativeLayout circle;
    public ImageView authorImageView;
    public TextView badge;
    private TextView username;
    private EditText usernameEditable;
    private ImageView editUsername;
    private ImageView ivUnhappy;
    private ImageView ivAppIcon;
    private Button btnRetry;
    private Button btnDownload;
    private boolean isChanged = false;
    private ImageButton btnInfoBadge;
    private LinearLayout infoBadge;
    private boolean infoBadgeAreDisplayed = true;
    private Switch switchImageUser;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private User user;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile_details, container, false);

        layout = v.findViewById(R.id.profile_layout);
        lError = v.findViewById(R.id.error_layout);
        tvError = v.findViewById(R.id.tvMessage);
        btnRetry = v.findViewById(R.id.retry_button);
        btnDownload = v.findViewById(R.id.download_button);
        ivUnhappy = v.findViewById(R.id.ivUnhappy);
        ivAppIcon = v.findViewById(R.id.ivTobanoIcon);

        checkSocialAccess();
        updateStartVariable();

        mSignOut = v.findViewById(R.id.btnSignOut);
        mSignOut.setOnClickListener(this);
        mSignIn = v.findViewById(R.id.sign_in_button);
        mSignIn.setOnClickListener(this);
        wallpaper = (View) v.findViewById(R.id.wallpaper);
        circle = (RelativeLayout) v.findViewById(R.id.circle);
        authorImageView = v.findViewById(R.id.post_author_photo);
        badge = (TextView) v.findViewById(R.id.social_level);
        username = (TextView) v.findViewById(R.id.tvUsername);
        usernameEditable = (EditText) v.findViewById(R.id.etUsername);
        usernameEditable.setVisibility(View.GONE);
        editUsername = (ImageView) v.findViewById(R.id.buttonUsername);
        editUsername.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        btnDownload.setOnClickListener(this);

        switchImageUser = (Switch) v.findViewById(R.id.switchImage);
        switchImageUser.setChecked(user != null ? user.getDisplayedPhotoURL() : true);
        switchImageUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                update(user, isChecked);
            }
        });
        infoBadge = (LinearLayout) v.findViewById(R.id.badge_info_text);
        infoBadge.setVisibility(VISIBLE);
        btnInfoBadge = (ImageButton) v.findViewById(R.id.badge_info_button);
        btnInfoBadge.setOnClickListener(this);

        setTheme();
        initialize();

        return v;
    }

    private boolean checkSocialAccess() {
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
        } else { // no issue to display the profile page
            lError.setVisibility(GONE);
            layout.setVisibility(VISIBLE);
            return true;
        }
    }

    private void updateStartVariable() {
        mDatabase = ((Start) getActivity()).mDatabase;
        mGoogleApiClient = ((Start) getActivity()).mGoogleApiClient;
        mAuth = ((Start) getActivity()).mAuth;
        mCurrentUser = ((Start) getActivity()).mCurrentUser;
        mLanguage = ((Start) getActivity()).mLanguage;
        user = ((Start) getActivity()).mUser;
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnSignOut) {
            signOut();
            updateUI(null);
        } else if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.buttonUsername) {
            if (user == null) {
                ((Start) getActivity()).startSocial(this);
            }
            editUsername();
        } else if (i == R.id.retry_button) {
            checkSocialAccess();
            if (!((Start) getActivity()).hasInternetConnection()) {
                showProgressDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                }, 1000);
            } else {
                ((Start) getActivity()).startSocial(this);
            }
        } else if (i == R.id.download_button || i == R.id.ivTobanoIcon) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.tobano.quitsmoking.app"));
            startActivity(intent);
        } else if (i == R.id.badge_info_button) {
            infoBadgeAreDisplayed = !infoBadgeAreDisplayed;
            if (infoBadgeAreDisplayed) {
                infoBadge.setVisibility(VISIBLE);
            } else {
                infoBadge.setVisibility(INVISIBLE);
            }
        }
    }

    private void editUsername() {
        if (checkSocialAccess()) {
            isChanged = !isChanged;
            if (isChanged) {
                if (user != null) {
                    username.setVisibility(View.GONE);
                    usernameEditable.setVisibility(View.VISIBLE);
                    editUsername.setImageDrawable(getResources().getDrawable(
                            R.drawable.pencil_changed));
                    usernameEditable.setText(user.getUsername());
                } else {
                    ((Start) getActivity()).getUser(this);
                }
            } else {
                String expectedUsername = usernameEditable.getText().toString();
                if (expectedUsername.length() <= 20) {
                    String checkUsername = expectedUsername.replaceAll("[a-zA-Z0-9]", "").replaceAll(" ", "");
                    if (checkUsername.length() == 0) {
                        update(user, expectedUsername);
                        usernameEditable.setVisibility(View.GONE);
                        username.setVisibility(View.VISIBLE);
                        editUsername.setImageDrawable(getResources().getDrawable(
                                R.drawable.pencil));
                    } else {
                        Toast.makeText(((Start) getActivity()),
                                getString(R.string.username_rules_chars),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(((Start) getActivity()),
                            getString(R.string.username_rules_length),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * update the username in user/username/related to post/related to comment tables
     * <p>
     * the request to update the username/author is sent
     * if success :)
     * if failed, we do the hypothesis that it is because the username already exists in the DB
     *
     * @param user        current user with the old username
     * @param newUsername new username
     */
    private void update(final User user, final String newUsername) {
        showProgressDialog();

        // get all post ids of the concerned user
        mDatabase.child("user-comments").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get all comments of the user
                        final HashMap<String, Comment> comments = new HashMap<>();
                        for (DataSnapshot comment : dataSnapshot.getChildren()) {
                            comments.put(comment.getKey(), comment.getValue(Comment.class));
                        }
                        // get all post ids of the concerned user
                        mDatabase.child("user-posts").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        HashMap<String, Post> posts = new HashMap<>();
                                        for (DataSnapshot post : dataSnapshot.getChildren()) {
                                            posts.put(post.getKey(), post.getValue(Post.class));
                                        }
                                        // build all the request to send
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put("/usernames/" + user.getUsername(), null);
                                        final User userUpdated = new User(user);
                                        userUpdated.username = newUsername;
                                        childUpdates.put("/users/" + mCurrentUser.getUid(), userUpdated);
                                        childUpdates.put("/usernames/" + newUsername, mCurrentUser.getUid());
                                        for (String postId : posts.keySet()) {
                                            Post p = posts.get(postId);
                                            Log.d(TAG_GOOGLE, "ProfileDetails userId: " + mCurrentUser.getUid());
                                            Log.d(TAG_GOOGLE, "ProfileDetails post: " + p.toString());
                                            p.author = newUsername;
                                            childUpdates.put("/posts/" + postId, p);
                                            childUpdates.put("/forum-posts/" + p.idForum + "/" + postId, p);
                                            childUpdates.put("/user-posts/" + mCurrentUser.getUid() + "/" + postId, p);
                                            childUpdates.put("/language-posts/" + ((Start) getActivity()).getLanguageByForum(p.idForum.toString()) + "/" + postId, p);
                                        }
                                        for (String commentId : comments.keySet()) {
                                            Comment comment = comments.get(commentId);
                                            comment.author = newUsername;
                                            childUpdates.put("/post-comments/" + comment.postId + "/" + commentId, comment);
                                            childUpdates.put("/user-comments/" + mCurrentUser.getUid() + "/" + commentId, comment);
                                        }
                                        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Log.d(TAG_GOOGLE, "ProfileDetails onComplete: success");
                                                    // set display name as pseudo in FirebaseUser object
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(newUsername).build();
                                                    mCurrentUser.updateProfile(profileUpdates);

                                                    updateUser(userUpdated);
                                                    updateUI(mCurrentUser);
                                                } else {
                                                    Log.w(TAG_GOOGLE, "ProfileDetails onComplete: fail", databaseError.toException());
                                                    Toast.makeText(((Start) getActivity()),
                                                            getString(R.string.username_already_used),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                hideProgressDialog();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG_GOOGLE, "getUserPosts:onCancelled", databaseError.toException());
                                        hideProgressDialog();
                                        checkSocialAccess();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG_GOOGLE, "getUserComments:onCancelled", databaseError.toException());
                        hideProgressDialog();
                        checkSocialAccess();
                    }
                }
        );
    }

    /**
     * update the variable to enabled the google image in user/related to post/related to comment tables
     * <p>
     * the request to update the username/author is sent
     *
     * @param user        current user with the old setting
     * @param displayedImage new setting
     */
    private void update(final User user, final Boolean displayedImage) {
        if (user == null) {
            ((Start) getActivity()).startSocial(this);
            switchImageUser.setChecked(!switchImageUser.isChecked());
        }

        showProgressDialog();

        // get all post ids of the concerned user
        mDatabase.child("user-comments").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get all comments of the user
                        final HashMap<String, Comment> comments = new HashMap<>();
                        for (DataSnapshot comment : dataSnapshot.getChildren()) {
                            comments.put(comment.getKey(), comment.getValue(Comment.class));
                        }
                        // get all post ids of the concerned user
                        mDatabase.child("user-posts").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        HashMap<String, Post> posts = new HashMap<>();
                                        for (DataSnapshot post : dataSnapshot.getChildren()) {
                                            posts.put(post.getKey(), post.getValue(Post.class));
                                        }
                                        // build all the request to send
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        final User userUpdated = new User(user);
                                        userUpdated.displayedPhotoURL = displayedImage;
                                        childUpdates.put("/users/" + mCurrentUser.getUid(), userUpdated);
                                        for (String postId : posts.keySet()) {
                                            Post p = posts.get(postId);
                                            p.displayedAuthorPhotoURL = displayedImage;
                                            childUpdates.put("/posts/" + postId, p);
                                            childUpdates.put("/forum-posts/" + p.idForum + "/" + postId, p);
                                            childUpdates.put("/user-posts/" + mCurrentUser.getUid() + "/" + postId, p);
                                            childUpdates.put("/language-posts/" + ((Start) getActivity()).getLanguageByForum(p.idForum.toString()) + "/" + postId, p);
                                        }
                                        for (String commentId : comments.keySet()) {
                                            Comment comment = comments.get(commentId);
                                            comment.displayedAuthorPhotoURL = displayedImage;
                                            childUpdates.put("/post-comments/" + comment.postId + "/" + commentId, comment);
                                            childUpdates.put("/user-comments/" + mCurrentUser.getUid() + "/" + commentId, comment);
                                        }
                                        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Log.d(TAG_GOOGLE, "ProfileDetails onComplete: success");
                                                    getUser();
                                                    updateUI(mCurrentUser);
                                                } else {
                                                    Log.w(TAG_GOOGLE, "ProfileDetails onComplete: fail", databaseError.toException());
                                                    Toast.makeText(((Start) getActivity()),
                                                            getString(R.string.username_already_used),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                hideProgressDialog();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG_GOOGLE, "getUserPosts:onCancelled", databaseError.toException());
                                        hideProgressDialog();
                                        checkSocialAccess();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG_GOOGLE, "getUserComments:onCancelled", databaseError.toException());
                        hideProgressDialog();
                        checkSocialAccess();
                    }
                }
        );
    }

    private void getUser() {
        // get the user from the db and update the view
        ((Start)getActivity()).getUser(this);
    }

    private void updateUser(User userUpdated) {
        this.user = userUpdated;
        this.username.setText(this.user.username);
        this.usernameEditable.setText(this.user.username);
    }

    private void signIn() {
        if (checkSocialAccess()) {
            Intent intent = new Intent((Start) getActivity(), SignInActivity.class);
            intent.putExtra(SIGN_IN_QUITTING_DATE, Start.quittingDate.getTime());
            intent.putExtra(SIGN_IN_NB_CIG_PER_DAY, Start.cigarettesPerDay);
            intent.putExtra(SIGN_IN_PRICE_OF_PACK, Start.priceOfAPack);
            intent.putExtra(SIGN_IN_NB_OF_CIG_PER_PACK, Start.cigarettesPerPack);
            intent.putExtra(SIGN_IN_CURRENCY, Start.currency);
            startActivityForResult(intent, ACTIVITY_SIGN_IN);
        }
    }

    // when we come from an other activity (here it is from SignInActivity)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SIGN_IN) {
            switch (resultCode) {
                case 0:
                    if (((Start) getActivity()).hasInternetConnection())
                        ((Start) getActivity()).startSocial(this);
                    break;
                case 1:
                    ((Start) getActivity()).onBackPressed();
                    // on back pressed
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initialize() {
        // only logged in users can log out (not anonymous accounts)
        if (user != null && mCurrentUser != null && !mCurrentUser.isAnonymous()) {
            displayUser();
            updateUI(mCurrentUser);
        } else {
            Log.d("ProfileDetails", "authenticated : false");
            updateUI(null);
            signIn();
        }
    }

    private void displayUser() {
        if (user != null) {
            username.setText(user.username);

            if (user.getDisplayedPhotoURL()) {
                Picasso.with(getContext()).load(user.photoURL).transform(new CircleTransform()).into(authorImageView);
            } else {
                Picasso.with(getContext()).load(R.drawable.ic_action_account_circle_40).transform(new CircleTransform()).into(authorImageView);
            }

            Date quittingDate = ((Start)getActivity()).quittingDate;
            Date now = new Date();
            int days = (int)( (now.getTime() - quittingDate.getTime()) / (1000 * 60 * 60 * 24));
            if (days < 31) {
                badge.setBackground(getResources().getDrawable(R.drawable.badge_square));
                badge.setText(String.valueOf(days));
            }
            else if (days < 365) {
                badge.setBackground(getResources().getDrawable(R.drawable.badge_circle));
                badge.setText(String.valueOf((long)days/30));
            }
            else {
                badge.setBackground(getResources().getDrawable(R.drawable.badge_star));
                badge.setText(String.valueOf((long)days/365));
            }
        }
        else {
            ((Start) getActivity()).getUser(this);
        }
    }

    private void setTheme() {
        mSignOut.setTextColor(Util.getColorText(getContext()));
        mSignOut.setBackground(Util.getButtonDrawable(getContext()));
        if (Start.theme == Theme.GREEN) {
            updateWallpaper();
            circle.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_green));
        } else {
            updateWallpaper();
            circle.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_blue));
        }
    }

    private void updateWallpaper() {
        Drawable drawableBig;
        if (Start.theme == Theme.GREEN) {
            drawableBig = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_kwit);
        } else {
            drawableBig = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_tabano);
        }
        try {
            wallpaper.setBackground(drawableBig);
        } catch (Exception e) {
            wallpaper.setBackgroundDrawable(drawableBig);
        }
    }

    private void signOut() {
        ((Start) getActivity()).signOut(this);
    }

    private void updateUI(FirebaseUser user) {
        if (v != null) {
            checkSocialAccess();
            if (user != null) {
                mSignOut.setVisibility(VISIBLE);
                mSignIn.setVisibility(GONE);
            } else {
                mSignIn.setVisibility(VISIBLE);
                mSignOut.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSocialAccess();
    }

    @Override
    public void onSuccess(String type) {
        switch (type) {
            case TYPE_USER:
                updateStartVariable();
                if (user != null)
                    displayUser();
                break;
            case SIGN_IN:
                updateStartVariable();
                break;
            case SIGN_OUT:
                ((Start) getActivity()).onBackPressed();
                break;
            default:
                break;
        }
        ((Start) getActivity()).hideProgressDialog();
        updateUI(mCurrentUser);
    }

    @Override
    public void onFailed(DatabaseError databaseError) {
        try {
            ((Start) getActivity()).hideProgressDialog();
            checkSocialAccess();
        } catch (Exception e) {
            // the fragment is already closed
        }
    }

    @Override
    public void onFirebaseStart() {
        if (((Start) getActivity()).hasInternetConnection())
            ((Start) getActivity()).showProgressDialog();
    }
}
