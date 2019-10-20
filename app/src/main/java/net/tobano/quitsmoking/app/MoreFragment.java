package net.tobano.quitsmoking.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.tobano.quitsmoking.app.R;

public class MoreFragment extends Fragment {

    private View v;

	public static final String TAG_MORELISTFRAGMENT = "MORE_LIST_FRAGMENT";
	public static final String TAG_SETTINGS = "SETTINGS";
	public static final String TAG_MOTIVATION_CARDS = "MOTIVATION_CARDS";
	public static final String TAG_PERSONALIZATION = "PERSONALIZATION";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.more_menu_items, container, false);

		// clear back stack (remove useless settings)
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (v.findViewById(R.id.fragment_container) != null) {

			displayMorelistFragment();
		}

		return v;
	}

	public void reinitilization() {
		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (v.findViewById(R.id.fragment_container) != null) {

			displaySettingsFragment();
		}
	}

	private void displayMorelistFragment(){

		// Create a new Fragment to be placed in the activity layout
		MoreListFragment firstFragment = new MoreListFragment();

		// Add the fragment to the 'fragment_container' FrameLayout
		getActivity().getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, firstFragment, TAG_MORELISTFRAGMENT).commit();
	}

	private void displaySettingsFragment(){

		// Create a new Fragment to be placed in the activity layout
		Settings settingsFragment = new Settings();

		// Add the fragment to the 'fragment_container' FrameLayout
		getActivity().getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, settingsFragment, TAG_MORELISTFRAGMENT).commit();
	}
}
