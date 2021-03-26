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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ServerListAdapter extends ArrayAdapter<ServerDescription> {

    ArrayList<ServerDescription> filteredList;
    ArrayList<ServerDescription> originalList;
    private Fragment fragment;

    public ServerListAdapter(@NonNull Context context,
                             Fragment fragment,
                             ArrayList<ServerDescription> servers) {

        super(context, 0);
        this.filteredList = new ArrayList<ServerDescription>(servers);
        this.originalList = servers;
        addAll(filteredList);
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


    private String convertToBasic(String value) {
        String result = Normalizer.normalize(value, Normalizer.Form.NFD);
        result = result.replaceAll("[^\\p{ASCII}]", "");

        return result.toLowerCase();

    }

    @Override
    public android.widget.Filter getFilter() {
        android.widget.Filter filter = new android.widget.Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                ArrayList<ServerDescription> newList = new ArrayList<>();
                if (originalList == null) {
                    originalList = new ArrayList<ServerDescription>(filteredList);
                }

                if (charSequence == null || charSequence.length() == 0) {

                    // set the Original result to return
                    results.count = originalList.size();
                    results.values = originalList;
                }
                else {
                    String nCharSequence = convertToBasic(charSequence.toString());

                    for (int i = 0; i < originalList.size(); ++i) {
                        ServerDescription description = originalList.get(i);
                        if (convertToBasic(description.getName()).contains(nCharSequence) ||
                                convertToBasic(description.getUrl()).contains(nCharSequence) ||
                                convertToBasic(description.getDescription()).contains(nCharSequence))
                            newList.add(description);
                    }
                    results.count = newList.size();
                    results.values = newList;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (ArrayList<ServerDescription>) filterResults.values;
                clear();
                addAll(filteredList);
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public void updateList() {
        clear();
        addAll(originalList);
        notifyDataSetChanged();
    }
}
