package net.tobano.quitsmoking.app;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.LinePageIndicator;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by Carine
 */
public class Tutorial extends FragmentActivity {

    // play sounds
    private SoundPool sounds;
    private int sSlide;
    public static String strSlide = "silde";
    private int sClick;
    public static String strClick = "click";
    int volume;

    private ViewPager mViewPager;

    List<Fragment> fragments;
    private PagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.tutorial);

        volume = Start.volume;
        initSounds();

        // ressources à afficher
        // 1 . tutorial_shake
        // 2 . tutorial_dashboard
        // 3 . tutorial_willpower -> que si hasPremium = true
        // 4 . unlock_fullversion -> que si hasPremium = false
        // 5 . tutorial_social -> que si FR ou EN
        // 6 . tutorial_start

        // Every class is added in a fragment list and then in the pager adapter
        fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(Tutorial.this,
                TutorialShake.class.getName()));
        fragments.add(Fragment.instantiate(Tutorial.this,
                TutorialDashboard.class.getName()));
        //if(!isPremium) {
        //    fragments.add(Fragment.instantiate(Tutorial.this,
        //            TutorialFullVersion.class.getName()));
        //}
        //else {
        fragments.add(Fragment.instantiate(Tutorial.this,
                TutorialWillpower.class.getName()));
        //}
        if (Locale.getDefault().getLanguage().toLowerCase().equals("en") || Locale.getDefault().getLanguage().toLowerCase().equals("fr")) {
            fragments.add(Fragment.instantiate(Tutorial.this, TutorialSocial.class.getName()));
        }

        fragments.add(Fragment.instantiate(Tutorial.this,
                TutorialStart.class.getName()));
        // Set the pager with an adapter
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(),
                fragments, getApplicationContext());

        // Initialization of the viewpager with the component in
        // viewpager_layout xml
        mViewPager = (ViewPager) findViewById(R.id.pagertuto);

        mViewPager.setAdapter(this.mPagerAdapter);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        mIndicator.setViewPager(mViewPager);
        mIndicator.setCurrentItem(mViewPager.getCurrentItem());
        mIndicator.setBackgroundColor(getResources().getColor(
                R.color.gray_background_transparent));
        final float density = getResources().getDisplayMetrics().density;
        mIndicator
                .setSelectedColor(getResources().getColor(R.color.white_tabano));
        mIndicator.setUnselectedColor(getResources().getColor(R.color.gray_tuto_lineindicator));
        mIndicator.setStrokeWidth(8 * density);
        mIndicator.setLineWidth(8 * density);
    }

    public void hideTutorial(){
        playSound(Statistics.strClick);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initSounds(){
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        Context c = getBaseContext();
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
}
