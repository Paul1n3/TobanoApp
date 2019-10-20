package net.tobano.quitsmoking.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import net.tobano.quitsmoking.app.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Nicolas Lett
 * 
 */
public class Quitter extends Fragment {
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */

	private Date quittingDate = Statistics.quittingDate;
	private String currentDateTimeString;

	private LinearLayout ltime;

	private TextView tvTitle;
	private TextView tvLabelQuitterSinceDays;
	private TextView tvQuitterSinceDays;
	private TextView tvLabelQuitterSinceHours;
	private TextView tvQuitterSinceHours;
	private TextView tvLabelQuitterSinceMinutes;
	private TextView tvQuitterSinceMinutes;
	private TextView tvLabelQuitterSinceSeconds;
	private TextView tvQuitterSinceSeconds;
	private TextView semiColon1;
	private TextView semiColon2;

	private View view1;
	private View view2;

	private Button mshareButton;
	private Button mBackButton;

	private LinearLayout layoutTitle;
	private LinearLayout layoutQuitter;

	private Handler mHandlerQuitter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());
		View v = (LinearLayout) inflater.inflate(R.layout.tab_quitter,
				container, false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

		mHandlerQuitter = new Handler();
		//System.out.println("Application handler called");
		mHandlerQuitter.post(mUpdateQuitter);

		ltime = (LinearLayout) v.findViewById(R.id.ltime);

		tvTitle = (TextView) v.findViewById(R.id.tvQuitterTitle);
		tvLabelQuitterSinceDays = (TextView) v.findViewById(R.id.tvQuitterSinceDaysLabel);
		tvQuitterSinceDays = (TextView) v.findViewById(R.id.tvQuitterSinceDays);
		tvLabelQuitterSinceHours = (TextView) v.findViewById(R.id.tvQuitterSinceHoursLabel);
		tvQuitterSinceHours = (TextView) v.findViewById(R.id.tvQuitterSinceHours);
		tvLabelQuitterSinceMinutes = (TextView) v.findViewById(R.id.tvQuitterSinceMinutesLabel);
		tvQuitterSinceMinutes = (TextView) v.findViewById(R.id.tvQuitterSinceMinutes);
		tvLabelQuitterSinceSeconds = (TextView) v.findViewById(R.id.tvQuitterSinceSecondsLabel);
		tvQuitterSinceSeconds = (TextView) v.findViewById(R.id.tvQuitterSinceSeconds);
		semiColon1 = (TextView) v.findViewById(R.id.firstColonSeparator);
		semiColon2 = (TextView) v.findViewById(R.id.secondColonSeparator);

		view1 = (View) v.findViewById(R.id.firstView);
		view2 = (View) v.findViewById(R.id.secondView);

		layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
		layoutQuitter = (LinearLayout) v.findViewById(R.id.lcenter);

		mshareButton = (Button) v.findViewById(R.id.btnShare);
		mshareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).playSound(Statistics.strClick);
				share();
				FlurryAgent.logEvent("Share_button_statistics_kwitter_since");
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

	private void setTheme() {
		int color = ((Statistics)getActivity()).getThemeColor();
		tvTitle.setTextColor(color);
		tvQuitterSinceDays.setTextColor(color);
		tvLabelQuitterSinceDays.setTextColor(color);
		tvQuitterSinceHours.setTextColor(color);
		tvLabelQuitterSinceHours.setTextColor(color);
		tvQuitterSinceMinutes.setTextColor(color);
		tvLabelQuitterSinceMinutes.setTextColor(color);
		tvQuitterSinceSeconds.setTextColor(color);
		tvLabelQuitterSinceSeconds.setTextColor(color);
		semiColon1.setTextColor(color);
		semiColon2.setTextColor(color);
		view1.setBackgroundColor(color);
		view2.setBackgroundColor(color);
	}

	private void share() {
		// create bitmap screen capture
		Bitmap bitmapTitle;
		View vTitle = layoutTitle;
		vTitle.setDrawingCacheEnabled(true);
		vTitle.layout(0, 0, vTitle.getMeasuredWidth(),
				vTitle.getMeasuredHeight());
		vTitle.buildDrawingCache(true);
		bitmapTitle = Bitmap.createBitmap(vTitle.getDrawingCache());
		vTitle.setDrawingCacheEnabled(false);
		Bitmap bitmap;
		View vCenter = layoutQuitter;
		vCenter.setDrawingCacheEnabled(true);
		vCenter.layout(0, vTitle.getMeasuredHeight(),
				vCenter.getMeasuredWidth(), vCenter.getMeasuredHeight()
						+ vTitle.getMeasuredHeight());
		vCenter.buildDrawingCache(true);
		bitmap = Bitmap.createBitmap(vCenter.getDrawingCache());
		vCenter.setDrawingCacheEnabled(false);
		Bitmap bitmapShare = Statistics.overlay(bitmapTitle, bitmap);

		String shareMessage;
		shareMessage = String
				.format(getResources().getString(
								R.string.strKwitterSinceEmailSentence),
						Statistics.diff[0], Statistics.diff[1],
						Statistics.diff[2], Statistics.diff[3]);

		Intent intent = Util.sharePicture(getActivity(), bitmapShare, "tobano_quittter", shareMessage, getResources().getString(R.string.share));

		if(intent != null){
			startActivity(intent);
		}
	}

	protected Runnable mUpdateQuitter = new Runnable() {
		public void run() {
			updateTvQuitterSince();
			mHandlerQuitter.postDelayed(this, 1000);
		}
	};

	public void updateTvQuitterSince() {
		// Display the number of days on 3 digits
		if (Statistics.diff[0] < 10)
			tvQuitterSinceDays
					.setText("000" + Long.toString(Statistics.diff[0]));
		else if (Statistics.diff[0] < 100)
			tvQuitterSinceDays.setText("00" + Long.toString(Statistics.diff[0]));
		else if (Statistics.diff[0] < 1000)
			tvQuitterSinceDays.setText("0" + Long.toString(Statistics.diff[0]));
		else if (Statistics.diff[0] < 10000)
			tvQuitterSinceDays.setText(Long.toString(Statistics.diff[0]));
		else
			tvQuitterSinceDays.setText("9999");

		// Display the number of hours on 2 digits
		if (Statistics.diff[1] < 10)
			tvQuitterSinceHours
					.setText("0" + Long.toString(Statistics.diff[1]));
		else
			tvQuitterSinceHours.setText(Long.toString(Statistics.diff[1]));

		// Display the number of minutes on 2 digits
		if (Statistics.diff[2] < 10)
			tvQuitterSinceMinutes.setText("0"
					+ Long.toString(Statistics.diff[2]));
		else
			tvQuitterSinceMinutes.setText(Long.toString(Statistics.diff[2]));

		// Display the number of seconds on 2 digits
		if (Statistics.diff[3] < 10)
			tvQuitterSinceSeconds.setText("0"
					+ Long.toString(Statistics.diff[3]));
		else
			tvQuitterSinceSeconds.setText(Long.toString(Statistics.diff[3]));

		if (Statistics.diff[0] > 10000) {
			tvQuitterSinceDays.setText("9999");
			tvQuitterSinceHours.setText("99");
			tvQuitterSinceMinutes.setText("99");
			tvQuitterSinceSeconds.setText("99");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		quittingDate = Statistics.quittingDate;
		updateTvQuitterSince();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Handler getmHandlerQuitter() {
		return mHandlerQuitter;
	}

	public void setmHandlerQuitter(Handler mHandlerQuitter) {
		this.mHandlerQuitter = mHandlerQuitter;
	}

	public Runnable getmUpdateQuitter() {
		return mUpdateQuitter;
	}

	public void setmUpdateQuitter(Runnable mUpdateQuitter) {
		this.mUpdateQuitter = mUpdateQuitter;
	}
}
