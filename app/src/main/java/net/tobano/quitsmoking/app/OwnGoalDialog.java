package net.tobano.quitsmoking.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.util.Theme;

/**
 * Created by Carine
 */

public class OwnGoalDialog extends DialogFragment {

    private static String GOAL_NAME = "goal_name";
    private static String GOAL_VALUE = "goal_value";

    public static OwnGoalDialog newInstance(String goalName, String goalValue, Theme theme) {
        OwnGoalDialog dialog = new OwnGoalDialog();
        Bundle args = new Bundle();
        args.putString(GOAL_NAME, goalName);
        args.putString(GOAL_VALUE, goalValue);
        args.putString(Constantes.THEME, theme.name());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.owngoal_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Theme theme = Theme.valueOf(getArguments().getString(Constantes.THEME));

        String name = getArguments().getString(GOAL_NAME);
        TextView tvName = (TextView) v.findViewById(R.id.goalTitle);
        tvName.setText(name);
        tvName.setBackgroundDrawable(getEditTextDrawable(theme));

        TextView tvValue = (TextView) v.findViewById(R.id.tvValue);
        tvValue.setText(getResources().getString(R.string.goalDialogValue) + " (" + Start.currency + ")");

        String value = getArguments().getString(GOAL_VALUE);
        EditText etValue = (EditText) v.findViewById(R.id.goalValue);
        if (!value.equals("")) {
            etValue.setText(value);
        }
        etValue.setBackgroundDrawable(getEditTextDrawable(theme));

        Button btnValidate = (Button) v.findViewById(R.id.btnValidate);
        btnValidate.setTextColor(getColor(theme));
        btnValidate.setBackgroundDrawable(getButtonDrawable(theme));

        Button btnBack = (Button) v.findViewById(R.id.btnBack);
        btnBack.setTextColor(getColor(theme));
        btnBack.setBackgroundDrawable(getButtonDrawable(theme));

        return v;
    }

    private Drawable getButtonDrawable(Theme theme) {
        if (theme == Theme.GREEN){
            return ContextCompat.getDrawable(getActivity(), R.drawable.dialog_green_button);
        }
        else {
            return ContextCompat.getDrawable(getActivity(), R.drawable.dialog_tobano_button);
        }
    }

    private Drawable getEditTextDrawable(Theme theme) {
        if (theme == Theme.GREEN){
            return ContextCompat.getDrawable(getActivity(), R.drawable.edittext_greenborders);
        }
        else {
            return ContextCompat.getDrawable(getActivity(), R.drawable.edittext_tabonoborders);
        }
    }

    private int getColor(Theme theme) {
        if (theme == Theme.GREEN){
            return ContextCompat.getColor(getActivity(), R.color.kwit);
        }
        else {
            return ContextCompat.getColor(getActivity(), R.color.primary_dark);
        }
    }
}
