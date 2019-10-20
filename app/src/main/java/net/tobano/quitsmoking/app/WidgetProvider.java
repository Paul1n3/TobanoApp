package net.tobano.quitsmoking.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

/**
 * Created by Carine on 01/06/16.
 */

public class WidgetProvider extends AppWidgetProvider {

    private static final String LIST_ACTION = "CLICK_FROM_LIST";
    private static final String STACK_ACTION = "CLICK_FROM_STACK";

    public static final String UPDATE_ACTION = "UPDATE_FROM_APP";

    private RemoteViews remoteView;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        final int N = appWidgetIds.length;
		/*int[] appWidgetIds holds ids of multiple instance of your widget
		 * meaning you are placing more than one widgets on your homescreen*/
        for (int i = 0; i < N; ++i) {
            // See the dimensions
            // TODO
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetIds[i]);
            // Get min width and height.
            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            // update widget
            RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        // See the dimensions
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId,
                getRemoteViews(context, minWidth, minHeight, appWidgetId));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
    }

    /**
     * Determine appropriate view based on width provided.
     *
     * @param minWidth
     * @param minHeight
     * @return
     */
    private RemoteViews getRemoteViews(Context context, int minWidth,
                                       int minHeight, int appWidgetId) {

        try {
            // when we have data by the app
            // First find out rows and columns based on width provided.
            int rows = getCellsForSize(minHeight);
            int columns = getCellsForSize(minWidth);

            // to update informations on container (list or stack)
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            if ((rows == 2 && columns == 2) ||
                    (columns == 3 && rows == 3) ||
                    (rows == 2 && columns == 3)) {
                widgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_view);
            }
            else{
                widgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.list);
            }

            if ((rows == 2 && columns == 2) ||
                    (columns == 3 && rows == 3) ||
                    (rows == 2 && columns == 3)) {
                //which layout to show on widget
                remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_layout_stack);
                //RemoteViews Service needed to provide adapter for ListView
                Intent svcIntent = new Intent(context, WidgetService.class);
                // define type of widget in the app
                svcIntent.putExtra(WidgetService.TYPE, WidgetService.TYPE_STACK);
                //passing app widget id to that RemoteViews Service
                svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                // passing min width and height of widget
                svcIntent.putExtra(WidgetService.MINWIDTH, minWidth);
                svcIntent.putExtra(WidgetService.MINHEIGHT, minHeight);
                //setting a unique Uri to the intent
                svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
                //setting adapter to listview of the widget
                remoteView.setRemoteAdapter(R.id.stack_view, svcIntent);
                // define Intent to getBack click on item of list
                Intent clickIntent = new Intent(context, WidgetProvider.class);
                clickIntent.setAction(STACK_ACTION);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                remoteView.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);
            }
            else{
                if (rows < columns) { // horizontal widget
                    //which layout to show on widget
                    remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_layout_horizontal);
                    //RemoteViews Service needed to provide adapter for ListView
                    Intent svcIntent = new Intent(context, WidgetService.class);
                    // define type of widget in the app
                    svcIntent.putExtra(WidgetService.TYPE, WidgetService.TYPE_LIST);
                    //passing app widget id to that RemoteViews Service
                    svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    // passing min width and height of widget
                    svcIntent.putExtra(WidgetService.MINWIDTH, minWidth);
                    svcIntent.putExtra(WidgetService.MINHEIGHT, minHeight);
                    //setting a unique Uri to the intent
                    svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
                    //setting adapter to listview of the widget
                    remoteView.setRemoteAdapter(R.id.list, svcIntent);
                    // define Intent to getBack click on item of list
                    Intent clickIntent = new Intent(context, WidgetProvider.class);
                    clickIntent.setAction(LIST_ACTION);
                    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                    PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteView.setPendingIntentTemplate(R.id.list, toastPendingIntent);
                }
                else { // vertical list
                    //which layout to show on widget
                    remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_layout_vertical);
                    //RemoteViews Service needed to provide adapter for ListView
                    Intent svcIntent = new Intent(context, WidgetService.class);
                    svcIntent.putExtra(WidgetService.TYPE, WidgetService.TYPE_LIST); // define type of widget in the app
                    svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    svcIntent.putExtra(WidgetService.MINWIDTH, minWidth);
                    svcIntent.putExtra(WidgetService.MINHEIGHT, minHeight);
                    svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));//setting a unique Uri to the intent
                    //setting adapter to listview of the widget
                    remoteView.setRemoteAdapter(R.id.list, svcIntent);
                    // define Intent to getBack click on item of list
                    Intent clickIntent = new Intent(context, WidgetProvider.class);
                    clickIntent.setAction(LIST_ACTION);
                    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                    PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteView.setPendingIntentTemplate(R.id.list, toastPendingIntent);
                }
                // Get level
                String level = Util.getUserLevel( Util.getUserXP() );
                remoteView.setTextViewText(R.id.level, level);
                // Get rank
                int img = getImageFromRank();
                remoteView.setImageViewResource(R.id.rank, img);
            }
            // Get how many achievements are unlockable
            int unlockable = 0;
            int nbCategory = Util.achievementsCategories.size();
//            if (!Start.isPremium) {
//                nbCategory -= 2;
//            }

            for(int i = 0 ; i < nbCategory ; ++i) {
                unlockable += Integer.valueOf(Util.getNbOfLevelAvailableForUnlockByCategory(Util.achievementsCategories.get(i)));
            }
            remoteView.setTextViewText(R.id.notific, String.valueOf(unlockable));
            // for opening Activity if user click on the level view in widget with list container
            Intent levelIntent = new Intent(context, Splash.class);
            levelIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);
            levelIntent.setData(Uri.parse(levelIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent levelPendingIntent = PendingIntent.getActivity(context, 0, levelIntent, 0);
            remoteView.setOnClickPendingIntent(R.id.first, levelPendingIntent);
            // for opening Activity if user click on the green button
            Intent greenButtonIntent = new Intent(context, Splash.class);
            greenButtonIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_ACHIEVEMENTS);
            greenButtonIntent.setData(Uri.parse(greenButtonIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent greenButtonPendingIntent = PendingIntent.getActivity(context, 0, greenButtonIntent, 0);
            remoteView.setOnClickPendingIntent(R.id.notific, greenButtonPendingIntent);
        }
        catch (Exception e) {
            // when this app is closed and the widget has not data
            remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_layout_init);
            Intent refreshIntent = new Intent(context, Splash.class);
            refreshIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);
            refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent refreshButtonPendingIntent = PendingIntent.getActivity(context, 0, refreshIntent, 0);
            remoteView.setOnClickPendingIntent(R.id.refresh, refreshButtonPendingIntent);

        }
        // return widget view
        return remoteView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        if (intent.getAction().equals(LIST_ACTION)) {
            // receive from list or stack item
            int fragmentWanted = intent.getIntExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);
            Intent activity = new Intent(context, Splash.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.putExtra(Start.OPEN_FROM_WIDGET, fragmentWanted);
            context.startActivity(activity);
        }
        else if (intent.getAction().equals(STACK_ACTION)) {
            // receive from list or stack item
            int fragmentWanted = intent.getIntExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);
            Intent activity = new Intent(context, Splash.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.putExtra(Start.OPEN_FROM_WIDGET, fragmentWanted);
            context.startActivity(activity);
        }
        else if (intent.getAction().equals(UPDATE_ACTION)) {
            onUpdate(context, mgr, ids);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public static int getImageFromRank() {
        String level = Util.getUserLevel(Util.getUserXP());
        int rank = Integer.parseInt(Util.getUserRankValue(Integer.parseInt(level)));
        if (rank == 1){
            return R.drawable.rank1;
        }
        else if (rank == 2){
            return R.drawable.rank2;
        }
        else if (rank == 3){
            return R.drawable.rank3;
        }
        else if (rank == 4){
            return R.drawable.rank4;
        }
        else if (rank == 5){
            return R.drawable.rank5;
        }
        else if (rank == 6){
            return R.drawable.rank6;
        }
        else if (rank == 7){
            return R.drawable.rank7;
        }
        else if (rank == 8){
            return R.drawable.rank8;
        }
        else if (rank == 9){
            return R.drawable.rank9;
        }
        else if (rank == 10){
            return R.drawable.rank10;
        }
        else if (rank == 11){
            return R.drawable.rank11;
        }
        else {
            return R.drawable.rank12;
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        // size = 70 × numberOfCells − 30
        // old formula : numberOfCells * 74 - 2 = size
        int numberOfCells = size + 30;
        return ((int)(numberOfCells/70));
    }
}
