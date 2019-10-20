package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import net.tobano.quitsmoking.app.R;

public class TutorialStart extends Fragment {
	/**
	 * (non-Javadoc)
	 *
	 * @see Fragment#onCreateView(LayoutInflater,
	 *      ViewGroup, Bundle)
	 */

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = (RelativeLayout) inflater.inflate(R.layout.tutorial_start,
				container, false);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);

//		TextView tvGetStarted = (TextView) v.findViewById(R.id.tvGetStarted);
//		tvGetStarted.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				getActivity().finish();
//			}
//		});

		RelativeLayout rlGetStarted = (RelativeLayout) v.findViewById(R.id.rlGetStarted);
		rlGetStarted.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Tutorial)getActivity()).hideTutorial();
			}
		});

		if (container == null) {
			return null;
		}
		return v;
	}
}
