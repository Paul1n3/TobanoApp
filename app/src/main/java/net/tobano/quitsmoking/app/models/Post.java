package net.tobano.quitsmoking.app.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();
    public String authorPhotoURL;
    public Boolean displayedAuthorPhotoURL;
    public Object authorQuittingDate;
    public int reportCount = 0;
    public Map<String, Boolean> reports = new HashMap<>();
    public int commentCount = 0;
    public Map<String, Boolean> comments = new HashMap<>();
    public Object dateCreated;
    public Long idForum;


    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(Post referencePost) {
        this.uid = referencePost.uid;
        this.author = referencePost.author;
        this.title = referencePost.title;
        this.body = referencePost.body;
        this.authorPhotoURL = referencePost.authorPhotoURL;
        this.displayedAuthorPhotoURL = referencePost.displayedAuthorPhotoURL;
        this.dateCreated = referencePost.dateCreated;
        this.idForum = referencePost.idForum;
        this.starCount = referencePost.starCount;
        this.stars = referencePost.stars;
        this.reportCount = referencePost.reportCount;
        this.reports = referencePost.reports;
        this.commentCount = referencePost.commentCount;
        this.comments = referencePost.comments;
        this.authorQuittingDate = referencePost.authorQuittingDate;
    }

    public Post(String uid, String author, String title, String body, String authorPhotoURL, Boolean displayedAuthorPhotoURL, Long idForum, long authorQuittingDate) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.authorPhotoURL = authorPhotoURL;
        this.displayedAuthorPhotoURL = displayedAuthorPhotoURL;
        this.dateCreated = ServerValue.TIMESTAMP;
        this.idForum = idForum;
        this.authorQuittingDate = authorQuittingDate;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("authorPhotoURL", authorPhotoURL);
        result.put("displayedAuthorPhotoURL", displayedAuthorPhotoURL);
        result.put("reportCount", reportCount);
        result.put("reports", reports);
        result.put("commentCount", commentCount);
        result.put("comments", comments);
        result.put("dateCreated", dateCreated);
        result.put("idForum", idForum);
        result.put("authorQuittingDate", authorQuittingDate);

        return result;
    }
    // [END post_to_map]


    @Override
    public String toString() {
        return "Post{" +
                "\"uid\":\"" + uid + '\"' +
                ",\"author\":\"" + author + '\"' +
                ",\"title\":\"" + title + '\"' +
                ",\"body\":\"" + body + '\"' +
                ",\"starCount\":" + starCount +
                ",\"stars\":" + stars +
                ",\"authorPhotoURL\":\"" + authorPhotoURL + '\"' +
                ",\"displayedAuthorPhotoURL\":" + displayedAuthorPhotoURL +
                ",\"reportCount\":" + reportCount +
                ",\"reports\":" + reports +
                ",\"commentCount\":" + commentCount +
                ",\"comments\":" + comments +
                ",\"dateCreated\":\"" + dateCreated + '\"' +
                ",\"idForum\":" + idForum +
                ",\"authorQuittingDate\":\"" + authorQuittingDate + '\"' +
                '}';
    }

}
// [END post_class]
