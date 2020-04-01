package openfoodfacts.github.scrachx.openfood.views.adapters;


import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.State;

public class DietIngredientsProductFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<String> navMenuTitles;
    private List<BaseFragment> fragments;

    public DietIngredientsProductFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.navMenuTitles = new ArrayList<>();
    }

    public void addFragment(BaseFragment fragment, String title) {
        this.fragments.add(fragment);
        this.navMenuTitles.add(title);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return navMenuTitles.get(position);
    }

    public void refresh(State state) {
        for (BaseFragment f : fragments) {
            if (f.isAdded()) {
                f.refreshView(state);
            }
        }
    }
}