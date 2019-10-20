package net.tobano.quitsmoking.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.tobano.quitsmoking.app.util.Theme;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Dashboard extends Fragment {

    public static long diff[] = Start.diff;
    public static double moneySaved = Start.moneySaved;
    public static double cigarettesNotSmoked = Start.cigarettesNotSmoked;
    public static double lifeSaved = Start.lifeSaved;
    public static double coSaved = Start.coSaved;
    public static ArrayList<String> ownGoal = Start.ownGoal;
    private static int SIZE_OF_MONTHS = 30;
    private static int SIZE_OF_YEARS = 365;
    private static Context context;
    public View v;
    InputMethodManager imm;
    private CardView bTime;
    private CardView bMoney;
    private CardView bHealth;
    private CardView bWellness;
    private CardView bOwnGoal;
    private CardView bCigarette;
    private CardView bCo;
    private CardView bLife;
    private CardView bCravings;
    //private CardView bAds;
    private FloatingActionButton bMotivationCard;
    private Intent intent;
    private TextView tTime;
    private TextView vTime;
    private TextView uTime;
    private TextView tMoney;
    private TextView vMoney;
    private TextView uMoney;
    private TextView tCigarette;
    private TextView vCigarette;
    private TextView uCigarette;
    private TextView tCo;
    private TextView vCo;
    private TextView uCo;
    private TextView tLife;
    //private ImageView imgAds;
    private TextView vLife;
    private TextView uLife;
    private TextView tHealth;
    private TextView tWellness;
    private TextView tCravings;
    private TextView vCravings;
    private TextView uCravings;
    private ImageView imgTime;
    private ImageView imgGoal;
    private ProgressBar pbLevelHealth;
    private ProgressBar pbLevelWellness;
    private ProgressBar pbLevelTime;
    private ProgressBar pbLevelMoney;
    private ProgressBar pbLevelCigarettes;
    private ProgressBar pbLevelLife;
    private ProgressBar pbLevelCo;
    private ProgressBar pbLevelWillpower;
    private CardView lGoal;
    private TextView tOwnGoal;
    private TextView tPercent;
    private ProgressBar pbOwnGoal;
    private RelativeLayout lDashboard;

    private Handler mHandler;
    protected Runnable mUpdate = new Runnable() {
        public void run() {
            updateView();
            mHandler.postDelayed(this, 1000);
        }
    };

    public static String[] updateLife() {
        String[] info = new String[2];
        lifeSaved = Start.lifeSaved;
        if (lifeSaved < 0) {
            info[0] = "0";
            info[1] = context.getResources().getString(R.string.unitSec);
        } else if (lifeSaved > 999 * 365 * 24 * 60 * 60) {
            info[0] = "999";
            info[1] = context.getResources().getString(R.string.unitYears);
        } else if (lifeSaved > 1 * 365 * 24 * 60 * 60) {
            int life = (int) (lifeSaved / 365 / 24 / 60 / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitYear);
            } else {
                info[1] = context.getResources().getString(R.string.unitYears);
            }
        } else if (lifeSaved > 31 * 24 * 60 * 60) {
            int life = (int) (lifeSaved / 31 / 24 / 60 / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitMonth);
            } else {
                info[1] = context.getResources().getString(R.string.unitMonths);
            }
        } else if (lifeSaved > 7 * 24 * 60 * 60) {
            int life = (int) (lifeSaved / 7 / 24 / 60 / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitWeek);
            } else {
                info[1] = context.getResources().getString(R.string.unitWeeks);
            }
        } else if (lifeSaved > 24 * 60 * 60) {
            int life = (int) (lifeSaved / 24 / 60 / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitDay);
            } else {
                info[1] = context.getResources().getString(R.string.unitDays);
            }
        } else if (lifeSaved > 60 * 60) {
            int life = (int) (lifeSaved / 60 / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitHour);
            } else {
                info[1] = context.getResources().getString(R.string.unitHours);
            }
        } else if (lifeSaved > 60) {
            int life = (int) (lifeSaved / 60);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitMin);
            } else {
                info[1] = context.getResources().getString(R.string.unitMins);
            }
        } else {
            int life = (int) (lifeSaved);
            info[0] = String.valueOf(life);
            if (life == 1) {
                info[1] = context.getResources().getString(R.string.unitSec);
            } else {
                info[1] = context.getResources().getString(R.string.unitSecs);
            }
        }
        return info;
    }

    public static String[] updateCo() {
        String[] info = new String[2];
        coSaved = Start.coSaved;
        if (coSaved < 0) {
            info[0] = "0";
            info[1] = context.getResources().getString(R.string.unitMg);
        } else if (coSaved < 1000) {
            info[0] = String.valueOf((int) coSaved);
            info[1] = context.getResources().getString(R.string.unitMg);
        } else if (coSaved < 1000000) {
            int co = ((int) coSaved / 1000);
            info[0] = String.valueOf(co);
            info[1] = context.getResources().getString(R.string.unitG);
        } else if (coSaved < 999000000) {
            int co = ((int) coSaved / 1000000);
            info[0] = String.valueOf(co);
            info[1] = context.getResources().getString(R.string.unitKg);
        } else {
            info[0] = "+999";
            info[1] = context.getResources().getString(R.string.unitKg);
        }
        return info;
    }

    public static String[] updateCigarette() {
        String[] info = new String[2];
        cigarettesNotSmoked = Start.cigarettesNotSmoked;
        if (cigarettesNotSmoked < 0) {
            cigarettesNotSmoked = 0;
        }
        info[0] = String.valueOf(((int) cigarettesNotSmoked));
        if (((int) cigarettesNotSmoked) == 1 || ((int) cigarettesNotSmoked) == 0) {
            info[1] = context.getResources().getString(R.string.cigaretteUnit);
        } else {
            info[1] = context.getResources().getString(R.string.cigarettesUnit);
        }
        return info;
    }

    public static String[] updateMoney() {
        String[] info = new String[2];
        moneySaved = Start.moneySaved;
        if (moneySaved > 0) {
            info[0] = round(moneySaved, 2);
        } else {
            info[0] = "0";
        }
        info[1] = Start.currency;

        return info;
    }

    public static String[] updateTime() {
        diff = Start.diff;
        String time_value = "";
        String time_unit = "";
        if (diff[0] > 9999) {
            time_value = "+ 9999";
            time_unit = context.getResources().getString(R.string.days);
        } else if (diff[0] > 0) { // display in days / months
            int time = (int) diff[0];
            if (time == 1 || time == 0) { // display one day
                time_value = String.valueOf(time);
                time_unit = context.getResources().getString(R.string.unitDay);
            } else if (time < SIZE_OF_MONTHS * 3) { // display in days
                time_value = String.valueOf(time);
                time_unit = context.getResources().getString(R.string.days);
            } else if (time > SIZE_OF_YEARS * 5) { //display in years
                int timeInYears = (int) time / SIZE_OF_YEARS;
                time_value = String.valueOf(timeInYears);
                time_unit = context.getResources().getString(R.string.years);
            } else { // display in months
                int timeInMonths = (int) time / SIZE_OF_MONTHS;
                time_value = String.valueOf(timeInMonths);
                time_unit = context.getResources().getString(R.string.months);
            }
        } else if (diff[1] > 0) { // display in hours
            time_value = Long.toString(Statistics.diff[1]);
            if (Integer.parseInt(time_value) == 1
                    || Integer.parseInt(time_value) == 0) {
                time_unit = context.getResources().getString(R.string.unitHour);
            } else {
                time_unit = context.getResources().getString(R.string.hours);
            }
        } else if (diff[2] > 0) { // display in minutes
            time_value = Long.toString(Statistics.diff[2]);
            if (Integer.parseInt(time_value) == 1
                    || Integer.parseInt(time_value) == 0) {
                time_unit = context.getResources().getString(R.string.unitMin);
            } else {
                time_unit = context.getResources().getString(R.string.minutes);
            }
        } else if (diff[3] > 0) { // display in seconds
            time_value = Long.toString(Statistics.diff[3]);
            if (Integer.parseInt(time_value) == 1
                    || Integer.parseInt(time_value) == 0) {
                time_unit = context.getResources().getString(R.string.unitSec);
            } else {
                time_unit = context.getResources().getString(R.string.seconds);
            }
        } else {
            time_value = "0";
            time_unit = context.getResources().getString(R.string.unitSec);
        }

        String[] info = new String[2];
        info[0] = time_value;
        info[1] = time_unit;
        return info;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        v = inflater.inflate(R.layout.dashboard, container, false);

        lDashboard = v.findViewById(R.id.dashboard);

        imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        initTile();

        initImage();

        initText();

        initProgressBar();

        initOwnGoal();

        initFloatingButton();

        setTheme();

        managerVersion();

        mHandler = new Handler();
        mHandler.post(mUpdate);

        return v;
    }

    private void managerVersion() {
//		if(Start.isPremium) {
//			bAds.setVisibility(View.GONE);
//		}
//		else{
//			bLife.setVisibility(View.GONE);
//			bCo.setVisibility(View.GONE);
//			bCravings.setVisibility(View.GONE);
//		}
    }

    private void initFloatingButton() {
        bMotivationCard = v.findViewById(R.id.btnMotivCard);
        Drawable dMotivationCard = getResources().getDrawable(R.drawable.button_draw_card);
        Drawable willBeGreen = dMotivationCard.getConstantState().newDrawable();
        int color = ContextCompat.getColor(getActivity(), R.color.white_tabano);
        willBeGreen.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        bMotivationCard.setImageDrawable(willBeGreen);
        bMotivationCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                ((Start) getActivity()).displayMotivationCard();
            }
        });
    }

    private void initProgressBar() {
        pbLevelHealth = v.findViewById(R.id.pbhealth);
        pbLevelHealth.setMax(12);
        pbLevelWellness = v.findViewById(R.id.pbwellness);
        pbLevelWellness.setMax(12);
        pbLevelTime = v.findViewById(R.id.pbtime);
        pbLevelTime.setMax(12);
        pbLevelMoney = v.findViewById(R.id.pbmoney);
        pbLevelMoney.setMax(12);
        pbLevelCigarettes = v.findViewById(R.id.pbcigarette);
        pbLevelCigarettes.setMax(12);
        pbLevelLife = v.findViewById(R.id.pblife);
        pbLevelLife.setMax(12);
        pbLevelCo = v.findViewById(R.id.pbco);
        pbLevelCo.setMax(12);
        pbLevelWillpower = v.findViewById(R.id.pbwillpower);
        pbLevelWillpower.setMax(12);
    }

//	private void updateImage() {
//		try {
//			Drawable img;
//			if (Start.theme == Theme.GREEN){
//				img = ContextCompat.getDrawable(getActivity(), R.drawable.unlock_tile_kwit);
//			}
//			else {
//				img = ContextCompat.getDrawable(getActivity(), R.drawable.unlock_tile_tobano);
//			}
//			//imgAds.setImageDrawable(img);
//		}
//		catch (Exception e) {
//			Bitmap bitmap;
//			if (Start.theme == Theme.GREEN){
//				bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.unlock_tile_kwit);
//			}
//			else {
//				bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.unlock_tile_tobano);
//			}
//			//imgAds.setImageBitmap(bitmap);
//		}
//	}

    public void updateWillpowerProgressDrawableColor() {
        if (getActivity() != null && isAdded()) {
            if (Integer.parseInt(Start.user_level_willpower) <= 5) {
                pbLevelWillpower.setProgressDrawable(getResources()
                        .getDrawable(R.drawable.progressbar_orange));
            } else {
                pbLevelWillpower.setProgressDrawable(getResources()
                        .getDrawable(R.drawable.progressbar_white));
            }
        }
    }

    private void initOwnGoal() {
        lGoal = v.findViewById(R.id.lGoal);
        tOwnGoal = v.findViewById(R.id.goal_title);
        tPercent = v.findViewById(R.id.goal_percent);
        pbOwnGoal = v.findViewById(R.id.pbGoal);
    }

    private void initText() {
        tTime = v.findViewById(R.id.timenowaste_title);
        vTime = v.findViewById(R.id.timenowaste_value);
        uTime = v.findViewById(R.id.timenowaste_unit);
        tMoney = v.findViewById(R.id.moneywin_title);
        vMoney = v.findViewById(R.id.moneywin_value);
        uMoney = v.findViewById(R.id.moneywin_unit);
        tCigarette = v.findViewById(R.id.cigarettenosmoked_title);
        vCigarette = v.findViewById(R.id.cigarettenosmoked_value);
        uCigarette = v.findViewById(R.id.cigarettenosmoked_unit);
        tCo = v.findViewById(R.id.cosaved_title);
        vCo = v.findViewById(R.id.cosaved_value);
        uCo = v.findViewById(R.id.cosaved_unit);
        tLife = v.findViewById(R.id.lifesaved_title);
        vLife = v.findViewById(R.id.lifesaved_value);
        uLife = v.findViewById(R.id.lifesaved_unit);
        tHealth = v.findViewById(R.id.health_text);
        tWellness = v.findViewById(R.id.wellbeing_text);
        tCravings = v.findViewById(R.id.cravings_title);
        vCravings = v.findViewById(R.id.cravings_value);
        uCravings = v.findViewById(R.id.cravings_unit);
    }

    private void initImage() {
        imgTime = v.findViewById(R.id.timenowaste_img);
        imgGoal = v.findViewById(R.id.goal_img);
        //imgAds = v.findViewById(R.id.ads_img);
    }

    private void initTile() {
        bTime = v.findViewById(R.id.timenowaste);
        bTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "time");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bMoney = v.findViewById(R.id.moneywin);
        bMoney.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "money");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bHealth = v.findViewById(R.id.health);
        bWellness = v.findViewById(R.id.wellbeing);
        bCigarette = v.findViewById(R.id.cigarettenosmoked);
        bCigarette.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "cigarette");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bCo = v.findViewById(R.id.cosaved);
        bCo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "co");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bLife = v.findViewById(R.id.lifesaved);
        bLife.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "life");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bOwnGoal = v.findViewById(R.id.lGoal);
        bOwnGoal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra("whichOne", "onwgoal");
                intent.putExtra("volume", Start.volume);
                startActivity(intent);
            }
        });
        bCravings = v.findViewById(R.id.cravings);
        bCravings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Start) getActivity()).playSound(Start.strClick);
                ((Start) getActivity()).selectPager(Start.FRAGMENT_WILLPOWER);
            }
        });
//		bAds = v.findViewById(R.id.ads);
//		bAds.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				((Start)getActivity()).playSound(Start.strClick);
//				Start.displayUnlockFullVersionDialog();
//			}
//		});
    }

    private void setTheme() {
        int colorText;
        int colorBg;
        //int colorAds;
        ColorStateList colorStateList;
        if (Start.theme == Theme.GREEN) {
            colorText = ContextCompat.getColor(getActivity(), R.color.kwit_dashboard_text);
            colorBg = ContextCompat.getColor(getActivity(), R.color.kwit);
            //colorAds = ContextCompat.getColor(getActivity(), R.color.tile_ads_kwit);
            colorStateList = ContextCompat.getColorStateList(getActivity(), R.color.kwit_dark);
        } else {
            colorText = ContextCompat.getColor(getActivity(), R.color.dashboard_text);
            colorBg = ContextCompat.getColor(getActivity(), R.color.dashboard_bg);
            //colorAds = ContextCompat.getColor(getActivity(), R.color.tile_ads_tabano);
            colorStateList = ContextCompat.getColorStateList(getActivity(), R.color.primary_dark);
        }

        setFloatingBtnTheme(colorStateList);
        //updateImage();
        updateColorText(colorText);
        updateColorBackground(colorBg);
        updateProgressbarColor();
        updateWallpaper();
    }

    private void updateProgressbarColor() {
        if (Start.theme == Theme.GREEN) {
            pbLevelMoney.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            pbLevelCigarettes.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            //if (Start.isPremium) {
            pbLevelLife.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            pbLevelCo.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_green));
            //}
        } else {
            pbLevelMoney.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
            pbLevelCigarettes.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
            //if (Start.isPremium) {
            pbLevelLife.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
            pbLevelCo.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_blue));
            //}
        }
    }

    private void updateColorBackground(int color/*, int colorAds*/) {
        bHealth.setBackgroundColor(color);
        bWellness.setBackgroundColor(color);
        bCravings.setBackgroundColor(color);
//		if(Start.isPremium) {
//			bCravings.setBackgroundColor(color);
//		}
//		else {
//			bAds.setBackgroundColor(colorAds);
//		}
    }

    private void updateColorText(int color) {
        tMoney.setTextColor(color);
        vMoney.setTextColor(color);
        uMoney.setTextColor(color);
        tCigarette.setTextColor(color);
        vCigarette.setTextColor(color);
        uCigarette.setTextColor(color);
        //if (Start.isPremium) {
        tCo.setTextColor(color);
        vCo.setTextColor(color);
        uCo.setTextColor(color);
        tLife.setTextColor(color);
        vLife.setTextColor(color);
        uLife.setTextColor(color);
        //}
    }

    private void updateWallpaper() {
        Drawable drawableBig;
        Drawable drawableSmall;
        if (Start.theme == Theme.GREEN) {
            drawableBig = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_kwit);
            drawableSmall = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_kwit_small);
        } else {
            drawableBig = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_tabano);
            drawableSmall = ContextCompat.getDrawable(getActivity(), R.drawable.container_dropshadow_tabano_small);
        }
        try {
            imgTime.setBackground(drawableBig);
            imgGoal.setBackground(drawableSmall);
        } catch (Exception e) {
            imgTime.setBackgroundDrawable(drawableBig);
            imgGoal.setBackgroundDrawable(drawableSmall);
        }
    }

    private void setFloatingBtnTheme(ColorStateList color) {
        try {
            bMotivationCard.setBackgroundTintList(color);
        } catch (Exception e) {
            //
        }
    }

    private void updateView() {
        if (getActivity() != null && isAdded()) {
            // time
            String[] info_time = updateTime();
            vTime.setText(info_time[0]);
            uTime.setText(info_time[1]);
            // money
            String[] info_money = updateMoney();
            vMoney.setText(info_money[0]);
            uMoney.setText(info_money[1]);
            // cigarettes
            String[] info_cig = updateCigarette();
            vCigarette.setText(info_cig[0]);
            uCigarette.setText(info_cig[1]);
            // cravings
            vCravings.setText(String.valueOf(Start.cravings));
            // own goal
            String lastTitleGoal = Util.getLastElement(Start.ownGoalTitles);
            ownGoal = Start.ownGoal;
            int goalValue = updateOwnGoal();
            if (!lastTitleGoal.equals("")) {
                tOwnGoal.setText(lastTitleGoal);
                int displayedPercent = (goalValue >= 100) ? 100 : goalValue;
                tPercent.setText(String.valueOf(displayedPercent) + "%");
            }
            pbOwnGoal.setProgress(goalValue);

            //if (Start.isPremium) {
            // co
            String[] info_co = updateCo();
            vCo.setText(info_co[0]);
            uCo.setText(info_co[1]);
            // life
            String[] info_life = updateLife();
            vLife.setText(info_life[0]);
            uLife.setText(info_life[1]);
            //}

            // progress bar
            updateProgressBar();

            // text for health and wellness
            updateText();
        }
        // TODO kill update function when the fragment is hidden
    }

    public void updateProgressBar() {
        pbLevelCigarettes.setProgress(Integer.parseInt(Start.user_level_cigarette));
        pbLevelCo.setProgress(Integer.parseInt(Start.user_level_co));
        pbLevelHealth.setProgress(Integer.parseInt(Start.user_level_health));
        pbLevelLife.setProgress(Integer.parseInt(Start.user_level_life));
        pbLevelMoney.setProgress(Integer.parseInt(Start.user_level_money));
        pbLevelTime.setProgress(Integer.parseInt(Start.user_level_time));
        pbLevelWellness.setProgress(Integer.parseInt(Start.user_level_wellness));

        //if (Start.isPremium) {
        updateWillpowerProgressDrawableColor();
        //}
        pbLevelWillpower.setProgress(Integer.parseInt(Start.user_level_willpower));
    }

    private void updateText() {
        try {
            int healthLevel = Integer.parseInt(Start.user_level_health);
            int healthId = getResources().getIdentifier("health" + healthLevel,
                    "string", getContext().getPackageName());
            String healthValue = getString(healthId);
            tHealth.setText(healthValue);
            tHealth.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            bHealth.setVisibility(View.GONE);
        }

        try {
            int wellnessLevel = Integer.parseInt(Start.user_level_wellness);
            int wellnessId = getResources().getIdentifier("wellness" + wellnessLevel,
                    "string", getContext().getPackageName());
            String wellnessValue = getString(wellnessId);
            tWellness.setText(wellnessValue);
            tWellness.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            bWellness.setVisibility(View.GONE);
        }
    }

    private int updateOwnGoal() {
        // if the goal is "saved more of X€ to do something
        return calculteOwnGoal((int) Math.floor(moneySaved));
        // else ex. the goal is like "no smoke since X days"
        // TODO idea of improvement
    }

    private int calculteOwnGoal(int currentValue) {
        if (ownGoal == null || ownGoal.isEmpty()) {
            return 0;
        }
        try {
            int goal = (int) Start.getOwnGoal();//Integer.valueOf(Util.getLastElement(ownGoal));
            return Util.calculatePercentage(currentValue, goal);
        } catch (Exception e) {
            return 0;
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
}