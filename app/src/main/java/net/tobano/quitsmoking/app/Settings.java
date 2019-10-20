package net.tobano.quitsmoking.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import net.tobano.quitsmoking.app.util.Theme;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static net.tobano.quitsmoking.app.Util.getColorText;

/**
 * This Activity contains all the quitting information as they have been set at
 * the beginning and the quitting date. It's the configuration page
 * 
 * @author Nicolas Lett
 */

public class Settings extends Fragment {

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	String etCigarettesPerDay = "0";
	String etPriceOfAPack = "0";
	String etCigarettesPerPack = "0";

	TextView tvQuittingDate;

	TextView tvCigarettesPerDay;
	TextView tvPriceOfAPack;
	EditText etvPriceOfAPack;
	TextView tvCigarettesPerPack;

	boolean isChanged = false;
	ImageView ivPriceOfAPack;
	LinearLayout lPriceOfAPack;

	TextView switchVolumeLabel;
	Switch switchVolume;

	Switch switchWillpowerWarning;

	int day;
	int month;
	int year;
	int hour;
	int minute;
	String currency;
	String price;

	InputMethodManager imm;

	private int ACTIVITY_INITIALIZE = 0;
	private int ACTIVITY_TUTORIAL = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.settings, container, false);

		tvQuittingDate = (TextView) v.findViewById(R.id.tvQuittingDate);
		tvQuittingDate.setTextColor(getColorText(getContext()));
		tvCigarettesPerDay = (TextView) v.findViewById(R.id.tvCigarettesPerDay);
		tvCigarettesPerDay.setTextColor(getColorText(getContext()));
		tvPriceOfAPack = (TextView) v.findViewById(R.id.tvPriceOfAPack);
		tvPriceOfAPack.setTextColor(getColorText(getContext()));
		etvPriceOfAPack = (EditText) v.findViewById(R.id.etPriceOfAPack);
		etvPriceOfAPack.setTextColor(getColorText(getContext()));
		etvPriceOfAPack.setVisibility(View.GONE);
		tvCigarettesPerPack = (TextView) v.findViewById(R.id.tvCigarettesPerPack);
		tvCigarettesPerPack.setTextColor(getColorText(getContext()));
		ivPriceOfAPack = (ImageView) v.findViewById(R.id.ivPriceOfAPack);
		lPriceOfAPack = (LinearLayout) v.findViewById(R.id.lPriceOfAPack);

		switchVolume = (Switch) v.findViewById(R.id.switchVolume);
		switchWillpowerWarning = (Switch) v.findViewById(R.id.switchWillpowerDialog);

		switchVolumeLabel = (TextView) v.findViewById(R.id.switchVolumeLabel);

        //if(!Start.isPremium){
        //	switchWillpowerWarning.setVisibility(View.INVISIBLE);
        //	switchVolumeLabel.setVisibility(View.INVISIBLE);
        //}
        //else{
        switchWillpowerWarning.setVisibility(View.VISIBLE);
        switchVolumeLabel.setVisibility(View.VISIBLE);
        //}

		imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		prefs = Start.prefs;
		Start.firstLaunch = prefs.getBoolean(Constantes.FIRST_LAUNCH, true);
		// See "if firstLaunch" Line after tabHost setting

		Boolean isMute = prefs.getBoolean(Constantes.IS_MUTE, true);
		switchVolume.setChecked(!isMute);
		switchVolume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				manageVolume();
			}
		});

		Boolean isWithDialog = prefs.getBoolean(Constantes.DIALOG_BEFORE_SMOKE, true);
		switchWillpowerWarning.setChecked(isWithDialog);
		switchWillpowerWarning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				manageWillpowerWarning();
			}
		});

		editor = Start.editor;

		if (Start.firstLaunch) { // firstLaunch detected
			initialize();
		}

		// Remove all preferences (used to test the preferences behavior)
		// editor.clear();
		// editor.commit();

		// The initialization process is launched when the user clicks the
		// corresponding button
		Button buttonOne = (Button) v.findViewById(R.id.btnInitialize);
		buttonOne.setTextColor(Util.getColorText(getContext()));
		buttonOne.setBackground(Util.getButtonDrawable(getContext()));
		buttonOne.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				initialize();
			}
		});
		// The pen button to change the price of a pack
		// with lPackOfAPack and the corresponding image
		lPriceOfAPack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				isChanged = !isChanged;
				if (isChanged) {
					tvPriceOfAPack.setVisibility(View.GONE);
					etvPriceOfAPack.setVisibility(View.VISIBLE);
					etvPriceOfAPack
							.setText(tvPriceOfAPack.getText().toString());
					price = tvPriceOfAPack.getText().toString();
					etvPriceOfAPack.setSelection(etvPriceOfAPack.getText()
							.length());
					ivPriceOfAPack.setImageDrawable(getResources().getDrawable(
							R.drawable.pencil_changed));
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					((Start) getActivity()).playSound(Start.strClick);
				} else {
					String newPrice = etvPriceOfAPack.getText().toString();
					newPrice = newPrice.replace(',', '.');
					Start s = ((Start) getActivity());
					if (savePreferencePriceOfAPack(newPrice)) {
						// update valide, on met à jour les prefs
						// gerer mise à jour profile / réussites / notif
						s.refreshMoneyLevel();
						s.playSound(Start.strInitialize);
						s.updateUserSettings(true);
					}
					else{
						s.playSound(Start.strError);
					}

					DecimalFormat dfPrice = new DecimalFormat("###.##");
					etvPriceOfAPack.setVisibility(View.GONE);
					tvPriceOfAPack.setVisibility(View.VISIBLE);
					tvPriceOfAPack.setText(dfPrice.format(Start.priceOfAPack));
					ivPriceOfAPack.setImageDrawable(getResources().getDrawable(
							R.drawable.pencil));
					// imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
					// 0);
					imm.hideSoftInputFromWindow(
							etvPriceOfAPack.getWindowToken(), 0);

					// update widget
					s.updateWidget();
				}
			}
		});

		setTheme();

		return v;
	}

	private void setTheme() {
		if (Start.theme == Theme.GREEN){
			if(switchVolume.isChecked())
				switchVolume.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.kwit), PorterDuff.Mode.MULTIPLY);
			else
				switchVolume.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray_tabano), PorterDuff.Mode.MULTIPLY);
			switchVolume.getTrackDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.kwit), PorterDuff.Mode.MULTIPLY);

			if(switchWillpowerWarning.isChecked())
				switchWillpowerWarning.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.kwit), PorterDuff.Mode.MULTIPLY);
			else
				switchWillpowerWarning.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray_tabano), PorterDuff.Mode.MULTIPLY);
			switchWillpowerWarning.getTrackDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.kwit), PorterDuff.Mode.MULTIPLY);
		}
		else {
			if(switchVolume.isChecked())
				switchVolume.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.tabano), PorterDuff.Mode.MULTIPLY);
			else
				switchVolume.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray_tabano), PorterDuff.Mode.MULTIPLY);
			switchVolume.getTrackDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.tabano), PorterDuff.Mode.MULTIPLY);

			if(switchWillpowerWarning.isChecked())
				switchWillpowerWarning.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.tabano), PorterDuff.Mode.MULTIPLY);
			else
				switchWillpowerWarning.getThumbDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray_tabano), PorterDuff.Mode.MULTIPLY);
			switchWillpowerWarning.getTrackDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.tabano), PorterDuff.Mode.MULTIPLY);
		}
	}

	public void initialize() {
		((Start) getActivity()).playSound(Start.strClick);
		try {
			Intent intent = new Intent(getActivity(), InitializationStep1.class);
			intent.putExtra("firstLaunch", Start.firstLaunch);
			intent.putExtra("volume", Start.volume);
			intent.putExtra(Constantes.THEME, Start.theme.name());
			startActivityForResult(intent, ACTIVITY_INITIALIZE);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.e("Initialization failed +",
						"Initialization failed +++ " + e);
			}
		}
	}

	public void displayTutorial() {
		Intent intent = new Intent(getActivity(), Tutorial.class);
		startActivityForResult(intent, ACTIVITY_TUTORIAL);
	}

	// when we come from an other activity (here it is from InitializationStep3)
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ACTIVITY_INITIALIZE) {
			switch (resultCode) {
				case 0: // = RESULT_CANCELED
					if (Start.firstLaunch) {
						getActivity().finish();
					}
					break;
				case 1:
					Start s = ((Start) getActivity());
					s.playSound(Start.strInitialize);

					FlurryAgent.logEvent("Init_data");
					Intent i = data;

					etCigarettesPerDay = i.getStringExtra("etCigarettesPerDay");
					etPriceOfAPack = i.getStringExtra("etPriceOfAPack");
					etCigarettesPerPack = i.getStringExtra("etCigarettesPerPack");
					day = i.getIntExtra("day", 1);
					month = i.getIntExtra("month", 1);
					year = i.getIntExtra("year", 1);
					hour = i.getIntExtra("hour", 1);
					minute = i.getIntExtra("minute", 1);
					currency = i.getStringExtra("currency");

					// update smoking habits in Start
					updateSmokingHabits();

					// update pref and clean it
					updatePreferences();

					// calculate level for achievements
					s.initiateAchievementsLevel();
					// reload the string for achievements
					s.retrieveAchievements();
					// calculate level of user (profile)
					s.initiateUserLevel();

					// remove the personal goal
					cleanOwnGoal();

					s.recalculateNotifications();

					// update the setting view
					updateView();
					if (Start.firstLaunch) {
						// Display the tutorial_shake screen
						displayTutorial();

						setFirstLaunchDone();
					}

					// upadte widget
					s.updateWidget();
			}
		}
		else if(requestCode == ACTIVITY_TUTORIAL){
			((Start) getActivity()).selectPager(Start.FRAGMENT_DEFAULT);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void cleanOwnGoal() {
		Start.ownGoalTitles = new ArrayList<>();
		Start.ownGoal = new ArrayList<>();
	}

	private void updatePreferences() {
		// save config choise (sound + willpower warning)
		Boolean isMute = prefs.getBoolean(Constantes.IS_MUTE, true);
		Boolean isWithDialog = prefs.getBoolean(Constantes.DIALOG_BEFORE_SMOKE, true);

		// keep achievements and willpower notifications from pref
		// to delete them after
		Start s = ((Start)getActivity());
		s.getPreferencesNotifications();

		// Reset preferences (achievements, smoking habits, etc.)
		editor.clear().commit();

		// save config
		editor.putBoolean(Constantes.IS_MUTE, isMute);
		editor.putBoolean(Constantes.DIALOG_BEFORE_SMOKE, isWithDialog);
		editor.commit();

		// re save pref for achievement alarm
		s.saveAchievementsLevelsNotifications();
		// re save pref for willpower alarm
		s.saveWillpowerLevelsNotifications();

		// re init other pref with new settings
		Start.firstLaunch = true;

		savePreferences(Start.quittingDate,
				Integer.toString(Integer.parseInt(etCigarettesPerDay)),
				Double.toString(Double.parseDouble(etPriceOfAPack)),
				Integer.toString(Integer.parseInt(etCigarettesPerPack)),
				currency);
	}

	private void setFirstLaunchDone() {
		// Set the firstLaunch flag to false for next time (not
		// to
		// display it again)
		editor.putBoolean(Constantes.FIRST_LAUNCH, false);
		editor.commit();
		Start.firstLaunch = prefs.getBoolean(Constantes.FIRST_LAUNCH, true);

	}

	public void updateView() {
		int cigarettesPerDay = 0, cigarettesPerPack = 0;
		// format Start.quittingDate as: 30 septembre 2012 15:00

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.SHORT, Locale.getDefault());
		tvQuittingDate.setText(df.format(Start.quittingDate));

		cigarettesPerDay = (int) Start.cigarettesPerDay;
		tvCigarettesPerDay.setText(Integer.toString(cigarettesPerDay));

		DecimalFormat dfPrice = new DecimalFormat("###.##");
		tvPriceOfAPack.setText(dfPrice.format(Start.priceOfAPack));

		cigarettesPerPack = (int) Start.cigarettesPerPack;
		tvCigarettesPerPack.setText(Integer.toString(cigarettesPerPack));
	}

	private void savePreferences(Date quittingDate, String cigarettesPerDay,
			String priceOfAPack, String cigarettesPerPack, String currency) {
		// Put the preferences
		// Convert Date to String (when putting the preferences)
		Format formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String s = formatter.format(quittingDate);

		editor.putString(Constantes.QUITTING_DATE, s);
		editor.putString(Constantes.CURRENCY, currency);
		editor.putString(Constantes.CIGARETTE_PER_DAY, cigarettesPerDay);
		editor.putString(Constantes.CIGARETTE_PER_PACK, cigarettesPerPack);
		editor.putString(Constantes.PRICE_OF_PACK, priceOfAPack);
		editor.putInt(Constantes.WILLPOWER_CIGARETTES_SMOKED, 0);
//
//		if(Start.isPremium)
//			((Start)getActivity()).saveIsPremiumPreferences();

		editor.commit();
	}

	private boolean savePreferencePriceOfAPack(String priceOfAPack) {
		if (priceOfAPack.equals("")) {
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
					.create();
			alertDialog.setTitle(Settings.this.getString(R.string.error));
			alertDialog.setMessage(Settings.this
					.getString(R.string.errorNumberFormat));
			alertDialog.setButton(
					Settings.this.getString(R.string.validateError),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog.show();
			return false;
		} else if (Double.parseDouble(priceOfAPack) > 100000) {
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
					.create();
			alertDialog.setTitle(Settings.this.getString(R.string.error));
			alertDialog.setMessage(Settings.this
					.getString(R.string.errorNumberFormatLength_packprice));
			alertDialog.setButton(
					Settings.this.getString(R.string.validateError),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog.show();
			return false;
		} else if (Double.parseDouble(priceOfAPack) > 0
				&& Double.parseDouble(priceOfAPack) <= 100000) {
			// Mise à jour des variables de start relatives au prix
			// (priceofapack et priceofacigarette)
			Start.priceOfAPack = Double.parseDouble(priceOfAPack);
			Start.priceOfACigarette = Start.priceOfAPack
					/ ((double) Start.cigarettesPerPack == 0 ? 1
							: (double) Start.cigarettesPerPack);
			Start.moneySaved = Start.cigarettesNotSmoked
					* Start.priceOfACigarette;
			editor.putString(Constantes.PRICE_OF_PACK, priceOfAPack);
			editor.commit();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateView();
	}

	private void updateSmokingHabits() {
		String dateString = month + "/" + day + "/" + year + " " + hour + ":"
				+ minute + ":00";
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");
		dateFormat.setLenient(false);
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(dateString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Start.quittingDate = convertedDate;

		Start.cigarettesPerDay = Integer.parseInt(etCigarettesPerDay);

		Start.priceOfAPack = Double.parseDouble(etPriceOfAPack);

		Start.cigarettesPerPack = Integer.parseInt(etCigarettesPerPack);

		Start.priceOfACigarette = Start.priceOfAPack
				/ ((double) Start.cigarettesPerPack == 0 ? 1
						: (double) Start.cigarettesPerPack);

		Date start = Start.quittingDate;
		Date end = new Date();
		long diffInSeconds = (end.getTime() - start.getTime()) / 1000;
		if (diffInSeconds < 0)
			diffInSeconds = 0;
		long interval = diffInSeconds;
		// cigarettes not smoked
		Start.cigarettesNotSmoked = ((double) Start.cigarettesPerDay / (24 * 60 * 60))
				* interval - Start.cigarettesSmoked;
		Start.moneySaved = Start.cigarettesNotSmoked * Start.priceOfACigarette;

		((Start) getActivity()).resetWillpower();
		if (currency.equals("AUTO")) {
			Locale local = new Locale(Locale.getDefault().getLanguage(), Locale
					.getDefault().getCountry());
			Currency c = Currency.getInstance(local);
			Start.currency = c.getSymbol();
		} else {
			Start.currency = currency;
		}
	}

	private void manageVolume() {
		// get current isMute
		Boolean isMute = prefs.getBoolean(Constantes.IS_MUTE, true);
		// update pref
		editor.putBoolean(Constantes.IS_MUTE, !isMute);
		editor.commit();
		// update view
		switchVolume.setChecked(isMute); // because it is the variable before change
		// update variable in Start
		Start.volume = !isMute ? 0 : 1;
		// play sound
		((Start)getActivity()).playSound(Start.strClick);
		setTheme();
	}

	private void manageWillpowerWarning(){
		// get current state
		Boolean isWithDialog = prefs.getBoolean(Constantes.DIALOG_BEFORE_SMOKE, true);
		// update pref
		editor.putBoolean(Constantes.DIALOG_BEFORE_SMOKE, !isWithDialog);
		editor.commit();
		// update view
		switchWillpowerWarning.setChecked(!isWithDialog);
		// update variable in Start
		Start.willpowerWarning = !isWithDialog;
		// play sound
		((Start)getActivity()).playSound(Start.strClick);
		setTheme();
	}

	public static String round(double unrounded, int precision) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(precision); // round to 2 numbers after the
		// coma
		df.setMinimumFractionDigits(precision);
		df.setDecimalSeparatorAlwaysShown(true);
		String s = df.format(unrounded);
		return s;
	}
}
