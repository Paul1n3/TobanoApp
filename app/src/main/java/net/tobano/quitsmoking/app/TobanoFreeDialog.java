package net.tobano.quitsmoking.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.flurry.android.FlurryAgent;

public class TobanoFreeDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public Button share, ok;

    public TobanoFreeDialog(Activity activity) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.activity = activity;
    }

    public static TobanoFreeDialog newInstance(Activity activity) {
        return new TobanoFreeDialog(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.fullversion_for_free);

        share = (Button) findViewById(R.id.share_button);
        share.setOnClickListener(this);

        ok = (Button) findViewById(R.id.ok_button);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_button:
                FlurryAgent.logEvent("Share_button_more");
                ((Start) activity).playSound(Start.strClick);
                String shareBody = activity.getResources().getString(
                        R.string.ILikeThisAppAndIThinkYouShouldTryItToo)
                        + "\n" + activity.getResources().getString(R.string.tabanoTag) + "\n"
                        + activity.getResources().getString(R.string.tabanoWebsite);
                Intent sharingIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        activity.getResources().getString(R.string.IQuitSmokingWithKwit));
                sharingIntent
                        .putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.share)));
                break;
            case R.id.ok_button:
                dismiss();
                break;
            default:
                break;
        }
    }
}
