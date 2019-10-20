package net.tobano.quitsmoking.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * @author Nicolas Lett
 * 
 */
public class Splash extends Activity {

	/**
	 * Default duration for the splash screen (milliseconds)
	 */
	private static final long SPLASHTIME = 1000;

	/**
	 * for initialize the connection
	 */
//	private IabHelper mHelper;
//
//	private IInAppBillingService mService;
//
//	private ServiceConnection mServiceConn = new ServiceConnection() {
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			mService = null;
//		}
//
//		@Override
//		public void onServiceConnected(ComponentName name,
//									   IBinder service) {
//			mService = IInAppBillingService.Stub.asInterface(service);
//		}
//	};

	/**
	 * variable to notify Start if user has purchase fullVersion/noAds/packs
	 */
//	private boolean isPremium;
//	private boolean hasNoAds;
//	private boolean hasAllCards;
	private int openFromWidget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		// get intent in case of app is lunched by widget
		Intent intent = getIntent();
		openFromWidget = intent.getIntExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);

		new WaitAndInit().execute(this);
		return;
	}

    /**
     * Verifies the developer payload of a purchase.
     */
//	boolean verifyDeveloperPayload(Purchase p) {
//			String payload = p.getDeveloperPayload();
//
//        /*
//         * TODO: verify that the developer payload of the purchase is correct. It will be
//         * the same one that you sent when initiating the purchase.
//         *
//         * WARNING: Locally generating a random string when starting a purchase and
//         * verifying it here might seem like a good approach, but this will fail in the
//         * case where the user purchases an item on one device and then uses your app on
//         * a different device, because on the other device you will not have access to the
//         * random string you originally generated.
//         *
//         * So a good developer payload has these characteristics:
//         *
//         * 1. If two different users purchase an item, the payload is different between them,
//         *    so that one user's purchase can't be replayed to another user.
//         *
//         * 2. The payload must be such that you can verify it even when the app wasn't the
//         *    one who initiated the purchase flow (so that items purchased by the user on
//         *    one device work on other devices owned by the user).
//         *
//         * Using your own server to store and verify developer payloads across app
//         * installations is recommended.
//         */
//
//			return true;
//	}
    protected void startApplication() {

        // create start
        final Intent intent = new Intent(Splash.this, Start.class);
//		intent.putExtra(Constantes.IS_PREMIUM, isPremium);
//		intent.putExtra("hasnoads", hasNoAds);
//		intent.putExtra("allcards", hasAllCards);
        intent.putExtra(Start.OPEN_FROM_WIDGET, openFromWidget);
        startActivity(intent);

//		// destroy the connection
//		if (mHelper != null) mHelper.dispose();
//		mHelper = null;
//		if (mService != null && mServiceConn != null) {
//			unbindService(mServiceConn);
//		}

        // stop splash and start app
        finish();
    }

    /**
     * to load informations about in-app purchase
     */
//	protected void initialization() {

//		// Binding to inAppBillingService
//		Intent serviceIntent =
//				new Intent("com.android.vending.billing.InAppBillingService.BIND");
//		serviceIntent.setPackage("com.android.vending");
//		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
//
//		String base64EncodedPublicKey = Util.stringTransform("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgGjvqzByEKIGBYxtHQK0Kmz+IVZcpG3KCFFq1a8uALp2xn2dugp8YWDdroTduYjgPJWlT8SZCArxwsaAtNRqojxzi5Ofv6oWDqLfjVRPXYLEO0FCBjcgDpyHP9vduStRmBfiUYvikZR0mVjZIpAcM+8w5Jg5MzdHgozWsCkdAmFH8di9+xTtDQSTIOJf4ED1beXWMyr0tx2STfeoXGwoQIEOkI+xHCcDLrWFgdorZqyBjMju2BIhQ0iCMyE7zvbtMPAV2HvZCA2Vb+BSE5iSFZEb8h+BSVgOO7204mDf/cPZD5f/3insoVfh2aNQ1QQb3Tg+hreOEVeHaUEnCV+wbQIDAQAB", 0x22);
//
//		mHelper = new IabHelper(getBaseContext(), Util.stringTransform(base64EncodedPublicKey, 0x22));
//
//		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//			public void onIabSetupFinished(IabResult result) {
//				Log.d(Constantes.TAG_BILLING, "Setup finished");
//				if (!result.isSuccess()) {
//					// Oh noes, there was a problem.
//					Log.d("BillingLog", "Problem setting up In-app Billing: " + result);
//				}
//				// Hooray, IAB is fully set up!
//
//				// Have we been disposed of in the meantime? If so, quit.
//				if (mHelper == null)
//					return;
//
//				Log.d(Constantes.TAG_BILLING, "Setup successful. Querying inventory.");
//				mHelper.queryInventoryAsync(Connectivity.isConnected(getApplicationContext()), mGotInventoryListener);
//			}
//		});
//	}

    // Listener that's called when we finish querying the items and subscriptions we own
//	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//		@Override
//		public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
//
//			Log.d(Constantes.TAG_BILLING, "Query inventory finished.");
//
//			// Have we been disposed of in the meantime? If so, quit.
//			if (mHelper == null) {
//				return;
//			}
//
//			// Is it a failure?
//			if (result.isFailure()) {
//				return;
//			}
//
//			if (inventory.hasPurchase("android.test.purchased")) {
//				mHelper.consumeAsync(inventory.getPurchase("android.test.purchased"), null);
//			}
//
//			//TODO morceau de code à mettre avant appel à queryInventoryAsync
//
//			/*
//			* Check for items we own. Notice that for each purchase, we check
//			* the developer payload to see if it's correct! See
//			* verifyDeveloperPayload().
//			*/
//
//			// Do we have the premium upgrade?
//			Purchase premiumPurchase = inventory.getPurchase(Constantes.SKU_PREMIUM);
//			isPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
//			Log.d(Constantes.TAG_BILLING, "User is " + (isPremium ? "PREMIUM" : "NOT PREMIUM"));
//			// Do we have the no ads option?
//			Purchase noAdsPurchase = inventory.getPurchase(Constantes.SKU_NO_ADS);
//			hasNoAds = (noAdsPurchase != null && verifyDeveloperPayload(noAdsPurchase));
//			Log.d(Constantes.TAG_BILLING, "User has " + (hasNoAds ? "NO ADS" : "ADS"));
//
//			// Do we have the all_cards pack of motivation cards?
//			Purchase allCardsPurchase = inventory.getPurchase(Constantes.SKU_MOTIVATION_ALL_CARDS);
//			hasAllCards = allCardsPurchase != null
//					&& verifyDeveloperPayload(allCardsPurchase);
//			Log.d(Constantes.TAG_BILLING, "User has " + (hasAllCards ? "all cards" : "not all cards"));
//
//			Log.d(Constantes.TAG_BILLING, "Initial inventory query finished; enabling main UI.");
//
//			startApplication();
//		}
//	};

	/**
	 * Async Task
	 */
	private class WaitAndInit extends AsyncTask<Context, Void, Void>{

		@Override
		protected Void doInBackground(Context... params) {

			try{
				// wait one second
				Thread.sleep(SPLASHTIME);

				// initiate mHelper
                //initialization();
                startApplication();
                }
			catch (Exception e){
				//
			}

			return null;
		}
	}

}
