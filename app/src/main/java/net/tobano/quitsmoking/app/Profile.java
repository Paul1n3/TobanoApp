package net.tobano.quitsmoking.app;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import net.tobano.quitsmoking.app.util.Theme;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nicolas Lett
 * 
 */
public class Profile extends Fragment {

	private View view;
	private View wallpaper;
	private TextView tvLevel;
	private TextView tvRankValue;
	private ImageView imgRank;
	private LinearLayout lCircle;
	private ProgressBar pbLevelHealth;
	private ProgressBar pbLevelWellness;
	private ProgressBar pbLevelTime;
	private ProgressBar pbLevelMoney;
	private ProgressBar pbLevelCigarettes;
	private ProgressBar pbLevelLife;
	private ProgressBar pbLevelCo;
	private ProgressBar pbLevelWillpower;

	String user_level_health = Start.user_level_health;
	String user_level_wellness = Start.user_level_wellness;
	String user_level_time = Start.user_level_time;
	String user_level_money = Start.user_level_money;
	String user_level_cigarette = Start.user_level_cigarette;
	String user_level_life = Start.user_level_life;
	String user_level_co = Start.user_level_co;
	String user_level_willpower = Start.user_level_willpower;

	private FloatingActionButton btnMotivCard;
	private FloatingActionButton btnShare;
	private RelativeLayout lProfile;

	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.profile, container, false);
		lProfile = (RelativeLayout) view.findViewById(R.id.profile);

		mHandler = new Handler();
		mHandler.post(mUpdate);

		wallpaper = (View) view.findViewById(R.id.wallpaper);

		pbLevelHealth = (ProgressBar) view.findViewById(R.id.pbhelth);
		pbLevelHealth.setMax(12);
		pbLevelWellness = (ProgressBar) view.findViewById(R.id.pbwellness);
		pbLevelWellness.setMax(12);
		pbLevelTime = (ProgressBar) view.findViewById(R.id.pbtime);
		pbLevelTime.setMax(12);
		pbLevelMoney = (ProgressBar) view.findViewById(R.id.pbmoney);
		pbLevelMoney.setMax(12);
		pbLevelCigarettes = (ProgressBar) view.findViewById(R.id.pbcigarette);
		pbLevelCigarettes.setMax(12);
		pbLevelLife = (ProgressBar) view.findViewById(R.id.pblife);
		pbLevelLife.setMax(12);
		pbLevelCo = (ProgressBar) view.findViewById(R.id.pbco);
		pbLevelCo.setMax(12);
		pbLevelWillpower = (ProgressBar) view.findViewById(R.id.pbwillpower);
		pbLevelWillpower.setMax(12);

		lCircle = (LinearLayout) view.findViewById(R.id.circle);

		TextView tvLife = (TextView) view.findViewById(R.id.tvLife);
		TextView tvCo = (TextView) view.findViewById(R.id.tvCo);
		TextView tvWillpower = (TextView) view.findViewById(R.id.tvWillpower);
//		if(!Start.isPremium){
//			tvLife.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//			tvCo.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//			tvWillpower.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//			pbLevelLife.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//			pbLevelCo.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//			pbLevelWillpower.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					Start.displayUnlockFullVersionDialog();
//				}
//			});
//		}
//		else{
			int black = ContextCompat.getColor(getActivity(), R.color.black);
			tvLife.setTextColor(black);
			tvCo.setTextColor(black);
			tvWillpower.setTextColor(black);
			updateWillpowerProgressDrawableColor();
//		}

		btnMotivCard = (FloatingActionButton) view.findViewById(R.id.btnMotivCard);
		btnMotivCard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Start)getActivity()).playSound(Start.strClick);

				((Start)getActivity()).displayMotivationCard();
//				MotivationCardsDialog dialog = new MotivationCardsDialog(
//						getActivity());
//				dialog.displayMotivationCards().show();
				Map<String, String> drawMotivCardParams = new HashMap<String, String>();
				drawMotivCardParams.put("source", "profile_button");

				FlurryAgent.logEvent("Draw_motivation_card",
						drawMotivCardParams, false);
			}
		});

		btnShare = (FloatingActionButton) view.findViewById(R.id.btnShare);
		btnShare.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Start)getActivity()).playSound(Start.strClick);
				((Start)getActivity()).shareScreenShot(lProfile, "kwit_profile");
				FlurryAgent.logEvent("Share_button_profile");
			}
		});

		imgRank = (ImageView) view.findViewById(R.id.imgRank);

		tvLevel = (TextView) view.findViewById(R.id.tvLevel);
		tvRankValue = (TextView) view.findViewById(R.id.tvRank);

		setTheme();

		refreshUserInterface();

		return view;
	}

	private void setTheme() {
		if (Start.theme == Theme.GREEN){
			tvLevel.setTextColor(ContextCompat.getColor(getActivity(), R.color.kwit));
			updateBtnMotivationCardTheme(ContextCompat.getColor(getActivity(), R.color.kwit_dark));
			updateBtnShare(ContextCompat.getColorStateList(getActivity(), R.color.kwit_dark));
			updateWallpaper(ContextCompat.getDrawable(getActivity(), R.drawable.wallpaper_green));
			lCircle.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_green));
			pbLevelHealth.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
			pbLevelMoney.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
			pbLevelCigarettes.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
			pbLevelTime.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
			pbLevelWellness.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            //if (Start.isPremium) {
				pbLevelLife.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
				pbLevelCo.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            //}
		}
		else {
			tvLevel.setTextColor(ContextCompat.getColor(getActivity(), R.color.tabano));
			updateBtnMotivationCardTheme(ContextCompat.getColor(getActivity(), R.color.primary_dark));
			updateBtnShare(ContextCompat.getColorStateList(getActivity(), R.color.primary_dark));
			updateWallpaper(ContextCompat.getDrawable(getActivity(), R.drawable.wallpaper));
			lCircle.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_blue));
			pbLevelHealth.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
			pbLevelMoney.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
			pbLevelCigarettes.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
			pbLevelTime.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
			pbLevelWellness.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
//			if (Start.isPremium) {
				pbLevelLife.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
				pbLevelCo.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
//			}
		}
	}

	private void updateWallpaper(Drawable drawable) {
		try {
			//wallpaper.setBackground(drawable);
		}
		catch (Exception e){
			//
		}
	}

	private void updateBtnMotivationCardTheme(int color){
		Drawable dMotivationCard = getResources().getDrawable(R.drawable.button_draw_card);
		Drawable willBeGreen = dMotivationCard.getConstantState().newDrawable();
		willBeGreen.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
		btnMotivCard.setImageDrawable(willBeGreen);
	}

	private void updateBtnShare(ColorStateList color){
		try {
			btnShare.setBackgroundTintList(color);
		}
		catch (Exception e){
			//
		}
	}

	protected Runnable mUpdate = new Runnable() {
		public void run() {
			refreshUserInterface();
			// mHandler.postDelayed(this, 1000);
		}
	};

	public void refreshUserInterface() {
		if (isAdded()) {
			Start.userXP = Util.getUserXP();
			Start.user_level = Util.getUserLevel(Start.userXP);
			Start.user_rank_value = Util.getUserRankValue(Integer
					.parseInt(Start.user_level));

			Start.user_rank_text = Util.getUserRankText(
					Integer.parseInt(Start.user_rank_value),
					((Start) getActivity()).getApplicationContext());

			user_level_health = Start.user_level_health;
			user_level_wellness = Start.user_level_wellness;
			user_level_time = Start.user_level_time;
			user_level_money = Start.user_level_money;
			user_level_cigarette = Start.user_level_cigarette;
			user_level_life = Start.user_level_life;
			user_level_co = Start.user_level_co;
			user_level_willpower = Start.user_level_willpower;

			tvLevel.setText(Start.user_level);
			tvRankValue.setText(Start.user_rank_text);
			int rank = Integer.parseInt(Start.user_rank_value);

			String imageName = getImageForRank(rank);

			int img = this.getResources().getIdentifier(imageName, "drawable",
					this.getActivity().getPackageName());
			imgRank.setImageResource(img);

			pbLevelCigarettes.setProgress(Integer
					.parseInt(user_level_cigarette));
			pbLevelCo.setProgress(Integer.parseInt(user_level_co));
			pbLevelHealth.setProgress(Integer.parseInt(user_level_health));
			pbLevelLife.setProgress(Integer.parseInt(user_level_life));
			pbLevelMoney.setProgress(Integer.parseInt(user_level_money));
			pbLevelTime.setProgress(Integer.parseInt(user_level_time));
			pbLevelWellness.setProgress(Integer.parseInt(user_level_wellness));

			try {
				ViewParent parent = pbLevelWillpower.getParent();
				((ViewManager) parent).removeView(pbLevelWillpower);
				LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				((ViewManager) parent).addView(pbLevelWillpower, parameter);
			} catch (Exception e) {
			}
			if(Start.isPremium) {
				updateWillpowerProgressDrawableColor();
			}
			pbLevelWillpower
					.setProgress(Integer.parseInt(user_level_willpower));
		}
	}

	public void updateWillpowerProgressDrawableColor() {
		if (getActivity() != null && isAdded()) {
			if (Integer.parseInt(user_level_willpower) <= 5) {
				pbLevelWillpower.setProgressDrawable(getResources()
						.getDrawable(R.drawable.progressbar_orange));
			} else {
				if (Start.theme == Theme.GREEN){
					pbLevelWillpower.setProgressDrawable(getResources()
							.getDrawable(R.drawable.progressbar_green));
				}
				else {
					pbLevelWillpower.setProgressDrawable(getResources()
							.getDrawable(R.drawable.progressbar_blue));
				}
			}
		}
	}

	public void onResume() {
		super.onResume();
	}

	public static String getImageForRank(int rank) {
		String result;
		switch (rank) {
		case 1:
			result = "rank1";
			break;
		case 2:
			result = "rank2";
			break;
		case 3:
			result = "rank3";
			break;
		case 4:
			result = "rank4";
			break;
		case 5:
			result = "rank5";
			break;
		case 6:
			result = "rank6";
			break;
		case 7:
			result = "rank7";
			break;
		case 8:
			result = "rank8";
			break;
		case 9:
			result = "rank9";
			break;
		case 10:
			result = "rank10";
			break;
		case 11:
			result = "rank11";
			break;
		case 12:
			result = "rank12";
			break;
		default:
			result = "rank1";
			break;
		}
		return result;
	}

	private String getRealPathFromURI(Uri contentURI) {
		Cursor cursor = getContext().getContentResolver().query(contentURI, null, null, null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file path
			return contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			return cursor.getString(idx);
		}
	}
}