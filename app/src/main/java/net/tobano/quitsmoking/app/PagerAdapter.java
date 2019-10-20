package net.tobano.quitsmoking.app;

import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * The <code>PagerAdapter</code> serves the fragments when paging.
 * 
 * @author Nicolas Lett
 */
public class PagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	private Context mContext;

	/**
	 * @param fm
	 * @param fragments
	 */
	public PagerAdapter(FragmentManager fm, List<Fragment> fragments,
			Context context) {
		super(fm);
		this.fragments = fragments;
		this.mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return this.fragments.size();
	}
}
