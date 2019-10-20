package net.tobano.quitsmoking.app;

import android.annotation.TargetApi;
import android.app.ActionBar.Tab;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import net.tobano.quitsmoking.app.fragment.RecentPostsFragment;
import net.tobano.quitsmoking.app.models.Comment;
import net.tobano.quitsmoking.app.models.Forum;
import net.tobano.quitsmoking.app.models.Language;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.models.User;
import net.tobano.quitsmoking.app.util.AchievementsNotificationsManager;
import net.tobano.quitsmoking.app.util.MoneyNotificationsManager;
import net.tobano.quitsmoking.app.util.OnGetDataListener;
import net.tobano.quitsmoking.app.util.Theme;
import net.tobano.quitsmoking.app.util.WillpowerNotificationsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is the starting Activity when the application is launched.
 * The main tabs are generated.
 *
 * @author Nicolas Lett
 */
public class Start extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, OnGetDataListener {

    public static int TAG_FORUM_MAINTENANCE = 1337;

    // SOCIAL VARIABLES
    protected static final String TAG_GOOGLE = "GoogleActivity";
    protected static final int RC_SIGN_IN = 9001;
    // [START declare_auth]
    protected FirebaseAuth mAuth;
    // [END declare_auth]
    // [START declare_database_ref]
    protected DatabaseReference mDatabase;
    // [END declare_database_ref]
    private boolean signInAnonymouslyInProgress = false;
    protected Language mLanguage;
    protected GoogleApiClient mGoogleApiClient;
    protected FirebaseUser mCurrentUser;
    protected User mUser;
    protected HashMap<String, Forum> forums;
    public static int FRAGMENT_DEFAULT = 0;

    public ProgressDialog mProgressDialog;

    private Handler mHandler;

    public static Context context;

    public static boolean firstLaunch = true;
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;

    public static long diff[] = new long[]{0, 0, 0, 0};
    public static long diffInSeconds = 0;
    public static long interval = 0;

    public static Date quittingDate = new Date();

    public static int cigarettesPerDay = 0;
    public static double priceOfAPack = 0.0;
    public static int cigarettesPerPack = 0;

    public static double moneySaved = 0.0;
    public static double cigarettesNotSmoked = 0;
    public static double cigarettesPerSecond = 0;
    public static double mgOfCarbonMonoxidePerSeconds = 0;
    public static double timeToGetOneMg = 0;
    public static double lifeGainEachSecond = 0;
    public static double timeToGainOneSecond = 0;
    public static int cigarettesSmoked = 0; // Willpower cigarettes
    public static int cravings = 0; // Willpower cravings
    public static float willpowerCounter = 1.0f;
    public static double priceOfACigarette = 0.0;
    public static double lifeSaved = 0.0;
    public static double coSaved = 0.0;
    public static ArrayList<String> ownGoalTitles;
    public static ArrayList<String> ownGoal;
    public static String wallpaper;

    public static int appVersionCode;
    public static int version;
    public static Theme theme = Theme.BLUE;
    public static String currency = "€";

    public static String level_health_available_for_unlock = "-2";
    public static String level_wellness_available_for_unlock = "-2";
    public static String level_time_available_for_unlock = "-2";
    public static String level_money_available_for_unlock = "-2";
    public static String level_cigarette_available_for_unlock = "-2";
    public static String level_life_available_for_unlock = "-2";
    public static String level_co_available_for_unlock = "-2";
    public static String user_level_willpower = "-2";

    public static String user_level_health;
    public static String user_level_wellness;
    public static String user_level_time;
    public static String user_level_money;
    public static String user_level_cigarette;
    public static String user_level_life;
    public static String user_level_co;

    public static int userXP = 0;

    public static String user_level = "0";
    public static String user_rank_value = "0";
    public static String user_rank_text = "";

    // flag needed when permission to read previous Tobano profile from old app has been refused.
    // if true during onresume, reinit app
    private boolean needInit = false;

    // Sets an ID variable for the notifications
    public static int mCurrentNotificationId = 001;
    // private DemoCollectionPagerAdapter mPagerAdapter;
    private static ViewPager mViewPager;
    private TabLayout tabs;
    private MenuPagerAdapter adapter;

    // To handle the shake event (motivation cards)
    private boolean motivationCardDisplayed = false;
    private Dialog dialog;

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private ArrayList<AchievementLevelAndNotificationPreference> achievementsLevelsNotifications;
    private ArrayList<WillpowerLevelAndNotificationPreference> willpowerLevelsNotifications;

    // play sounds
    private SoundPool sounds;
    private int sStillLock;
    public static String strStillLock = "still_lock";
    private int sUnlock;
    public static String strUnlock = "unlock";
    private int sHideUnlocked;
    public static String strHideUnlocked = "hide_unlocked";
    private int sSlide;
    public static String strSlide = "silde";
    private int sInitialize;
    public static String strInitialize = "initialize";
    private int sClick;
    public static String strClick = "click";
    private int sError;
    public static String strError = "error";
    private int sCraving;
    public static String strCraving = "craving";
    private int sSmoke;
    public static String strSmoke = "smoke";
    private int sGameOver;
    public static String strGameOver = "game_over";
    public static int volume;

    // from widget
    public static String OPEN_FROM_WIDGET = "WIDGET_POWER";
    public static int FRAGMENT_DASHBOARD = 0;
    public static int FRAGMENT_ACHIEVEMENTS = 1;
    public static int FRAGMENT_SOCIAL = 2;
    public static int FRAGMENT_WILLPOWER = 3;
    public static int FRAGMENT_MORE = 4;
    protected Boolean showSocial = false;
    private int fragmentWantedByWidget;

    // to display or not a dialog before add cigarette when smoke is clicked
    public static boolean willpowerWarning;

    private FirebaseAnalytics mFirebaseAnalytics;

    // IAB info
    // set the variable to true, because it is available for everyone now
    public static boolean isPremium = true;
    public static boolean hasNoAds = true;
    public static boolean hasAllCards = true;

    // IAB tabano
    // not use anymore, because all is free
    // public static SkuDetails skuDetailsAllCards;
    // public static SkuDetails skuDetailsPremium;
    // public static SkuDetails skuDetailsNoAds;
    // Inventory has been deleted, see in git /utils
    // public static Inventory inventoryInformation;
    // idem
    // public static InterstitialAd mInterstitialAd;
    //    private IabHelper mHelper;
    private IInAppBillingService mService; // TODO DELETE ?

    private final int REQUEST_READ_STORAGE = 121;

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > 10) {
                // Toast toast = Toast.makeText(getApplicationContext(),
                // "Device has shaken.", Toast.LENGTH_LONG);
                // toast.show();
                displayMotivationCard();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private static final String TAG = "Start";
    protected Long minVersion = 0L;
    private FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    public void displayMotivationCard() {
        if (dialog != null && !dialog.isShowing())
            motivationCardDisplayed = false;
        if (!motivationCardDisplayed) {
            motivationCardDisplayed = true;

            MotivationCardsDialog dialogMotivationCard = new MotivationCardsDialog(
                    this);
            dialog = dialogMotivationCard.displayMotivationCards();
            dialog.show();
            Map<String, String> drawMotivCardParams = new HashMap<String, String>();
            drawMotivCardParams.put("source", "shake");

            FlurryAgent.logEvent("Draw_motivation_card", drawMotivCardParams,
                    false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main);

        context = this;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionCode = -1;
        }

        // init Ads
//        if(!Start.isPremium && !Start.hasNoAds) {
//            Ads.initAds(getBaseContext());
//            Ads.requestNewAd();
//            mInterstitialAd.setAdListener(new AdListener() {
//                @Override
//                public void onAdClosed() {
//                    Log.d("ADS", "Nouvelle pub demandée !");
//                    Ads.requestNewAd();
//                }
//            });
//        }

        // get information about in app purchase
        Intent intent = getIntent();

        // Make Tobano Free
        isPremium = true;// intent.getBooleanExtra(Constantes.IS_PREMIUM, false);
        //        if (BuildConfig.DEBUG) {
        //            isPremium = true;
        //        }
        // hasNoAds = intent.getBooleanExtra("hasnoads", false);
        // hasAllCards = intent.getBooleanExtra("allcards", false);

        fragmentWantedByWidget = intent.getIntExtra(OPEN_FROM_WIDGET, FRAGMENT_DEFAULT);

        // Binding to inAppBillingService
        // Intent serviceIntent =
        //        new Intent("com.android.vending.billing.InAppBillingService.BIND");
        // serviceIntent.setPackage("com.android.vending");
        // bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // String base64EncodedPublicKey = Util.stringTransform("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgGjvqzByEKIGBYxtHQK0Kmz+IVZcpG3KCFFq1a8uALp2xn2dugp8YWDdroTduYjgPJWlT8SZCArxwsaAtNRqojxzi5Ofv6oWDqLfjVRPXYLEO0FCBjcgDpyHP9vduStRmBfiUYvikZR0mVjZIpAcM+8w5Jg5MzdHgozWsCkdAmFH8di9+xTtDQSTIOJf4ED1beXWMyr0tx2STfeoXGwoQIEOkI+xHCcDLrWFgdorZqyBjMju2BIhQ0iCMyE7zvbtMPAV2HvZCA2Vb+BSE5iSFZEb8h+BSVgOO7204mDf/cPZD5f/3insoVfh2aNQ1QQb3Tg+hreOEVeHaUEnCV+wbQIDAQAB", 0x22);

        // Log.d(Constantes.TAG_BILLING, "Creating IAB helper.");
        // mHelper = new IabHelper(this, Util.stringTransform(base64EncodedPublicKey, 0x22));

        // mHelper.enableDebugLogging(true);

        //        Log.d(Constantes.TAG_BILLING, "Starting setup");
        //        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
        //            public void onIabSetupFinished(IabResult result) {
        //                Log.d(Constantes.TAG_BILLING, "Setup finished");
        //                if (!result.isSuccess()) {
        //                    // Oh noes, there was a problem.
        //                    Log.d("BillingLog", "Problem setting up In-app Billing: " + result);
        //                }
        //                // Hooray, IAB is fully set up!
        //
        //                // Have we been disposed of in the meantime? If so, quit.
        //                if (mHelper == null)
        //                    return;
        //
        //                Log.d(Constantes.TAG_BILLING, "Setup successful. Querying inventory.");
        //                //mHelper.queryInventoryAsync(mGotInventoryListener);
        //                List additionalSkuList = new ArrayList();
        //
        //                additionalSkuList.add(Constantes.SKU_PREMIUM);
        //                additionalSkuList.add(Constantes.SKU_NO_ADS);
        //                additionalSkuList.add(Constantes.SKU_MOTIVATION_ALL_CARDS);
        //                mHelper.queryInventoryAsync(Connectivity.isConnected(getApplicationContext()), additionalSkuList,
        //                        mGotInventoryListener);
        //            }
        //        });

        initFlurry();
        initGoogleAnalytics();

        // Set language as a user property on all opens.
        String userCurrentLanguage = Locale.getDefault().getISO3Language();
        Log.d(TAG, "setLanguage: " + userCurrentLanguage);
        mFirebaseAnalytics.setUserProperty("user_language", userCurrentLanguage);

        initializationPreference();

        initSounds();

        initiateUserLevel();

        initViewPager();
        showFreePopup();

        initiateMotivationCards();

        willpowerWarning = prefs.getBoolean(Constantes.DIALOG_BEFORE_SMOKE, true);

        mHandler = new Handler();
        mHandler.post(mUpdate);
    }

    private void showFreePopup() {
        Boolean containsIsPremium = Start.prefs.contains(Constantes.IS_PREMIUM);
        if (!containsIsPremium) {
            TobanoFreeDialog.newInstance(this).show();
        }
        editor.putBoolean(Constantes.IS_PREMIUM, false);
    }

    private void initGoogleAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(Constantes.TAG_BILLING, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
//        if (mHelper == null) return;
//
//        // Pass on the activity result to the helper for handling
//        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
//            // not handled, so handle it ourselves (here's where you'd
//            // perform any handling of activity results not related to in-app
//            // billing...
//            super.onActivityResult(requestCode, resultCode, data);
//        } else {
//            Log.d(Constantes.TAG_BILLING, "onActivityResult handled by IABUtil.");
//        }
//    }

    // Listener that's called when we finish querying the items and
// subscriptions we own
//    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//        @Override
//        public void onQueryInventoryFinished(final IabResult result,
//                                             final Inventory inventory) {
//            Log.d(Constantes.TAG_BILLING, "Query inventory finished.");
//
//            // Have we been disposed of in the meantime? If so, quit.
//            if (mHelper == null) {
//                return;
//            }
//
//            // Is it a failure?
//            if (result.isFailure()) {
//                return;
//            }
//
//            if (inventory.hasPurchase("android.test.purchased")) {
//                mHelper.consumeAsync(inventory.getPurchase("android.test.purchased"), null);
//            }
//
//            inventoryInformation = inventory;
//
//            skuDetailsPremium = inventory.getSkuDetails(Constantes.SKU_PREMIUM);
//            skuDetailsNoAds = inventory.getSkuDetails(Constantes.SKU_NO_ADS);
//            skuDetailsAllCards = inventory.getSkuDetails(Constantes.SKU_MOTIVATION_ALL_CARDS);
//
//            Log.d(Constantes.TAG_BILLING, "Query inventory was successful.");
//        }
//    };

    /** Verifies the developer payload of a purchase. */
//    boolean verifyDeveloperPayload(Purchase p) {
//        String payload = p.getDeveloperPayload();
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
//        return true;
//    }

//    // Called when consumption is complete
//    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
//        public void onConsumeFinished(Purchase purchase, IabResult result) {
//            Log.d(Constantes.TAG_BILLING, "Consumption finished. Purchase: " + purchase + ", result: " + result);
//
//            // if we were disposed of in the meantime, quit.
//            if (mHelper == null) return;
//
//            // We know this is the "gas" sku because it's the only one we consume,
//            // so we don't check which sku was consumed. If you have more than one
//            // sku, you probably should check...
//            if (result.isSuccess()) {
//                // successfully consumed, so we apply the effects of the item in our
//                // game world's logic, which in our case means filling the gas tank a bit
//                Log.d(Constantes.TAG_BILLING, "Consumption successful. Provisioning.");
//                //mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
//                //alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
//            }
//            else {
//                Log.d(Constantes.TAG_BILLING,"Error while consuming: " + result);
//            }
//
//            Log.d(Constantes.TAG_BILLING, "End consumption flow.");
//        }
//    };

    // Callback for when a purchase is finished
//    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//            Log.d(Constantes.TAG_BILLING, "Purchase finished: " + result + ", purchase: " + purchase);
//
//            // if we were disposed of in the meantime, quit.
//            if (mHelper == null) return;
//
//            if (result.isFailure()) {
//                Log.d(Constantes.TAG_BILLING, "Error purchasing: " + result);
//                return;
//            }
//            if (!verifyDeveloperPayload(purchase)) {
//                Log.d(Constantes.TAG_BILLING, "Error purchasing. Authenticity verification failed.");
//                return;
//            }
//
//            Log.d(Constantes.TAG_BILLING, "Purchase successful.");
//
//            if(BuildConfig.DEBUG){
//                // Pour tester l'achat, si problème de query inventory, update verifyPurchase() dans Security.java
//                if (purchase.getSku().equals("android.test.purchased")) {
//                    hasNoAds = true;
//                    Log.d(Constantes.TAG_BILLING, "Purchase detected is noAds. Update of hasNoAds flag");
//                    restartApp();
//                }
//            }
//            if (purchase.getSku().equals(Constantes.SKU_MOTIVATION_ALL_CARDS)) {
//                hasAllCards = true;
//                Log.d(Constantes.TAG_BILLING, "Purchase detected is all cards. Update of hasAllCards flag");
//            }
//            else if (purchase.getSku().equals(Constantes.SKU_PREMIUM)) {
//                isPremium = true;
//                Log.d(Constantes.TAG_BILLING, "Purchase detected is pro. Update of isPremium flag");
//                saveIsPremiumPreferences();
//                restartApp();
//            }
//            else if (purchase.getSku().equals(Constantes.SKU_NO_ADS)) {
//                hasNoAds = true;
//                Log.d(Constantes.TAG_BILLING, "Purchase detected is noAds. Update of hasNoAds flag");
//                restartApp();
//            }
//
//            if (adapter.getItem(FRAGMENT_MORE).isAdded()) {
//                MotivationCards f;
//                f = (MotivationCards) adapter.getItem(FRAGMENT_MORE).getFragmentManager().findFragmentByTag(MoreFragment.TAG_MOTIVATION_CARDS);
//                if(f != null){
//                    f.updateUI();
//                }
//            }
//        }
//    };

//    private void restartApp() {
//        Intent intent = new Intent(getApplicationContext(), Splash.class);
//        ((Activity)context).finish();
//        startActivity(intent);
//    }

//    public void saveIsPremiumPreferences() {
//        editor.putBoolean(Constantes.IS_PREMIUM, true);
//        editor.commit();
//    }
    public void saveIsPremiumPreferences(boolean v) {
        editor.putBoolean(Constantes.IS_PREMIUM, v);
        editor.commit();
    }

    private void updateTabFragmentIdByPhoneLanguage() {
        String currentUserLanguage = Locale.getDefault().getLanguage().toLowerCase();
        if (currentUserLanguage.equals("en") || currentUserLanguage.equals("fr")) {
            showSocial = true;
            FRAGMENT_SOCIAL = 2;
            FRAGMENT_WILLPOWER = 3;
            FRAGMENT_MORE = 4;
        } else {
            // update fragment IDs after social
            showSocial = false;
            FRAGMENT_WILLPOWER = 2;
            FRAGMENT_MORE = 3;
        }
    }

    private void initViewPager() {
        // adapt the fragment id depending the user language
        updateTabFragmentIdByPhoneLanguage();
        // Initialize the ViewPager and set an adapter
        mViewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new MenuPagerAdapter(getSupportFragmentManager());
//        adapter.addFrag(new Profile(), Profile.class.getName());
        adapter.addFrag(new Dashboard(), Dashboard.class.getName());
        adapter.addFrag(new Achievements(), Achievements.class.getName());
        if (showSocial) {
            adapter.addFrag(new RecentPostsFragment(), RecentPostsFragment.class.getName());
        }
        adapter.addFrag(new Willpower(), Willpower.class.getName());
        adapter.addFrag(new MoreFragment(), MoreFragment.class.getName());
        mViewPager.setAdapter(adapter);

        if (firstLaunch) {
//            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//            if (!hasPermission) {
//                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int choice) {
//                        switch (choice) {
//                            case DialogInterface.BUTTON_POSITIVE:
//                                ActivityCompat.requestPermissions((Activity)context,
//                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                        REQUEST_READ_STORAGE);
//                                break;
//                            case DialogInterface.BUTTON_NEGATIVE:
//                                mViewPager.setCurrentItem(FRAGMENT_MORE);
//                                break;
//                        }
//                    }
//                };
//
//                // setup the alert builder
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setCancelable(false);
//                builder.setTitle(getResources().getString(R.string.tobanoProfileTitle));
//                builder.setMessage(getResources().getString(R.string.tobanoProfileText));
//
//                // add a button
//                builder.setPositiveButton(android.R.string.yes, dialogClickListener);
//                builder.setNegativeButton(android.R.string.no, dialogClickListener);
//
//                // create and show the alert dialog
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//            else{
                SharedPreferences preferences = this.getSharedPreferences(Constantes.PREF_NAME, Context.MODE_PRIVATE);
                restoreSharedPreferencesToFile(preferences);
//            }
        }
        else{
            // choose which fragment is opened (useful if app opened by widget)
            mViewPager.setCurrentItem(fragmentWantedByWidget);
            // display ads if free version without premium add-on
//            if(!Start.isPremium && !Start.hasNoAds) {
//                // afficher une pub une fois sur 4
//                ArrayList<Boolean> lucky = new ArrayList<>();
//                lucky.add(true);
//                for(int i = 1 ; i < Ads.ONE_OUT_OF ; i++){
//                    lucky.add(false);
//                }
//                Random rand = new Random();
//                int randomNum = rand.nextInt((Ads.ONE_OUT_OF - 1) + 1) + 0;
//                if (lucky.get(randomNum)) {
//                    mInterstitialAd.setAdListener(new AdListener(){
//                        public void onAdLoaded(){
//                            Ads.showAds();
//                        }
//                    });
//                }
//            }
        }

        // Bind the tabs to the ViewPager
        tabs = (TabLayout) findViewById(R.id.tabs);
        updateTabsColor();
        tabs.removeAllTabs();
        tabs.setupWithViewPager(mViewPager);
        tabs.getTabAt(FRAGMENT_DASHBOARD).setIcon(MenuPagerAdapter.getPageIconResId(FRAGMENT_DASHBOARD));
        tabs.getTabAt(FRAGMENT_ACHIEVEMENTS).setIcon(MenuPagerAdapter.getPageIconResId(FRAGMENT_ACHIEVEMENTS));
        if (showSocial) {
            tabs.getTabAt(FRAGMENT_SOCIAL).setIcon(MenuPagerAdapter.getPageIconResId(FRAGMENT_SOCIAL));
        }
        tabs.getTabAt(FRAGMENT_WILLPOWER).setIcon(MenuPagerAdapter.getPageIconResId(FRAGMENT_WILLPOWER));
        tabs.getTabAt(FRAGMENT_MORE).setIcon(MenuPagerAdapter.getPageIconResId(FRAGMENT_MORE));

        mViewPager.getCurrentItem();
    }

    private void updateTabsColor(){
        if (theme == Theme.GREEN){
            tabs.setBackgroundColor(getResources().getColor(R.color.kwit));
        }
        else {
            tabs.setBackgroundColor(getResources().getColor(R.color.tabano));
        }
    }

    private void initiateMotivationCards() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    public Fragment getFragment(int position) {
        return adapter.getItem(position);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (hasInternetConnection() && showSocial) {
            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();
            startSocial(this);
        }
    }

    @Override
    protected void onStop() {
        // // Stop the analytics tracking
        // GoogleAnalytics.getInstance(this).reportActivityStop(this);

        super.onStop();

        //if currently logged in as anonymous, delete that account to clean DB and then GSign in
        if(mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
            Log.d("ANONYMOUS", "delete");
            mAuth.getCurrentUser().delete();
        }

        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

        updateWidget();
    }

    // This runnable is used to refresh the statistics and the profile
    // information on live (every seconds)
    protected Runnable mUpdate = new Runnable() {
        public void run() {
            refreshData();
            mHandler.postDelayed(this, 1000);
        }
    };

    // Executed each second to refresh app data
    public void refreshData() {
        // update variable
        // Stop time
        Date start = quittingDate;
        Date end = new Date();
        diffInSeconds = (end.getTime() - start.getTime()) / 1000;
        if (diffInSeconds < 0)
            diffInSeconds = 0;
        interval = diffInSeconds;
        long[] dhms = Util.convertInDayHourMinSec(diffInSeconds);
        diff[3] = dhms[3]; // seconds
        diff[2] = dhms[2]; // minutes
        diff[1] = dhms[1]; // hours
        diff[0] = dhms[0]; // days
        // cigarettes not smoked
        cigarettesNotSmoked = ((double) cigarettesPerDay / (24 * 60 * 60))
                * interval - cigarettesSmoked;
        // life saved
        lifeSaved = cigarettesNotSmoked * 660;
        // CO saved
        coSaved = cigarettesNotSmoked * 10;
        // money saved
        moneySaved = cigarettesNotSmoked * priceOfACigarette;

        // Lecture des préférences pour récupérer les derniers niveaux
        // déblocables pour chaque catégorie
        updateBadgeOfCategories();

        if (prefs.getLong(Constantes.DATE_LAST_CIGARETTE_ADDED, 0) != 0) {
            int currentLevel = Integer.parseInt(user_level_willpower);
            willpowerCounter = prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f);
            user_level_willpower = String.valueOf(Util.getLevelByWillpower());
            if (currentLevel != Integer.parseInt(user_level_willpower)) {
                // update willpower view
                ((Willpower) adapter.getItem(FRAGMENT_WILLPOWER)).updateInformations();
                // ((Profile) fragments.get(FRAGMENT_DEFAULT)).refreshUserInterface();
            }
        }
    }

    public void updateBadgeOfCategories(){
        String levelAvailableForUnlock;
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(0));
        level_health_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(1));
        level_wellness_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(2));
        level_time_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(3));
        level_money_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(4));
        level_cigarette_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(5));
        level_life_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
        levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(6));
        level_co_available_for_unlock = prefs.getString(levelAvailableForUnlock, "0");
    }

    private void updateWillpowerCounter(Float currentWillpower) {
        editor.putFloat(Constantes.WILLPOWER_COUNTER, currentWillpower).commit();
    }

    /**
     * Method setting the alarm that will update the level_available_for_unlock
     * preference variable and send the notification after "seconds" seconds
     *
     * @param level
     * @param idCategory
     * @param title
     * @param text
     * @param seconds
     */
    @TargetApi(23)
    public void setAlarmAchievementLevelAndNotification(int level,
                                                               int idCategory, String title, String text, long seconds,
                                                               final int _id) {

        Intent intent = new Intent(this,
                NotificationsAchievementsBroadcastReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("category", Util.achievementsCategories.get(idCategory));
        intent.putExtra("level", String.valueOf(level));
        intent.putExtra("id", _id);

        //System.out.println("Creation alarm: category = " + Util.achievementsCategories.get(idCategory) + " level = " + level + " in " + String.valueOf(seconds) + " seconds");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), _id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Util.setAlarm(alarmManager, AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (1000 * seconds), pendingIntent);
    }

    /**
     * Method setting the alarm that will update the willpowerCounter
     * (currentWillpower) preference variable and send the notification after
     * "seconds" seconds
     *
     * @param level
     * @param title
     * @param text
     * @param seconds
     */
    @TargetApi(23)
    public void setAlarmWillpowerLevelAndNotification(int level, String title,
                                                             String text, long seconds, final int _id) {

        Intent intent = new Intent(this,
                NotificationsWillpowerBroadcastReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("level", String.valueOf(level));
        intent.putExtra("id", _id);

        //System.out.println("Creation alarm: willpower level = " + level + " in " + String.valueOf(seconds) + " seconds");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), _id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Util.setAlarm(alarmManager, AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (1000 * seconds), pendingIntent);
    }

    /**
     * Method used to add a notification to the shared preferences before
     * setting the alarm The alarms have to be recreated after each boot of the
     * mobile phone, so we store their values in the preferences (see
     * NotificationsAchievementsBroadcastReceiver for more info)
     *
     * @param np
     */
    public void addNotificationAchievementsPreference(
            AchievementLevelAndNotificationPreference np) {
        if (null == achievementsLevelsNotifications) {
            achievementsLevelsNotifications = new ArrayList<AchievementLevelAndNotificationPreference>();
        }
        achievementsLevelsNotifications.add(np);
        saveAchievementsLevelsNotifications();
    }

    public void saveAchievementsLevelsNotifications() {
        editor.putString(Constantes.NOTIFICATIONS_ACHIEVEMENTS,
                ObjectSerializer.serialize(achievementsLevelsNotifications))
                .commit();
    }

    /**
     * Method used to add a notification to the shared preferences before
     * setting the alarm The alarms have to be recreated after each boot of the
     * mobile phone, so we store their values in the preferences (see
     * NotificationsWillpowerBroadcastReceiver for more info)
     *
     * @param np
     */
    private void addNotificationWillpowerPreference(
            WillpowerLevelAndNotificationPreference np) {
        if (null == willpowerLevelsNotifications) {
            willpowerLevelsNotifications = new ArrayList<WillpowerLevelAndNotificationPreference>();
        }
        willpowerLevelsNotifications.add(np);

        // save the notifications list to preference
        saveWillpowerLevelsNotifications();
    }

    public void saveWillpowerLevelsNotifications() {
        editor.putString(Constantes.NOTIFICATIONS_WILLPOWER,
                ObjectSerializer.serialize(willpowerLevelsNotifications));
        editor.commit();
    }

    /**
     * set achievements levels and notifications alarms for all achievements
     * categories
     */
    public void setAchievementLevelsAndNotifications() {
        String newUserLevelHealth = Util.getLevelByHealth((int) interval);
        String newUserLevelWellbeing = Util.getLevelByWellness((int) interval);
        String newUserLevelTime = Util.getLevelByTime((int) interval);
        String newUserLevelMoney = Util.getLevelByMoney((float) moneySaved);
        String newUserLevelCigarettes = Util
                .getLevelByCigarette(cigarettesNotSmoked);
        String newUserLevelLife = Util.getLevelByLife(lifeSaved);
        String newUserLevelCo = Util.getLevelByCo(coSaved);

        setAchievementsLevelAndNotification(newUserLevelHealth,
                Util.categoryResources[0], 0, Util.healthAchievements);
        setAchievementsLevelAndNotification(newUserLevelWellbeing,
                Util.categoryResources[1], 1, Util.wellnessAchievements);
        setAchievementsLevelAndNotification(newUserLevelTime,
                Util.categoryResources[2], 2, Util.timeAchievements);
        setAchievementsLevelAndNotification(newUserLevelMoney,
                Util.categoryResources[3], 3, Util.moneyAchievements);
        setAchievementsLevelAndNotification(newUserLevelCigarettes,
                Util.categoryResources[4], 4, Util.cigarettesAchievements);
        setAchievementsLevelAndNotification(newUserLevelLife,
                Util.categoryResources[5], 5, Util.lifeAchievements);
        setAchievementsLevelAndNotification(newUserLevelCo,
                Util.categoryResources[6], 6, Util.coAchievements);
    }

    /**
     * Calculate for category and future user levels in parameter, the time in
     * seconds when the notifications have to be sent
     *
     * @param userLevel
     * @param categoryResId
     * @param categoryId
     * @param achievements
     */
    private void setAchievementsLevelAndNotification(String userLevel /*de la categorie specifié*/,
                                                     int categoryResId, int categoryId, String[] achievements) {
        // On envoie les notif que pour les niveaux futurs à débloquer
        for (int i = Integer.parseInt(userLevel) + 1; i <= 12; i++) {
            // Calcul des détails de la notification (titre, texte, nb de
            // secondes avant déclenchement)

            // seconds before the notification coming
            Date dUnlock = Util.getDateOfAchievementUnlockByCategoryAndLevel(
                    categoryId, i);
            Date dNow = new Date();
            long seconds = (dUnlock.getTime() - dNow.getTime()) / 1000;

            // title and text
            String achievementTitle = getString(R.string.Achievement) + " "
                    + getString(categoryResId) + " "
                    + getString(R.string.Level) + " " + i + " "
                    + getString(R.string.unlocked);
            String achievementText = achievements[i - 1];

            // id
            final int _id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            // Mise en place de l'alarme de maniere asynchrone
            setAlarmAchievementLevelAndNotification(i, categoryId,
                    achievementTitle, achievementText, seconds, _id);

            // Enregistrement dans la liste des préférences qui retient quel
            // niveau de quelle catégorie doit être mis à jour et le détail de
            // la notif correspondante à envoyer
            addNotificationAchievementsPreference(new AchievementLevelAndNotificationPreference(
                    dUnlock, achievementTitle, achievementText,
                    Util.achievementsCategories.get(categoryId),
                    String.valueOf(i), _id));
        }
    }

    @SuppressWarnings("unchecked")
    public void getPreferencesNotifications() {
        if (null == achievementsLevelsNotifications) {
            achievementsLevelsNotifications = new ArrayList<AchievementLevelAndNotificationPreference>();
        }
        achievementsLevelsNotifications = (ArrayList<AchievementLevelAndNotificationPreference>) ObjectSerializer
                .deserialize(prefs
                        .getString(
                                Constantes.NOTIFICATIONS_ACHIEVEMENTS,
                                ObjectSerializer
                                        .serialize(new ArrayList<AchievementLevelAndNotificationPreference>())));

        if (null == willpowerLevelsNotifications) {
            willpowerLevelsNotifications = new ArrayList<WillpowerLevelAndNotificationPreference>();
        }
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        try {
            // mShaker.pause();

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e("Start", "Sensor or Accelerometer not supported +++ " + e);
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        try {
            // mShaker.resume();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e("Start", "Sensor or Accelerometer not supported +++ " + e);
            }
        }
        super.onResume();
        refreshData();
        // restorePreferences();
        updateWidget();
        // manage first launch (user settings never initialize)
        if(needInit) {
            mViewPager.setCurrentItem(FRAGMENT_MORE);
            needInit = false;
        }
        // manage the user connection
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    // Following are the functions for the many calculations (levels, ranks,
    // etc.)
    public final void onTabReselected(Tab tab,
                                      FragmentTransaction fragmentTransaction) {
        View focus = getCurrentFocus();
        if (focus != null) {
            hiddenKeyboard(focus);
        }
    }

    public final void onTabselected(Tab tab,
                                    FragmentTransaction fragmentTransaction) {
        View focus = getCurrentFocus();
        if (focus != null) {
            hiddenKeyboard(focus);
        }
    }

    public final void onTabUnselected(Tab tab,
                                      FragmentTransaction fragmentTransaction) {
        View focus = getCurrentFocus();
        if (focus != null) {
            hiddenKeyboard(focus);
        }
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void refreshMoneyLevel() {
        // mise à jour Pref
        // done - dans Settings

        // mise à jour des variables dans Start
        // done - dans Settings

        // mise à jour Réussites (=recalcul les reussites bloquées/debloquables/debloquées)
        initiateMoneyAchievementsLevel();

        // mise à jour Profile
        initiateUserLevel();

        // mise à jour notification/alarme
        refreshData();
        new MoneyNotificationsManager().execute(this);
    }

    public void manageMoneyNotifications() {
        // remove existing money notifications
        cancelExistingMoneyNotifications();

        // put new money notifications
        for (int i = Integer.parseInt(level_money_available_for_unlock) + 1; i <= 12; i++) {

            // Calcul des détails de la notification (titre, texte, nb de
            // secondes avant déclenchement)
            Date dUnlock = Util.getDateOfAchievementUnlockByCategoryAndLevel(3,
                    i);
            Date dNow = new Date();
            long seconds = (dUnlock.getTime() - dNow.getTime()) / 1000;

            String achievementTitle = getString(R.string.Achievement) + " "
                    + getString(Util.categoryResources[3]) + " "
                    + getString(R.string.Level) + " " + i + " "
                    + getString(R.string.unlocked);
            String achievementText = Util.moneyAchievements[i - 1];

            final int _id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            int idCategory = 3;

            // Mise en place de l'alarme de maniere asynchrone
            setAlarmAchievementLevelAndNotification(i, idCategory,
                    achievementTitle, achievementText, seconds, _id);

            // TODO set up function setAlarmWillpowerLevelAndNotification

            // Enregistrement dans la liste des préférences qui retient quel
            // niveau de quelle catégorie doit être mis à jour et le détail de
            // la notif correspondante à envoyer
            addNotificationAchievementsPreference(new AchievementLevelAndNotificationPreference(
                    dUnlock, achievementTitle, achievementText,
                    Util.achievementsCategories.get(idCategory),
                    String.valueOf(i), _id));
        }
    }

    public void resetWillpower() {
        editor.putInt(Constantes.WILLPOWER_CIGARETTES_SMOKED, 0);
        updateWillpowerCounter(1.0f);
        editor.commit();
        cigarettesSmoked = 0;
        cravings = 0;
        willpowerCounter = 1;
        refreshData();
        ((Willpower) adapter.getItem(FRAGMENT_WILLPOWER)).updateInformations();
    }

    /**
     * Affiche le fragment index (le fragment 4 avec firstlaunch à true entraîne
     * la réinitialisation de l'app et l'affichage du tutoriel du shake
     *
     * @param index
     */
    public void selectPager(int index) {
        if (index >= 0 && index <= adapter.getCount()) {
            mViewPager.setCurrentItem(index);
            if (index == FRAGMENT_MORE && firstLaunch) {
                Start.firstLaunch = prefs.getBoolean(Constantes.FIRST_LAUNCH, true);
                if (firstLaunch) {
                    ((MoreFragment) adapter.getItem(FRAGMENT_MORE)).reinitilization();
                }
            }
        }
    }

    public void initiateUserLevel() {
        // level for profile from achievements level unlocked
        Start.user_level_health = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(0));
        Start.user_level_wellness = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(1));
        Start.user_level_time = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(2));
        Start.user_level_money = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(3));
        Start.user_level_cigarette = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(4));
        Start.user_level_life = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(5));
        Start.user_level_co = Util
                .getNbOfLevelUnlockedByCategory(Util.achievementsCategories
                        .get(6));

        Start.userXP = Util.getUserXP();
        Start.user_level = Util.getUserLevel(Start.userXP);
        Start.user_rank_value = Util.getUserRankValue(Integer
                .parseInt(Start.user_level));
        Start.user_rank_text = Util.getUserRankText(
                Integer.parseInt(Start.user_rank_value),
                this.getApplicationContext());
    }

    // initialize variable to know one specific level (achievements and profile)
    public void initiateSpecificAchievementsLevel(String oldLevelAvailableForUnlock, String newLevel, int positionInAchievementsCategories){
        if (Integer.parseInt(oldLevelAvailableForUnlock) < Integer.parseInt(newLevel)) {
            // on a gagné un ou plusieurs niveaux,
            // donc on met en setToUnlock les réussites entre
            for (int i = Integer.parseInt(oldLevelAvailableForUnlock)+1; i <= Integer
                    .parseInt(newLevel); i++) {
                Util.setToUnlock(positionInAchievementsCategories, i);
            }
        }
        else {
            // on a perdu un ou plusieurs niveaux,
            // donc on met en setLocked les réussites entres
            for (int i = Integer.parseInt(newLevel) + 1; i <= Integer
                    .parseInt(oldLevelAvailableForUnlock); i++) {
                Util.setToLocked(positionInAchievementsCategories, i);
            }
        }

        // Mise à jour du niveau pour cette catégorie dans les préférences
        String levelAvailableForUnlock = String
                .format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK,
                        Util.achievementsCategories.get(positionInAchievementsCategories));
        editor.putString(levelAvailableForUnlock, newLevel);
        editor.commit();
        // mise à jour automatique de la variable level_X_available_for_unlocked grace à refreshData (du handler)
    }

    // initialize variable to know all levels (achievements and profile)
    public void initiateAllAchivementsAndProfileLevels(){
        initiateHealthAchievementsLevel();
        initiateWellnessAchievementsLevel();
        initiateTimeAchievementsLevel();
        initiateMoneyAchievementsLevel();
        initiateCigaretteAchievementsLevel();
        initiateLifeAchievementsLevel();
        initiateCoAchievementsLevel();
    }

    // initialize variable to know health level (achievements and profile)
    public void initiateHealthAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(0));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByHealth((int) Start.interval);
        int positionInAchievementsCategories = 0;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know wellness level (achievements and profile)
    public void initiateWellnessAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(1));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByWellness((int) Start.interval);
        int positionInAchievementsCategories = 1;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know time level (achievements and profile)
    public void initiateTimeAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(2));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByTime(Start.interval);
        int positionInAchievementsCategories = 2;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know money level (achievements and profile)
    public void initiateMoneyAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(3));
        String oldLevelMoneyAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByMoney(Start.moneySaved);
        int positionInAchievementsCategories = 3;
        initiateSpecificAchievementsLevel(oldLevelMoneyAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know cigarette level (achievements and profile)
    public void initiateCigaretteAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(4));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByCigarette(Start.cigarettesNotSmoked);
        int positionInAchievementsCategories = 4;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know life level (achievements and profile)
    public void initiateLifeAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(5));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByLife(Start.lifeSaved);
        int positionInAchievementsCategories = 5;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize variable to know co level (achievements and profile)
    public void initiateCoAchievementsLevel() {
        String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, Util.achievementsCategories.get(6));
        String oldLevelAvailableForUnlock = prefs.getString(levelAvailableForUnlock, "0");
        String newLevel = Util.getLevelByCo(Start.coSaved);
        int positionInAchievementsCategories = 6;
        initiateSpecificAchievementsLevel(oldLevelAvailableForUnlock, newLevel, positionInAchievementsCategories);
    }

    // initialize preferences variable to know levels of different categories
    // (achievements and profile)
    public void initiateAchievementsLevel() {
        // Mise à jour des preferences avec le niveau initial pour chaque
        // catégorie
        // Pour chaque catégorie, on calcule le niveau initial déverouillable
        for (int idCat = 0; idCat < Util.achievementsCategories.size(); idCat++) {
            String level = "";
            switch (idCat) {
                case 0:
                    level = Util.getLevelByHealth((int) Start.interval);
                    break;
                case 1:
                    level = Util.getLevelByWellness((int) Start.interval);
                    break;
                case 2:
                    level = Util.getLevelByTime((int) Start.interval);
                    break;
                case 3:
                    level = Util.getLevelByMoney(Start.moneySaved);
                    break;
                case 4:
                    level = Util.getLevelByCigarette(Start.cigarettesNotSmoked);
                    break;
                case 5:
                    level = Util.getLevelByLife(Start.lifeSaved);
                    break;
                default:
                    level = Util.getLevelByCo(Start.coSaved);
                    break;
            }

            // Mise à jour du niveau pour cette catégorie dans les préférences
            String levelAvailableForUnlock = String
                    .format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK,
                            Util.achievementsCategories.get(idCat));
            editor.putString(levelAvailableForUnlock, level);
            editor.commit();
        }
    }

    public void initializationPreference() {
        // initialize variables prefs and editor
        prefs = getSharedPreferences(Constantes.PREF_NAME, MODE_PRIVATE);
        editor = prefs.edit();
        // set to true for everyone
        //isPremium = prefs.getBoolean(Constantes.IS_PREMIUM, isPremium);

        restorePreferences();

        refreshData();

        // Fill achievements arrays
        retrieveAchievements();

        if (version < appVersionCode || version > 10) {
            recalculateNotifications();
            updateVersion();
        }

        initiateAllAchivementsAndProfileLevels();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                    SharedPreferences preferences=this.getSharedPreferences(Constantes.PREF_NAME, Context.MODE_PRIVATE);
                    restoreSharedPreferencesToFile(preferences);
                } else
                {
                    needInit = true;
                }
            }
        }
    }

    private void restoreSharedPreferencesToFile(final SharedPreferences prefs) {
        File myPath = new File(Environment.getExternalStorageDirectory().toString()+"/Tobano");
        final File myFile = new File(myPath, "TobanoSharedPreferences");

        if(myFile.exists()){

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int choice) {
                    switch (choice) {
                        case DialogInterface.BUTTON_POSITIVE:
                            String s;
                            Map<String, Object> prefsMap = new HashMap<>();
                            try {
                                final FileReader fr = new FileReader(myFile);
                                final BufferedReader br = new BufferedReader(fr);
                                while ((s = br.readLine()) != null) {
                                    // 0: key, 1: type, 2: value
                                    String[] tab = s.split(":", 3);
                                    prefsMap.put(tab[0], tab[2]);
                                    if (tab[1].equals(String.class.toString())) {
                                        editor.putString(tab[0], tab[2]);
                                    } else if (tab[1].equals(Integer.class.toString())) {
                                        editor.putInt(tab[0], Integer.parseInt(tab[2]));
                                    } else if (tab[1].equals(Long.class.toString())) {
                                        editor.putLong(tab[0], Long.parseLong(tab[2]));
                                    } else if (tab[1].equals(Float.class.toString())) {
                                        editor.putFloat(tab[0], Float.parseFloat(tab[2]));
                                    } else if (tab[1].equals(Boolean.class.toString())) {
                                        if (tab[0].equalsIgnoreCase(Constantes.IS_PREMIUM)) {
                                            editor.putBoolean(Constantes.IS_PREMIUM, Boolean.parseBoolean(tab[2]));
                                        }
                                        else {
                                            editor.putBoolean(tab[0], Boolean.parseBoolean(tab[2]));
                                        }
                                    }
                                }
                                editor.commit();
                                fr.close();

                                if (prefsMap.size() < 2) {
                                    // empty if user came from free version of kwit
                                    // size of 1 if user came from pro version of kwit
                                    mViewPager.setCurrentItem(FRAGMENT_MORE);
                                }
                                else {
                                    // recalculate variables
                                    restorePreferences();
                                    // recalculate data
                                    refreshData();
                                    //recalculate notifications
                                    setAchievementLevelsAndNotifications();

                                    //restart application to reload prefs/UI
                                    // create start
                                    final Intent intent = new Intent(Start.this, Splash.class);
                                    startActivity(intent);
                                    finish();
                                }
                                break;
                            }
                            catch (FileNotFoundException e) {
                                //Toast.makeText(getApplicationContext(), "No old pref files found :(", Toast.LENGTH_LONG).show();
                                mViewPager.setCurrentItem(FRAGMENT_MORE);
                            }
                            catch (Exception e) {
                                // what a terrible failure...
                                Log.wtf(getClass().getName(), e.toString());
                            }
                        case DialogInterface.BUTTON_NEGATIVE:
                            mViewPager.setCurrentItem(FRAGMENT_MORE);
                            break;
                    }
                }
            };

            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getResources().getString(R.string.tobanoProfileFoundTitle));
            builder.setMessage(getResources().getString(R.string.tobanoProfileFoundText));

            // add a button
            builder.setPositiveButton(android.R.string.yes, dialogClickListener);
            builder.setNegativeButton(android.R.string.no, dialogClickListener);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            needInit = true;
        }
    }

    public void retrieveAchievements() {
        Util.initializeAchievements(this.getApplicationContext());
    }

    public void restorePreferences() {
        // Set impossible Date just for tests
        Date d;
        try {
            d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse("2015-01-01 00:00:00");
        } catch (Exception e) {
            d = new Date();
        }
        /// theme
        Start.theme = Theme.valueOf(Start.prefs.getString(Constantes.THEME, Theme.BLUE.name()));
        // currency
        Start.currency = Start.prefs.getString(Constantes.CURRENCY,"AUTO");
        if (Start.currency.equals("AUTO")) {
            Locale local = new Locale(Locale.getDefault().getLanguage(), Locale
                    .getDefault().getCountry());
            Currency c = Currency.getInstance(local);
            Start.currency = c.getSymbol();
        }
        // cig per day / pack
        Start.cigarettesPerDay = Integer.parseInt(Start.prefs.getString(
                Constantes.CIGARETTE_PER_DAY, "0"));
        Start.cigarettesPerPack = Integer.parseInt(Start.prefs.getString(
                Constantes.CIGARETTE_PER_PACK, "0"));
        Start.priceOfAPack = Double.parseDouble(Start.prefs.getString(
                Constantes.PRICE_OF_PACK, "0"));
        // stop date / stop since
        String dateString = Start.prefs.getString(Constantes.QUITTING_DATE, d.toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd/yyyy HH:mm:ss");
        try {
            Start.quittingDate = dateFormat.parse(dateString);
            // Calculate interval since quitting date
            Date start = Start.quittingDate;
            Date end = new Date();
            Start.diffInSeconds = (end.getTime() - start.getTime()) / 1000;
            if (Start.diffInSeconds < 0)
                Start.diffInSeconds = 0;
            Start.interval = Start.diffInSeconds;
        } catch (Exception e) {
            Start.diffInSeconds = 0;
            e.printStackTrace();
        }
        // cig smoked / willpower
        Start.cigarettesSmoked = Start.prefs.getInt(
                Constantes.WILLPOWER_CIGARETTES_SMOKED, 0);
        Start.cravings = Start.prefs.getInt(
                Constantes.WILLPOWER_CRAVINGS, 0);
        Start.willpowerCounter = Start.prefs.getFloat(Constantes.WILLPOWER_COUNTER, 1.0f);
        Start.user_level_willpower = String.valueOf(Util.getLevelByWillpower());
        // cig saved / not smoked
        Start.cigarettesNotSmoked = ((double) Start.cigarettesPerDay / (24 * 60 * 60))
                * Start.diffInSeconds - Start.cigarettesSmoked;
        Start.priceOfACigarette = Start.priceOfAPack
                / ((double) Start.cigarettesPerPack == 0 ? 1
                : (double) Start.cigarettesPerPack);
        // Life
        Start.cigarettesPerSecond = (double) Start.cigarettesPerDay / 86400;
        Start.lifeGainEachSecond = Start.cigarettesPerSecond * 660;
        Start.timeToGainOneSecond = 1 / Start.lifeGainEachSecond;
        Start.lifeSaved = Start.cigarettesNotSmoked * 660;
        // CO
        Start.mgOfCarbonMonoxidePerSeconds = Start.cigarettesPerSecond * 10;
        Start.timeToGetOneMg = 1 / Start.mgOfCarbonMonoxidePerSeconds;
        Start.coSaved = Start.cigarettesNotSmoked * 10;
        // Money
        Start.moneySaved = Start.cigarettesNotSmoked * Start.priceOfACigarette;
        // Own Goal
        preferenceGoalDeserializer();
        // first launch / used (tuto)
        Start.firstLaunch = Start.prefs.getBoolean(Constantes.FIRST_LAUNCH, true);
        // version
        Start.version = Start.prefs.getInt(Constantes.VERSION, -1);
    }

    private void updateVersion() {
        editor.putInt(Constantes.VERSION, appVersionCode);
        editor.commit();
    }

    private void preferenceGoalDeserializer(){
        if (Start.ownGoalTitles == null) {
            Start.ownGoalTitles = new ArrayList<>();
        }
        Start.ownGoalTitles = (ArrayList<String>) ObjectSerializer
                .deserialize(Start.prefs
                        .getString(
                                Constantes.OWN_GOAL_TITLE,
                                ObjectSerializer
                                        .serialize(new ArrayList<String>())));

        if (Start.ownGoal == null) {
            Start.ownGoal = new ArrayList<>();
        }
        Start.ownGoal = (ArrayList<String>) ObjectSerializer
                .deserialize(Start.prefs
                        .getString(
                                Constantes.OWN_GOAL,
                                ObjectSerializer
                                        .serialize(new ArrayList<String>())));
    }

    public static double getOwnGoal() {
        double total = 0;
        if (ownGoal != null){
            for (String value : ownGoal){
                if (value != null)
                    total += Double.valueOf(value);
            }
        }
        return total;
    }

    public void changeTheme(Theme theme) {
        editor.putString(Constantes.THEME, theme.name());
        editor.commit();
        Start.theme = theme;
        updateTabsColor();
    }

    public void recalculateNotifications() {
        // install alarm for notifications of achievements
        new AchievementsNotificationsManager().execute(this);

        // delete alarm for notifications of willpower
        new WillpowerNotificationsManager().execute(this);
    }

    /**
     * Cancel existing alarms notifications, in preferences and
     * achievementsLevelsNotifications ArrayList
     */
    public void cancelExistingAchievementsNotifications() {
        getPreferencesNotifications();
        for (AchievementLevelAndNotificationPreference cn : achievementsLevelsNotifications) {
            Intent intent = new Intent(this,
                    NotificationsAchievementsBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), cn.getId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            System.out.println("delete alarm : category " + cn.getCategory() + " level " + cn.getLevel());
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
        achievementsLevelsNotifications = null;
        editor.remove(Constantes.NOTIFICATIONS_ACHIEVEMENTS).commit();
    }

    /**
     * Cancel existing money notifications in preferences and
     * achievementsLevelsNotifications ArrayList
     */
    public void cancelExistingMoneyNotifications() {
        getPreferencesNotifications();
        Iterator<AchievementLevelAndNotificationPreference> j = achievementsLevelsNotifications
                .iterator();
        while (j.hasNext()) {
            AchievementLevelAndNotificationPreference cn = j.next();

            if (cn.getCategory().equals(Util.badgeMoneyKey)) {
                Intent intent = new Intent(this,
                        NotificationsAchievementsBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(), cn.getId(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                j.remove();
                //System.out.println("delete alarm : category " + cn.getCategory() + " level " + cn.getLevel());
            }
        }
        saveAchievementsLevelsNotifications();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // if (mHelper != null) mHelper.dispose();
        // mHelper = null;
        if (mService != null) {
            unbindService(mServiceConn);
        }
        updateWidget();
    }

    // public IabHelper getmHelper() {
    //    return mHelper;
    // }

    /**
     * Display dialog for unlock full version
     */
//    public static void displayUnlockFullVersionDialog() {
//        int color;
//        if (Start.theme == Theme.GREEN){
//            color = context.getResources().getColor(R.color.kwit_light);
//        }
//        else {
//            color = context.getResources().getColor(R.color.primary_light);
//        }
//        final Dialog fullVersionDialog = new Dialog(context,
//                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//        fullVersionDialog.getWindow();
//        fullVersionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        fullVersionDialog.setContentView(R.layout.unlock_fullversion);
//        fullVersionDialog.setCancelable(true);
//
//        ImageButton dialogButtonClose = (ImageButton) fullVersionDialog.findViewById(R.id.actionclose);
//        Drawable dMotivationCard = context.getResources().getDrawable(R.drawable.action_close);
//        Drawable dNewColor = dMotivationCard.getConstantState().newDrawable();
//        dNewColor.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
//        dialogButtonClose.setImageDrawable(dNewColor);
//
//        dialogButtonClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fullVersionDialog.dismiss();
//            }
//        });
//        Button dialogButtonBuy = (Button) fullVersionDialog.findViewById(R.id.unlock_button);
//        if (Start.theme == Theme.GREEN){
//            dialogButtonBuy.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_full_green));
//        }
//        else {
//            dialogButtonBuy.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_full_tabono));
//        }
//        try {
//            String strButtonBuy = context.getResources().getString(R.string.unlockFullVersion);
//            strButtonBuy += "\n(";
//            strButtonBuy += skuDetailsPremium.getPrice();
//            strButtonBuy += ")";
//            dialogButtonBuy.setText(strButtonBuy);
//        }
//        catch (Exception e){
//            Log.d("Start - skuDetails", "SyncTask is not over - "+e.toString());
//        }
//        dialogButtonBuy.setOnClickListener(new View.OnClickListener(){
//            @Override
//        public void onClick(View v){
//                startStoreToBuyFullVersion();
//            }
//        });
//        fullVersionDialog.show();
//    }

//    public static void startStoreToBuyFullVersion() {
//        FlurryAgent.logEvent("Unlock_full_version");
//        int RC_REQUEST = 10001;
//        String sku = "";
//        if(BuildConfig.DEBUG){
//            sku = "android.test.purchased";
//        }
//        else {
//            sku = Constantes.SKU_PREMIUM;
//        }
//        String payload = ""; //TODO to handle see example
//        try{
//            ((Start) context).getmHelper().launchPurchaseFlow(((Start) context), sku, RC_REQUEST,
//                    ((Start) context).mPurchaseFinishedListener, payload);
//        }
//        catch(Exception e){
//            Log.d("Start - fullversion", "too many AsyncTask are started : "+e.toString());
//        }
//    }

    private void initFlurry(){
        // init Flurry
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, Constantes.MY_FLURRY_APIKEY_APP);
    }

    public void shareScreenShot(View v, String pictureName) {
        // create bitmap screen capture
        Bitmap bitmap;
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        String shareMessage;
        shareMessage = getResources().getString(R.string.IQuitSmokingWithKwit);

        Intent intent = Util.sharePicture(getBaseContext(), bitmap, pictureName, shareMessage, getResources().getString(R.string.share));

        if(intent != null){
            startActivity(intent);
        }
    }

    private void initSounds(){
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sStillLock = sounds.load(context, R.raw.hide, 1);
        sUnlock = sounds.load(context, R.raw.unlock, 1);
        sHideUnlocked = sounds.load(context, R.raw.bell_tree, 1);
        sSlide = sounds.load(context, R.raw.slide, 1);
        sInitialize = sounds.load(context, R.raw.init, 1);
        sClick = sounds.load(context, R.raw.click, 1);
        sError = sounds.load(getBaseContext(), R.raw.error, 1);
        sCraving = sounds.load(getBaseContext(), R.raw.bell_tree, 1);
        sSmoke = sounds.load(getBaseContext(), R.raw.smoke, 1);
        sGameOver = sounds.load(getBaseContext(), R.raw.over, 1);
        Boolean isMute = prefs.getBoolean(Constantes.IS_MUTE, true);
        volume = isMute ? 0 : 1;
    }

    public void playSound(String nameSound){
        try {
            if (nameSound.equals(strStillLock)) {
                sounds.play(sStillLock, 0.2f * volume, 0.2f * volume, 0, 0, 1.0f);
            } else if (nameSound.equals(strUnlock)) {
                sounds.play(sUnlock, 1.0f * volume, 1.0f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strHideUnlocked)) {
                sounds.play(sHideUnlocked, 0.2f * volume, 0.2f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strSlide)) {
                sounds.play(sSlide, 1.0f * volume, 1.0f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strInitialize)) {
                sounds.play(sInitialize, 1.0f * volume, 1.0f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strClick)) {
                sounds.play(sClick, 0.1f * volume, 0.1f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strError)) {
                sounds.play(sError, 0.1f * volume, 0.1f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strCraving)) {
                sounds.play(sCraving, 0.1f * volume, 0.1f * volume, 0, 0, 1.5f);
            } else if (nameSound.equals(strSmoke)) {
                sounds.play(sSmoke, 0.05f * volume, 0.05f * volume, 0, 0, 1.3f);
            } else if (nameSound.equals(strGameOver)) {
                sounds.play(sGameOver, 0.1f * volume, 0.1f * volume, 0, 0, 1.0f);
            }
        } catch (Exception e) {
            // sound exception
        }
    }

    public void saveWallpaper(Uri selectedImage) {
        Start.wallpaper = selectedImage.getPath();
        Start.editor.putString(Constantes.WALLPAPER, Start.wallpaper).commit();
    }

    public void updateWidget() {
        Intent i = new Intent(this, WidgetProvider.class);
        i.setAction(WidgetProvider.UPDATE_ACTION);
        sendBroadcast(i);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
        catch (Exception e) {
            System.out.println("hideProgressDialog : isNull=" + (mProgressDialog != null) + " / isShowing=" + mProgressDialog.isShowing());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

      //           //
     /*SOCIAL PART*/
    //           //

    protected void startSocial(final OnGetDataListener listener) {

        // retrieve minVersion to check if social part can be launched
        initMinVersion();

        connectGoogleApi();

        showProgressDialog();
        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        // Check if user is signed in (non-null) and update UI accordingly.
        mCurrentUser = mAuth.getCurrentUser();

        getForum(listener);

        if(mCurrentUser == null) {
            Log.d("ANONYMOUS", "null");
            //if user not logged in with Google or email/pasword, create anonymous login
            if (!signInAnonymouslyInProgress) {
                signInAnonymouslyInProgress = true;
                mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                mCurrentUser = mAuth.getCurrentUser();
                                Log.d("ANONYMOUS", "success - " + mCurrentUser.getUid());
                                listener.onSuccess(SIGN_IN);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                listener.onFailed(null);
                            }
                            signInAnonymouslyInProgress = false;
                        }
                    });
            }
        }
        else{
            Log.d("ANONYMOUS", mCurrentUser.isAnonymous() + " - " + mCurrentUser.getUid());
            // user is anonymous or logged in
            getUser(listener);
        }
    }

    /**
     * Fetches minVersion from Firebase Remote Config
     * IF (appVersionCode < minVersion)
     * THEN request to update app
     * ELSE IF (minVersion == 1337)
     * THEN forum not available
     */
    private void initMinVersion() {
        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("min_version", 0l); // by default 0
        remoteConfig.setDefaults(defaults);

        Task<Void> fetch = remoteConfig.fetch(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12));
        fetch.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                minVersion = remoteConfig.getLong("min_version");
                Log.d("Start RemoteConfig", "onSuccess, minVersion set to " + minVersion);
            }
        });
        fetch.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Start RemoteCOnfigg", "onFailure : " + e.toString());
            }
        });
    }

    private void connectGoogleApi() {
        if(mGoogleApiClient == null){
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 1, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        } else {
            // authenticated
        }
        mAuth = FirebaseAuth.getInstance();
        mGoogleApiClient.connect();
    }

    public void getUser(final OnGetDataListener listener) {
        listener.onFirebaseStart();
        mDatabase.child("users").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                if (mUser != null) {
                    listener.onSuccess(TYPE_USER);
                }
                getUserLanguage(listener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG_GOOGLE, "getUser:onCancelled", databaseError.toException());
                listener.onFailed(databaseError);
            }
        });
    }

    public void getUserLanguage(final OnGetDataListener listener) {
        listener.onFirebaseStart();
        mDatabase.child("language").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentLanguage = Locale.getDefault().getISO3Language();
                for (DataSnapshot languageSnapshot: dataSnapshot.getChildren()) {
                    if(currentLanguage.equalsIgnoreCase(languageSnapshot.child("nameAbbrev").getValue(String.class))) {
                        mLanguage = new Language(languageSnapshot.child("idLanguage").getValue(Long.class),
                                languageSnapshot.child("name").getValue(String.class), languageSnapshot.child("nameAbbrev").getValue(String.class));
                        break;
                    }
                }
                hideProgressDialog();
                listener.onSuccess(TYPE_USER_LANGUAGE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
                listener.onFailed(databaseError);
            }
        });
    }

    private void getForum(final OnGetDataListener listener) {
        listener.onFirebaseStart();
        mDatabase.child("forum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                forums = new HashMap<>();
                for (DataSnapshot f: dataSnapshot.getChildren()) {
                    forums.put(f.getKey(), f.getValue(Forum.class));
                }
                listener.onSuccess(TYPE_FORUM);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG_GOOGLE, "getForum:onCancelled", databaseError.toException());
                listener.onFailed(databaseError);
            }
        });
    }

    public String getLanguageByForum(String idForum) {
        if (forums != null && forums.containsKey(idForum)) {
            return forums.get(idForum).idLanguage.toString();
        } else {
            Log.d("Start", "getLanguageByForum - idForum:" + idForum);
            return "1";
        }
    }

    public void updateUserSettings() {
        updateUserSettings(false);
    }

    public void updateUserSettings(Boolean updateOnlyPriceOfPack) {
        if (hasInternetConnection() && mCurrentUser != null && !mCurrentUser.isAnonymous() && mUser != null) {
            final String uid = mCurrentUser.getUid();
            showProgressDialog();
            final Map<String, Object> childUpdates = new HashMap<>();
            // update users
            final User user = new User(mUser);
            user.setDateQuitting(quittingDate.getTime());
            user.setCurrency(currency);
            user.setNumberOfCigarettePerDay(cigarettesPerDay);
            user.setNumberOfCigarettePerPack(cigarettesPerPack);
            user.setPriceOfPack(priceOfAPack);
            childUpdates.put("/users/" + uid, user);
            // update author quitting date in concerned posts and comments
            if (updateOnlyPriceOfPack == null || !updateOnlyPriceOfPack) {
                // get all comments wrote by the user
                mDatabase.child("user-comments").child(uid).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final HashMap<String, Comment> comments = new HashMap<>();
                                for (DataSnapshot comment : dataSnapshot.getChildren()) {
                                    comments.put(comment.getKey(), comment.getValue(Comment.class));
                                }
                                // get all post ids of the concerned user
                                mDatabase.child("user-posts").child(uid).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, Post> posts = new HashMap<>();
                                                for (DataSnapshot post : dataSnapshot.getChildren()) {
                                                    posts.put(post.getKey(), post.getValue(Post.class));
                                                }
                                                // build all the request to send
                                                for (String postId : posts.keySet()) {
                                                    Post p = posts.get(postId);
                                                    p.authorQuittingDate = quittingDate.getTime();
                                                    childUpdates.put("/posts/" + postId, p);
                                                    childUpdates.put("/forum-posts/" + p.idForum + "/" + postId, p);
                                                    childUpdates.put("/user-posts/" + uid + "/" + postId, p);
                                                    childUpdates.put("/language-posts/" + getLanguageByForum(p.idForum.toString()) + "/" + postId, p);
                                                }
                                                for (String commentId : comments.keySet()) {
                                                    Comment comment = comments.get(commentId);
                                                    comment.authorQuittingDate = quittingDate.getTime();
                                                    childUpdates.put("/post-comments/" + comment.postId + "/" + commentId, comment);
                                                    childUpdates.put("/user-comments/" + uid + "/" + commentId, comment);
                                                }
                                                mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Log.d(TAG_GOOGLE, "update quitting date onComplete: success");
                                                        } else {
                                                            Log.w(TAG_GOOGLE, "update quitting date onComplete: fail", databaseError.toException());
                                                        }
                                                        hideProgressDialog();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.w(TAG_GOOGLE, "getUserPosts:onCancelled", databaseError.toException());
                                                hideProgressDialog();
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG_GOOGLE, "getUserComments:onCancelled", databaseError.toException());
                                hideProgressDialog();
                            }
                        }
                );
            }
            else {
                mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Log.d(TAG_GOOGLE, "update user settings onComplete: success");
                        } else {
                            Log.w(TAG_GOOGLE, "update user settings onComplete: fail", databaseError.toException());
                        }
                        hideProgressDialog();
                    }
                });
            }
        }
    }

    public void signOut(final OnGetDataListener listener) {
        listener.onFirebaseStart();

        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        if(mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        listener.onSuccess(SIGN_OUT);
                    }
                }
            );
        }
        else {
            listener.onSuccess(SIGN_OUT);
        }

        // initialize the connection for an anonymous user
        startSocial(this);
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    @Override
    public void onFirebaseStart() {
        if (hasInternetConnection())
            showProgressDialog();
    }

    @Override
    public void onFailed(DatabaseError databaseError) {
        hideProgressDialog();
    }

    @Override
    public void onSuccess(String type) {
        switch (type) {
            case TYPE_USER:
                updateUserSettings();
                break;
        }
        hideProgressDialog();
    }

    // [START auth_with_google]
	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + acct.getId());
		// [START_EXCLUDE silent]
		// [END_EXCLUDE]

        final OnGetDataListener onGetDataListener = this;

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                        mCurrentUser = mAuth.getCurrentUser();
                        getUser(onGetDataListener);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getBaseContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    // [START_EXCLUDE]
                    hideProgressDialog();
                    // [END_EXCLUDE]
                }
            });
	}

    protected boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}