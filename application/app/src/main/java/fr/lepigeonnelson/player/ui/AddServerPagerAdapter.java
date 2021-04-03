package fr.lepigeonnelson.player.ui;

import android.util.ArrayMap;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class AddServerPagerAdapter extends FragmentStatePagerAdapter {

    private AddServerFragment addFragment;

    public AddServerPagerAdapter(FragmentManager fm, AddServerFragment addFragment) {
        super(fm);
        this.addFragment = addFragment;
    }

    private ArrayMap<Integer, Fragment> fragments = new ArrayMap<>();
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0: {
                PublicServerSelectionFragment fragment = new PublicServerSelectionFragment();
                return fragment;
            }
            case 1: {
                ScannerFragment fragment = new ScannerFragment();
                if (addFragment != null) {
                    fragment.setAddFragment(addFragment);
                    Log.d("AddServerPagerAdapter", "set add fragment " + addFragment);
                }
                else {
                    Log.d("AddServerPagerAdapter", "no add fragment");
                }
                return fragment;
            }
            case 2:
            default: {
                EditServerFragment fragment = new EditServerFragment();
                return fragment;
            }
        }

    }

    public Fragment getFragment(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Serveurs publics";
            case 1:
                return "QR-code";
            case 2:
            default:
                return "Saisie manuelle";
        }
    }

}

