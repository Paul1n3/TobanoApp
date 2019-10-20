package net.tobano.quitsmoking.app.util;

import com.google.firebase.database.DatabaseError;

public interface OnGetDataListener {

    public String TYPE_USER = "USER";
    public String TYPE_FORUM = "FORUM";
    public String TYPE_USER_LANGUAGE = "USER_LANGUAGE";

    public String SIGN_OUT = "SIGN_OUT";
    public String SIGN_IN = "SIGN_IN";

    public void onFirebaseStart();
    public void onSuccess(String type);
    public void onFailed(DatabaseError databaseError);
}
