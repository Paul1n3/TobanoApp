package net.tobano.quitsmoking.app.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import net.tobano.quitsmoking.app.Start;
import net.tobano.quitsmoking.app.util.Theme;

import java.util.HashMap;
import java.util.Map;


public class BaseActivity extends AppCompatActivity {

    public static Theme theme = Start.theme;
    private static final String TAG = "BaseActivity";


    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void suspendUser(String uid, DatabaseReference database) {
        //database.child("users").child(uid).child("isBlocked").setValue(true);
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users-banned/" + uid, true);

        // request post creation with completion listener
        database.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "onComplete: success");
                } else {
                    Log.w(TAG, "onComplete: fail", databaseError.toException());
                }
            }
        });
    }
}
