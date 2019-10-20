package net.tobano.quitsmoking.app;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Carine on 02/06/16.
 */
public class WidgetService extends RemoteViewsService {

    // for the adapter of the ListView / StackView in Widget

    public static String TYPE = "TYPE";
    public static String TYPE_LIST = "LIST";
    public static String TYPE_STACK = "STACK";
    public static String MINWIDTH = "minwidth_widget";
    public static String MINHEIGHT = "minheight_widget";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        RemoteViewsFactory factory = null;

        String type = intent.getStringExtra(TYPE);
        if (type.equals(TYPE_LIST)){
            factory = new WidgetListProvider(this.getApplicationContext(), intent);
            return (factory);
        }
        else if (type.equals(TYPE_STACK)){
            factory = new WidgetStackProvider(this.getApplicationContext(), intent);
            return (factory);
        }
        else{
            return factory;
        }
    }
}