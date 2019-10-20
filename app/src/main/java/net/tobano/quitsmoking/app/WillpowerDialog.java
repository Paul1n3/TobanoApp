package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import net.tobano.quitsmoking.app.R;

public class WillpowerDialog extends DialogFragment {

	public static WillpowerDialog newInstance(String message) {
		WillpowerDialog dialog = new WillpowerDialog();
		Bundle args = new Bundle();
		args.putString("willpower_message", message);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.willpower_dialog, container, false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		String message = getArguments().getString("willpower_message");

		TextView tvMessage = (TextView) v.findViewById(R.id.tvMessage);
		tvMessage.setText(message);

		getDialog().setTitle(message);

		return v;
	}
}
