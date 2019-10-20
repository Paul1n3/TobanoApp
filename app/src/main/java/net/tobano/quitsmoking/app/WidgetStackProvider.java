package net.tobano.quitsmoking.app;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.ViewSizeManager;

/**
 * Created by Carine on 02/06/16.
 */
public class WidgetStackProvider implements RemoteViewsFactory {

    private ArrayList<String> listItem;
    private Context context = null;
    private int appWidgetId;
    private int minWidth;
    private int minHeight;

    public WidgetStackProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        minWidth = intent.getIntExtra(WidgetService.MINWIDTH, 40);
        minHeight = intent.getIntExtra(WidgetService.MINHEIGHT, 20);

        listItem = new ArrayList<>();
        listItem.add(context.getResources().getString(R.string.level));
        listItem.add(context.getResources().getString(R.string.tvKwitterTitle));
        listItem.add(context.getResources().getString(R.string.saved));
        listItem.add(context.getResources().getString(R.string.notSmoked));
        if(Start.isPremium) {
            listItem.add(context.getResources().getString(R.string.tvCoNotInhaled));
            listItem.add(context.getResources().getString(R.string.tvLifeExpectancyGained));
        }
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_stack_item);

        String title = listItem.get(position);

        if (title.equals(context.getResources().getString(R.string.level))){
            remoteView.setViewVisibility(R.id.levelview, LinearLayout.VISIBLE);
            remoteView.setViewVisibility(R.id.statview, RelativeLayout.GONE);

            String level = Util.getUserLevel( Util.getUserXP() );
            remoteView.setTextViewText(R.id.level, level);
            int img = WidgetProvider.getImageFromRank();
            remoteView.setImageViewResource(R.id.rank, img);

            //onclick item stackview
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DEFAULT);
            remoteView.setOnClickFillInIntent(R.id.item, fillInIntent);
        }
        else{
            remoteView.setViewVisibility(R.id.levelview, LinearLayout.GONE);
            remoteView.setViewVisibility(R.id.statview, RelativeLayout.VISIBLE);

            int padding = 5;
            int width = minWidth - 2*padding;
            int height = minHeight - 2*padding;

            remoteView.setTextViewText(R.id.title, title);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                remoteView.setTextViewTextSize(R.id.title, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height/3, title));
            }

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
                remoteView.setTextViewTextSize(R.id.value, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height/3, info[0]));
                remoteView.setTextViewTextSize(R.id.unit, TypedValue.COMPLEX_UNIT_SP, ViewSizeManager.textFitWithTextview(context, width, height/3, info[1]));
            }
            remoteView.setTextViewText(R.id.value, info[0]);
            remoteView.setTextViewText(R.id.unit, info[1]);

            //onclick item stackview
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_DASHBOARD);
            remoteView.setOnClickFillInIntent(R.id.item, fillInIntent);
        }

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