package net.tobano.quitsmoking.app;

import java.util.Currency;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.Theme;

/**
 * @author Nicolas Lett
 * 
 */
public class InitializationStep3 extends Activity {
	String etCigarettesPerDay;
	String etPriceOfAPack;
	String etCigarettesPerPack;
	Spinner s;
	String currency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.initialization_step3_layout);

		final TextView tvUpdateCurrency = (TextView) findViewById(R.id.tvUpdateCurrency);

		Intent i = getIntent();
		etCigarettesPerDay = i.getStringExtra("etCigarettesPerDay");
		etPriceOfAPack = i.getStringExtra("etPriceOfAPack");
		etCigarettesPerPack = i.getStringExtra("etCigarettesPerPack");
		final int day = i.getIntExtra("day", 1);
		final int month = i.getIntExtra("month", 1);
		final int year = i.getIntExtra("year", 1);
		final int hour = i.getIntExtra("hour", 1);
		final int minute = i.getIntExtra("minute", 1);
		Theme theme = Theme.valueOf(getIntent().getStringExtra(Constantes.THEME));

		TextView title = (TextView) findViewById(R.id.tv1);
		title.setTextColor(getColorText(theme));

		TextView imgNav3Selected = (TextView) findViewById(R.id.imgNav3Selected);
		imgNav3Selected.setTextColor(getColorText(theme));

		tvUpdateCurrency.setTextColor(getColorText(theme));

		s = (Spinner) findViewById(R.id.spCurrency); // We set the element
														// (spinner) which will
														// contain the drag
														// factor value
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.currency, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter); // And we link the spinner items (in the adapter)
								// to our spinner

		s.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				currency = s.getSelectedItem().toString();
				if (s.getSelectedItem().toString().equals("AUTO")) {
					Locale local = new Locale(
							Locale.getDefault().getLanguage(), Locale
									.getDefault().getCountry());
					Currency c = Currency.getInstance(local);
					tvUpdateCurrency.setText(c.getSymbol());
				} else {
					tvUpdateCurrency.setText(s.getSelectedItem().toString());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// do nothing
			}
		});

		Button button = (Button) findViewById(R.id.start);
		button.setTextColor(getColorText(theme));
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();

				// putExtra beginning
				// Re-send the DatePicker parameters (init1)
				intent.putExtra("day", day);
				intent.putExtra("month", month);
				intent.putExtra("year", year);
				intent.putExtra("hour", hour);
				intent.putExtra("minute", minute);
				// Send the init2 parameters
				intent.putExtra("etCigarettesPerDay", etCigarettesPerDay);
				intent.putExtra("etPriceOfAPack", etPriceOfAPack);
				intent.putExtra("etCigarettesPerPack", etCigarettesPerPack);
				// Send the init3 parameter
				intent.putExtra("currency", currency);
				// putExtra end

				setResult(-1, intent);
				finish();
			}
		});
	}

	private int getColorText(Theme theme) {
		if (theme == Theme.GREEN){
			return ContextCompat.getColor(getBaseContext(), R.color.kwit);
		}
		else {
			return ContextCompat.getColor(getBaseContext(), R.color.tabano);
		}
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