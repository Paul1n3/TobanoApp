package net.tobano.quitsmoking.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import net.tobano.quitsmoking.app.util.Theme;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressLint({ "NewApi", "DefaultLocale" })
public class Achievements extends Fragment implements OnChildClickListener {

	private Handler mHandler;

	private View wallpaper;
	private LinearLayout circle;
	private TextView tvLevel;
	private TextView tvRankValue;

	public final static String imgBadgeKey = "imgBadge";

	private ExpandableListView expandableListView;

	private ArrayList result;

	private Dialog dialog;

	private int groupposition;

	private int childposition;

	private int currentDialogBadgeState;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private Button dialogButton;

	LayoutInflater layoutInflater = null;

	private int numberOfUnlockable;
	private FloatingActionButton unlockAllButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.achievements, container, false);

		prefs = Start.prefs;
		editor = Start.editor;

		wallpaper = v.findViewById(R.id.wallpaper);
		circle = v.findViewById(R.id.circle);
	 	tvLevel = v.findViewById(R.id.tvLevel);
		tvRankValue = v.findViewById(R.id.tvRank);
		updateLevelTheme();

		expandableListView = v.findViewById(R.id.elv);

		unlockAllButton = v.findViewById(R.id.open_lock);
		numberOfUnlockable = 0;

		try {
			refreshLevel();
			refreshUserInterface();

			// used to remove the black color when scrolling the list view
			expandableListView.setCacheColorHint(0);

			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				expandableListView.setIndicatorBounds(15, 65);
			} else {
				//expandableListView.setIndicatorBoundsRelative(15, 69);
				expandableListView.setIndicatorBoundsRelative(GetPixelFromDips(5), GetPixelFromDips(39));
			}
		} catch (Exception e) {
			// System.out.println("refreshUserInterface : " + e);
		}

		return v;
	}

	private void updateLevelTheme() {
		Drawable drawableWallpaper;
		Drawable drawableCircle;
		if (Start.theme == Theme.GREEN){
			drawableWallpaper = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_kwit_small);
			drawableCircle = ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_green);
		}
		else {
			drawableWallpaper = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_tabano_small);
			drawableCircle = ContextCompat.getDrawable(getActivity(), R.drawable.shape_circle_blue);
		}
		wallpaper.setBackground(drawableWallpaper);
		circle.setBackground(drawableCircle);
	}

	private void setFloatingBtnTheme(){
		ColorStateList color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColorStateList(getActivity(), R.color.kwit_dark);
		}
		else {
			color = ContextCompat.getColorStateList(getActivity(), R.color.primary_dark);
		}
		try {
			unlockAllButton.setBackgroundTintList(color);
		}
		catch (Exception e){
			//
		}
	}

	public int GetPixelFromDips(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	// TODO modifier cette méthode pour ne pas avoir à recréer l'adapter à
	// chaque fois, mais le mettre à jour avec les nouvelles données
	public void refreshUserInterface() {
		if(getActivity()== null){
			return;
		}

		layoutInflater = (LayoutInflater) this
				.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// refresh list
		expandableListView.setAdapter(new SimpleExpandableListAdapter(
				this.getActivity(),
				createGroupList(Util.categoryResources), // generating the group list
				R.layout.group_layout, // group item layout xml
				new String[] { "Group Item" }, // key of group item
				new int[] { R.id.categoryName }, // id of each group item. Data
				// under the key goes into this TextView.

				createChildList(Util.achievementsCategories,
						Util.healthAchievements, Util.wellnessAchievements,
						Util.timeAchievements, Util.moneyAchievements,
						Util.cigarettesAchievements, Util.lifeAchievements,
						Util.coAchievements),
						// childData describes second-level entries.
				0, null, new int[] {}) {
			@SuppressWarnings("unchecked")
			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				final View v = super.getChildView(groupPosition, childPosition,
						isLastChild, convertView, parent);

				// Populate your custom view here
				// / level X
				((TextView) v.findViewById(R.id.tvTitle))
						.setText((String) ((Map<String, Object>) getChild(
								groupPosition, childPosition)).get("tvTitle"));
				// / text
				String text = (String) ((Map<String, Object>) getChild(
						groupPosition, childPosition)).get("tvGroupChild");
				((TextView) v.findViewById(R.id.tvGroupChild)).setText(text);
				// System.out.println("-" + text + "-");
				if (Arrays.asList(Util.healthAchievements).contains(text)
						|| Arrays.asList(Util.wellnessAchievements).contains(
								text)
						|| Arrays.asList(Util.timeAchievements).contains(text)
						|| Arrays.asList(Util.moneyAchievements).contains(text)
						|| Arrays.asList(Util.cigarettesAchievements).contains(
								text)
						|| Arrays.asList(Util.lifeAchievements).contains(text)
						|| Arrays.asList(Util.coAchievements).contains(text)) {
					((TextView) v.findViewById(R.id.tvGroupChild))
							.setTextColor(getActivity().getResources()
									.getColor(R.color.black));
				} else {
					((TextView) v.findViewById(R.id.tvGroupChild))
							.setTextColor(getActivity().getResources()
									.getColor(R.color.gray_splash_subtitle));
				}
				// / icon
				((ImageView) v.findViewById(R.id.imgBadge))
						.setImageDrawable((Drawable) ((Map<String, Object>) getChild(
								groupPosition, childPosition)).get(imgBadgeKey));

				return v;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				View v = super.getGroupView(groupPosition, isExpanded,
						convertView, parent);

				// Populate your custom view here
				TextView tvCategoryName = ((TextView) v
						.findViewById(R.id.categoryName));
				TextView tvAchievementsAvailableForUnlock = ((TextView) v
						.findViewById(R.id.achievementsAvailableForUnlock));
				ImageView ivUnlockTile = ((ImageView) v.findViewById(R.id.unlock_tile));
				if(!Start.isPremium && (groupPosition == 5 || groupPosition == 6)) {
					v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.gray_free_version_achievements));
					ivUnlockTile.setVisibility(View.VISIBLE);
					tvCategoryName.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.gray_free_version_achievements));
					tvCategoryName.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_free_version_text));
					tvAchievementsAvailableForUnlock.setVisibility(View.GONE);
				}
				else{
					v.setBackgroundColor(Color.WHITE);
					ivUnlockTile.setVisibility(View.GONE);
					tvCategoryName.setTextColor(getColorTheme());
					tvCategoryName.setBackgroundColor(Color.WHITE);
					tvAchievementsAvailableForUnlock.setVisibility(View.VISIBLE);

					String nbOfLevelAvailableForUnlock = Util
							.getNbOfLevelAvailableForUnlockByCategory(Util.achievementsCategories
									.get(groupPosition));

					if (Integer.valueOf(nbOfLevelAvailableForUnlock) > 0) {
						tvAchievementsAvailableForUnlock
								.setText(nbOfLevelAvailableForUnlock);
					} else {
						tvAchievementsAvailableForUnlock.setText("");
					}
				}

				return v;
			}

			@Override
			public View newChildView(boolean isLastChild, ViewGroup parent) {
				return layoutInflater.inflate(R.layout.child_layout, null,
						false);
			}
		});
		expandableListView.setOnChildClickListener(this);

		expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				((Start)getActivity()).playSound(Start.strClick);
//				if(!Start.isPremium) {
//					if (groupPosition == 5) { // life clicked detected
//						Start.displayUnlockFullVersionDialog();
//						return true;
//					} else if (groupPosition == 6) { // co clicked detected
//						Start.displayUnlockFullVersionDialog();
//						return true;
//					} else
//						return false;
//				}
				return false;
			}
		});
	}

	private void refreshLevel() {
		Start.userXP = Util.getUserXP();
		Start.user_level = Util.getUserLevel(Start.userXP);
		Start.user_rank_value = Util.getUserRankValue(Integer
				.parseInt(Start.user_level));
		Start.user_rank_text = Util.getUserRankText(
				Integer.parseInt(Start.user_rank_value),
				((Start) getActivity()).getApplicationContext());

		tvLevel.setText(Start.user_level);
		tvRankValue.setText(Start.user_rank_text);
	}

	// creating the Hashmap for the row
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List createGroupList(int[] tab) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < tab.length; ++i) { // 5 groups
			HashMap m = new HashMap();
			// put in the key and it's value.
			m.put("Group Item", getResources().getString(tab[i]));
			result.add(m);
		}
		return (List) result;
	}

	// creating the HashMap for the children
	@SuppressWarnings("unchecked")
	private List createChildList(ArrayList<String> achievementsCategories,
			String[] healthAchievements, String[] wellnessAchievements,
			String[] timeAchievements, String[] moneyAchievements,
			String[] cigarettesAchievements, String[] lifeAchievements,
			String[] coAchievements) {
		final ArrayList<ArrayList<HashMap<String, Object>>> childData = new ArrayList<ArrayList<HashMap<String, Object>>>();
		final ArrayList<HashMap<String, Object>> group1data = new ArrayList<HashMap<String, Object>>();
		childData.add(group1data);

		final ArrayList<HashMap<String, Object>> group2data = new ArrayList<HashMap<String, Object>>();
		childData.add(group2data);

		final ArrayList<HashMap<String, Object>> group3data = new ArrayList<HashMap<String, Object>>();
		childData.add(group3data);

		final ArrayList<HashMap<String, Object>> group4data = new ArrayList<HashMap<String, Object>>();
		childData.add(group4data);

		final ArrayList<HashMap<String, Object>> group5data = new ArrayList<HashMap<String, Object>>();
		childData.add(group5data);

		numberOfUnlockable = 0;

		result = new ArrayList();
		for (int categoryIndex = 0; categoryIndex < achievementsCategories
				.size(); ++categoryIndex) {
			// this is the number of groups (7 here)
			// each group needs each HashMap.
			// Here for each group we have 12 subgroups
			ArrayList secList = new ArrayList();

			// adding the badges for each level of the different categories
			for (int levelIndex = 0; levelIndex < healthAchievements.length; levelIndex++) {
				final HashMap<String, Object> child = new HashMap<String, Object>();
				// Set the title of the listview childs
				child.put("tvTitle", Achievements.this.getResources()
						.getString(R.string.Level) + " " + (levelIndex + 1));

				// By default, badge state is 1
				// (0: locked, 1: toUnlock, 2: unlocked
				int badgeState;
				switch (categoryIndex) {
					// Set the subtitles of the listview childs
					case 0: // category health
						if (levelIndex < Integer
								.parseInt(Start.level_health_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(0)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2) {
								child.put("tvGroupChild",
										healthAchievements[levelIndex]);
								Drawable drawable = getDrawableWithTheme(getResources().getDrawable(R.drawable.health_unlocked));
								child.put(imgBadgeKey,drawable);
							} else {
								// System.out.println("setToUnlock called for catId="
								// + categoryIndex + " levelIndex="
								// + levelIndex);
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.health_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.health_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.HEALTH, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
					case 1:
						child.put("tvGroupChild", wellnessAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_wellness_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(1)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.wellness_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.wellness_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.wellness_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.WELLBEING, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
					case 2:
						child.put("tvGroupChild", timeAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_time_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(2)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.time_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.time_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.time_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.TIME, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
					case 3:
						child.put("tvGroupChild", moneyAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_money_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(3)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.money_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.money_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.money_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.MONEY, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
					case 4:
						child.put("tvGroupChild",
								cigarettesAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_cigarette_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(4)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.cigarette_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.cigarette_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.cigarette_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.CIGARETTES, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
					case 5:
						child.put("tvGroupChild", lifeAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_life_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(5)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.life_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.life_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(
									imgBadgeKey,
									getResources().getDrawable(
											R.drawable.life_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.LIFE, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;

					case 6:
						child.put("tvGroupChild", coAchievements[levelIndex]);
						if (levelIndex < Integer
								.parseInt(Start.level_co_available_for_unlock)) {
							badgeState = prefs.getInt(
									Util.achievementsCategories.get(6)
											+ (levelIndex + 1), 1);
							// Test if badge already unlocked
							if (badgeState == 2)
								child.put(imgBadgeKey,getDrawableWithTheme(getResources().getDrawable(R.drawable.co_unlocked)));
							else {
								Util.setToUnlock(categoryIndex, levelIndex + 1);
								child.put(
										imgBadgeKey,
										getResources().getDrawable(
												R.drawable.co_tounlock));
								child.put(
										"tvGroupChild",
										getResources().getString(
												R.string.readyToUnlock));
								++numberOfUnlockable;
							}
						} else {
							child.put(imgBadgeKey,
									getResources()
											.getDrawable(R.drawable.co_locked));
							String unlockedIn = calculateDaysRemaining(
									Util.CATEGORIES.CO, levelIndex + 1);
							child.put("tvGroupChild", unlockedIn);
						}
						break;
				}

				secList.add(child);
			}
			result.add(secList);
		}

		if (numberOfUnlockable > 5) {
			manageButtonUnlockAll(true);
		}
		else {
			manageButtonUnlockAll(false);
		}

		return result;
	}

	private Drawable getDrawableWithTheme(Drawable drawable) {
		Drawable willBe = drawable.getConstantState().newDrawable();
		willBe.mutate().setColorFilter(getColorTheme(), PorterDuff.Mode.SRC_IN);
		return willBe;
	}

	private int getColorTheme() {
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(getActivity(), R.color.kwit_achievements);
		}
		else {
			color = ContextCompat.getColor(getActivity(), R.color.color_tabano);
		}
		return color;
	}

	private void manageButtonUnlockAll(Boolean needToDisplay) {
		if (needToDisplay){
			unlockAllButton.setVisibility(View.VISIBLE);
			setFloatingBtnTheme();

			unlockAllButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((Start)getActivity()).playSound(Start.strClick);

					final AlertDialog.Builder builder;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						builder = new AlertDialog.Builder(Start.context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
					} else {
						builder = new AlertDialog.Builder(Start.context);
					}
					builder
							.setMessage(getResources().getString(R.string.dialogUnlockAllMessage))
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									unlockAll();
									refreshUserInterface();
									refreshLevel();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// do nothing
								}
							});
					builder.show();
				}
			});
		}
		else {
			unlockAllButton.setVisibility(View.GONE);
		}
	}

	private void unlockAll() {
		// for each categories
		for (int categoryIndex = 0; categoryIndex < Util.achievementsCategories.size(); ++categoryIndex) {
			// for each level of one category
			for (int levelIndex = 0; levelIndex < Util.healthAchievements.length; ++levelIndex) {
				int state = prefs.getInt(Util.achievementsCategories.get(categoryIndex)+ (levelIndex + 1), 0);
				// if state is unlockable (=1)
				if (state == 1){
					logInFlurry("Achievement_unlocked_all", categoryIndex, levelIndex);
					saveUnlockStatusInPreference(categoryIndex, levelIndex);
				}
			}
		}
		refreshUserInterface();
		refreshLevel();
	}

	private String calculateDaysRemaining(Util.CATEGORIES category, int level) {
		// Less than a day
		long timeRemaining = timeRemainingByCategoryAndLevel(category, level);
		if (timeRemaining < 86400) {
			return getResources().getString(R.string.unlockedInLessThanOneDay);
		} else {
			// 86400 added to add one day "unlocked in less than : e.g. 12 days
			// for 11 days 11 hours, 11 min and 11 sec
			return getResources().getString(R.string.unlockedIn) + " "
					+ (int) TimeUnit.SECONDS.toDays(timeRemaining + 86400)
					+ " " + getResources().getString(R.string.days) + ".";
		}
	}

	// This function is called on each child click
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		((Start)getActivity()).playSound(Start.strClick);

		mHandler = new Handler();
		mHandler.post(mUpdate);

		groupposition = groupPosition;
		childposition = childPosition;
		createAndShowDialog();

		return true;
	}

	private void createAndShowDialog() {
		// custom dialog
		dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);
		dialog.getWindow();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.achievement_dialog);
		dialog.setCancelable(true);

		final Start s = ((Start) getActivity());

		// Get badge state from prefs (0: blocked, 1: tounlock, 2: unlocked)
		currentDialogBadgeState = prefs.getInt(
				Util.achievementsCategories.get(groupposition)
						+ (childposition + 1), 0);
		dialogButton = (Button) dialog.findViewById(R.id.btnOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentDialogBadgeState == 1) { // dialog is orange
					s.playSound(Start.strUnlock);
//					if(childposition > 2) { // do not display ads for first 3 levels
//						Ads.showAds();
//					}

					logInFlurry("Achievement_unlocked", groupposition, childposition);
					// save unlock status in prefs
					saveUnlockStatusInPreference(groupposition, childposition);

					dialog.dismiss();
					currentDialogBadgeState = 2;
					refreshUserInterface();
					refreshLevel();
					expandableListView.expandGroup(groupposition);
					updateDialog();
					dialog.dismiss();
					createAndShowDialog();
					s.updateWidget();

				} else if (currentDialogBadgeState == 0
						|| currentDialogBadgeState == 2) {
					if(currentDialogBadgeState == 0){ // dialog is red
						s.playSound(Start.strStillLock);
					}
					else{ // dialog is green
						s.playSound(Start.strHideUnlocked);
					}
					dialog.dismiss();
				}

				mHandler.removeCallbacks(mUpdate);
			}
		});

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				refreshUserInterface();
				refreshLevel();
				expandableListView.expandGroup(groupposition);
			}
		});

		ImageView shareButton = (ImageView) dialog.findViewById(R.id.imgShareIcon);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				s.playSound(Start.strClick);

				// create bitmap screen capture
				Bitmap bitmap;
				View v1 = dialog.getWindow().getDecorView().getRootView();
				v1.setDrawingCacheEnabled(true);
				bitmap = Bitmap.createBitmap(v1.getDrawingCache());
				v1.setDrawingCacheEnabled(false);

				HashMap<String, Object> hm = ((ArrayList<HashMap<String, Object>>) result
						.get(groupposition)).get(childposition);

				String shareMessage;
				shareMessage = getResources().getString(R.string.Achievement);
				shareMessage += " "
						+ getResources().getString(
								Util.categoryResources[groupposition]) + " "
						+ (CharSequence) hm.get("tvTitle");
				shareMessage += " "
						+ getResources().getString(R.string.unlocked) + " : ";
				shareMessage += " " + hm.get("tvGroupChild");

				Intent intent = Util.sharePicture(getActivity().getBaseContext(), bitmap, "kwit_achievement", shareMessage, getResources().getString(R.string.share));

				if(intent != null){
					startActivity(Intent.createChooser(intent, getString(R.string.share)));
				}

				logInFlurry("Share_button_achievements", groupposition, childposition);
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		updateDialog();
		dialog.show();
		dialog.getWindow().setAttributes(lp);

	}

	private void saveUnlockStatusInPreference(int categoryIndex, int levelIndex) {
		editor.putInt(
			Util.achievementsCategories.get(categoryIndex) + (levelIndex + 1), 2);
		editor.commit();
		updateUserLevel(Util.achievementsCategories
				.get(categoryIndex) + (levelIndex + 1));
	}

	private void logInFlurry(String tagFlurry, int categoryIndex, int levelIndex) {
		Map<String, String> shareParams = new HashMap<String, String>();
		shareParams.put("category",
				getStringEnglish( Util.categoryResources[categoryIndex]));
		shareParams.put("level", String.valueOf(levelIndex+1));

		FlurryAgent.logEvent(tagFlurry, shareParams, false);
	}

	private String getStringEnglish(int id) {
		Resources res = getResources();
		Configuration conf = res.getConfiguration();
		Locale savedLocale = conf.locale;
		Locale enLocale = Locale.US;
		conf.locale = enLocale;
		res.updateConfiguration(conf, null);

		// retrieve resources from desired locale
		String str = res.getString(id);

		// restore original locale
		conf.locale = savedLocale;
		res.updateConfiguration(conf, null);

		return str;
	}

	private void updateUserLevel(String category) {
		if (category.contains(Util.badgeHealthKey)) {
			Start.user_level_health = String.valueOf(Integer
					.valueOf(Start.user_level_health) + 1);
		} else if (category.contains(Util.badgeWellnessKey)) {
			Start.user_level_wellness = String.valueOf(Integer
					.valueOf(Start.user_level_wellness) + 1);
		} else if (category.contains(Util.badgeTimeKey)) {
			Start.user_level_time = String.valueOf(Integer
					.valueOf(Start.user_level_time) + 1);
		} else if (category.contains(Util.badgeMoneyKey)) {
			Start.user_level_money = String.valueOf(Integer
					.valueOf(Start.user_level_money) + 1);
		} else if (category.contains(Util.badgeCigaretteKey)) {
			Start.user_level_cigarette = String.valueOf(Integer
					.valueOf(Start.user_level_cigarette) + 1);
		} else if (category.contains(Util.badgeCoKey)) {
			Start.user_level_co = String.valueOf(Integer
					.valueOf(Start.user_level_co) + 1);
		} else if (category.contains(Util.badgeLifeKey)) {
			Start.user_level_life = String.valueOf(Integer
					.valueOf(Start.user_level_life) + 1);
		}
	}

	protected Runnable mUpdate = new Runnable() {
		public void run() {
			updateDialog();
			mHandler.postDelayed(this, 1000);
		}
	};

	public Runnable getmUpdate() {
		return mUpdate;
	}

	public void setmUpdate(Runnable mUpdate) {
		this.mUpdate = mUpdate;
	}

	@SuppressLint("DefaultLocale")
	@SuppressWarnings("unchecked")
	private void updateDialog() {
		if (getActivity() != null && isAdded()) {
			refreshLevel();
			// set the custom dialog components - text, image and button
			LinearLayout header = (LinearLayout) dialog.findViewById(R.id.header);
			TextView achievementSate = (TextView) dialog.findViewById(R.id.tvStatusText);
			ImageView imageLevelCigarette = (ImageView) dialog.findViewById(R.id.imgLevelCigarettes);

			LinearLayout llTimeLeft = (LinearLayout) dialog.findViewById(R.id.llTimeLeft);

			LinearLayout llTimeOfUnlock = (LinearLayout) dialog.findViewById(R.id.llTimeOfUnlock);
			TextView timeOfUnlock = (TextView) dialog.findViewById(R.id.timeOfUnlock);

			TextView badgeTitle = (TextView) dialog.findViewById(R.id.tvBadgeTitle);
			TextView badgeText = (TextView) dialog.findViewById(R.id.tvBadgeText);

			TextView daysLabel = (TextView) dialog.findViewById(R.id.tvQuitterSinceDaysLabel);
			daysLabel.setTextColor(getColorTheme());
			TextView days = (TextView) dialog.findViewById(R.id.daysremaining);
			days.setTextColor(getColorTheme());
			TextView hoursLabel = (TextView) dialog.findViewById(R.id.tvQuitterSinceHoursLabel);
			hoursLabel.setTextColor(getColorTheme());
			TextView hours = (TextView) dialog.findViewById(R.id.hoursremaining);
			hours.setTextColor(getColorTheme());
			TextView semiColon1 = (TextView) dialog.findViewById(R.id.firstColonSeparator);
			semiColon1.setTextColor(getColorTheme());
			TextView minutesLabel = (TextView) dialog.findViewById(R.id.tvQuitterSinceMinutesLabel);
			minutesLabel.setTextColor(getColorTheme());
			TextView minutes = (TextView) dialog.findViewById(R.id.minutesremaining);
			minutes.setTextColor(getColorTheme());
			TextView semiColon2 = (TextView) dialog.findViewById(R.id.secondColonSeparator);
			semiColon2.setTextColor(getColorTheme());
			TextView secondesLabel = (TextView) dialog.findViewById(R.id.tvQuitterSinceSecondsLabel);
			secondesLabel.setTextColor(getColorTheme());
			TextView secondes = (TextView) dialog.findViewById(R.id.secondesremaining);
			secondes.setTextColor(getColorTheme());

			ImageView imageShareIcon = (ImageView) dialog.findViewById(R.id.imgShareIcon);

			// fill dialog
			// / badge selected
			HashMap<String, Object> hm = ((ArrayList<HashMap<String, Object>>) result
					.get(groupposition)).get(childposition);

			Drawable imgBadge = getImgBadgeToDisplay(
					Util.achievementsCategories.get(groupposition),
					childposition);

			// if achievement is blocked
			if (currentDialogBadgeState == 0) {
				achievementSate.setText(R.string.LOCKED);
				header.setBackgroundColor(getResources().getColor(
						R.color.gray_achievements));
				dialogButton.setTextColor(getResources().getColor(
						R.color.gray_achievements));
				dialogButton.setText(R.string.btnOK);
				badgeText.setVisibility(View.GONE);
				imageShareIcon.setVisibility(View.GONE);
				llTimeOfUnlock.setVisibility(View.GONE);
				llTimeLeft.setVisibility(View.VISIBLE);
			}
			else if (currentDialogBadgeState == 1) { // badge tounlock
				achievementSate.setText(R.string.TO_UNLOCK);
				header.setBackgroundColor(getResources().getColor(
						R.color.orange_achievements));
				dialogButton.setTextColor(getResources().getColor(
						R.color.orange_achievements));
				dialogButton.setText(R.string.UNLOCK);
				llTimeOfUnlock.setVisibility(View.VISIBLE);
				DateFormat df = android.text.format.DateFormat
						.getDateFormat(getActivity().getApplicationContext());
				String dateFormatted = df.format(Util
						.getDateOfAchievementUnlockByCategoryAndLevel(
								groupposition, childposition + 1));
				Calendar cal = Calendar.getInstance();
				cal.setTime(Util.getDateOfAchievementUnlockByCategoryAndLevel(
						groupposition, childposition + 1));
				dateFormatted = DateUtils.formatDateTime(getActivity()
						.getApplicationContext(), cal.getTimeInMillis(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
								| DateUtils.FORMAT_SHOW_TIME);
				timeOfUnlock.setText(getResources().getString(
						R.string.unlockedOn)
						+ " " + dateFormatted);
				badgeText.setVisibility(View.GONE);
				imageShareIcon.setVisibility(View.GONE);
				llTimeLeft.setVisibility(View.GONE);
			} else { // badge unlocked
				achievementSate.setText(R.string.UNLOCKED);
				header.setBackgroundColor(getColorTheme());
				dialogButton.setTextColor(getColorTheme());
				dialogButton.setText(R.string.btnOK);
				badgeText.setVisibility(View.VISIBLE);
				imageShareIcon.setVisibility(View.GONE);
				llTimeOfUnlock.setVisibility(View.GONE);
				llTimeLeft.setVisibility(View.GONE);
			}
			imageLevelCigarette.setImageDrawable(imgBadge);
			String levelName = getResources().getString(
					Util.categoryResources[groupposition]);
			String levelLabel = (String) hm.get("tvTitle");
			String bttext = levelName.substring(0, 1).toUpperCase()
					+ levelName.substring(1).toLowerCase() + " "
					+ levelLabel.toLowerCase();

			badgeTitle.setText(bttext);
			badgeText.setText((CharSequence) hm.get("tvGroupChild"));

			long timeleft = calculateTimeLeft();

			llTimeLeft.setVisibility(View.GONE);
			if (timeleft <= 0) {

				if (currentDialogBadgeState == 0) {
					editor.putInt(
							Util.achievementsCategories.get(groupposition)
									+ (childposition + 1), 1);
					editor.commit();

					currentDialogBadgeState = 1;
					refreshUserInterface();
					refreshLevel();
					expandableListView.expandGroup(groupposition);

					if (dialog.isShowing()) {
						dialog.dismiss();
						createAndShowDialog();
					}

				}

			} else {
				if (currentDialogBadgeState == 0) {
					llTimeLeft.setVisibility(View.VISIBLE);

					int d = (int) timeleft / 86400;
					if (d == 0) {
						days.setText(R.string.threeZeroDigit);
					} else {
						String strDays = String.valueOf(d);
						while (strDays.length() < 3) {
							strDays = "0" + strDays;
						}
						days.setText(strDays);
					}
					int h = (int) ((timeleft - d * 86400) / 3600);
					if (h == 0) {
						hours.setText(R.string.twoZeroDigit);
					} else {
						String strHours = String.valueOf(h);
						while (strHours.length() < 2) {
							strHours = "0" + strHours;
						}
						hours.setText(strHours);
					}
					int m = (int) ((timeleft - d * 86400 - h * 3600) / 60);
					if (m == 0) {
						minutes.setText(R.string.twoZeroDigit);
					} else {
						String strMinutes = String.valueOf(m);
						while (strMinutes.length() < 2) {
							strMinutes = "0" + strMinutes;
						}
						minutes.setText(strMinutes);
					}
					int s = (int) (timeleft - d * 86400 - h * 3600 - m * 60);
					if (s == 0) {
						secondes.setText(R.string.twoZeroDigit);
					} else {
						String strSecondes = String.valueOf(s);
						while (strSecondes.length() < 2) {
							strSecondes = "0" + strSecondes;
						}
						secondes.setText(strSecondes);
					}
				}
			}
		}
	}

	private Drawable getImgBadgeToDisplay(String categoryName, int position) {
		String uri = "";
		prefs = Start.prefs;

		int state = prefs.getInt(categoryName + (position + 1), 0);

		switch (state) {
		case 0:
			uri += categoryName + "_locked";
			break;
		case 1:
			uri += categoryName + "_tounlock";
			break;
		default:
			uri += categoryName + "_unlocked";
			break;
		}

		Drawable drawable = getResources().getDrawable(
				getResources().getIdentifier(uri, "drawable",
						getActivity().getPackageName()));

		if (state > 1){
			drawable = getDrawableWithTheme(drawable);
		}

		return drawable;
	}

	private long calculateTimeLeft() {

		long timeLeft = -1;
		if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(0))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.HEALTH,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(1))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.WELLBEING,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(2))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.TIME,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(3))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.MONEY,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(4))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.CIGARETTES,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(5))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.LIFE,
					childposition + 1);
		} else if (Util.achievementsCategories.get(groupposition).equals(
				Util.achievementsCategories.get(6))) {
			timeLeft = timeRemainingByCategoryAndLevel(Util.CATEGORIES.CO,
					childposition + 1);
		}

		return timeLeft;
	}

	// This functions is called for calculating time before unblocked this item
	// return time left in seconds
	public long timeRemainingByCategoryAndLevel(Util.CATEGORIES category, int level) {
		Date quittingdate = Start.quittingDate;
		Date now = new Date();

		// time in seconds
		long timeWithoutCigarette = 0;
		switch (category) {
		case HEALTH:
			timeWithoutCigarette = Util.getHealthByLevel(level);
			break;
		case WELLBEING:
			timeWithoutCigarette = Util.getWellnessByLevel(level);
			break;
		case TIME:
			timeWithoutCigarette = Util.getTimeByLevel(level);
			break;
		case MONEY:
			timeWithoutCigarette = Util.getMoneyByLevel(level);
			break;
		case CIGARETTES:
			timeWithoutCigarette = Util.getCigaretteByLevel(level);
			break;
		case LIFE:
			timeWithoutCigarette = Util.getLifeByLevel(level);
			break;
		case CO:
			timeWithoutCigarette = Util.getCoByLevel(level);
			break;
		}

		// temps depuis lequel on a arreter de fumer (en secondes)
		long timeElapsedInSec = (now.getTime() - quittingdate.getTime()) / 1000;
		if (timeElapsedInSec < 0) {
			timeElapsedInSec = 0;
		}

		// temps restant pour debloquer le niveau (en secondes)
		return (timeWithoutCigarette - timeElapsedInSec);
	}

	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		refreshUserInterface();
		refreshLevel();
	}
}
