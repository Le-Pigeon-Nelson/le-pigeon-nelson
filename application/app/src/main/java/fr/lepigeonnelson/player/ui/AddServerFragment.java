package fr.lepigeonnelson.player.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;

public class AddServerFragment extends Fragment {

    private AddServerPagerAdapter pagerAdapter;


    private MainActivity activity;
    private ViewPager viewPager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_server, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) getActivity();

        pagerAdapter = new AddServerPagerAdapter(getChildFragmentManager());
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        activity.setActiveFragment(MainActivity.ADD_SERVER_FRAGMENT, this);

    }
}

