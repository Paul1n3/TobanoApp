package net.tobano.quitsmoking.app.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START comment_class]
@IgnoreExtraProperties
public class Comment {

    public String uid;
    public String postId;
    public String author;
    public String text;
    public String authorPhotoURL;
    public Boolean displayedAuthorPhotoURL;
    public Object authorQuittingDate;
    public Object dateCreated;
    public int reportCount = 0;
    public Map<String, Boolean> reports = new HashMap<>();

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text, String authorPhotoURL, Boolean displayedAuthorPhotoURL, String postId, long authorQuittingDate) {
        this.uid = uid;
        this.postId = postId;
        this.author = author;
        this.text = text;
        this.authorPhotoURL = authorPhotoURL;
        this.displayedAuthorPhotoURL = displayedAuthorPhotoURL;
        this.dateCreated = ServerValue.TIMESTAMP;
        this.authorQuittingDate = authorQuittingDate;
    }

    // [START comment_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("postId", postId);
        result.put("author", author);
        result.put("text", text);
        result.put("authorPhotoURL", authorPhotoURL);
        result.put("displayedAuthorPhotoURL", displayedAuthorPhotoURL);
        result.put("reportCount", reportCount);
        result.put("reports", reports);
        result.put("dateCreated", dateCreated);
        result.put("authorQuittingDate", authorQuittingDate);

        return result;
    }
    // [END comment_to_map]

    @Override
    public String toString() {
        return "Comment{" +
                "\"uid:'" + uid + "\"" +
                "\", postId:'" + postId + "\"" +
                "\", author:'" + author + "\"" +
                "\", text:'" + text + "\"" +
                "\", authorPhotoURL:'" + authorPhotoURL + "\"" +
                "\", displayedAuthorPhotoURL\":" + displayedAuthorPhotoURL +
                "\", dateCreated:" + dateCreated +
                "\", reportCount:" + reportCount +
                "\", reports:" + reports +
                "\", authorQuittingDate:'" + authorQuittingDate + "\"" +
                '}';
    }
}
// [END comment_class]
