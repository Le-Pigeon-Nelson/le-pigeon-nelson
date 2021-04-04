package fr.lepigeonnelson.player.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;
import fr.lepigeonnelson.player.broadcastplayer.ServerDescription;

import static android.view.View.GONE;

public class ListenBroadcastFragment extends Fragment {

    private MainActivity activity;
    private ListView listVerbose;
    private VerboseAdapter verboseAdapter;
    private View view;

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

        verboseAdapter = new VerboseAdapter(getContext(), this);
        listVerbose = view.findViewById(R.id.list_verbose);
        listVerbose.setAdapter(verboseAdapter);

        this.view = view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.getActiveServer();
        activity.setActiveFragment(MainActivity.LISTEN_BROADCAST_FRAGMENT, this);
        activity.playBroadcast();

        if (activity.getShowVerboseMessages()) {
            view.findViewById(R.id.list_verbose_panel).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.list_verbose_panel).setVisibility(GONE);
        }
    }

    public void setActiveServer(String name) {
        TextView text = getView().findViewById(R.id.textview_name);
        text.setText("Vous écoutez " + name);
    }


    public void setActiveServerDescription(String description) {
        TextView text = getView().findViewById(R.id.textview_description);
        text.setText(description);

    }

    public void setInternalValues(ArrayList<Pair<String, String>> values) {
        verboseAdapter.clear();
        verboseAdapter.addAll(values);
        verboseAdapter.notifyDataSetChanged();
    }

    private class VerboseAdapter extends ArrayAdapter<Pair<String, String> > {

        private ListenBroadcastFragment fragment;

        public VerboseAdapter(@NonNull Context context,
                              ListenBroadcastFragment fragment) {
            super(context, 0);
            this.fragment = fragment;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = fragment.getLayoutInflater().inflate(R.layout.item_verbose, container, false);
            }
            ((TextView) convertView.findViewById(R.id.key)).setText(getItem(position).first);
            ((TextView) convertView.findViewById(R.id.value)).setText(getItem(position).second);
            return convertView;
        }
    }
}