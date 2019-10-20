package net.tobano.quitsmoking.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

//import com.astuetz.PagerSlidingTabStrip.IconTabProvider;

class MenuPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public MenuPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

	public static int getPageIconResId(int position) {
        if (position == Start.FRAGMENT_DASHBOARD)
            return R.drawable.menu_icon_statistics_selector;
        if (position == Start.FRAGMENT_ACHIEVEMENTS)
            return R.drawable.menu_icon_achievements_selector;
        if (position == Start.FRAGMENT_WILLPOWER)
            return R.drawable.menu_icon_willpower_selector;
        if (position == Start.FRAGMENT_MORE)
            return R.drawable.menu_icon_more_selector;
        //Social as last because FRAGMENT_WILLPOWER == FRAGMENT_SOCIAL when social is not displayed
        if (position == Start.FRAGMENT_SOCIAL)
            return R.drawable.menu_icon_social_selector;
        return R.drawable.menu_icon_more_selector;
	}
}
