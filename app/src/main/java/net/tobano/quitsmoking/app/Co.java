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

public class Co extends Fragment {
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */

	private LinearLayout lco;
	private TextView tvTitleCoNotInhaled;
	private TextView tvCONotInhaled;
	private TextView tvLabelCoNotInhaled;
	private View view1;
	private View view2;

	private Button mshareButton;
	private Button mBackButton;

	private LinearLayout layoutTitle;
	private LinearLayout layoutCenter;

	private Handler mHandlerCo;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = (LinearLayout) inflater.inflate(R.layout.tab_co, container,
				false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

		mHandlerCo = new Handler();
		mHandlerCo.post(mUpdateCo);

		lco = (LinearLayout) v.findViewById(R.id.lco);

		tvTitleCoNotInhaled = (TextView) v.findViewById(R.id.tvCoTitle);
		tvCONotInhaled = (TextView) v.findViewById(R.id.tvCo);
		tvLabelCoNotInhaled = (TextView) v.findViewById(R.id.tvCoLabel);
		view1 = (View) v.findViewById(R.id.firstView);
		view2 = (View) v.findViewById(R.id.secondView);

		layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
		layoutCenter = (LinearLayout) v.findViewById(R.id.lcenter);

		mshareButton = (Button) v.findViewById(R.id.btnShare);
		mshareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).playSound(Statistics.strClick);
				share();
				FlurryAgent.logEvent("Share_button_statistics_co");
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
		tvTitleCoNotInhaled.setTextColor(color);
		tvCONotInhaled.setTextColor(color);
		tvLabelCoNotInhaled.setTextColor(color);
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
		int coNotInhaled = (int) Start.coSaved;
		shareMessage = String
				.format(getResources().getString(
						R.string.strCoNotinhaledEmailSentence),
						coInStringFormat(coNotInhaled));

		Intent intent = Util.sharePicture(getActivity(), bitmapShare, "kwit_CoNotInhaled", shareMessage, getResources().getString(R.string.share));

		if(intent != null){
			startActivity(intent);
		}
	}

	protected Runnable mUpdateCo = new Runnable() {
		public void run() {
			updateTvCoNotInhaled();
			mHandlerCo.postDelayed(this, 1000);
		}
	};

	public void updateTvCoNotInhaled() {
		int coNotInhaled = (int) Start.coSaved;
		tvCONotInhaled.setText(coInStringFormat(coNotInhaled));
	}

	private String coInStringFormat(int coNotInhaled) {
		if (coNotInhaled < 0) {
			return "0";
		}
		String uglyValue = String.valueOf(coNotInhaled);
		String value = "";
		for (int i = (uglyValue.length() - 1); i >= 0; --i) {
			value += uglyValue.charAt(uglyValue.length() - i - 1);
			if (i % 3 == 0 && i != 0) {
				value += " ";
			}
		}
		return value;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateTvCoNotInhaled();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Handler getmHandlerCo() {
		return mHandlerCo;
	}

	public void setmHandlerCo(Handler mHandlerCo) {
		this.mHandlerCo = mHandlerCo;
	}

	public Runnable getmUpdateCo() {
		return mUpdateCo;
	}

	public void setmUpdateCo(Runnable mUpdateCo) {
		this.mUpdateCo = mUpdateCo;
	}
}