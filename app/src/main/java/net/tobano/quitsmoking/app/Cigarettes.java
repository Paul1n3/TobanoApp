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


/**
 * @author Nicolas Lett
 * 
 */
public class Cigarettes extends Fragment {

	private LinearLayout lcig;

	private TextView tvTitle;
	private TextView tvCigarettesNotSmokedThousand;
	private TextView tvCigarettesNotSmokedUnit;
	private TextView tvLabel;

	private View view1;
	private View view2;

	private Button mshareButton;
	private Button mBackButton;

	private LinearLayout layoutTitle;
	private LinearLayout layoutCenter;

	private Handler mHandlerCigarettes;

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = (LinearLayout) inflater.inflate(R.layout.tab_cigarettes,
				container, false);

		if (container == null) {
			return null;
		}

		mHandlerCigarettes = new Handler();
		mHandlerCigarettes.post(mUpdateCigarettes);

		lcig = (LinearLayout) v.findViewById(R.id.lcig);

		tvTitle = (TextView) v.findViewById(R.id.tvCigarettesTitle);
		tvCigarettesNotSmokedThousand = (TextView) v
				.findViewById(R.id.tvCigarettesNotSmokedThousand);
		tvCigarettesNotSmokedUnit = (TextView) v
				.findViewById(R.id.tvCigarettesNotSmokedUnit);
		tvLabel = (TextView) v.findViewById(R.id.tvCigarettesNotSmokedLabel);

		view1 = (View) v.findViewById(R.id.firstView);
		view2 = (View) v.findViewById(R.id.secondView);

		layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
		layoutCenter = (LinearLayout) v.findViewById(R.id.lcenter);

		mshareButton = (Button) v.findViewById(R.id.btnShare);
		mshareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).playSound(Statistics.strClick);
				share();
				FlurryAgent.logEvent("Share_button_statistics_cigarettes");
			}
		});
		mBackButton = (Button) v.findViewById(R.id.btnBack);
		mBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).pressedCustomBackButton();
			}
		});

		setTheme();

		return v;
	}

	private void setTheme() {
		int color = ((Statistics)getActivity()).getThemeColor();
		tvTitle.setTextColor(color);
		tvCigarettesNotSmokedThousand.setTextColor(color);
		tvCigarettesNotSmokedUnit.setTextColor(color);
		tvLabel.setTextColor(color);
		view1.setBackgroundColor(color);
		view2.setBackgroundColor(color);
	}

	protected Runnable mUpdateCigarettes = new Runnable() {
		public void run() {
			updateTvCigarettesNotSmoked();
			mHandlerCigarettes.postDelayed(this, 1000);
		}
	};

	public void updateTvCigarettesNotSmoked() {
		double cigNotSmoked = Start.cigarettesNotSmoked;
		// Warning !! substring index begins from 1 and substring(x, y) doesn't
		// include the y character
		if (cigNotSmoked < 1) {
			tvCigarettesNotSmokedThousand.setText("000");
			tvCigarettesNotSmokedUnit.setText("000");
		} else if (cigNotSmoked < 10) {
			tvCigarettesNotSmokedThousand.setText("000");
			tvCigarettesNotSmokedUnit.setText("00"
					+ (int) cigNotSmoked);
		} else if (cigNotSmoked < 100) {
			tvCigarettesNotSmokedThousand.setText("000");
			tvCigarettesNotSmokedUnit.setText("0"
					+ (int) cigNotSmoked);
		} else if (cigNotSmoked < 1000) {
			tvCigarettesNotSmokedThousand.setText("000");
			tvCigarettesNotSmokedUnit.setText(""
					+ (int) cigNotSmoked);
		} else if (cigNotSmoked < 10000) {
			tvCigarettesNotSmokedThousand.setText("00"
					+ Integer.toString((int) cigNotSmoked)
							.substring(0, 1));
			tvCigarettesNotSmokedUnit.setText(""
					+ Integer.toString((int) cigNotSmoked)
							.substring(1));
		} else if (cigNotSmoked < 100000) {
			tvCigarettesNotSmokedThousand.setText("0"
					+ Integer.toString((int) cigNotSmoked)
							.substring(0, 2));
			tvCigarettesNotSmokedUnit.setText(""
					+ Integer.toString((int) cigNotSmoked)
							.substring(2));
		} else {
			tvCigarettesNotSmokedThousand.setText("999");
			tvCigarettesNotSmokedUnit.setText("999");
		}
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
		String strCigarettesNotSmokedTwitterFormat = getResources().getString(
				R.string.strCigarettesNotSmokedEmailSentence);
		String thousand = tvCigarettesNotSmokedThousand.getText().toString();
		int thousand_amount = Integer.valueOf(thousand);
		if (thousand_amount == 0){
			thousand = "";
		}
		else if (thousand_amount < 10){
			thousand = thousand.substring(thousand.length() -1);
		}
		else if (thousand_amount < 100){
			thousand = thousand.substring(thousand.length() -2);
		}
		String unit = tvCigarettesNotSmokedUnit.getText().toString();
		int unit_amount = Integer.valueOf(unit);
		if(unit_amount < 10){
			unit = unit.substring(unit.length() -1);
		}
		else if (unit_amount < 100){
			unit = unit.substring(unit.length() -2);
		}
		shareMessage = String.format(strCigarettesNotSmokedTwitterFormat, thousand, unit);

		Intent intent = Util.sharePicture(getActivity(), bitmapShare, "kwit_cigarettesNotSmoked", shareMessage, getResources().getString(R.string.share));

		if(intent != null){
			startActivity(intent);
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
		updateTvCigarettesNotSmoked();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Handler getmHandlerCigarettes() {
		return mHandlerCigarettes;
	}

	public void setmHandlerCigarettes(Handler mHandlerCigarettes) {
		this.mHandlerCigarettes = mHandlerCigarettes;
	}

	public Runnable getmUpdateCigarettes() {
		return mUpdateCigarettes;
	}

	public void setmUpdateCigarettes(Runnable mUpdateCigarettes) {
		this.mUpdateCigarettes = mUpdateCigarettes;
	}
}
