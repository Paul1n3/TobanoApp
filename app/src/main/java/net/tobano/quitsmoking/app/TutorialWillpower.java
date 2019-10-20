package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.tobano.quitsmoking.app.R;

/**
 * @author Nicolas Lett
 *
 */
public class TutorialWillpower extends Fragment {
	/**
	 * (non-Javadoc)
	 *
	 * @see Fragment#onCreateView(LayoutInflater,
	 *      ViewGroup, Bundle)
	 */

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = (RelativeLayout) inflater.inflate(R.layout.tutorial_willpower,
				container, false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

		if (container == null) {
			return null;
		}
		return v;
	}
}
