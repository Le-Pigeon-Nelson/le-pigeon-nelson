package com.jmtrivial.lepigeonnelson.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jmtrivial.lepigeonnelson.MainActivity;
import com.jmtrivial.lepigeonnelson.R;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

import java.util.ArrayList;

public class ServerSelectionFragment extends Fragment implements ServerDescription.ServerDescriptionListener {
    private ServerListAdapter serverListAdapter;
    private MainActivity activity;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server_selection, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) getActivity();

        serverListAdapter = new ServerListAdapter(view.getContext(), activity.servers);

        for (ServerDescription server: activity.servers) {
            server.setListener(this);
        }

        ListView list = view.findViewById(R.id.list_view);
        list.setAdapter(serverListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.setActiveServer(serverListAdapter.getItem(position));
                NavHostFragment.findNavController(ServerSelectionFragment.this)
                        .navigate(R.id.action_ListFragment_to_ListenFragment);
            }
        });
        list.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        ServerDescription server = serverListAdapter.getItem(position);
                        if (server.isEditable()) {
                            activity.setEditServer(server);
                            NavHostFragment.findNavController(ServerSelectionFragment.this)
                                    .navigate(R.id.action_edit);
                        }
                        return true;
                    }
                }
        );

        FloatingActionButton addButton = view.findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setEditNewServer();
                NavHostFragment.findNavController(ServerSelectionFragment.this)
                        .navigate(R.id.action_edit);
            }
        });

    }

    @Override
    public void onResume() {
        serverListAdapter.notifyDataSetChanged();
        super.onResume();
        activity.setActiveFragment(MainActivity.SERVER_SELECTION_FRAGMENT, this);

    }

    @Override
    public void onUpdatedDescription(ServerDescription description) {
        activity.saveServerDescription(description);

        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.notifyDataSetChanged();
            }
        });
    }

    private class ServerListAdapter extends ArrayAdapter<ServerDescription> {


        public ServerListAdapter(@NonNull Context context, ArrayList<ServerDescription> servers) {
            super(context, 0, servers);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_serverlist, container, false);
            }

            ((TextView) convertView.findViewById(R.id.serverName)).setText(getItem(position).getName());
            ((TextView) convertView.findViewById(R.id.serverDescription)).setText(getItem(position).getDescription());
            if (getItem(position).getPeriod() != 0) {
                convertView.findViewById(R.id.broadcastSingle).setVisibility(View.GONE);
                convertView.findViewById(R.id.broadcastStreaming).setVisibility(View.VISIBLE);
            }
            else {
                convertView.findViewById(R.id.broadcastSingle).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.broadcastStreaming).setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}