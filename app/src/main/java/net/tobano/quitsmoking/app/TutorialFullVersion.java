package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import net.tobano.quitsmoking.app.R;

public class TutorialFullVersion extends Fragment {
	/**
	 * (non-Javadoc)
	 *
	 * @see Fragment#onCreateView(LayoutInflater,
	 *      ViewGroup, Bundle)
	 */

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = (RelativeLayout) inflater.inflate(R.layout.unlock_fullversion,
				container, false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

		ImageButton btnActionClose = (ImageButton) v.findViewById(R.id.actionclose);
		btnActionClose.setVisibility(View.GONE);
		Button btnUnlock = (Button) v.findViewById(R.id.unlock_button);
		btnUnlock.setVisibility(View.GONE);

		if (container == null) {
			return null;
		}
		return v;
	}
}
