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

public class Life extends Fragment {
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */

	private LinearLayout ltime;

	private TextView tvTitle;
	private TextView tvLabelDaysSaved;
	private TextView tvDaysSaved;
	private TextView tvLabelHoursSaved;
	private TextView tvHoursSaved;
	private TextView tvLabelMinutesSaved;
	private TextView tvMinutesSaved;
	private TextView tvLabelSecondsSaved;
	private TextView tvSecondsSaved;
	private TextView semiColon1;
	private TextView semiColon2;

	private View view1;
	private View view2;

	private Button mshareButton;
	private Button mBackButton;

	private LinearLayout layoutTitle;
	private LinearLayout layoutCenter;

	private Handler mHandlerLife;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = (LinearLayout) inflater.inflate(R.layout.tab_life,
				container, false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

		mHandlerLife = new Handler();
		mHandlerLife.post(mUpdateLife);

		ltime = (LinearLayout) v.findViewById(R.id.ltime);

		tvTitle = (TextView) v.findViewById(R.id.tvQuitterTitle);
		tvLabelDaysSaved = (TextView) v.findViewById(R.id.tvDaysLabel);
		tvDaysSaved = (TextView) v.findViewById(R.id.tvDaysSaved);
		tvLabelHoursSaved = (TextView) v.findViewById(R.id.tvHoursLabel);
		tvHoursSaved = (TextView) v.findViewById(R.id.tvHoursSaved);
		tvLabelMinutesSaved = (TextView) v.findViewById(R.id.tvMinutesLabel);
		tvMinutesSaved = (TextView) v.findViewById(R.id.tvMinutesSaved);
		tvLabelSecondsSaved = (TextView) v.findViewById(R.id.tvSecondsLabel);
		tvSecondsSaved = (TextView) v.findViewById(R.id.tvSecondsSaved);
		semiColon1 = (TextView) v.findViewById(R.id.firstColonSeparator);
		semiColon2 = (TextView) v.findViewById(R.id.secondColonSeparator);

		view1 = (View) v.findViewById(R.id.firstView);
		view2 = (View) v.findViewById(R.id.secondView);

		layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
		layoutCenter = (LinearLayout) v.findViewById(R.id.lcenter);

		mshareButton = (Button) v.findViewById(R.id.btnShare);
		mshareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).playSound(Statistics.strClick);
				share();
				FlurryAgent.logEvent("Share_button_statistics_life");
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
		tvLabelDaysSaved.setTextColor(color);
		tvDaysSaved.setTextColor(color);
		tvLabelHoursSaved.setTextColor(color);
		tvHoursSaved.setTextColor(color);
		tvLabelMinutesSaved.setTextColor(color);
		tvMinutesSaved.setTextColor(color);
		tvLabelSecondsSaved.setTextColor(color);
		tvSecondsSaved.setTextColor(color);
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
		View vCenter = layoutCenter;
		vCenter.setDrawingCacheEnabled(true);
		vCenter.layout(0, vTitle.getMeasuredHeight(),
				vCenter.getMeasuredWidth(), vCenter.getMeasuredHeight()
						+ vTitle.getMeasuredHeight());
		vCenter.buildDrawingCache(true);
		bitmap = Bitmap.createBitmap(vCenter.getDrawingCache());
		vCenter.setDrawingCacheEnabled(false);
		Bitmap bitmapShare = Statistics.overlay(bitmapTitle, bitmap);

		String shareMessage;
		String days = "0";
		String hours = "0";
		String minutes = "0";
		String seconds = "0";
		int lifeSaved = (int) Start.lifeSaved;
		if (lifeSaved > 999 * 365 * 24 * 60 * 60) {
			days = hours = minutes = seconds = "999";
		} else {
			if (lifeSaved >= 24 * 60 * 60) {
				days = String.valueOf((int) (lifeSaved / 24 / 60 / 60));
				lifeSaved = lifeSaved - Integer.valueOf(days) * 24 * 60 * 60;
			}
			if (lifeSaved >= 60 * 60) {
				hours = String.valueOf((int) (lifeSaved / 60 / 60));
				lifeSaved = lifeSaved - Integer.valueOf(hours) * 60 * 60;
			}
			if (lifeSaved >= 60) {
				minutes = String.valueOf((int) (lifeSaved / 60));
				lifeSaved = lifeSaved - Integer.valueOf(minutes) * 60;
			}
			seconds = String.valueOf((int) (lifeSaved));
		}
		shareMessage = String.format(
				getResources().getString(R.string.strLifeGainedEmailSentence),
				days, hours, minutes, seconds);

		Intent intent = Util.sharePicture(getActivity(), bitmapShare, "kwit_timeWon", shareMessage, getResources().getString(R.string.share));

		if(intent != null){
			startActivity(intent);
		}
	}

	protected Runnable mUpdateLife = new Runnable() {
		public void run() {
			updateTvLifeSaved();
			mHandlerLife.postDelayed(this, 1000);
		}
	};

	public void updateTvLifeSaved() {
		int lifeSaved = (int) Start.lifeSaved;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		if (lifeSaved >= 24 * 60 * 60) {
			days = (int) (lifeSaved / 24 / 60 / 60);
			lifeSaved = lifeSaved - days * 24 * 60 * 60;
		}
		if (lifeSaved >= 60 * 60) {
			hours = (int) (lifeSaved / 60 / 60);
			lifeSaved = lifeSaved - hours * 60 * 60;
		}
		if (lifeSaved >= 60) {
			minutes = (int) (lifeSaved / 60);
			lifeSaved = lifeSaved - minutes * 60;
		}
		seconds = (int) (lifeSaved);
		// Display the number of days on 3 digits
		if (days < 0)
			tvDaysSaved.setText("000");
		else if (days < 10)
			tvDaysSaved.setText("00" + Integer.toString(days));
		else if (days < 100)
			tvDaysSaved.setText("0" + Integer.toString(days));
		else if (days < 1000)
			tvDaysSaved.setText(Integer.toString(days));

		// Display the number of hours on 2 digits
		if (hours < 0)
			tvHoursSaved.setText("00");
		else if (hours < 10)
			tvHoursSaved.setText("0" + Integer.toString(hours));
		else
			tvHoursSaved.setText(Integer.toString(hours));

		// Display the number of minutes on 2 digits
		if (minutes < 0)
			tvMinutesSaved.setText("00");
		else if (minutes < 10)
			tvMinutesSaved.setText("0" + Integer.toString(minutes));
		else
			tvMinutesSaved.setText(Integer.toString(minutes));

		// Display the number of seconds on 2 digits
		if (seconds < 0)
			tvSecondsSaved.setText("00");
		else if (seconds < 10)
			tvSecondsSaved.setText("0" + Integer.toString(seconds));
		else
			tvSecondsSaved.setText(Integer.toString(seconds));

		if (days > 1000) {
			tvDaysSaved.setText("999");
			tvHoursSaved.setText("99");
			tvMinutesSaved.setText("99");
			tvSecondsSaved.setText("99");
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
		updateTvLifeSaved();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Handler getmHandlerLife() {
		return mHandlerLife;
	}

	public void setmHandlerLife(Handler mHandlerLife) {
		this.mHandlerLife = mHandlerLife;
	}

	public Runnable getmUpdateLife() {
		return mUpdateLife;
	}

	public void setmUpdateLife(Runnable mUpdateLife) {
		this.mUpdateLife = mUpdateLife;
	}
}