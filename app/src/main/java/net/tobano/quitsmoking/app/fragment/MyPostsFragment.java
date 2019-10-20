package net.tobano.quitsmoking.app.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import net.tobano.quitsmoking.app.SocialFragment;

public class MyPostsFragment extends SocialFragment {

    public MyPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("user-posts")
                .child(getUid());
    }
}
