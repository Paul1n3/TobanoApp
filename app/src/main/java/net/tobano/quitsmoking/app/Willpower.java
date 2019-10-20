package net.tobano.quitsmoking.app;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import net.tobano.quitsmoking.app.util.AchievementsNotificationsManager;
import net.tobano.quitsmoking.app.util.Theme;
import net.tobano.quitsmoking.app.util.WillpowerNotificationsManager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nicolas Lett
 * 
 */
@SuppressLint("NewApi")
public class Willpower extends Fragment {
	// private Handler mHandler;

	private RelativeLayout willpowerView;
	private LinearLayout background;
	private FloatingActionButton bShare;
	private FloatingActionButton fabList;
	private TextView tSmoke, tMotivation, tResist;
	private FloatingActionButton fabSmoke, fabMotivation, fabResist;
	private boolean isWrapped;
	private Animation fab_open,fab_close;
	private TextView level;
	private ImageButton btnToday;
    private ImageButton btnLastWeek;
    private ImageButton btnLastMonth;
    private ImageButton btnLastQuarter;
	private TextView tvLastXCigarettes;
	private TextView tvLastXCravings;
	private TextView tvLastXLabel;
	private ArrayList<Date> cravingsDatesList;
	private ArrayList<Date> cigarettesSmokedDatesList;
	private LineChart willpowerChart;
    //private Button action_buy;

    private int mGraphUnitDays = 7;

	private int MAX_CRAVINGS = 9999;

	private Start s;

	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		s = ((Start)getActivity());
		if(Start.isPremium) {
			v = inflater.inflate(R.layout.willpower, container, false);

			willpowerView = (RelativeLayout) v.findViewById(R.id.willpower);

            willpowerChart = (LineChart) v.findViewById(R.id.chartWillpower);
            configureGraphProperties();

			refreshUserInterface();

			bShare = (FloatingActionButton) v.findViewById(R.id.bshare);
			bShare.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					((Start)getActivity()).playSound(Start.strClick);
					((Start)getActivity()).shareScreenShot(willpowerView, "tobano_willpower");
					FlurryAgent.logEvent("Share_button_willpower");
				}
			});

			createFloatingActionButton(v);

			fabResist.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					s.playSound(Start.strCraving);
					addCraving();
					closeFloattingButton();
					FlurryAgent.logEvent("Willpower_resist");
				}
			});

			fabSmoke.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if ((int) Start.cigarettesSmoked + 1 >= Util
							.maxCigaretteSmokedBeforeLosing() - 1) {
						displayWarningDialog();
					} else {
						smoke();
					}
					closeFloattingButton();
				}
			});

			fabMotivation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					s.playSound(Start.strClick);
					addCraving();
					closeFloattingButton();
					MotivationCardsDialog dialog = new MotivationCardsDialog(getActivity());
					dialog.displayMotivationCards().show();
					Map<String, String> drawMotivCardParams = new HashMap<String, String>();
					drawMotivCardParams.put("source", "resist_willpower");
					FlurryAgent.logEvent("Draw_motivation_card",
							drawMotivCardParams, false);
				}
			});

			btnToday = (ImageButton) v.findViewById(R.id.btnToday);
			btnToday.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					s.playSound(Start.strClick);
					btnToday.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_today_selected));
					btnLastWeek.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_week));
					btnLastMonth.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_month));
					btnLastQuarter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_quarter));
					refreshGraph(2);
					calculateLastX();
				}
			});

            btnLastWeek = (ImageButton) v.findViewById(R.id.btnLastWeek);
            btnLastWeek.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_week_selected));
            btnLastWeek.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
					s.playSound(Start.strClick);
					btnToday.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_today));
                    btnLastWeek.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_week_selected));
                    btnLastMonth.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_month));
                    btnLastQuarter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_quarter));
                    refreshGraph(7);
					calculateLastX();
                }
            });

            btnLastMonth = (ImageButton) v.findViewById(R.id.btnLastMonth);
            btnLastMonth.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
					s.playSound(Start.strClick);
					btnToday.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_today));
                    btnLastWeek.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_week));
                    btnLastMonth.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_month_selected));
                    btnLastQuarter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_quarter));
                    refreshGraph(30);
					calculateLastX();
                }
            });

            btnLastQuarter = (ImageButton) v.findViewById(R.id.btnLastQuarter);
            btnLastQuarter.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
					s.playSound(Start.strClick);
					btnToday.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_today));
                    btnLastWeek.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_week));
                    btnLastMonth.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_month));
                    btnLastQuarter.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button_last_quarter_selected));
                    refreshGraph(90);
					calculateLastX();
                }
            });
		}
		else{
			v = inflater.inflate(R.layout.unlock_fullversion, container, false);
			ImageButton action_close = (ImageButton) v.findViewById(R.id.actionclose);
			action_close.setVisibility(View.GONE);
//			action_buy = (Button) v.findViewById(R.id.unlock_button);
//			try {
//				String strButtonBuy = getActivity().getResources().getString(R.string.unlockFullVersion);
//				strButtonBuy += " (";
//				strButtonBuy += Start.skuDetailsPremium.getPrice();
//				strButtonBuy += ")";
//				action_buy.setText(strButtonBuy);
//			}
//			catch(Exception e){
//				Log.d("Willpower - skuDetails", "SyncTask is not over - "+e.toString());
//			}
//			action_buy.setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v){
//					Start.startStoreToBuyFullVersion();
//				}
//			});
		}

		setTheme();

		return v;
	}

	private int getColor() {
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(getActivity(),R.color.kwit);
		}
		else {
			color = ContextCompat.getColor(getActivity(),R.color.primary_dark);
		}
		return color;
	}

	private int getColorLight() {
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(getActivity(),R.color.kwit_light);
		}
		else {
			color = ContextCompat.getColor(getActivity(),R.color.primary_light);
		}
		return color;
	}

	public void setTheme() {
//		if(Start.isPremium) {
        ColorStateList colorStateList;
        if (Start.theme == Theme.GREEN) {
            colorStateList = ContextCompat.getColorStateList(getActivity(), R.color.kwit_dark);

        } else {
            colorStateList = ContextCompat.getColorStateList(getActivity(), R.color.primary_dark);
        }

        try {
            bShare.setBackgroundTintList(colorStateList);
        } catch (Exception e) {
            //
        }

        refreshBackgroundColor(Util.getLevelByWillpower());

        Drawable dMotivationCard = ContextCompat.getDrawable(getContext(), R.drawable.fab_plus);
        fabList.setImageDrawable(colorDrawable(dMotivationCard));

        calculateGraph();
//		}
//		else{
//			if (Start.theme == Theme.GREEN){
//				action_buy.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_full_green));
//			}
//			else {
//				action_buy.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_full_tabono));
//			}
//		}
	}

	private Drawable colorDrawable(Drawable d){
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(getActivity(),R.color.kwit_dark);
		}
		else {
			color = ContextCompat.getColor(getActivity(),R.color.primary_dark);
		}

		Drawable newColor = d.getConstantState().newDrawable();
		newColor.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
		return newColor;
	}

	private void createFloatingActionButton(View v) {
		fabList = (FloatingActionButton) v.findViewById(R.id.fablist);
		fabSmoke = (FloatingActionButton) v.findViewById(R.id.fabsmoke);
		fabMotivation = (FloatingActionButton) v.findViewById(R.id.fabmotivation);
		fabResist = (FloatingActionButton) v.findViewById(R.id.fabresist);
		tSmoke = (TextView) v.findViewById(R.id.tsmoke);
		tMotivation = (TextView) v.findViewById(R.id.tmotivation);
		tResist = (TextView) v.findViewById(R.id.tresist);
		isWrapped = false;
		fabList.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fab_plus));
		fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
		fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);
		fabList.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((Start)getActivity()).playSound(Start.strClick);
				if (isWrapped){
					closeFloattingButton();
				}
				else {
					openFloattingButton();
				}
				FlurryAgent.logEvent("Action_button_willpower");
			}
		});
	}

	private void openFloattingButton() {
		fabList.setImageDrawable(colorDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fab_hide)));
		fabSmoke.startAnimation(fab_open);
		fabMotivation.startAnimation(fab_open);
		fabResist.startAnimation(fab_open);
		tSmoke.startAnimation(fab_open);
		tMotivation.startAnimation(fab_open);
		tResist.startAnimation(fab_open);
		fabSmoke.setClickable(true);
		fabMotivation.setClickable(true);
		fabResist.setClickable(true);
		isWrapped = true;
	}

	private void closeFloattingButton() {
		fabList.setImageDrawable(colorDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fab_plus)));
		fabSmoke.startAnimation(fab_close);
		fabMotivation.startAnimation(fab_close);
		fabResist.startAnimation(fab_close);
		tSmoke.startAnimation(fab_close);
		tMotivation.startAnimation(fab_close);
		tResist.startAnimation(fab_close);
		fabSmoke.setClickable(false);
		fabMotivation.setClickable(false);
		fabResist.setClickable(false);
		isWrapped = false;
	}

	private void calculateLastX() {
		ArrayList<Entry> yCravingsVals = getEntriesByNbDays(mGraphUnitDays, cravingsDatesList);
		int cravings = 0;
		for (Entry c: yCravingsVals) {
			cravings += c.getVal();
		}

		tvLastXCravings.setText(""+cravings);
		ArrayList<Entry> yCigarettesVals = getEntriesByNbDays(mGraphUnitDays, cigarettesSmokedDatesList);
		int cigarettes = 0;
		for (Entry c: yCigarettesVals) {
			cigarettes += c.getVal();
		}
		tvLastXCigarettes.setText(""+cigarettes);

		if (mGraphUnitDays == 7) {
			tvLastXLabel.setText(getResources().getString(R.string.lastSevenDays));
		}
		else if (mGraphUnitDays == 30) {
			tvLastXLabel.setText(getResources().getString(R.string.lastThirtyDays));
		}
		else if (mGraphUnitDays == 90) {
			tvLastXLabel.setText(getResources().getString(R.string.lastNinetyDays));
		}
		else {
			tvLastXLabel.setText(getResources().getString(R.string.today));
		}
	}

	private void calculateGraph() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < mGraphUnitDays; i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> yCravingsVals = new ArrayList<Entry>();
		ArrayList<Entry> yCigarettesVals = new ArrayList<Entry>();

        getPreferencesCravingsDates();
		yCravingsVals = getEntriesByNbDays(mGraphUnitDays, cravingsDatesList);
		yCigarettesVals = getEntriesByNbDays(mGraphUnitDays, cigarettesSmokedDatesList);

        LineDataSet setCravings = new LineDataSet(yCravingsVals, "Cravings");
		LineDataSet setCigarettes = new LineDataSet(yCigarettesVals, "Cigarettes");
		if(Start.theme == Theme.GREEN){
			setCravings.setColor(ContextCompat.getColor(getContext(), R.color.kwit));
		}
		else {
			setCravings.setColor(ContextCompat.getColor(getContext(), R.color.color_tabano));
		}
		setCigarettes.setColor(ContextCompat.getColor(getContext(), R.color.orange_willpower));
        setCravings.setCircleSize(0);
		setCigarettes.setCircleSize(0);
		setCravings.setDrawCubic(true);
		setCigarettes.setDrawCubic(true);
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
		dataSets.add(setCigarettes);
        dataSets.add(setCravings); // add the craving datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setDrawValues(false);

        // set data
        willpowerChart.setData(data);
        //willpowerChart.invalidate(); // to refresh the chart
        willpowerChart.getLegend().setEnabled(false);
        willpowerChart.animateY(1000);
    }

    private void configureGraphProperties() {
        willpowerChart.setDescription("");
        willpowerChart.setGridBackgroundColor(Color.WHITE);
        willpowerChart.getAxisRight().setEnabled(false);
        willpowerChart.getAxisRight().setDrawAxisLine(false);
        willpowerChart.getAxisRight().setDrawGridLines(false);
        willpowerChart.getAxisRight().setDrawLabels(false);
		willpowerChart.getAxisRight().setStartAtZero(false);
        willpowerChart.getAxisLeft().setEnabled(false);
        willpowerChart.getAxisLeft().setDrawAxisLine(false);
        willpowerChart.getAxisLeft().setDrawGridLines(false);
        willpowerChart.getAxisLeft().setDrawLabels(false);
		willpowerChart.getAxisLeft().setStartAtZero(false);
        willpowerChart.getXAxis().setEnabled(false);
        willpowerChart.getXAxis().setDrawAxisLine(false);
        willpowerChart.getXAxis().setDrawGridLines(false);
        willpowerChart.getXAxis().setDrawLabels(false);

        // Disable graph interaction
        willpowerChart.setTouchEnabled(false);
        willpowerChart.setDragEnabled(false);
        willpowerChart.setScaleEnabled(false);
        willpowerChart.setPinchZoom(false);
        willpowerChart.setDoubleTapToZoomEnabled(false);
        willpowerChart.setHighlightPerDragEnabled(false);
        willpowerChart.setHighlightPerTapEnabled(false);
        willpowerChart.setDragDecelerationEnabled(false);
    }

    /**
     * Refresh graph depending on unit selected (7, 30, 90)
     * @param i
     */
    private void refreshGraph(int i) {
        mGraphUnitDays = i;
        calculateGraph();
        willpowerChart.invalidate();
    }

    private ArrayList<Entry> getEntriesByNbDays(int nbJours, ArrayList<Date> datesList) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		Log.d("WILLPOWER", "getEntriesByNbDays called with dateslist size = " + datesList.size());
        if(datesList.size() > 0){
            int j = 0;
            DateTime currentDay = new DateTime().withTimeAtStartOfDay();
			int i = datesList.size()-1;
			while(j < nbJours){
                int cCount = 0; // craving/cigarette counter for current day
                while(i >= 0 && currentDay.isBefore(new DateTime(datesList.get(i)))){
					cCount++;
					i--;
                }
                result.add(new Entry(cCount, nbJours-(j+1)));
                currentDay = currentDay.minusDays(1);
                j++;
            }
        }
        else{
            for(int i = 0 ; i < nbJours ; i++)
                result.add(new Entry(0, i));
        }
		return result;
    }

    private void addCraving() {
		getPreferencesCravingsDates();
		if (cravingsDatesList.size() >= MAX_CRAVINGS){
			String message = getResources().getString(R.string.motivationTitle4) + "\n" + getResources().getString(R.string.maxcravings) + " " + String.valueOf(MAX_CRAVINGS);
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			return;
		}
		cravingsDatesList.add(new Date());
        Log.d("CRAVING", "" + cravingsDatesList.size());
        Log.d("CRAVING", "Craving dates list table content <<<<");
        for (int i = 0 ; i < cravingsDatesList.size() ; i++) {
            Log.d("CRAVING", "" + cravingsDatesList.get(i));
        }
        Log.d("CRAVING", ">>>>>>>");
        saveCravingDatePreferences();

        // Add a craving to the craving counter on the screen
		int oldValue = Start.prefs.getInt(Constantes.WILLPOWER_CRAVINGS, 0);
		int newValue = oldValue + 1;

		Start.cravings = newValue;
		setCravings(newValue);

        refreshUserInterface();
	}

	private void getPreferencesCravingsDates(){
		if (null == cravingsDatesList) {
			cravingsDatesList = new ArrayList<>();
		}
		cravingsDatesList = (ArrayList<Date>) ObjectSerializer
				.deserialize(Start.prefs
						.getString(
								Constantes.CRAVINGS_DATES,
								ObjectSerializer
										.serialize(new ArrayList<Date>())));
	}

	private void getPreferencesCigarettesSmokedDates(){
		if (null == cigarettesSmokedDatesList) {
			cigarettesSmokedDatesList = new ArrayList<>();
		}
		cigarettesSmokedDatesList = (ArrayList<Date>) ObjectSerializer
				.deserialize(Start.prefs
						.getString(
								Constantes.CIGARETTES_DATES,
								ObjectSerializer
										.serialize(new ArrayList<Date>())));
	}

	private void saveCravingDatePreferences() {
		Start.editor.putString(Constantes.CRAVINGS_DATES,
				ObjectSerializer.serialize(cravingsDatesList))
				.commit();
	}

	private void saveCigaretteSmokedDatePreferences() {
		Start.editor.putString(Constantes.CIGARETTES_DATES,
				ObjectSerializer.serialize(cigarettesSmokedDatesList))
				.commit();
	}

	protected void displayWarningDialog() {
		String message = getResources().getString(R.string.willpowerRestartMessage);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		WillpowerDialog dialog = WillpowerDialog.newInstance(message);
		dialog.show(getFragmentManager(), "WillpowerDialog");
		getFragmentManager().executePendingTransactions();

		setDialogEvents(dialog);
	}

	private void setDialogEvents(final WillpowerDialog dialog) {
		Button dialogButtonResist;
		Button dialogButtonSmoke;
		Button dialogButtonGetMotivated;

		dialogButtonResist = (Button) dialog.getView().findViewById(
				R.id.btnResist);
		dialogButtonResist.setBackground(getDrawableDialogButton());
		dialogButtonResist.setTextColor(getColor());
		dialogButtonResist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				s.playSound(Start.strCraving);
				addCraving();
				FlurryAgent.logEvent("Willpower_resist");
				dialog.dismiss();
			}
		});

		dialogButtonSmoke = (Button) dialog.getView().findViewById(
				R.id.btnSmoke);
		dialogButtonSmoke.setBackground(getDrawableDialogButton());
		dialogButtonSmoke.setTextColor(getColor());
		dialogButtonSmoke.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				smoke();
				dialog.dismiss();
			}
		});

		dialogButtonGetMotivated = (Button) dialog.getView().findViewById(
				R.id.btnGetmotivated);
		dialogButtonGetMotivated.setBackground(getDrawableDialogButton());
		dialogButtonGetMotivated.setTextColor(getColor());
		dialogButtonGetMotivated.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				s.playSound(Start.strClick);
				dialog.dismiss();
				addCraving();
				MotivationCardsDialog dialog = new MotivationCardsDialog(
						getActivity());
				dialog.displayMotivationCards().show();
				Map<String, String> drawMotivCardParams = new HashMap<String, String>();
				drawMotivCardParams.put("source", "resist_willpower");

				FlurryAgent.logEvent("Draw_motivation_card",
						drawMotivCardParams, false);
			}
		});

	}

	private Drawable getDrawableDialogButton() {
		if (Start.theme == Theme.GREEN){
			return ContextCompat.getDrawable(getActivity(), R.drawable.dialog_green_button);
		}
		else {
			return ContextCompat.getDrawable(getActivity(), R.drawable.dialog_tobano_button);
		}
	}

	private void smoke() {
		FlurryAgent.logEvent("Willpower_add_cigarette");
		Boolean gameOver = addCigarette();
		if (gameOver){
			s.playSound(Start.strGameOver);
		}
		else{
			s.playSound(Start.strSmoke);
		}
	}

	public Boolean updateInformations() {
		Boolean gameOver;
		// to manage updating
		int newLevel = Util.getLevelByWillpower();
		Start.user_level_willpower = String.valueOf(newLevel);
		// smoke too many cig. ! You lose the game !
		if (Integer.valueOf(newLevel) == 0) {
			FlurryAgent.logEvent("Restart_from_willpower");
			Start.cigarettesSmoked = 0;
            Start.cravings = 0;
			Start.firstLaunch = true;
			Start.editor.putBoolean(Constantes.FIRST_LAUNCH, true);
			Start.editor.commit();
			s.selectPager(4);
			gameOver = true;
		} else {
			refreshUserInterface();
			gameOver = false;
		}
		return gameOver;
	}

	private void refreshBackgroundColor(int newLevel) {
		if (newLevel <= 5) {
			background.setBackgroundColor(ContextCompat.getColor(getActivity(),
					R.color.orange_achievements));
		} else if (newLevel <= 8) {
			background.setBackgroundColor(getColor());
		} else {
			background.setBackgroundColor(getColor());
		}
	}

	protected Boolean addCigarette() {
		// Add cigarette to the cigarettes smoked list in pref (values used for the graph)
		getPreferencesCigarettesSmokedDates();
		cigarettesSmokedDatesList.add(new Date());
		Log.d("CIGARETTESMOKED", "" + cigarettesSmokedDatesList.size());
		saveCigaretteSmokedDatePreferences();

        // actualize cigaretteSmoked
		int oldValue = Start.prefs.getInt(Constantes.WILLPOWER_CIGARETTES_SMOKED, 0);
		int newValue = oldValue + 1;
		Start.cigarettesSmoked = newValue;
		// update pref with the new number of cig smoked
		setCigarettesSmoked(newValue);

		// actualize level of willpower counter in Start
		Start.willpowerCounter = Start.prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f) + 1;
		// update pref with the new number of "willpower" (cig smoked and cig forgiven)
		incrementWillpowerCounter();

		// update willpower view
		Boolean gameOver = updateInformations();

		// Schedule notifications for willpower
		new WillpowerNotificationsManager().execute((Start) getActivity());

		// update in pref the date of last cig smoked
		setDateLastCigaretteAdded(new Date());

		Start s = ((Start) getActivity());
		// update smoking variable
		updateSmokingPassed();

		// update level for each badge (lock/unlock/unlockable) in pref
		s.initiateAllAchivementsAndProfileLevels();
		// update badge of category from pref (for achievements)
		s.updateBadgeOfCategories();
		// update achievements view
		((Achievements) ((Start) getActivity()).getFragment(Start.FRAGMENT_ACHIEVEMENTS))
				.refreshUserInterface();

		// update user level (for profile)
		s.initiateUserLevel();
		// the view will be update when user display it

		// replace notifications for achievements
		new AchievementsNotificationsManager().execute(s);

		return gameOver;
	}

	private void updateSmokingPassed() {
		// cigarettes not smoked
		Start.cigarettesNotSmoked = ((double) Start.cigarettesPerDay / (24 * 60 * 60))
				* Start.interval - Start.cigarettesSmoked;
		Start.moneySaved = Start.cigarettesNotSmoked * Start.priceOfACigarette;
		Start.lifeSaved = Start.cigarettesNotSmoked * 660;
		Start.coSaved = Start.cigarettesNotSmoked * 10;
	}

	private void incrementWillpowerCounter() {
		Float currentWillpower = Start.prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f);
		currentWillpower++;
		Start.editor.putFloat(Constantes.WILLPOWER_COUNTER, currentWillpower);
		Start.editor.commit();
	}

	private void setCigarettesSmoked(int c) {
		Start.editor.putInt(Constantes.WILLPOWER_CIGARETTES_SMOKED, c);
		Start.editor.commit();
	}

	private void setCravings(int c) {
		Start.editor.putInt(Constantes.WILLPOWER_CRAVINGS, c);
		Start.editor.commit();
	}

	private void setDateLastCigaretteAdded(Date date) {
		Start.editor.putLong(Constantes.DATE_LAST_CIGARETTE_ADDED, date.getTime());
		Start.editor.commit();
	}

	/**
	 * Update color of background depending on willpower level
	 */
	private void refreshUserInterface() {
		if (v != null && Start.isPremium) {
			background = (LinearLayout) v.findViewById(R.id.texts);
			level = (TextView) v.findViewById(R.id.number_level);
			int lvl = Util.getLevelByWillpower();
			level.setText(Integer.toString(lvl));
			refreshBackgroundColor(lvl);

            getPreferencesCravingsDates();
            getPreferencesCigarettesSmokedDates();

			calculateGraph();

			tvLastXCigarettes = (TextView) v.findViewById(R.id.lastX_number_cigarette);
			tvLastXCravings = (TextView) v.findViewById(R.id.lastX_number_cravings);
			tvLastXLabel = (TextView) v.findViewById(R.id.lastX_label_cravings);

			calculateLastX();

            willpowerChart.invalidate();
		}
	}
}
