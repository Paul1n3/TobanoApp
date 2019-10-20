package net.tobano.quitsmoking.app;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationsWillpowerBroadcastReceiver extends BroadcastReceiver {

	// paramètres de la notification
	private String title;
	private String text;
	private String level;
	private int id;
	private ArrayList<WillpowerLevelAndNotificationPreference> notifications;

	protected static final int Build_Version = Build.VERSION.SDK_INT;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			title = intent.getStringExtra("title");
			text = intent.getStringExtra("text");
			level = intent.getStringExtra("level");
			id = intent.getIntExtra("id", 0);

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(Util.getNotificationIcon())
					.setContentTitle(title).setContentText(text);
			mBuilder.setAutoCancel(true);

			// Gets an instance of the NotificationManager service
			NotificationManager mNotifyMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// la notification lance l'application si on clique dessus
			Intent willpowerIntent = new Intent(context, Splash.class);
			PendingIntent pIntent = PendingIntent.getActivity(context, id,
					willpowerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentIntent(pIntent);

			mNotifyMgr.notify(id, mBuilder.build());

			// Mise à jour du niveau déblocable pour cette catégorie dans les
			// préférences
			SharedPreferences prefs = context.getSharedPreferences(Constantes.PREF_NAME,
					Context.MODE_PRIVATE);
			Editor editor = prefs.edit();
			// System.out
			// .println("-->>>>> Notif level detected, saved willpowerCounter level = "
			// + Float.parseFloat(level));
			editor.putFloat(Constantes.WILLPOWER_COUNTER,
					(float) Util.getWillpowerByLevel(Integer.parseInt(level)));
			editor.commit();

		} else {
			// Récupération des notifications des préférences
			getPreferencesNotifications(context);
			// boucle pour récupérer toutes les notifications des préférences
			for (int j = 0; j < notifications.size(); j++) {
				Intent i = new Intent(context,
						NotificationsWillpowerBroadcastReceiver.class);
				i.putExtra("title", notifications.get(j).getTitle());
				i.putExtra("text", notifications.get(j).getText());
				i.putExtra("level", notifications.get(j).getLevel());
				i.putExtra("id", String.valueOf(notifications.get(j).getId()));

				Date dNow = new Date();
				long seconds = (notifications.get(j).getDate().getTime() - dNow
						.getTime()) / 1000;

				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context.getApplicationContext(), notifications.get(j)
								.getId(), i, 0);
				AlarmManager alarmManager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				Util.setAlarm(alarmManager, AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis() + (1000 * seconds),
						pendingIntent);
				// Toast.makeText(context, "Alarm set in " + seconds +
				// " seconds",
				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void getPreferencesNotifications(Context context) {
		if (null == notifications) {
			notifications = new ArrayList<WillpowerLevelAndNotificationPreference>();
		}

		// load tasks from preference
		SharedPreferences prefs = context.getSharedPreferences(Constantes.PREF_NAME,
				Context.MODE_PRIVATE);

		notifications = (ArrayList<WillpowerLevelAndNotificationPreference>) ObjectSerializer
				.deserialize(prefs
						.getString(
								Constantes.NOTIFICATIONS_WILLPOWER,
								ObjectSerializer
										.serialize(new ArrayList<WillpowerLevelAndNotificationPreference>())));

	}
}
