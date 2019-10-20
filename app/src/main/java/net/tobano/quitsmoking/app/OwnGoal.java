package net.tobano.quitsmoking.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

/**
 * @author Carine
 */

public class OwnGoal extends Fragment {

    private LinearLayout layoutTitle;
    private LinearLayout layoutCenter;
    private LinearLayout layoutTime;
    private LinearLayout layoutCongrats;
    private Button mGoalButton;
    private Button mshareButton;
    private Button mBackButton;
    private TextView tvTitle;
    private TextView tvOwnGoal;
    private TextView tvDays;
    private TextView tvTxtDays;
    private TextView tvHours;
    private TextView tvTxtHours;
    private TextView tvMinutes;
    private TextView tvTxtMinutes;
    private TextView tvSeconds;
    private TextView tvTxtSeconds;
    private TextView semiColon1;
    private TextView semiColon2;
    private View view1;
    private View view2;

    private Handler mHandlerOwnGoal;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (LinearLayout) inflater.inflate(R.layout.tab_owngoal,
                container, false);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);

        mHandlerOwnGoal = new Handler();
        mHandlerOwnGoal.post(mUpdateOwnGoal);

        tvTitle = (TextView) v.findViewById(R.id.tvGoalTitle);
        tvTxtDays = (TextView) v.findViewById(R.id.tvQuitterSinceDaysLabel);
        tvTxtHours = (TextView) v.findViewById(R.id.tvQuitterSinceHoursLabel);
        tvTxtMinutes = (TextView) v.findViewById(R.id.tvQuitterSinceMinutesLabel);
        tvTxtSeconds = (TextView) v.findViewById(R.id.tvQuitterSinceSecondsLabel);
        semiColon1 = (TextView) v.findViewById(R.id.firstColonSeparator);
        semiColon2 = (TextView) v.findViewById(R.id.secondColonSeparator);
        view1 = (View) v.findViewById(R.id.firstView);
        view2 = (View) v.findViewById(R.id.secondView);

        tvOwnGoal = (TextView) v.findViewById(R.id.tvGoalDescription);
        tvDays = (TextView) v.findViewById(R.id.tvDays);
        tvHours = (TextView) v.findViewById(R.id.tvHours);
        tvMinutes = (TextView) v.findViewById(R.id.tvMinutes);
        tvSeconds = (TextView) v.findViewById(R.id.tvSeconds);

        layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
        layoutCenter = (LinearLayout) v.findViewById(R.id.lcenter);
        layoutTime = (LinearLayout) v.findViewById(R.id.lgoal);
        layoutCongrats = (LinearLayout) v.findViewById(R.id.lCongrats);

        mGoalButton = (Button) v.findViewById(R.id.btnAdd);
        mGoalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Statistics)getActivity()).playSound(Statistics.strClick);
                addGoal();
                FlurryAgent.logEvent("Add_button_statistics_owngoal");
            }
        });
        mshareButton = (Button) v.findViewById(R.id.btnShare);
        mshareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Statistics)getActivity()).playSound(Statistics.strClick);
                share();
                FlurryAgent.logEvent("Share_button_statistics_owngoal");
            }
        });
        mBackButton = (Button) v.findViewById(R.id.btnBack);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Statistics)getActivity()).pressedCustomBackButton();
            }
        });

        setTheme();

        if (container == null) {
            return null;
        }
        return v;
    }

    protected Runnable mUpdateOwnGoal = new Runnable() {
        public void run() {
            update();
            mHandlerOwnGoal.postDelayed(this, 1000);
        }
    };

    private void setTheme() {
        int color = ((Statistics)getActivity()).getThemeColor();
        tvTitle.setTextColor(color);
        tvDays.setTextColor(color);
        tvTxtDays.setTextColor(color);
        tvHours.setTextColor(color);
        tvTxtHours.setTextColor(color);
        tvMinutes.setTextColor(color);
        tvTxtMinutes.setTextColor(color);
        tvSeconds.setTextColor(color);
        tvTxtSeconds.setTextColor(color);
        semiColon1.setTextColor(color);
        semiColon2.setTextColor(color);
        view1.setBackgroundColor(color);
        view2.setBackgroundColor(color);
    }

    private void update() {
        boolean goalAchieved = isFinished();
        try {
            Drawable drawable;
            if (Statistics.ownGoal == 0) {
                layoutCongrats.setVisibility(View.INVISIBLE);
                drawable = ContextCompat.getDrawable(getActivity(), R.drawable.addgoal);
            }
            else if (goalAchieved){
                layoutCongrats.setVisibility(View.VISIBLE);
                drawable = ContextCompat.getDrawable(getActivity(), R.drawable.addgoal);
            }
            else {
                layoutCongrats.setVisibility(View.INVISIBLE);
                drawable = ContextCompat.getDrawable(getActivity(), R.drawable.changegoal);
            }
            mGoalButton.setBackground(drawable);

            tvOwnGoal.setText(Statistics.ownGoalTitle);
            String[] ownGoalInTime = getTimeBeforeSuccessGoal();
            tvDays.setText(ownGoalInTime[0]);
            tvHours.setText(ownGoalInTime[1]);
            tvMinutes.setText(ownGoalInTime[2]);
            tvSeconds.setText(ownGoalInTime[3]);
        }
        catch (Exception e) {
            // to avoid a fail when the user clicks on back pressed before the update method is finished
        }
    }

    private boolean isFinished(){
        if (Statistics.ownGoal > Start.moneySaved) {
            return false;
        }
        else {
            return true;
        }
    }

    private void addGoal() {
        String name = isFinished() ? "" : Statistics.ownGoalTitle;
        String value = isFinished() ? "" : String.valueOf(Util.getLastElement(Start.ownGoal));
        OwnGoalDialog dialog = OwnGoalDialog.newInstance(name, value, Start.theme);
        dialog.show(getFragmentManager(), "OwnGoalDialod");
        getFragmentManager().executePendingTransactions();
        setDialogEvents(dialog);
    }

    private void setDialogEvents(final OwnGoalDialog dialog) {
        final EditText goalTitle = (EditText) dialog.getView().findViewById(R.id.goalTitle);
        final EditText goalValue = (EditText) dialog.getView().findViewById(R.id.goalValue);
        Button validate = (Button) dialog.getView().findViewById(R.id.btnValidate);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Statistics)getActivity()).playSound(Statistics.strClick);
                String name = goalTitle.getText().toString();
                String value = goalValue.getText().toString();
                if (!name.equals("") && !value.equals("")) {
                    try {
                        int test = Integer.valueOf(value); // do not remove this line
                        defineGoal(name, value);
                        FlurryAgent.logEvent("OwnGoal_define");
                        dialog.dismiss();
                    }
                    catch (Exception e){
                        Toast.makeText(getContext(), getString(R.string.errorNumberFormat), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.errorEmpty), Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button back = (Button) dialog.getView().findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Statistics)getActivity()).playSound(Statistics.strClick);
                dialog.dismiss();
            }
        });
    }

    private void defineGoal(String name, String value) {
        if (isFinished() || Statistics.ownGoal == 0){
            Statistics.addGoalInPreferences(name, value);
        }
        else {
            Statistics.replaceGoalInPreferences(name, value);
        }
    }

    private void share() {
        // create bitmap screen capture
        Bitmap bitmapTitle;
        View vTitle = layoutTitle;
        vTitle.setDrawingCacheEnabled(true);
        vTitle.layout(
                0,
                0,
                vTitle.getMeasuredWidth(),
                vTitle.getMeasuredHeight());
        vTitle.buildDrawingCache(true);
        bitmapTitle = Bitmap.createBitmap(vTitle.getDrawingCache());
        vTitle.setDrawingCacheEnabled(false);

        Bitmap bitmap;
        View vCenter = layoutCenter;
        vCenter.setDrawingCacheEnabled(true);
        vCenter.layout(
                0,
                vTitle.getMeasuredHeight(),
                vCenter.getMeasuredWidth(),
                vCenter.getMeasuredHeight() + vTitle.getMeasuredHeight());
        vCenter.buildDrawingCache(true);
        bitmap = Bitmap.createBitmap(vCenter.getDrawingCache());
        vCenter.setDrawingCacheEnabled(false);

        Bitmap bitmapShare = Statistics.overlay(bitmapTitle, bitmap);

        String[] ownGoalInTime = getTimeBeforeSuccessGoal();
        String shareMessage = String
                .format(getResources().getString(
                        R.string.strGoal),
                        ownGoalInTime[0], ownGoalInTime[1],
                        ownGoalInTime[2], ownGoalInTime[3],
                        Statistics.ownGoalTitle);

        Intent intent = Util.sharePicture(getActivity(), bitmapShare, "kwit_OwnGoal", shareMessage, getResources().getString(R.string.share));

        if(intent != null){
            startActivity(intent);
        }
    }

    private String[] getTimeBeforeSuccessGoal() {
        String[] result = new String[4];
        double missingMoney = Statistics.ownGoal - Start.moneySaved;
        if (missingMoney <= 0){
            result[0] = result[1] = result[2] = result[3] = "00";
        }
        else {
            double moneySavedByDay = Statistics.priceOfACigarette * Statistics.cigarettesPerDay;
            double moneySavedBySecond = moneySavedByDay / 24/60/60;
            double stayingTimeInSeconds = missingMoney / moneySavedBySecond;
            long[] dhms = Util.convertInDayHourMinSec((long) stayingTimeInSeconds);
            result[0] = Util.parseIntToString((int)dhms[0], 9999);
            result[1] = Util.parseIntToString((int)dhms[1], 99);
            result[2] = Util.parseIntToString((int)dhms[2], 99);
            result[3] = Util.parseIntToString((int)dhms[3], 99);
        }
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public Handler getmHandlerOwnGoal() {
        return mHandlerOwnGoal;
    }

    public void setmHandlerOwnGoal(Handler mHandlerQuitter) {
        this.mHandlerOwnGoal = mHandlerQuitter;
    }

    public Runnable getmUpdateOwnGoal() {
        return mUpdateOwnGoal;
    }

    public void setmUpdateOwnGoal(Runnable mUpdateQuitter) {
        this.mUpdateOwnGoal = mUpdateQuitter;
    }
}
