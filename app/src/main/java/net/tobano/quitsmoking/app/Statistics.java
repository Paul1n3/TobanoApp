package net.tobano.quitsmoking.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.LinePageIndicator;

import net.tobano.quitsmoking.app.util.Theme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Statistics extends FragmentActivity {

	private static Context c;

	// play sounds
	private SoundPool sounds;
	private int sSlide;
	public static String strSlide = "silde";
	private int sClick;
	public static String strClick = "click";
	private int volume;

	/** maintains the pager adapter */
	private ViewPager pager;
	private PagerAdapter mPagerAdapter;

	public static long diff[] = Start.diff;
	public static Date quittingDate = new Date();
	public static double cigarettesPerDay = 0;
	public static double priceOfAPack = 0.0;
	public static double cigarettesPerPack = 0;
	public static double moneySaved = 0.0;
	public static double cigarettesNotSmoked = 0;
	public static double priceOfACigarette = 0.0;
	public static double coSaved = 0;
	public static double lifeSaved = 0;
	public static String ownGoalTitle;
	public static double ownGoal;

	public static Theme theme = Start.theme;

	List<Fragment> fragments;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.viewpager_layout);

		Intent i = getIntent();

		c = getBaseContext();

		volume = i.getExtras().getInt("volume");
		initSounds();

		String whichOne = i.getExtras().getString("whichOne");

		initialisePaging(whichOne);

		updateValue();
	}

	private static void updateValue() {
		cigarettesPerDay = Start.cigarettesPerDay;
		priceOfAPack = Start.priceOfAPack;
		cigarettesPerPack = Start.cigarettesPerPack;
		cigarettesNotSmoked = Start.cigarettesNotSmoked;
		priceOfACigarette = Start.priceOfACigarette;
		moneySaved = Start.moneySaved;
		coSaved = Start.coSaved;
		lifeSaved = Start.lifeSaved;
		ownGoalTitle = Util.getLastElement(Start.ownGoalTitles);
		ownGoal = Start.getOwnGoal();
	}

	private void initialisePaging(String whichOne) {
		// Every class is added in a fragment list and then in the pager adapter
		fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(Statistics.this,
				Quitter.class.getName()));
		fragments.add(Fragment.instantiate(Statistics.this,
				Money.class.getName()));
		fragments.add(Fragment.instantiate(Statistics.this,
				Cigarettes.class.getName()));
		//if(Start.isPremium) {
		fragments.add(Fragment.instantiate(Statistics.this,
				Life.class.getName()));
		fragments.add(Fragment.instantiate(Statistics.this,
				Co.class.getName()));
		//}
		fragments.add(Fragment.instantiate(Statistics.this,
				OwnGoal.class.getName()));

		// Set the pager with an adapter
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(),
				fragments, getApplicationContext());

		// Initialization of the viewpager with the component in
		// viewpager_layout xml
		pager = (ViewPager) findViewById(R.id.viewpager);

		pager.setAdapter(this.mPagerAdapter);
		if (whichOne.equals("time")) {
			pager.setCurrentItem(0);
		} else if (whichOne.equals("money")) {
			pager.setCurrentItem(1);
		} else if (whichOne.equals("cigarette")) {
			pager.setCurrentItem(2);
		} else if (whichOne.equals("life")) {
			pager.setCurrentItem(3);
		} else if (whichOne.equals("co")) {
			pager.setCurrentItem(4);
		} else if (whichOne.equals("onwgoal")) {
			pager.setCurrentItem(fragments.size()-1);
		} else {
			pager.setCurrentItem(0);
		}

		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				playSound(strSlide);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		// Bind the title indicator to the adapter
		LinePageIndicator mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(pager);
		mIndicator.setCurrentItem(pager.getCurrentItem());
		mIndicator.setBackgroundColor(getResources().getColor(
				R.color.transparent));
		final float density = getResources().getDisplayMetrics().density;
		mIndicator
				.setSelectedColor(getThemeColor());
		mIndicator.setUnselectedColor(getResources().getColor(R.color.black));
		mIndicator.setStrokeWidth(4 * density);
		mIndicator.setLineWidth(30 * density);
	}

    public static void addGoalInPreferences(String title, String value) {
		ArrayList<String> titles = Start.ownGoalTitles;
        titles.add(title);
        Start.editor.putString(Constantes.OWN_GOAL_TITLE,
                ObjectSerializer.serialize(new ArrayList<>(titles)))
                .commit();

		ArrayList<String> values = Start.ownGoal;
        values.add(value);
        Start.editor.putString(Constantes.OWN_GOAL,
                ObjectSerializer.serialize(new ArrayList<>(values)))
                .commit();

		updateValue();
    }

    public static void replaceGoalInPreferences(String title, String value) {
        ArrayList<String> titles = Start.ownGoalTitles;
        if (!titles.isEmpty()) {
            titles.remove(titles.size()-1);
        }
        titles.add(title);
        Start.editor.putString(Constantes.OWN_GOAL_TITLE,
                ObjectSerializer.serialize(new ArrayList<>(titles)))
                .commit();

		ArrayList<String> values = Start.ownGoal;
        if (!values.isEmpty()){
            values.remove(titles.size()-1);
        }
        values.add(value);
        Start.editor.putString(Constantes.OWN_GOAL,
                ObjectSerializer.serialize(new ArrayList<>(values)))
                .commit();

		updateValue();
    }

	public void onResume() {
		super.onResume();
		// On redémarre tous les handlers mis en pause
		if (fragments.size() > 0) {
			Quitter quitterFragment = (Quitter) fragments.get(0);
			Money moneyFragment = (Money) fragments.get(1);
			Cigarettes cigarettesFragment = (Cigarettes) fragments.get(2);
			OwnGoal ownGoalFragment = (OwnGoal) fragments.get(fragments.size()-1);
			Life lifeFragment = null;
			Co coFragment = null;
			if(Start.isPremium) {
				lifeFragment = (Life) fragments.get(3);
				coFragment = (Co) fragments.get(4);
			}

			if (quitterFragment != null
					&& quitterFragment.getmHandlerQuitter() != null)
				quitterFragment.getmHandlerQuitter().post(
						quitterFragment.getmUpdateQuitter());
			if (moneyFragment != null
					&& moneyFragment.getmHandlerMoney() != null)
				moneyFragment.getmHandlerMoney().post(
						moneyFragment.getmUpdateMoney());
			if (cigarettesFragment != null
					&& cigarettesFragment.getmHandlerCigarettes() != null)
				cigarettesFragment.getmHandlerCigarettes().post(
						cigarettesFragment.getmUpdateCigarettes());
			if (lifeFragment != null && lifeFragment.getmHandlerLife() != null)
				lifeFragment.getmHandlerLife().post(
						lifeFragment.getmUpdateLife());
			if (coFragment != null && coFragment.getmHandlerCo() != null)
				coFragment.getmHandlerCo().post(coFragment.getmUpdateCo());
			if (ownGoalFragment != null && ownGoalFragment.getmHandlerOwnGoal() != null){
				ownGoalFragment.getmHandlerOwnGoal().post(
						ownGoalFragment.getmUpdateOwnGoal());
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// On met en pause tous les handler pour économiser les ressources
		if (fragments.size() > 0) {
			Quitter quitterFragment = (Quitter) fragments.get(0);
			Money moneyFragment = (Money) fragments.get(1);
			Cigarettes cigarettesFragment = (Cigarettes) fragments.get(2);
			OwnGoal ownGoalFragment = (OwnGoal) fragments.get(fragments.size()-1);
			Life lifeFragment = null;
			Co coFragment = null;
			//if(Start.isPremium) {
				lifeFragment = (Life) fragments.get(3);
				coFragment = (Co) fragments.get(4);
			//}

			if (quitterFragment != null
					&& quitterFragment.getmHandlerQuitter() != null)
				quitterFragment.getmHandlerQuitter().removeCallbacks(
						quitterFragment.getmUpdateQuitter());
			if (moneyFragment != null
					&& moneyFragment.getmHandlerMoney() != null)
				moneyFragment.getmHandlerMoney().removeCallbacks(
						moneyFragment.getmUpdateMoney());
			if (cigarettesFragment != null
					&& cigarettesFragment.getmHandlerCigarettes() != null)
				cigarettesFragment.getmHandlerCigarettes().removeCallbacks(
						cigarettesFragment.getmUpdateCigarettes());
			if (lifeFragment != null && lifeFragment.getmHandlerLife() != null)
				lifeFragment.getmHandlerLife().removeCallbacks(
						lifeFragment.getmUpdateLife());
			if (coFragment != null && coFragment.getmHandlerCo() != null)
				coFragment.getmHandlerCo().removeCallbacks(
						coFragment.getmUpdateCo());
			if (ownGoalFragment != null && ownGoalFragment.getmHandlerOwnGoal() != null){
				ownGoalFragment.getmHandlerOwnGoal().removeCallbacks(
						ownGoalFragment.getmUpdateOwnGoal());
			}
		}
	}

	public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Bitmap bmOverlay = Bitmap.createBitmap(
				bmp1.getWidth() > bmp2.getWidth() ? bmp1.getWidth() : bmp2.getWidth(),
				bmp1.getHeight() + bmp2.getHeight(),
				bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawColor(c.getResources().getColor(R.color.white_tabano));
		canvas.drawBitmap(bmp1, 0, 0, null);
		canvas.drawBitmap(bmp2, 0, bmp1.getHeight(), null);
		return bmOverlay;
	}

	private void initSounds(){
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
		sSlide = sounds.load(c, R.raw.slide, 1);
		sClick = sounds.load(c, R.raw.click, 1);
	}

	public void playSound(String nameSound){
		if(nameSound.equals(strSlide)){
			sounds.play(sSlide, 1.0f*volume, 1.0f*volume, 0, 0, 1.5f);
		}
		else if(nameSound.equals(strClick)){
			sounds.play(sClick, 0.1f*volume, 0.1f*volume, 0, 0, 1.5f);
		}
	}

	public void pressedCustomBackButton() {
		playSound(Statistics.strClick);
		finish();
	}

	public int getThemeColor() {
		int color;
		if (Start.theme == Theme.GREEN){
			color = getResources().getColor(R.color.kwit_dark);
		}
		else {
			color = getResources().getColor(R.color.color_tabano);
		}
		return color;
	}
}