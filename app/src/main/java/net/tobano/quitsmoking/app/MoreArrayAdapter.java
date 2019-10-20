package net.tobano.quitsmoking.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.tobano.quitsmoking.app.util.Theme;

import java.util.ArrayList;

public class MoreArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> values;

	public MoreArrayAdapter(Context context, ArrayList<String> values) {
		super(context, -1, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater
				.inflate(R.layout.more_row_layout, parent, false);
		TextView tvLabel = (TextView) rowView.findViewById(R.id.label);
		ImageView imIcon = (ImageView) rowView.findViewById(R.id.icon);
		TextView tvNewBadge = (TextView) rowView.findViewById(R.id.tvNew);
		tvNewBadge.setBackgroundDrawable(getLabelDrawable());

		tvLabel.setText(values.get(position));
		tvNewBadge.setText(context.getResources().getString(
				R.string.newBadge));
		String s = values.get(position);
		if (s.startsWith(context.getResources().getString(R.string.profile))) {
			imIcon.setImageResource(R.drawable.menu_ic_profile);
			tvNewBadge.setVisibility(View.VISIBLE);
		}else if (s.startsWith(context.getResources().getString(R.string.settings))) {
			imIcon.setImageResource(R.drawable.menu_settings_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources().getString(R.string.personalization))) {
			imIcon.setImageResource(R.drawable.menu_perso_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources()
				.getString(R.string.share))) {
			imIcon.setImageResource(R.drawable.menu_share_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources().getString(
				R.string.contactUs))) {
			imIcon.setImageResource(R.drawable.menu_mail_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources().getString(
				R.string.motivationCards))) {
			imIcon.setImageResource(R.drawable.menu_inapp_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources().getString(
				R.string.leaveAReviewOnThePlayStore))) {
			imIcon.setImageResource(R.drawable.menu_review_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		} else if (s.startsWith(context.getResources().getString(
				R.string.removeAds))) {
			imIcon.setImageResource(R.drawable.menu_removeads_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		}
		else if (s.startsWith(context.getResources().getString(R.string.unlockFullVersion))){
			imIcon.setImageResource(R.drawable.menu_fullversion_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		}
		else if (s.startsWith(context.getResources().getString(R.string.help))){
			imIcon.setImageResource(R.drawable.menu_help_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		}
		else if (s.startsWith(context.getResources().getString(R.string.policy))){
			imIcon.setImageResource(R.drawable.menu_policy_gray);
			tvNewBadge.setVisibility(View.INVISIBLE);
		}

		return rowView;
	}

	private Drawable getLabelDrawable() {
		if (Start.theme == Theme.GREEN){
			return ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_kwit);
		}
		else{
			return ContextCompat.getDrawable(context, R.drawable.rounded_rectangle);
		}
	}
}
