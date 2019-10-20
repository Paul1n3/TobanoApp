package net.tobano.quitsmoking.app;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import net.tobano.quitsmoking.app.util.ViewSizeManager;

import java.util.ArrayList;

/**
 * Created by Carine on 02/06/16.
 */
public class WidgetListProvider implements RemoteViewsFactory {

    private ArrayList<String> listItem;
    private Context context = null;
    private int appWidgetId;
    private int minWidth;
    private int minHeight;

    public WidgetListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        minWidth = intent.getIntExtra(WidgetService.MINWIDTH, 40);
        minHeight = intent.getIntExtra(WidgetService.MINHEIGHT, 20);

        listItem = new ArrayList<>();
        listItem.add(context.getResources().getString(R.string.tvKwitterTitle));
        listItem.add(context.getResources().getString(R.string.saved));
        listItem.add(context.getResources().getString(R.string.notSmoked));
            listItem.add(context.getResources().getString(R.string.tvCoNotInhaled));
            listItem.add(context.getResources().getString(R.string.tvLifeExpectancyGained));
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_list_row);

        String title = listItem.get(position);

        int padding = 5;
        int width = minWidth - 2*padding;
        int height = 50 - 2*padding;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteView.setTextViewTextSize(R.id.title, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height *2/5, title));
        }
        remoteView.setTextViewText(R.id.title, title);

        String[] info;

        if (title.equals(context.getResources().getString(R.string.saved))){
            info = Dashboard.updateMoney();
        }
        else if (title.equals(context.getResources().getString(R.string.notSmoked))){
            info = Dashboard.updateCigarette();
            info[1] = context.getResources().getString(R.string.unitCigarette);
        }
        else if (title.equals(context.getResources().getString(R.string.tvLifeExpectancyGained))){
            info = Dashboard.updateLife();
        }
        else if (title.equals(context.getResources().getString(R.string.tvCoNotInhaled))) {
            info = Dashboard.updateCo();
        }
        else {
            info = Dashboard.updateTime();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteView.setTextViewTextSize(R.id.value, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height*3/5, info[0]));
            remoteView.setTextViewTextSize(R.id.unit, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height*3/5, info[1]));
        }
        remoteView.setTextViewText(R.id.value, info[0]);
        remoteView.setTextViewText(R.id.unit, info[1]);

        //onclick item listview
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DASHBOARD);
        remoteView.setOnClickFillInIntent(R.id.item, fillInIntent);

        return remoteView;
    }

    @Override
    public void onCreate() {
        // no-op
    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.)
        // If you return null here, you will get the default loading view
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }
}