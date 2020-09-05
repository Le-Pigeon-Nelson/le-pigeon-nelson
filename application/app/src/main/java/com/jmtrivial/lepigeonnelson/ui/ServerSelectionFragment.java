package com.jmtrivial.lepigeonnelson.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.jmtrivial.lepigeonnelson.MainActivity;
import com.jmtrivial.lepigeonnelson.R;
import com.jmtrivial.lepigeonnelson.broadcastplayer.LocationService;
import com.jmtrivial.lepigeonnelson.broadcastplayer.Server;

import java.util.ArrayList;

public class ServerSelectionFragment extends Fragment {
    private ServerListAdapter serverListAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server_selection, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();

        serverListAdapter = new ServerListAdapter(view.getContext(), activity.servers);

        final ListView list = view.findViewById(R.id.list_view);
        list.setAdapter(serverListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (LocationService.getLocationManager(getContext()).locationServiceAvailable) {
                    activity.setActiveServer(serverListAdapter.getItem(position));
                    NavHostFragment.findNavController(ServerSelectionFragment.this)
                            .navigate(R.id.action_ListFragment_to_ListenFragment);
                }
                else {
                    Toast.makeText(getActivity(), "Localisation non disponible.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class ServerListAdapter extends ArrayAdapter<Server> {


        public ServerListAdapter(@NonNull Context context, ArrayList<Server> servers) {
            super(context, 0, servers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_serverlist, container, false);
            }

            ((TextView) convertView.findViewById(R.id.serverName)).setText(getItem(position).getName());
            ((TextView) convertView.findViewById(R.id.serverDescription)).setText(getItem(position).getDescription());

            return convertView;
        }
    }
}