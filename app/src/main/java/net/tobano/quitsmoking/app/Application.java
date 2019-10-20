package net.tobano.quitsmoking.app;

import android.content.Context;

public class Application extends android.app.Application {

	// The following line should be changed to include the correct property
	// id.
	private static final String PROPERTY_ID = "UA–55043711–1";

	// Logging TAG
	private static final String TAG = "Kwit";

	public static int GENERAL_TRACKER = 0;

	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions
							// from a company.
	}

	private static Application instance = new Application();

	public Application() {
		instance = this;
	}

	public static Context getContext() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
}
