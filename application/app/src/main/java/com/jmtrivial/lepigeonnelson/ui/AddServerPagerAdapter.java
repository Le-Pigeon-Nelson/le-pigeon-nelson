package com.jmtrivial.lepigeonnelson.ui;

import android.os.Bundle;
import android.util.ArrayMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class AddServerPagerAdapter extends FragmentStatePagerAdapter {

    public AddServerPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private ArrayMap<Integer, Fragment> fragments = new ArrayMap<>();
    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            PublicServerSelectionFragment fragment = new PublicServerSelectionFragment();
            return fragment;
        }
        else {
            EditServerFragment fragment = new EditServerFragment();
            return fragment;
        }
    }

    public Fragment getFragment(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return "Serveurs publics";
        else
            return "Serveur personnalisé";
    }
}

