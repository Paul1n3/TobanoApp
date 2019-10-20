package net.tobano.quitsmoking.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.tobano.quitsmoking.app.util.Theme;

public class MotivationCards extends Fragment {

	private Boolean hasInternetAccess = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.motivation_cards_store, container, false);

//		Log.d("MotivationCards", "début de motivation cards");
//		String titleValue;
//		String priceValue;
//		try {
//			titleValue = Start.skuDetailsAllCards.getTitle().replaceAll("\\(.*\\)", "");
//			priceValue = Start.skuDetailsAllCards.getPrice();
//			hasInternetAccess = true;
//		}
//		catch (Exception e){
//			hasInternetAccess = false;
//			titleValue = getResources().getString(R.string.packAllCards);
//			priceValue = "";
//		}
//		TextView title = (TextView) v.findViewById(R.id.title);
//		title.setText(titleValue);
//
//		Button btnBuy = (Button) v.findViewById(R.id.btnBuy);
//		btnBuy.setBackground(getButtonBackground());
//		if (!priceValue.isEmpty()) {
//			btnBuy.setText(btnBuy.getText() + String.format(" (%s)", priceValue));
//		}
//		btnBuy.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View v) {
//				FlurryAgent.logEvent("buy_encouragements_cards");
//				onBuyClick();
//			}
//		});
//
//		Log.d("MotivationCards", "fin de la mise en place de motivation cards");

		return v;
	}

//	public void onBuyClick() {
//		if (!Start.isPremium && !Start.hasAllCards) {
//			if (hasInternetAccess) {
//				purchase(Constantes.SKU_MOTIVATION_ALL_CARDS);
//			}
//		}
//	}

//	private void purchase(String sku) {
//		// (arbitrary) request code for the purchase flow
//		int RC_REQUEST = 10001;
//		String payload = "";
//		try{
//			((Start) getActivity()).getmHelper().launchPurchaseFlow(getActivity(), sku, RC_REQUEST,
//					((Start) getActivity()).mPurchaseFinishedListener, payload);
//		}
//		catch(Exception e){
//			Log.d("MotivationCards", "too many AsyncTask are started : "+e.toString());
//		}
//	}

	public void updateUI() {
		Log.d(Constantes.TAG_BILLING, "MotivationCards updateUI called after purchase");
		// TODO : notifyDataSetChanged();
		Log.d(Constantes.TAG_BILLING, "MotivationCards updateUI notifysetdatasetchanged called");
	}

	private Drawable getButtonBackground() {
		if (Start.theme == Theme.GREEN) {
			return ContextCompat.getDrawable(getActivity(), R.drawable.btn_full_green);
		}
		else {
			return ContextCompat.getDrawable(getActivity(), R.drawable.btn_full_tabono);
		}
	}
}
