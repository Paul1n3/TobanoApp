package net.tobano.quitsmoking.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Carine on 12/07/16.
 */
public class WidgetConfigure extends Activity {

    // NOW NOT USED
    // BECAUSE WIDGET DOES NOT APPEAR

    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        Intent intent = new Intent(WidgetConfigure.this, Splash.class);
        intent.putExtra(Start.OPEN_FROM_WIDGET, Start.FRAGMENT_ACHIEVEMENTS);
        startActivity(intent);

        setResult(RESULT_OK, intent);
    }
}
