package net.tobano.quitsmoking.app.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import net.tobano.quitsmoking.app.SocialFragment;

public class RecentPostsFragment extends SocialFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts")
                .limitToFirst(100);
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
