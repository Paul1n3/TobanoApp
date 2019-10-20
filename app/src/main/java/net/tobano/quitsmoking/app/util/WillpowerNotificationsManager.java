package net.tobano.quitsmoking.app.util;

import android.content.Context;
import android.os.AsyncTask;

import net.tobano.quitsmoking.app.Start;

/**
 * Created by Carine
 */
public class WillpowerNotificationsManager extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... params) {
        Context context;
        try{
            context = params[0];
        }
        catch (Exception e){
            return null;
        }
        Start s = ((Start) context);
        //s.setWillpowerLevelsAndNotifications();
        return null;
    }
}
