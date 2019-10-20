package net.tobano.quitsmoking.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import net.tobano.quitsmoking.app.util.Theme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Util {
	private static int[] levelHealth = { 22, 423, 544, 1445, 2166, 2887, 37448,
			43209, 201610, 230411, 504012, 936013 };// in minutes
	private static int[] levelWellness = { 5, 6, 8, 13, 16, 24, 28, 34, 42, 62,
			95, 190 }; // in days
	private static int[] levelMoney = { 10, 20, 30, 50, 100, 150, 200, 300,
			500, 1000, 1500, 3000 };
	private static int[] levelTime = { 1, 3, 7, 10, 14, 21, 28, 60, 90, 180,
			365, 730 }; // in days
	private static int[] levelCigarette = { 10, 20, 50, 100, 150, 300, 600,
			1000, 2000, 3000, 5000, 10000 }; // cigarette
	private static int[] levelLife = { 1, 4, 8, 12, 24, 24 * 2, 24 * 7,
			24 * 14, 24 * 21, 24 * 28, 24 * 60, 24 * 90 }; // in hour
	private static int[] levelCo = { 100, 200, 500, 1000, 2000, 4000, 6000,
			10000, 15000, 25000, 50000, 100000 }; // mg
	private static int[] levelWillpower = { 1, 2, 3, 5, 9, 13, 17, 22, 26, 32,
			37, 42, 45 }; // willpower

	// Achievements data
	public static String[] healthAchievements;
	public static String[] wellnessAchievements;
	public static String[] timeAchievements;
	public static String[] moneyAchievements;
	public static String[] cigarettesAchievements;
	public static String[] lifeAchievements;
	public static String[] coAchievements;

	public final static String badgeHealthKey = "health";
	public final static String badgeWellnessKey = "wellness";
	public final static String badgeTimeKey = "time";
	public final static String badgeMoneyKey = "money";
	public final static String badgeCigaretteKey = "cigarette";
	public final static String badgeLifeKey = "life";
	public final static String badgeCoKey = "co";

	public final static long ONE_SECOND_IN_MILLIS = 1000;

	@SuppressWarnings("serial")
	public final static ArrayList<String> achievementsCategories = new ArrayList<String>() {
		{
			add(badgeHealthKey);
			add(badgeWellnessKey);
			add(badgeTimeKey);
			add(badgeMoneyKey);
			add(badgeCigaretteKey);
			add(badgeLifeKey);
			add(badgeCoKey);
		}
	};

	public final static int[] categoryResources = { R.string.health,
			R.string.wellness, R.string.time, R.string.money,
			R.string.cigarettes, R.string.life, R.string.co };

	protected static final int Build_Version = Build.VERSION.SDK_INT;

	public static int getWillpowerByLevel(int level) {
		switch (level) {
		case 0:
			return levelWillpower[12];
		case 1:
			return levelWillpower[11];
		case 2:
			return levelWillpower[10];
		case 3:
			return levelWillpower[9];
		case 4:
			return levelWillpower[8];
		case 5:
			return levelWillpower[7];
		case 6:
			return levelWillpower[6];
		case 7:
			return levelWillpower[5];
		case 8:
			return levelWillpower[4];
		case 9:
			return levelWillpower[3];
		case 10:
			return levelWillpower[2];
		case 11:
			return levelWillpower[1];
		case 12:
			return levelWillpower[0];
		default:
			return levelWillpower[0];
		}
	}

	public static int getLevelByWillpower() {
		Float currentWillpower = Start.prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f);
		if (currentWillpower == levelWillpower[0])
			return 12;
		else if (currentWillpower <= levelWillpower[1])
			return 11;
		else if (currentWillpower <= levelWillpower[2])
			return 10;
		else if (currentWillpower <= levelWillpower[3])
			return 9;
		else if (currentWillpower <= levelWillpower[4])
			return 8;
		else if (currentWillpower <= levelWillpower[5])
			return 7;
		else if (currentWillpower <= levelWillpower[6])
			return 6;
		else if (currentWillpower <= levelWillpower[7])
			return 5;
		else if (currentWillpower <= levelWillpower[8])
			return 4;
		else if (currentWillpower <= levelWillpower[9])
			return 3;
		else if (currentWillpower <= levelWillpower[10])
			return 2;
		else if (currentWillpower <= levelWillpower[11])
			return 1;
		else if (currentWillpower > levelWillpower[11])
			return 0;
		else
			return 12;
	}

	public static int maxCigaretteSmokedBeforeLosing() {
		return getWillpowerByLevel(1);
	}

	public enum CATEGORIES {
		HEALTH, WELLBEING, TIME, MONEY, CIGARETTES, LIFE, CO
	}

	// in seconds
	public static int getCigaretteByLevel(int level) {
		double div = Double.parseDouble(String.valueOf(Start.cigarettesPerDay))
				/ (24 * 60 * 60);
		int nbCigMustBeNotSmoked = (levelCigarette[level - 1]) + Start.cigarettesSmoked;
		try {
			return (int) (nbCigMustBeNotSmoked / div);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByCigarette(double cigarettesNotSmoked) {
		if (cigarettesNotSmoked >= levelCigarette[11]) {
			return "12";
		} else if (cigarettesNotSmoked >= levelCigarette[10]) {
			return "11";
		} else if (cigarettesNotSmoked >= levelCigarette[9]) {
			return "10";
		} else if (cigarettesNotSmoked >= levelCigarette[8]) {
			return "9";
		} else if (cigarettesNotSmoked >= levelCigarette[7]) {
			return "8";
		} else if (cigarettesNotSmoked >= levelCigarette[6]) {
			return "7";
		} else if (cigarettesNotSmoked >= levelCigarette[5]) {
			return "6";
		} else if (cigarettesNotSmoked >= levelCigarette[4]) {
			return "5";
		} else if (cigarettesNotSmoked >= levelCigarette[3]) {
			return "4";
		} else if (cigarettesNotSmoked >= levelCigarette[2]) {
			return "3";
		} else if (cigarettesNotSmoked >= levelCigarette[1]) {
			return "2";
		} else if (cigarettesNotSmoked >= levelCigarette[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	public static Date getDateOfAchievementUnlockByCategoryAndLevel(
			int category, int level) {
		int secondsOfLevel;
		long t = Start.quittingDate.getTime();
		Date result;

		switch (category) {
		case 0:
			secondsOfLevel = getHealthByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 1:
			secondsOfLevel = getWellnessByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 2:
			secondsOfLevel = getTimeByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 3:
			secondsOfLevel = getMoneyByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 4:
			secondsOfLevel = getCigaretteByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 5:
			secondsOfLevel = getLifeByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		case 6:
			secondsOfLevel = getCoByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		default:
			secondsOfLevel = getCoByLevel(level);
			result = new Date(t + (secondsOfLevel * ONE_SECOND_IN_MILLIS));
			break;
		}

		return result;
	}

	public static Date getDateOfWillpowerUnlockByLevel(int level) {
		float secondsOfLevel = 0;

		secondsOfLevel = (Start.prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f) - getWillpowerByLevel(level))
				/ Constantes.CONST_INCREASE_SMOKED;
		long t = new Date().getTime();
		Date result = null;
		result = new Date(t
				+ (Math.round(secondsOfLevel) * ONE_SECOND_IN_MILLIS));

		// System.out.println("Date for willpower level " + level + " = " +
		// result
		// + " secondsOfLevel = " + secondsOfLevel);
		return result;
	}

	// in seconds
	public static int getTimeByLevel(int level) {
		try {
			return (levelTime[level - 1] * 24 * 60 * 60);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByTime(long time /* in seconds */) {
		// levelTime[x] in days
		double timeDouble = time; // in seconds
		double coef =  24 * 60 * 60; // to convert days in second
		if (timeDouble > levelTime[11] * coef ) {
			return "12";
		} else if (timeDouble > levelTime[10] * coef) {
			return "11";
		} else if (timeDouble > levelTime[9] * coef) {
			return "10";
		} else if (timeDouble > levelTime[8] * coef) {
			return "9";
		} else if (timeDouble > levelTime[7] * coef) {
			return "8";
		} else if (timeDouble > levelTime[6] * coef) {
			return "7";
		} else if (timeDouble > levelTime[5] * coef) {
			return "6";
		} else if (timeDouble > levelTime[4] * coef) {
			return "5";
		} else if (timeDouble > levelTime[3] * coef) {
			return "4";
		} else if (timeDouble > levelTime[2] * coef) {
			return "3";
		} else if (timeDouble > levelTime[1] * coef) {
			return "2";
		} else if (timeDouble > levelTime[0] * coef) {
			return "1";
		} else {
			return "0";
		}
	}

	// in seconds
	public static int getMoneyByLevel(int level) {
		double div = Double.parseDouble(String.valueOf(Start.cigarettesPerDay))
				/ (24 * 60 * 60) * Start.priceOfACigarette;
		double moneySmoked = Start.cigarettesSmoked * Start.priceOfACigarette;
		try {
			return (int) ((levelMoney[level - 1] + moneySmoked) / div);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByMoney(double moneySaved) {
		if (moneySaved > levelMoney[11]) {
			return "12";
		} else if (moneySaved > levelMoney[10]) {
			return "11";
		} else if (moneySaved > levelMoney[9]) {
			return "10";
		} else if (moneySaved > levelMoney[8]) {
			return "9";
		} else if (moneySaved > levelMoney[7]) {
			return "8";
		} else if (moneySaved > levelMoney[6]) {
			return "7";
		} else if (moneySaved > levelMoney[5]) {
			return "6";
		} else if (moneySaved > levelMoney[4]) {
			return "5";
		} else if (moneySaved > levelMoney[3]) {
			return "4";
		} else if (moneySaved > levelMoney[2]) {
			return "3";
		} else if (moneySaved > levelMoney[1]) {
			return "2";
		} else if (moneySaved > levelMoney[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	// in seconds
	public static int getWellnessByLevel(int level) {
		try {
			return (levelWellness[level - 1] * 24 * 60 * 60);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByWellness(int time /* seconds */) {
		double timeDouble = time;
		timeDouble = timeDouble / 60 / 60 / 24;
		if (timeDouble > levelWellness[11]) {
			return "12";
		} else if (timeDouble > levelWellness[10]) {
			return "11";
		} else if (timeDouble > levelWellness[9]) {
			return "10";
		} else if (timeDouble > levelWellness[8]) {
			return "9";
		} else if (timeDouble > levelWellness[7]) {
			return "8";
		} else if (timeDouble > levelWellness[6]) {
			return "7";
		} else if (timeDouble > levelWellness[5]) {
			return "6";
		} else if (timeDouble > levelWellness[4]) {
			return "5";
		} else if (timeDouble > levelWellness[3]) {
			return "4";
		} else if (timeDouble > levelWellness[2]) {
			return "3";
		} else if (timeDouble > levelWellness[1]) {
			return "2";
		} else if (timeDouble > levelWellness[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	// in seconds
	public static int getHealthByLevel(int level) {
		try {
			return (levelHealth[level - 1] * 60);
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByHealth(int time /* in seconds */) {
		time = time / 60; // in minutes
		if (time >= levelHealth[11]) {
			return "12";
		} else if (time >= levelHealth[10]) {
			return "11";
		} else if (time >= levelHealth[9]) {
			return "10";
		} else if (time >= levelHealth[8]) {
			return "9";
		} else if (time >= levelHealth[7]) {
			return "8";
		} else if (time >= levelHealth[6]) {
			return "7";
		} else if (time >= levelHealth[5]) {
			return "6";
		} else if (time >= levelHealth[4]) {
			return "5";
		} else if (time >= levelHealth[3]) {
			return "4";
		} else if (time >= levelHealth[2]) {
			return "3";
		} else if (time >= levelHealth[1]) {
			return "2";
		} else if (time >= levelHealth[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	public static String getNbOfLevelUnlockedByCategory(String badgeTypeName) {
		int res = 0;
		for (int i = 1; i <= 12; i++) {
			if (Start.prefs.getInt(badgeTypeName + i, 0) == 2) {
				res += 1;
			}
		}
		return String.valueOf(res);
	}

	public static String getNbOfLevelAvailableForUnlockByCategory(
			String badgeTypeName) {
		int res = 0;
		for (int i = 1; i <= 13; i++) {
			if (Start.prefs.getInt(badgeTypeName + i, 0) == 1) {
				// System.out
				// .println("getGroupView():getNbOfLevelAvailableForUnlockByCategory():levelEquals1 for badge="
				// + badgeTypeName);
				res += 1;
			}
		}
		return String.valueOf(res);
	}

	// in seconds
	public static int getLifeByLevel(int level) {
		long timeToUnlockLevel = levelLife[level - 1] * 60 * 60; // in seconds
		long timeWasteWithCigSmoked = Start.cigarettesSmoked * 660; // in seconds
		try {
			return ((int) ((timeToUnlockLevel + timeWasteWithCigSmoked) * Start.timeToGainOneSecond));
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByLife(double lifeSaved /*en sec depuis date d arret*/) {
		lifeSaved = lifeSaved / 60 / 60; /*en heure*/
		if (lifeSaved >= levelLife[11]) {
			return "12";
		} else if (lifeSaved >= levelLife[10]) {
			return "11";
		} else if (lifeSaved >= levelLife[9]) {
			return "10";
		} else if (lifeSaved >= levelLife[8]) {
			return "9";
		} else if (lifeSaved >= levelLife[7]) {
			return "8";
		} else if (lifeSaved >= levelLife[6]) {
			return "7";
		} else if (lifeSaved >= levelLife[5]) {
			return "6";
		} else if (lifeSaved >= levelLife[4]) {
			return "5";
		} else if (lifeSaved >= levelLife[3]) {
			return "4";
		} else if (lifeSaved >= levelLife[2]) {
			return "3";
		} else if (lifeSaved >= levelLife[1]) {
			return "2";
		} else if (lifeSaved >= levelLife[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	public static int getCoByLevel(int level) {
		// une cigarette c'est 10mg de Co non inhalé
		double coTakeWithCigSmoked = Start.cigarettesSmoked * 10;
		try {
			return ((int) ((levelCo[level - 1] + coTakeWithCigSmoked) * Start.timeToGetOneMg));
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getLevelByCo(double coSaved) {
		if (coSaved >= levelCo[11]) {
			return "12";
		} else if (coSaved >= levelCo[10]) {
			return "11";
		} else if (coSaved >= levelCo[9]) {
			return "10";
		} else if (coSaved >= levelCo[8]) {
			return "9";
		} else if (coSaved >= levelCo[7]) {
			return "8";
		} else if (coSaved >= levelCo[6]) {
			return "7";
		} else if (coSaved >= levelCo[5]) {
			return "6";
		} else if (coSaved >= levelCo[4]) {
			return "5";
		} else if (coSaved >= levelCo[3]) {
			return "4";
		} else if (coSaved >= levelCo[2]) {
			return "3";
		} else if (coSaved >= levelCo[1]) {
			return "2";
		} else if (coSaved >= levelCo[0]) {
			return "1";
		} else {
			return "0";
		}
	}

	public static void initializeAchievements(Context context) {
		// These arrays will contain all achievement messages
		healthAchievements = new String[12];
		wellnessAchievements = new String[12];
		timeAchievements = new String[12];
		moneyAchievements = new String[12];
		cigarettesAchievements = new String[12];
		lifeAchievements = new String[12];
		coAchievements = new String[12];

		String healthx, wellnessx;
		for (int i = 0; i < 12; i++) {
			// Determine the number of the message
			healthx = "health" + (i + 1);
			wellnessx = "wellness" + (i + 1);

			int h = context.getResources().getIdentifier(healthx, "string",
					context.getPackageName());
			int w = context.getResources().getIdentifier(wellnessx, "string",
					context.getPackageName());

			healthAchievements[i] = context.getString(h);
			wellnessAchievements[i] = context.getString(w);

			String textMoneyFormatted = String.format(context
					.getString(R.string.money1), Util.getMoneyText(i));
			moneyAchievements[i] = textMoneyFormatted;

			String textCigFormatted = String.format(context
					.getString(R.string.cigarettes1), Util.getCigaretteText(i));
			cigarettesAchievements[i] = textCigFormatted;

			String textTimeFormatted = String.format(context
					.getString(R.string.time1), Util.getTimeText(i, context));
			timeAchievements[i] = textTimeFormatted;

			String textLifeFormatted = String.format(context
					.getString(R.string.life1), Util.getLifeText(i, context));
			lifeAchievements[i] = textLifeFormatted;

			String textCoFormatted = String.format(context
					.getString(R.string.co1), Util.getCoText(i, context));
			coAchievements[i] = textCoFormatted;
		}
	}

	private static String getCigaretteText(int level) {
		int value = levelCigarette[level];
		return String.valueOf(value);
	}

	private static String getMoneyText(int level) {
		int value = levelMoney[level];
		return String.valueOf(value) + Start.currency;
	}

	private static String getTimeText(int level, Context context) {
		int value = levelTime[level];
		String unit;
		if (value == 365) {
			unit = context.getString(R.string.unitOneYear);
			value = value / 365;
		}
		else if (value > 365) {
			unit = context.getString(R.string.years);
			value = value / 365;
		}
		else if (value > 30) {
			unit = context.getString(R.string.months);
			value = value / 30;
		}
		else if (value == 30) {
			unit = context.getString(R.string.unitMonth);
			value = value / 30;
		}
		else if (value == 1) {
			unit = context.getString(R.string.unitDay);
		}
		else {
			unit = context.getString(R.string.days);
		}
		return String.valueOf(value) + " " + unit;
	}

	private static String getLifeText(int level, Context context) {
		int value = levelLife[level];
		String unit;
		if (value > 24*30) {
			unit = context.getString(R.string.months);
			value = value / 24 / 30;
		}
		else if (value == 24*30) {
			unit = context.getString(R.string.unitMonth);
			value = value / 24 / 30;
		}
		else if (value > 24) {
			unit = context.getString(R.string.days);
			value = value / 24;
		}
		else if (value == 24) {
			unit = context.getString(R.string.unitDay);
			value = value / 24;
		}
		else if (value == 1) {
			unit = context.getString(R.string.unitHour);
		}
		else {
			unit = context.getString(R.string.hours);
		}
		return String.valueOf(value) + " " + unit;
	}

	private static String getCoText(int level, Context c) {
		int value = levelCo[level];
		String unit;
		if (value > 1000){
			value = value / 1000;
			unit = c.getString(R.string.unitG);
		}
		else {
			unit = c.getString(R.string.unitMg);
		}
		return String.valueOf(value) + " " + unit;
	}

	// int healthLevel, int wellnessLevel, int timeLevel,
	// int moneyLevel, int cigaretteLevel, int lifeLevel, int coLevel
	// user_level_welness
	public static int getUserXP() {
		int userXP = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = 1; j <= 12; j++) {
				if (Start.prefs.getInt(Util.achievementsCategories.get(i) + j, 0) == 2)
					userXP += j;
			}
		}
		return userXP;
	}

	public static String getUserLevel(int xp) {
		if (xp > 545) {
			return "28";
		} else if (xp > 520) {
			return "27";
		} else if (xp > 495) {
			return "26";
		} else if (xp > 470) {
			return "25";
		} else if (xp > 445) {
			return "24";
		} else if (xp > 420) {
			return "23";
		} else if (xp > 395) {
			return "22";
		} else if (xp > 370) {
			return "21";
		} else if (xp > 345) {
			return "20";
		} else if (xp > 320) {
			return "19";
		} else if (xp > 295) {
			return "18";
		} else if (xp > 270) {
			return "17";
		} else if (xp > 245) {
			return "16";
		} else if (xp > 220) {
			return "15";
		} else if (xp > 195) {
			return "14";
		} else if (xp > 170) {
			return "13";
		} else if (xp > 145) {
			return "12";
		} else if (xp > 120) {
			return "11";
		} else if (xp > 100) {
			return "10";
		} else if (xp > 80) {
			return "9";
		} else if (xp > 60) {
			return "8";
		} else if (xp > 40) {
			return "7";
		} else if (xp > 30) {
			return "6";
		} else if (xp > 20) {
			return "5";
		} else if (xp > 15) {
			return "4";
		} else if (xp > 10) {
			return "3";
		} else if (xp > 5) {
			return "2";
		} else {
			return "1";
		}
	}

	public static String getUserRankValue(int userLevel) {
		String result = "";
		switch (userLevel) {
		case 1:
		case 2:
			result = "1";
			break;
		case 3:
		case 4:
			result = "2";
			break;
		case 5:
		case 6:
			result = "3";
			break;
		case 7:
		case 8:
			result = "4";
			break;
		case 9:
		case 10:
			result = "5";
			break;
		case 11:
		case 12:
			result = "6";
			break;
		case 13:
		case 14:
			result = "7";
			break;
		case 15:
		case 16:
			result = "8";
			break;
		case 17:
		case 18:
		case 19:
			result = "9";
			break;
		case 20:
		case 21:
		case 22:
			result = "10";
			break;
		case 23:
		case 24:
		case 25:
			result = "11";
			break;
		case 26:
		case 27:
		case 28:
			result = "12";
			break;
		default:
			result = "1";
			break;
		}
		return result;
	}

	public static String getUserRankText(int userRank, Context context) {
		String result = "";
		switch (userRank) {
		case 1:
			result = context.getResources().getString(R.string.Rank1);
			break;
		case 2:
			result = context.getResources().getString(R.string.Rank2);
			break;
		case 3:
			result = context.getResources().getString(R.string.Rank3);
			break;
		case 4:
			result = context.getResources().getString(R.string.Rank4);
			break;
		case 5:
			result = context.getResources().getString(R.string.Rank5);
			break;
		case 6:
			result = context.getResources().getString(R.string.Rank6);
			break;
		case 7:
			result = context.getResources().getString(R.string.Rank7);
			break;
		case 8:
			result = context.getResources().getString(R.string.Rank8);
			break;
		case 9:
			result = context.getResources().getString(R.string.Rank9);
			break;
		case 10:
			result = context.getResources().getString(R.string.Rank10);
			break;
		case 11:
			result = context.getResources().getString(R.string.Rank11);
			break;
		case 12:
			result = context.getResources().getString(R.string.Rank12);
			break;
		default:
			result = "None";
			break;
		}
		return result;
	}

	@SuppressWarnings("unused")
	private String formatNumber(int number, int digits) {
		String result = "";
		switch (digits) {
		case 3:
			if (number > 99) {
				result = "" + number;
			} else if (number > 9) {
				result = "0" + number;
			} else {
				result = "00" + number;
			}
			break;
		case 2:
			if (number < 10) {
				result = "0" + number;
			} else {
				result = "" + number;
			}
		default:
			result = "0";
			break;
		}
		return result;
	}

	public static int getTimeBeforeHealthUnlockable(int quitterSince,
			String level_health) {
		int nextLevel = Integer.valueOf(level_health) + 1;
		int timeRemaining = getHealthByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeWellnessUnlockable(int quitterSince,
			String level_wellness) {
		int nextLevel = Integer.valueOf(level_wellness) + 1;
		int timeRemaining = getWellnessByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeTimeUnlockable(int quitterSince,
			String level_time) {
		int nextLevel = Integer.valueOf(level_time) + 1;
		int timeRemaining = getTimeByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeMoneyUnlockable(int quitterSince,
			String level_money) {
		int nextLevel = Integer.valueOf(level_money) + 1;
		int timeRemaining = getMoneyByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeCigaretteUnlockable(int quitterSince,
			String level_cigarette) {
		int nextLevel = Integer.valueOf(level_cigarette) + 1;
		int timeRemaining = getCigaretteByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeLifeUnlockable(int quitterSince,
			String level_life) {
		int nextLevel = Integer.valueOf(level_life) + 1;
		int timeRemaining = getLifeByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static int getTimeBeforeCoUnlockable(int quitterSince,
			String level_co) {
		int nextLevel = Integer.valueOf(level_co) + 1;
		int timeRemaining = getCoByLevel(nextLevel) - quitterSince;
		if (timeRemaining < 0) {
			return -1;
		} else {
			return timeRemaining;
		}
	}

	public static void setToUnlock(int categoryIndex, int level) {
		Start.editor.putInt(Util.achievementsCategories.get(categoryIndex)
				+ level, 1);
		Start.editor.commit();
	}

	public static void setToLocked(int categoryIndex, int level) {
		Start.editor.putInt(Util.achievementsCategories.get(categoryIndex)
				+ level, 0);
		Start.editor.commit();
	}

	public static int getNotificationIcon() {
		boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
		return whiteIcon ? R.drawable.kwit_icon_silhouette
				: R.drawable.kwit_icon;
	}

	@TargetApi(23)
	public static void setAlarm(AlarmManager alarmManager, int rtcWakeup,
			long l, PendingIntent pendingIntent) {
		if (Build_Version < android.os.Build.VERSION_CODES.KITKAT) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, l, pendingIntent);
		} else if (Build_Version >= android.os.Build.VERSION_CODES.KITKAT
				&& Build_Version < android.os.Build.VERSION_CODES.M) {
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, l, pendingIntent);
		} else if (Build_Version >= android.os.Build.VERSION_CODES.M) {
			alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l,
					pendingIntent);
		}

	}

	public static Intent sharePicture(Context context, Bitmap picture, String picture_name, String shareMessage, String chooserTitle){

		// IMAGE
		// folder where picture will be saved
		File file = getAlbumStorageDir(context, context.getString(R.string.app_name));
		String mPath = file.toString() + "/" + picture_name + ".jpeg";

		OutputStream fout;
		File imageFile = new File(mPath);

		try {
			fout = new FileOutputStream(imageFile);
			picture.compress(Bitmap.CompressFormat.JPEG, 90, fout);
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Uri imageUri;
		imageUri = Uri.fromFile(new File(mPath));

		// MESSAGE
		String tagAndUrl = '\n' + context.getResources().getString(R.string.tabanoTag) + '\n' + context.getResources().getString(R.string.tabanoWebsite);
		shareMessage += tagAndUrl;

		// BUILD
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
		emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareMessage));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.IQuitSmokingWithKwit));
		emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
		emailIntent.setType("*/*");

		PackageManager pm = context.getPackageManager();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");

		Intent openInChooser = Intent.createChooser(emailIntent, chooserTitle);

		List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
		List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
		for (int i = 0; i < resInfo.size(); i++) {
			// Extract the label, append it, and repackage it in a LabeledIntent
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			emailIntent.setPackage(packageName);
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
			intent.putExtra(Intent.EXTRA_STREAM, imageUri);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					context.getResources().getString(R.string.IQuitSmokingWithKwit));
			if (packageName.contains("facebook")) {
				intent.setType("image/*");
			}

			intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));

		}

		// convert intentList to array
		LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

		openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
		//startActivity(openInChooser);
		return openInChooser;
	}

	private static File getAlbumStorageDir(Context context, String albumName){
		File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), albumName);
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}

	/**
	 * Simple String transformation by XOR-ing all characters by value.
	 */
	static String stringTransform(String s, int i) {
		char[] chars = s.toCharArray();
		for(int j = 0; j<chars.length; j++)
			chars[j] = (char)(chars[j] ^ i);
		return String.valueOf(chars);
	}

	public static String getLastElement(final Collection c) {
        if (c == null || c.isEmpty()){
            return "";
        }

		final Iterator itr = c.iterator();
		Object lastElement = itr.next();
		while(itr.hasNext()) {
			lastElement=itr.next();
		}
		return (String) lastElement;
	}

	public static String parseIntToString(int value, int max) {
		if (value > max) {
			return String.valueOf(max);
		}
		else if (value >= 10){
			return String.valueOf(value);
		}
		else {
			return "0"+String.valueOf(value);
		}
	}

	public static int calculatePercentage(int currentValue, int maxValue) {
		double progressValue = 0;
		if (currentValue > 0) {
			progressValue = (int) (100 * currentValue);
			progressValue = progressValue / maxValue;
		}
		return (int) Math.floor(progressValue);
	}

	public static long[] convertInDayHourMinSec(long diffInSeconds) {
		long[] diff = new long[4];
		diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds); // seconds
		diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60
				: diffInSeconds; // minutes
		diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24
				: diffInSeconds; // hours
		diff[0] = (diffInSeconds = (diffInSeconds / 24)); // days
		return diff;
	}

	public static boolean isNetworkAvailable(Context ct) {
		boolean connected = false;
		//get the connectivity manager object to identify the network state.
		ConnectivityManager connectivityManager = (ConnectivityManager)ct.getSystemService(Context.CONNECTIVITY_SERVICE);
		//Check if the manager object is NULL, this check is required. to prevent crashes in few devices.
		if(connectivityManager != null && (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null)) {
			//Check Mobile data or Wifi net is present

			if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)  {
				//we are connected to a network
				connected = true;
			} else {
				connected = false;
			}
			return connected;
		} else  {
			return false;
		}
	}

	public static Drawable colorImage(Drawable drawable, int color){
		Drawable newOne = drawable.getConstantState().newDrawable();
		drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
		return newOne;
	}

	public static String convertDateToLocalFormat(Context context, Date date) {
		try {
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
			return dateFormat.format(date);
		} catch (Exception e) {
			return "";
		}
	}

    public static SpannableStringBuilder customErrorMessage(Context c, String errorMsg) {
		int errorColor;
		final int version = Build.VERSION.SDK_INT;

		//Get the defined errorColor from color resource.
		if (version >= 23) {
			errorColor = ContextCompat.getColor(c, R.color.white_tabano);
		} else {
			errorColor = c.getResources().getColor(R.color.white_tabano);
		}

		ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(errorColor);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errorMsg);
        spannableStringBuilder.setSpan(foregroundColorSpan, 0, errorMsg.length(), 0);
		return spannableStringBuilder;
	}

	public static int getColorText(Context c) {
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(c, R.color.kwit);
		}
		else {
			color = ContextCompat.getColor(c, R.color.tabano);
		}
		return color;
	}

	public static Drawable getButtonDrawable(Context c) {
		if (Start.theme == Theme.GREEN){
			return ContextCompat.getDrawable(c, R.drawable.dialog_green_button);
		}
		else {
			return ContextCompat.getDrawable(c, R.drawable.dialog_tobano_button);
		}
	}
}
