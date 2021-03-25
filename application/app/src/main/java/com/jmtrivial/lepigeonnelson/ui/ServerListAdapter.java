package com.jmtrivial.lepigeonnelson.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jmtrivial.lepigeonnelson.R;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

import java.util.ArrayList;

public class ServerListAdapter extends ArrayAdapter<ServerDescription> {


    private Fragment fragment;

    public ServerListAdapter(@NonNull Context context,
                             Fragment fragment,
                             ArrayList<ServerDescription> servers) {
        super(context, 0, servers);
        this.fragment = fragment;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.item_serverlist, container, false);
        }

        ((TextView) convertView.findViewById(R.id.serverName)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.serverDescription)).setText(getItem(position).getDescription());
        if (getItem(position).getPeriod() != 0) {
            convertView.findViewById(R.id.broadcastSingle).setVisibility(View.GONE);
            convertView.findViewById(R.id.broadcastStreaming).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.broadcastSingle).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.broadcastStreaming).setVisibility(View.GONE);
        }

        return convertView;
    }
}
