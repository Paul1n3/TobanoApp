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

import java.text.DecimalFormat;

/**
 * @author Nicolas Lett
 * 
 */
public class Money extends Fragment {

	private LinearLayout lmoney;

	private TextView tvTitle;
	private TextView tvMoneyEarnedThousand;
	private TextView tvMoneyEarnedUnit;
	private TextView tvMoneyEarnedCents;
	private TextView tvMoneyEarnedComma;
	private TextView tvMoneyEarnedUnitLabel;

	private View view1;
	private View view2;

	private Button mshareButton;
	private Button mBackButton;

	private LinearLayout layoutTitle;
	private LinearLayout layoutCenter;

	private Handler mHandlerMoney;

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = (LinearLayout) inflater.inflate(R.layout.tab_money,
				container, false);

		if (container == null) {
			return null;
		}

		mHandlerMoney = new Handler();
		mHandlerMoney.post(mUpdateMoney);

		lmoney = (LinearLayout) v.findViewById(R.id.lmoney);

		tvTitle = (TextView) v.findViewById(R.id.tvMoneyTitle);
		tvMoneyEarnedThousand = (TextView) v
				.findViewById(R.id.tvMoneyEarnedThousand);
		tvMoneyEarnedUnit = (TextView) v.findViewById(R.id.tvMoneyEarnedUnit);
		tvMoneyEarnedCents = (TextView) v.findViewById(R.id.tvMoneyEarnedCents);
		tvMoneyEarnedUnitLabel = (TextView) v
				.findViewById(R.id.tvMoneyEarnedUnitLabel);
		tvMoneyEarnedComma = (TextView) v
				.findViewById(R.id.tvComa);

		view1 = (View) v.findViewById(R.id.firstView);
		view2 = (View) v.findViewById(R.id.secondView);

		layoutTitle = (LinearLayout) v.findViewById(R.id.ltitle);
		layoutCenter = (LinearLayout) v.findViewById(R.id.lcenter);

		mshareButton = (Button) v.findViewById(R.id.btnShare);
		mshareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Statistics)getActivity()).playSound(Statistics.strClick);
				share();
				FlurryAgent.logEvent("Share_button_statistics_money");
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
		tvMoneyEarnedThousand.setTextColor(color);
		tvMoneyEarnedUnit.setTextColor(color);
		tvMoneyEarnedCents.setTextColor(color);
		tvMoneyEarnedComma.setTextColor(color);
		tvMoneyEarnedUnitLabel.setTextColor(color);
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
		String strMoneyEarnedTwitterFormat = getResources().getString(
				R.string.strMoneyEarnedEmailSentence);
		String thousand = tvMoneyEarnedThousand.getText().toString();
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
		String unit = tvMoneyEarnedUnit.getText().toString();
		int unit_amount = Integer.valueOf(tvMoneyEarnedUnit.getText().toString());
		if(unit_amount < 10){
			unit = unit.substring(unit.length() -1);
		}
		else if (unit_amount < 100){
			unit = unit.substring(unit.length() -2);
		}
		shareMessage = String.format(strMoneyEarnedTwitterFormat,
				thousand, unit,	tvMoneyEarnedCents.getText(), Start.currency);

		Intent intent = Util.sharePicture(getActivity(), bitmapShare, "kwit_moneySaved", shareMessage, getResources().getString(R.string.share));

		if(intent != null){
			startActivity(intent);
		}
	}

	protected Runnable mUpdateMoney = new Runnable() {
		public void run() {
			updateTvMoneyEarned();
			mHandlerMoney.postDelayed(this, 1000);
		}
	};

	public void updateTvMoneyEarned() {
		DecimalFormat df = new DecimalFormat("#.00");
		double moneySaved = Start.moneySaved;
		// Warning !! substring(x, y) doesn't include the y character
		if (moneySaved < 0.01) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText("000");
			tvMoneyEarnedCents.setText("00");
		} else if (moneySaved < 0.1) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText("000");
			tvMoneyEarnedCents.setText("0"
					+ (Double.toString(moneySaved)).charAt(3));
		} else if (moneySaved < 1.0) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText("000");
			try {
				tvMoneyEarnedCents.setText((Double
						.toString(moneySaved)).substring(2, 4));
			} catch (Exception e) {
				tvMoneyEarnedCents.setText((Double
						.toString(moneySaved)).charAt(2) + "0"); // e.g.
																			// "0.7"
																			// value
																			// exactly
																			// and
																			// no
																			// "0.73"
			}
		} else if (moneySaved < 10.0) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText("00"
					+ Double.toString(moneySaved).charAt(0));
			try {
				tvMoneyEarnedCents.setText(Double.toString(
						moneySaved).substring(2, 4)); // e.g. "7.12"
																	// value
			} catch (Exception e) {
				try {
					tvMoneyEarnedCents.setText(Double.toString(
							moneySaved).charAt(2)
							+ "0"); // e.g. "7.1" value
				} catch (Exception ex) {
					tvMoneyEarnedCents.setText("00"); // e.g. "7" value
				}
			}
		} else if (moneySaved < 100.0) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText("0"
					+ Double.toString(moneySaved).substring(0, 2));
			try {
				tvMoneyEarnedCents.setText(Double.toString(
						moneySaved).substring(3, 5)); // e.g. "7.12"
																	// value
			} catch (Exception e) {
				try {
					tvMoneyEarnedCents.setText(Double.toString(
							moneySaved).charAt(3)
							+ "0"); // e.g. "7.1" value
				} catch (Exception ex) {
					tvMoneyEarnedCents.setText("00"); // e.g. "7" value
				}
			}
		} else if (moneySaved < 1000.0) {
			tvMoneyEarnedThousand.setText("000");
			tvMoneyEarnedUnit.setText(Double.toString(moneySaved)
					.substring(0, 3));
			try {
				tvMoneyEarnedCents.setText(Double.toString(
						moneySaved).substring(4, 6)); // e.g. "7.12"
																	// value
			} catch (Exception e) {
				try {
					tvMoneyEarnedCents.setText(Double.toString(
							moneySaved).charAt(4)
							+ "0"); // e.g. "7.1" value
				} catch (Exception ex) {
					tvMoneyEarnedCents.setText("00"); // e.g. "7" value
				}
			}
		} else if (moneySaved < 10000.0) {
			tvMoneyEarnedThousand.setText("00"
					+ df.format(moneySaved).charAt(0));
			tvMoneyEarnedUnit.setText(df.format(moneySaved)
					.substring(1, 4));
			try {
				tvMoneyEarnedCents.setText(Double.toString(
						moneySaved).substring(5, 7)); // e.g. "7.12"
																	// value
			} catch (Exception e) {
				try {
					tvMoneyEarnedCents.setText(Double.toString(
							moneySaved).charAt(5)
							+ "0"); // e.g. "7.1" value
				} catch (Exception ex) {
					tvMoneyEarnedCents.setText("00"); // e.g. "7" value
				}
			}
		} else if (moneySaved < 100000.0) {
			tvMoneyEarnedThousand.setText("0"
					+ df.format(moneySaved).substring(0, 2));
			tvMoneyEarnedUnit.setText(df.format(moneySaved)
					.substring(2, 5));
			try {
				tvMoneyEarnedCents.setText(Double.toString(
						moneySaved).substring(6, 8)); // e.g. "7.12"
																	// value
			} catch (Exception e) {
				try {
					tvMoneyEarnedCents.setText(Double.toString(
							moneySaved).charAt(6)
							+ "0"); // e.g. "7.1" value
				} catch (Exception ex) {
					tvMoneyEarnedCents.setText("00"); // e.g. "7" value
				}
			}
		} else {
			tvMoneyEarnedThousand.setText("999");
			tvMoneyEarnedUnit.setText("999");
			tvMoneyEarnedCents.setText("99");
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
		tvMoneyEarnedUnitLabel.setText(Start.currency); // set the right
														// currency symbol from
														// the preferences
		updateTvMoneyEarned();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Handler getmHandlerMoney() {
		return mHandlerMoney;
	}

	public void setmHandlerMoney(Handler mHandlerMoney) {
		this.mHandlerMoney = mHandlerMoney;
	}

	public Runnable getmUpdateMoney() {
		return mUpdateMoney;
	}

	public void setmUpdateMoney(Runnable mUpdateMoney) {
		this.mUpdateMoney = mUpdateMoney;
	}
}
