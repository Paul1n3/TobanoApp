package net.tobano.quitsmoking.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.Theme;

public class MotivationCardsDialog {

	private Context c;
	private String title;
	private String text;
	private String author;
	private int image;

	// play sounds
	private SoundPool sounds;
	private int sClick;
	private int volume;

	private static final int ID_MOTIVATION_DEFAULT = 0;
	private static final int ID_MOTIVATION_BUDDHIST = 1;
	private static final int ID_MOTIVATION_ANTIQUITE = 2;
	private static final int ID_MOTIVATION_CHINESE = 3;
	private static final int ID_MOTIVATION_SPEAKER = 4;
	private static final int ID_MOTIVATION_SPORT1 = 5;
	private static final int ID_MOTIVATION_SPORT2 = 6;

	public MotivationCardsDialog(Context context) {
		c = context;
	}

	public Dialog displayMotivationCards() {
		// Beginning of the motivation cards
		// set up dialog
		getResourceMotivationTitle();
		getMotivationCardInfos();

		// init sound
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
		sClick = sounds.load(c, R.raw.click, 1);
		volume = Start.volume;

		final Dialog dialog = new Dialog(c, R.style.PauseDialog);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.motivation_dialog);
//		dialog.setTitle(title);
		dialog.setCancelable(true);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialog.getWindow().setAttributes(lp);

		LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.lmain);
		layout.setBackgroundColor(getColor());

		TextView tvText = (TextView) dialog.findViewById(R.id.tvMotivationText);
		tvText.setText(text);

		TextView tvAuthor = (TextView) dialog.findViewById(R.id.tvAuthorText);
		if(author == null){
			tvAuthor.setVisibility(View.GONE);
		}
		else {
			tvAuthor.setVisibility(View.VISIBLE);
			tvAuthor.setText(author);
		}

		ImageView img = (ImageView) dialog.findViewById(R.id.imgMotivation);
		img.setImageResource(image);

		Button button = (Button) dialog.findViewById(R.id.btnOK);
		button.setTextColor(getColor());
		button.setBackgroundDrawable(getBtnDrawable());
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sounds.play(sClick, 0.1f*volume, 0.1f*volume, 0, 0, 1.5f);
				dialog.dismiss();
			}
		});

		return dialog;
	}

	private Drawable getBtnDrawable() {
		if (Start.theme == Theme.GREEN){
			return ContextCompat.getDrawable(c, R.drawable.dialog_green_button);
		}
		else {
			return ContextCompat.getDrawable(c, R.drawable.dialog_tobano_button);
		}
	}

	private int getColor() {
		int color;
		if (Start.theme == Theme.GREEN){
			color = ContextCompat.getColor(c, R.color.kwit);
		}
		else {
			color = ContextCompat.getColor(c, R.color.tabano);
		}
		return color;
	}

	private void getMotivationCardInfos() {
		List<Integer> listID = new ArrayList<Integer>();
		listID.add(ID_MOTIVATION_DEFAULT);
		if(Start.hasAllCards || Start.isPremium){
			listID.add(ID_MOTIVATION_BUDDHIST);
			listID.add(ID_MOTIVATION_ANTIQUITE);
			listID.add(ID_MOTIVATION_CHINESE);
			listID.add(ID_MOTIVATION_SPEAKER);
			listID.add(ID_MOTIVATION_SPORT1);
			listID.add(ID_MOTIVATION_SPORT2);
		}
		int tas = listID.get(randInt(0, listID.size()-1));

		getCardInfosFromTas(tas);
	}

	private void getCardInfosFromTas(int tas) {
		Random r = new Random();
		int numberOfMotivationCard;
		switch(tas){
			case ID_MOTIVATION_BUDDHIST:
				numberOfMotivationCard = randInt(81, 100);
				break;
			case ID_MOTIVATION_ANTIQUITE:
				numberOfMotivationCard = randInt(41, 60);
				break;
			case ID_MOTIVATION_CHINESE:
				numberOfMotivationCard = randInt(61, 80);
				break;
			case ID_MOTIVATION_SPEAKER:
				numberOfMotivationCard = randInt(141, 160);
				break;
			case ID_MOTIVATION_SPORT1:
				numberOfMotivationCard = randInt(101, 120);
				break;
			case ID_MOTIVATION_SPORT2:
				numberOfMotivationCard = randInt(121, 140);
				break;
			default:
				numberOfMotivationCard = randInt(1, 40);
				break;
		}

		int idText;
		int idAuthor = -1;
		int idImage;
		if(numberOfMotivationCard <= 40) {
			idText = c.getResources().getIdentifier("motivation" + numberOfMotivationCard,
					"string", c.getPackageName());
			idImage = c.getResources().getIdentifier("motivation" + numberOfMotivationCard,
					"drawable", c.getPackageName());
		}
		else {
			idText = c.getResources().getIdentifier("quote" + numberOfMotivationCard,
					"string", c.getPackageName());
			idAuthor = c.getResources().getIdentifier("quoteauthor" + numberOfMotivationCard,
					"string", c.getPackageName());
			idImage = c.getResources().getIdentifier(c.getString(c.getResources().getIdentifier("quoteimage" + numberOfMotivationCard,
							"string", c.getPackageName())),
					"drawable", c.getPackageName());
		}

		text = c.getString(idText);
		if(idAuthor != -1)
			author = c.getString(idAuthor);
		else
			author = null;
		image = idImage;
	}

	private void getResourceMotivationTitle() {
		String motivationCardTitle = "motivationTitle";

		// Generate random number for the title of the motivation cards
		int numberOfMotivationCardTitle = randInt(1, 4);

		motivationCardTitle += numberOfMotivationCardTitle;

		int mctitle = c.getResources().getIdentifier(motivationCardTitle,
				"string", c.getPackageName());

		title = c.getString(mctitle);
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
}
