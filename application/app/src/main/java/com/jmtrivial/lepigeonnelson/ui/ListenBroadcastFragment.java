package com.jmtrivial.lepigeonnelson.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.jmtrivial.lepigeonnelson.MainActivity;
import com.jmtrivial.lepigeonnelson.R;
import com.jmtrivial.lepigeonnelson.broadcastplayer.BroadcastPlayer;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

public class ListenBroadcastFragment extends Fragment {

    private MainActivity activity;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listen_broadcast, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.stopBroadcast();
                NavHostFragment.findNavController(ListenBroadcastFragment.this).popBackStack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.getActiveServer();
        activity.setActiveFragment(MainActivity.LISTEN_BROADCAST_FRAGMENT, this);
        activity.playBroadcast();
    }

    public void setActiveServer(String name) {
        TextView text = getView().findViewById(R.id.textview_second);
        text.setText("Vous écoutez " + name);
    }





}