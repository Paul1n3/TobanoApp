package net.tobano.quitsmoking.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.Locale;

public class MoreListFragment extends ListFragment {

	private MoreArrayAdapter mAdapter;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ArrayList<String> values = new ArrayList<>();
		if (((Start) getActivity()).showSocial)
			values.add(getResources().getString(R.string.profile));
		values.add(getResources().getString(R.string.settings));
		values.add(getResources().getString(R.string.personalization));
		values.add(getResources().getString(R.string.contactUs));
		if (!Start.isPremium && !Start.hasAllCards) {
			values.add(getResources().getString(R.string.motivationCards));
		}
		values.add(getResources().getString(R.string.leaveAReviewOnThePlayStore));
		values.add(getResources().getString(R.string.share));
		if (!Start.isPremium){
			values.add(getResources().getString(R.string.unlockFullVersion));
		}
		if (!Start.hasNoAds && !Start.isPremium){
			values.add(getResources().getString(R.string.removeAds));
		}
		values.add(getResources().getString(R.string.help));
		values.add(getResources().getString(R.string.policy));

		mAdapter = new MoreArrayAdapter(getActivity(), values);
		setListAdapter(mAdapter);
		getView().setBackgroundColor(Color.WHITE);
		getListView().setDivider(new ColorDrawable(0x99EDECED)); // 0xAARRGGBB
		getListView().setDividerHeight(2);

		if (Start.prefs.getBoolean(Constantes.FIRST_LAUNCH, true)) {
			startSettings();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((Start)getActivity()).playSound(Start.strClick);
		String item = (String) getListAdapter().getItem(position);
		if (item.equals(getResources().getString(R.string.profile))) {
			// Ajout du fragment à la liste de fragments
			startProfileDetails();
		}
		else if (item.equals(getResources().getString(R.string.settings))) {
			// Ajout du fragment à la liste de fragments
			startSettings();
		}
		else if (item.equals(getResources().getString(
				R.string.leaveAReviewOnThePlayStore))) {
			FlurryAgent.logEvent("Leave_review_button");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=net.tobano.quitsmoking.app"));
			// Handle Android environment without Play Store
			if (intent.resolveActivity(getActivity().getPackageManager()) != null)
				startActivity(intent);
		}
		else if (item.equals(getResources().getString(R.string.share))) {
			FlurryAgent.logEvent("Share_button_more");
			String shareBody = getResources().getString(
					R.string.ILikeThisAppAndIThinkYouShouldTryItToo)
					+ "\n" + getResources().getString(R.string.tabanoTag) + "\n"
					+ getResources().getString(R.string.tabanoWebsite);
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getResources().getString(R.string.IQuitSmokingWithKwit));
			sharingIntent
					.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
		}
		else if (item.equals(getResources().getString(R.string.contactUs))) {
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			FlurryAgent.logEvent("Email_support");
			emailIntent.setType("text/plain");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { "nosmoke.help@gmail.com" });
			String appName = getResources().getString(R.string.app_name);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					appName);

			startActivity(Intent.createChooser(emailIntent, getResources()
					.getString(R.string.shareByEmail)));
		}
		else if (item.equals(getResources().getString(R.string.personalization))) {
			Log.d("PersonalizationCards", "PersonalizationCards");
			FlurryAgent.logEvent("Open_perso_color");
			startPersonalization();
		}
		else if (item.equals(getResources().getString(R.string.motivationCards))){
			Log.d("MotivationCards", "appel de startMotivationCards");
			FlurryAgent.logEvent("Open_cards_list");
			startMotivationCards();
		}
//		else if (item.equals(getResources().getString(R.string.removeAds))){
//			Log.d(Constantes.TAG_BILLING, "Demande d'achat no ads");
//			// (arbitrary) request code for the purchase flow
//			int RC_REQUEST = 10001;
//			String payload = "";
//			FlurryAgent.logEvent("Remove_ads");
//			((Start) getActivity()).getmHelper().launchPurchaseFlow(getActivity(), Constantes.SKU_NO_ADS, RC_REQUEST,
//					((Start) getActivity()).mPurchaseFinishedListener, payload);
//		}
//		else if (item.equals(getResources().getString(R.string.unlockFullVersion))){
//			Start.startStoreToBuyFullVersion();
//		}
		else if (item.equals(getResources().getString(R.string.help))){
			FlurryAgent.logEvent("Help");
			Intent intent = new Intent(getActivity(), Tutorial.class);
			startActivity(intent);
		}
		else if (item.equals(getResources().getString(R.string.policy))) {
			FlurryAgent.logEvent("Policy");
			Intent browserIntent;
			if (Locale.getDefault().getLanguage().toLowerCase().equals("fr")) {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/tobano-app/français"));
			} else {
				browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/tobano-app"));
			}
			startActivity(browserIntent);
		}
	}

	private void startPersonalization() {
		Personalization newFragment = new Personalization();

		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();

		transaction.setCustomAnimations(R.anim.slide_left_in,
				R.anim.slide_left_out, R.anim.slide_right_in,
				R.anim.slide_right_out);

		transaction.replace(R.id.fragment_container, newFragment, MoreFragment.TAG_PERSONALIZATION);
		transaction.addToBackStack(MoreFragment.TAG_PERSONALIZATION);

		transaction.commit();
	}

	private void startMotivationCards() {
		MotivationCards newFragment = new MotivationCards();
		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();

		transaction.setCustomAnimations(R.anim.slide_left_in,
				R.anim.slide_left_out, R.anim.slide_right_in,
				R.anim.slide_right_out);

		transaction.replace(R.id.fragment_container, newFragment, MoreFragment.TAG_MOTIVATION_CARDS);
		transaction.addToBackStack(MoreFragment.TAG_MOTIVATION_CARDS);

		transaction.commit();

	}

	private void startProfileDetails() {
		ProfileDetails newFragment = new ProfileDetails();
		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();

		transaction.setCustomAnimations(R.anim.slide_left_in,
				R.anim.slide_left_out, R.anim.slide_right_in,
				R.anim.slide_right_out);

		transaction.replace(R.id.fragment_container, newFragment, MoreFragment.TAG_SETTINGS);
		transaction.addToBackStack(MoreFragment.TAG_SETTINGS);

		transaction.commit();

	}

	private void startSettings() {
		Settings newFragment = new Settings();
		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();

		transaction.setCustomAnimations(R.anim.slide_left_in,
				R.anim.slide_left_out, R.anim.slide_right_in,
				R.anim.slide_right_out);

		transaction.replace(R.id.fragment_container, newFragment, MoreFragment.TAG_SETTINGS);
		transaction.addToBackStack(MoreFragment.TAG_SETTINGS);

		transaction.commit();

	}

	public void updateUI() {
		Log.d(Constantes.TAG_BILLING, "MoreListFragment updateUI called after purchase");
		mAdapter.notifyDataSetChanged();
		Log.d(Constantes.TAG_BILLING, "MoreListFragment updateUI notifysetdatasetchanged called");
	}
}
