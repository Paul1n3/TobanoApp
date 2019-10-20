package net.tobano.quitsmoking.app;

import android.widget.EditText;

/**
 * Created by sky_cooker on 04/03/18.
 */

public class Validator {

    public static boolean isEmpty(EditText editText) {

        String input = editText.getText().toString().trim();
        return input.length() == 0;

    }

    public static void setError(EditText editText, String errorString) {

        editText.setError(errorString);

    }

    public static void clearError(EditText editText) {

        editText.setError(null);

    }
}
