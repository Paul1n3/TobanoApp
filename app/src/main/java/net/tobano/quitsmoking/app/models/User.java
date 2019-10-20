package net.tobano.quitsmoking.app.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public Boolean emailVerified;
    public Boolean isBlocked;
    public Object dateCreated;
    public String photoURL;
    public Boolean displayedPhotoURL;
    public Object dateLastComment;
    public Object dateLastPost;
    public Object dateQuitting;
    public int numberOfCigarettePerDay;
    public double priceOfPack;
    public int numberOfCigarettePerPack;
    public String currency;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(User referenceUser) {
        this.username = referenceUser.username;
        this.email = referenceUser.email;
        this.emailVerified = referenceUser.emailVerified;
        this.isBlocked = referenceUser.isBlocked;
        this.dateCreated = referenceUser.dateCreated;
        this.photoURL = referenceUser.photoURL;
        this.displayedPhotoURL = referenceUser.displayedPhotoURL;
        this.dateLastComment = referenceUser.dateLastComment;
        this.dateLastPost = referenceUser.dateLastPost;
        this.dateQuitting = referenceUser.dateQuitting;
        this.numberOfCigarettePerDay = referenceUser.numberOfCigarettePerDay;
        this.priceOfPack = referenceUser.priceOfPack;
        this.numberOfCigarettePerPack = referenceUser.numberOfCigarettePerPack;
        this.currency = referenceUser.currency;
    }

    public User(String username, String email, Boolean emailVerified, Boolean isBlocked, String photoURL, Boolean displayedPhotoURL,
                long quittingDate, int numberOfCigarettePerDay, double priceOfPack, int numberOfCigarettePerPack, String currency) {
        this.username = username;
        this.email = email;
        this.dateCreated = ServerValue.TIMESTAMP;
        this.emailVerified = emailVerified;
        this.isBlocked = isBlocked;
        this.photoURL = photoURL;
        this.displayedPhotoURL = displayedPhotoURL;
        this.dateLastComment = 1l;
        this.dateLastPost = 1l;
        this.dateQuitting = quittingDate;
        this.numberOfCigarettePerDay = numberOfCigarettePerDay;
        this.numberOfCigarettePerPack = numberOfCigarettePerPack;
        this.priceOfPack = priceOfPack;
        this.currency = currency;
    }

    public Long getDateLastComment() {
        if (dateLastComment instanceof Long) {
            return (Long) dateLastComment;
        }
        else {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
    }

    public Long getDateLastPost() {
        if (dateLastPost instanceof Long) {
            return (Long) dateLastPost;
        }
        else {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setDateQuitting(Object dateQuitting) {
        this.dateQuitting = dateQuitting;
    }

    public void setNumberOfCigarettePerDay(int numberOfCigarettePerDay) {
        this.numberOfCigarettePerDay = numberOfCigarettePerDay;
    }

    public void setPriceOfPack(double priceOfPack) {
        this.priceOfPack = priceOfPack;
    }

    public void setNumberOfCigarettePerPack(int numberOfCigarettePerPack) {
        this.numberOfCigarettePerPack = numberOfCigarettePerPack;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getDisplayedPhotoURL() {
        return displayedPhotoURL;
    }

    // [START user_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("emailVerified", emailVerified);
        result.put("isBlocked", isBlocked);
        result.put("dateCreated", dateCreated);
        result.put("photoURL", photoURL);
        result.put("displayedPhotoURL", displayedPhotoURL);
        result.put("dateLastComment", dateLastComment);
        result.put("dateLastPost", dateLastPost);
        result.put("dateQuitting",dateQuitting);
        result.put("numberOfCigarettePerDay",numberOfCigarettePerDay);
        result.put("numberOfCigarettePerPack",numberOfCigarettePerPack);
        result.put("priceOfPack",priceOfPack);
        result.put("currency",currency);

        return result;
    }
    // [END user_to_map

    @Override
    public String toString() {
        return "User{" +
                "\"username\":\"" + username + '\"' +
                ", \"email\":\"" + email + '\"' +
                ", \"emailVerified\":\"" + emailVerified + '\"' +
                ", \"isBlocked\":" + isBlocked +
                ", \"dateCreated\":\"" + dateCreated + '\"' +
                ", \"photoURL\":\"" + photoURL + '\"' +
                ", \"displayedPhotoURL\":" + displayedPhotoURL +
                ", \"dateLastComment\":\"" + dateLastComment + '\"' +
                ", \"dateLastPost\":\"" + dateLastPost + '\"' +
                ", \"dateQuitting\":\"" + dateQuitting + '\"' +
                ", \"numberOfCigarettePerDay\":" + numberOfCigarettePerDay +
                ", \"numberOfCigarettePerPack\":" + numberOfCigarettePerPack +
                ", \"priceOfPack\":" + priceOfPack +
                ", \"currency\":" + currency +
                '}';
    }
}
// [END blog_user_class]
