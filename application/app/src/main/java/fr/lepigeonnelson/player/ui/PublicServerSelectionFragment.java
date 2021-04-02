package fr.lepigeonnelson.player.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;
import fr.lepigeonnelson.player.broadcastplayer.ServerDescription;

public class PublicServerSelectionFragment extends Fragment implements ServerDescription.ServerDescriptionListener {

    private ServerListAdapter serverListAdapter;
    private MainActivity activity;
    private ListView list;
    private EditText filterText;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_server_selection, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) getActivity();

        for (ServerDescription server: activity.servers) {
            server.setListener(this);
        }

        filterText = (EditText) view.findViewById(R.id.filter_text);

        filterText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                serverListAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        serverListAdapter = new ServerListAdapter(view.getContext(), this, activity.publicServers);


        list = view.findViewById(R.id.list_view);
        list.setAdapter(serverListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.updateServer(serverListAdapter.getItem(position));
                NavHostFragment.findNavController(getParentFragment()).popBackStack();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        // update list
        notifyDataSetChanged();
    }

    @Override
    public void onUpdatedDescription(ServerDescription description) {
        description.setListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        for(ServerDescription desc: activity.publicServers) {
            desc.setListener(this);
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.updateList();
            }
        });
    }
}
