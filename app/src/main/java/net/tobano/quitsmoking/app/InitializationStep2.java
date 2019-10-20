package net.tobano.quitsmoking.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.Theme;

/**
 * @author Nicolas Lett
 * 
 */
public class InitializationStep2 extends Activity {
	EditText etCigarettesPerDay;
	EditText etPriceOfAPack;
	EditText etCigarettesPerPack;

	private SoundPool sounds;
	private int sClick;
	private int sError;
	int volume = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.initialization_step2_layout);

		Intent i = getIntent();
		final int day = i.getIntExtra("day", 1);
		final int month = i.getIntExtra("month", 1);
		final int year = i.getIntExtra("year", 1);
		final int hour = i.getIntExtra("hour", 1);
		final int minute = i.getIntExtra("minute", 1);
		final Theme theme = Theme.valueOf(getIntent().getStringExtra(Constantes.THEME));

		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
		sClick = sounds.load(getBaseContext(), R.raw.click, 1);
		sError = sounds.load(getBaseContext(), R.raw.error, 1);
		volume = i.getIntExtra("volume", 1);

		TextView title = (TextView) findViewById(R.id.tv1);
		title.setTextColor(getColorText(theme));

		TextView imgNav2Selected = (TextView) findViewById(R.id.imgNav2Selected);
		imgNav2Selected.setTextColor(getColorText(theme));

		Button button = (Button) findViewById(R.id.goToStep3);
		button.setTextColor(getColorText(theme));

		etCigarettesPerDay = (EditText) findViewById(R.id.etCigarettesPerDay);
		etCigarettesPerDay.setBackgroundDrawable(getEditTextDrawable(theme));
		etPriceOfAPack = (EditText) findViewById(R.id.etPriceOfAPack);
		etPriceOfAPack.setBackgroundDrawable(getEditTextDrawable(theme));
		etCigarettesPerPack = (EditText) findViewById(R.id.etCigarettesPerPack);
		etCigarettesPerPack.setBackgroundDrawable(getEditTextDrawable(theme));

		// retrieve info if previous habits exist in the preference
		if (Start.cigarettesPerDay != 0)
			etCigarettesPerDay.setText(String.valueOf(Start.cigarettesPerDay));
		if (Start.priceOfAPack != 0)
			etPriceOfAPack.setText(String.valueOf(Start.priceOfAPack));
		if (Start.cigarettesPerDay != 0)
			etCigarettesPerPack
					.setText(String.valueOf(Start.cigarettesPerPack));

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try // if the user enters correct information about his habits,
					// move on to the next step else display errors messages
				{
					if (etCigarettesPerDay.getText().toString().equals("")
							|| etPriceOfAPack.getText().toString().equals("")
							|| etCigarettesPerPack.getText().toString()
									.equals("")) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								InitializationStep2.this).create();
						alertDialog.setTitle(InitializationStep2.this
								.getString(R.string.error));
						alertDialog.setMessage(InitializationStep2.this
								.getString(R.string.errorNumberFormat));
						alertDialog.setButton(InitializationStep2.this
										.getString(R.string.validateError),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int which) {
									}
								});
						alertDialog.show();
						// play sound
						playSound(Start.strError);
					}
					else if (Double.parseDouble(etPriceOfAPack.getText()
							.toString()) > 100000) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								InitializationStep2.this).create();
						alertDialog.setTitle(InitializationStep2.this
								.getString(R.string.error));
						alertDialog.setMessage(InitializationStep2.this
								.getString(R.string.errorNumberFormatLength_packprice));
						alertDialog.setButton(InitializationStep2.this
										.getString(R.string.validateError),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int which) {
									}
								});
						alertDialog.show();
						// play sound
						playSound(Start.strError);
					}
					else if (Double.parseDouble(etCigarettesPerDay.getText()
							.toString()) > 999
							|| Double.parseDouble(etCigarettesPerPack.getText()
									.toString()) > 999) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								InitializationStep2.this).create();
						alertDialog.setTitle(InitializationStep2.this
								.getString(R.string.error));
						alertDialog.setMessage(InitializationStep2.this
								.getString(R.string.errorNumberFormatLength));
						alertDialog.setButton(InitializationStep2.this
										.getString(R.string.validateError),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int which) {
									}
								});
						alertDialog.show();
						// play sound
						playSound(Start.strError);
					}
					else if (// continue init
							Double.parseDouble(etCigarettesPerDay.getText()
							.toString()) > 0
							&& Double.parseDouble(etCigarettesPerDay.getText()
									.toString()) <= 999
							&& Double.parseDouble(etPriceOfAPack.getText()
									.toString()) > 0
							&& Double.parseDouble(etPriceOfAPack.getText()
									.toString()) <= 100000
							&& Double.parseDouble(etCigarettesPerPack.getText()
									.toString()) > 0
							&& Double.parseDouble(etCigarettesPerPack.getText()
									.toString()) <= 999) {
						// play sound
						playSound(Start.strClick);

						Intent intent = new Intent(InitializationStep2.this,
								InitializationStep3.class);
						intent.putExtra("volume", volume);
						// Re-send the DatePicker parameters (init1)
						intent.putExtra("day", day);
						intent.putExtra("month", month);
						intent.putExtra("year", year);
						intent.putExtra("hour", hour);
						intent.putExtra("minute", minute);
						// Send the init2 parameters
						intent.putExtra("etCigarettesPerDay",
								etCigarettesPerDay.getText().toString());
						intent.putExtra("etPriceOfAPack", etPriceOfAPack
								.getText().toString());
						intent.putExtra("etCigarettesPerPack",
								etCigarettesPerPack.getText().toString());
						intent.putExtra("firstLaunch", getIntent()
								.getBooleanExtra("firstLaunch", true));
						// and the theme
						intent.putExtra(Constantes.THEME, theme.name());

						startActivityForResult(intent, 0);
					}
					else {

					}
				}
				catch (Exception e) {

				}
			}
		});
	}

	private Drawable getEditTextDrawable(Theme theme) {
		if (theme == Theme.GREEN){
			return ContextCompat.getDrawable(getBaseContext(), R.drawable.edittext_greenborders);
		}
		else {
			return ContextCompat.getDrawable(getBaseContext(), R.drawable.edittext_tabonoborders);
		}
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
			case -1:
				setResult(-1, data);
				finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (getIntent().getBooleanExtra("firstLaunch", true)) {
			finish();
		}
		else {
			super.onBackPressed();
		}
	}

	private void playSound(String strSound){
		if(strSound.equals(Start.strError)){
			sounds.play(sError, 0.1f*volume, 0.1f*volume, 0, 0, 1.5f);
		}
		else if(strSound.equals(Start.strClick)) {
			sounds.play(sClick, 0.1f*volume, 0.1f*volume, 0, 0, 1.5f);
		}
	}
}