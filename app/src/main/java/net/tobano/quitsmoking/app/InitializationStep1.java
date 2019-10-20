package net.tobano.quitsmoking.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.Theme;

/**
 * @author Nicolas Lett
 * 
 */
public class InitializationStep1 extends FragmentActivity {

	private Theme theme;
	private Locale local;
	private Calendar myCalendar;

	private EditText date;
	private EditText time;
	private DatePickerDialog.OnDateSetListener datePicker;
	private TimePickerDialog.OnTimeSetListener timePicker;

	private SoundPool sounds;
	private int sClick;
	int volume = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.initialization_step1_layout);

		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
		sClick = sounds.load(getBaseContext(), R.raw.click, 1);
		volume = getIntent().getIntExtra("volume", 1);
		theme = Theme.valueOf(getIntent().getStringExtra(Constantes.THEME));

		TextView title = (TextView) findViewById(R.id.currentDate);
		title.setTextColor(getColorText());
		
		TextView imgNav1Selected = (TextView) findViewById(R.id.imgNav1Selected);
		imgNav1Selected.setTextColor(getColorText());

		local = new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry());

		myCalendar = Calendar.getInstance();

		date = (EditText) findViewById(R.id.date);
		defineDatePicker();
		date.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int theme = getPickerTheme();
					new DatePickerDialog(InitializationStep1.this,
							theme,
							datePicker,
							myCalendar.get(Calendar.YEAR),
							myCalendar.get(Calendar.MONTH),
							myCalendar.get(Calendar.DAY_OF_MONTH)).show();
				}
				catch (Exception e) {
					new DatePickerDialog(InitializationStep1.this,
							datePicker,
							myCalendar.get(Calendar.YEAR),
							myCalendar.get(Calendar.MONTH),
							myCalendar.get(Calendar.DAY_OF_MONTH)).show();
				}
			}
		});
		updateDateLabel();

		time = (EditText) findViewById(R.id.time);
		defineTimePicker();
		time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int theme = getPickerTheme();
					new TimePickerDialog(InitializationStep1.this,
							theme,
							timePicker,
							myCalendar.get(Calendar.HOUR_OF_DAY),
							myCalendar.get(Calendar.MINUTE),
							true).show();
				}
				catch (Exception e) {
					new TimePickerDialog(InitializationStep1.this,
							timePicker,
							myCalendar.get(Calendar.HOUR_OF_DAY),
							myCalendar.get(Calendar.MINUTE),
							true).show();
				}
			}
		});
		updateTimeLabel();

		Button button = (Button) findViewById(R.id.goToStep2);
		button.setTextColor(getColorText());
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// play sound
				sounds.play(sClick, 0.1f*volume, 0.1f*volume, 0, 0, 1.5f);

				Intent intent = new Intent(InitializationStep1.this,
						InitializationStep2.class);
				intent.putExtra("day", myCalendar.get(Calendar.DAY_OF_MONTH));
				intent.putExtra("month", myCalendar.get(Calendar.MONTH)+1);
				intent.putExtra("year", myCalendar.get(Calendar.YEAR));
				intent.putExtra("hour", myCalendar.get(Calendar.HOUR_OF_DAY));
				intent.putExtra("minute", myCalendar.get(Calendar.MINUTE));
				intent.putExtra("firstLaunch", getIntent().getBooleanExtra("firstLaunch", true));
				intent.putExtra("volume", volume);
				intent.putExtra(Constantes.THEME, theme.name());

				startActivityForResult(intent, 0);
			}
		});

		Button buttonCancel = (Button) findViewById(R.id.cancel);
		buttonCancel.setTextColor(getColorText());
		buttonCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void defineDatePicker() {
		datePicker = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				myCalendar.set(Calendar.YEAR, year);
				myCalendar.set(Calendar.MONTH, monthOfYear);
				myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateDateLabel();
			}
		};
	}

	private void defineTimePicker() {
		timePicker = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				myCalendar.set(Calendar.MINUTE, minute);
				updateTimeLabel();
			}
		};
	}

	private void updateDateLabel() {
		date.setText(Util.convertDateToLocalFormat(this, myCalendar.getTime()));
	}

	private void updateTimeLabel() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
		String timeValue = simpleDateFormat.format(myCalendar.getTime());
		time.setText(timeValue);
	}

	private int getPickerTheme() {
		if (theme == Theme.GREEN){
			return R.style.datepickerKwit;
		}
		else {
			return R.style.datepickerTobano;
		}
	}

	private int getColorText() {
		if (theme == Theme.GREEN){
			return ContextCompat.getColor(getBaseContext(), R.color.kwit);
		}
		else {
			return ContextCompat.getColor(getBaseContext(), R.color.tabano);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case -1:
			setResult(1, data);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (getIntent().getBooleanExtra("firstLaunch", true)) {
			finish();
		} else {
			super.onBackPressed();
		}
	}
}
