package net.tobano.quitsmoking.app;

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

import java.util.ArrayList;
import java.util.Date;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationsAchievementsBroadcastReceiver extends
		BroadcastReceiver {

	// paramètres de la notification
	private String title;
	private String text;
	private String category;
	private String level;
	private int id;
	private ArrayList<AchievementLevelAndNotificationPreference> notifications;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			// récupération du titre et du texte de la notification passée à
			// l'intent lors de la création de l'alarme
			title = intent.getStringExtra("title");
			text = intent.getStringExtra("text");
			// Pour pouvoir changer le niveau dans les préférences, on a besoin
			// d'identifier la réussite concernée
			category = intent.getStringExtra("category");
			level = intent.getStringExtra("level");
			id = intent.getIntExtra("id", 0);

			// load tasks from preference
			SharedPreferences prefs = context.getSharedPreferences(Constantes.PREF_NAME,
					Context.MODE_PRIVATE);
			Boolean isPremium = prefs.getBoolean(Constantes.IS_PREMIUM, false);

            if(isPremium ||	!(category.equals(Util.badgeLifeKey) || category.equals(Util.badgeCoKey))) {

				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
						context).setSmallIcon(Util.getNotificationIcon())
						.setContentTitle(title).setContentText(text);
				mBuilder.setAutoCancel(true);

				// Gets an instance of the NotificationManager service
				NotificationManager mNotifyMgr = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				// la notification lance l'application si on clique dessus
				Intent achievementsIntent = new Intent(context, Splash.class);
				PendingIntent pIntent = PendingIntent.getActivity(context, id,
						achievementsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				mBuilder.setContentIntent(pIntent);

				mNotifyMgr.notify(id, mBuilder.build());

//				// Mise à jour du niveau déblocable pour cette catégorie dans les préférences
				String levelAvailableForUnlock = String.format(Constantes.LEVEL_AVAILABLE_FOR_UNLOCK, category);
				Editor editor = prefs.edit();
				editor.putString(levelAvailableForUnlock, level);
//				// Mise à jour de l'état de la réussite dans la liste de tous les niveaux
//				// équivalent à Util.setToUnlock
//				// mais on ne peut pas l'utiliser à cause de pref qui ne sont pas init dans Start ici
				editor.putInt(Util.achievementsCategories.get(Util.achievementsCategories.indexOf(category))+level, 1);
				editor.commit();
			}
		}
		else { // reboot détecté, il faut remettre les alarmes en place selon les préférences
			// Récupération des notifications des préférences
			getPreferencesNotifications(context);
			// boucle pour récupérer toutes les notifications des préférences
			for (int j = 0; j < notifications.size(); j++) {
				Intent i = new Intent(context,
						NotificationsAchievementsBroadcastReceiver.class);
				i.putExtra("title", notifications.get(j).getTitle());
				i.putExtra("text", notifications.get(j).getText());
				i.putExtra("category", notifications.get(j).getCategory());
				i.putExtra("level", notifications.get(j).getLevel());
				i.putExtra("id", notifications.get(j).getId());

				// load tasks from preference
				SharedPreferences prefs = context.getSharedPreferences(Constantes.PREF_NAME,
						Context.MODE_PRIVATE);

				category = notifications.get(j).getCategory();

				Date dNow = new Date();
				long seconds = (notifications.get(j).getDate().getTime() - dNow
						.getTime()) / 1000;

				if (seconds >= 0) {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(
							context.getApplicationContext(), notifications.get(j)
									.getId(), i, 0);
					AlarmManager alarmManager = (AlarmManager) context
							.getSystemService(Context.ALARM_SERVICE);
					Util.setAlarm(alarmManager, AlarmManager.RTC_WAKEUP,
							System.currentTimeMillis() + (1000 * seconds),
							pendingIntent);

//					System.out.println("---> Boot Notif category " + notifications.get(j).getCategory()
//							+ ", level " + notifications.get(j).getLevel() + " in " + seconds + " seconds");
				}
			}
		}
	}

	private void getPreferencesNotifications(Context context) {
		if (null == notifications) {
			notifications = new ArrayList<AchievementLevelAndNotificationPreference>();
		}

		// load tasks from preference
		SharedPreferences prefs = context.getSharedPreferences(Constantes.PREF_NAME,
				Context.MODE_PRIVATE);

		notifications = (ArrayList<AchievementLevelAndNotificationPreference>) ObjectSerializer
				.deserialize(prefs
						.getString(
								Constantes.NOTIFICATIONS_ACHIEVEMENTS,
								ObjectSerializer
										.serialize(new ArrayList<AchievementLevelAndNotificationPreference>())));

	}
}
