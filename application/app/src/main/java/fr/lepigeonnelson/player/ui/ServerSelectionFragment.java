package fr.lepigeonnelson.player.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import fr.lepigeonnelson.player.MainActivity;
import fr.lepigeonnelson.player.R;
import fr.lepigeonnelson.player.broadcastplayer.SensorsService;
import fr.lepigeonnelson.player.broadcastplayer.ServerDescription;

public class ServerSelectionFragment extends Fragment implements ServerDescription.ServerDescriptionListener {
    private ServerListAdapter serverListAdapter;
    private MainActivity activity;
    private View messageBar;
    private TextView messageText;
    private Button messageButton;
    private ListView list;

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

        serverListAdapter = new ServerListAdapter(view.getContext(), this, activity.servers);

        for (ServerDescription server: activity.servers) {
            server.setListener(this);
        }

        list = view.findViewById(R.id.list_view);
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
                        .navigate(R.id.action_add);
            }
        });

        messageBar = view.findViewById(R.id.message_bar);
        messageText = view.findViewById(R.id.message_text);
        messageButton = view.findViewById(R.id.message_button);

        messageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.checkSensorsSettings();
                    }});

    }

    @Override
    public void onResume() {
        serverListAdapter.updateList();
        super.onResume();
        activity.setActiveFragment(MainActivity.SERVER_SELECTION_FRAGMENT, this);
        updateGPSMessage();
    }

    @Override
    public void onUpdatedDescription(ServerDescription description) {
        activity.saveServerDescription(description, "");
        description.setListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.updateList();
            }
        });
    }

    public void updateGPSMessage() {
        int result = activity.getCurrentSensorSettingResult();
        if (result == SensorsService.INIT_OK) {
            messageBar.setVisibility(View.GONE);
            list.setEnabled(true);
        }
        else {
            switch(result) {
                case SensorsService.ERROR_DURING_INITIALIZATION:
                    messageText.setText("Erreur d'initialisation du GPS");
                    break;
                case SensorsService.MISSING_PERMISSIONS:
                    messageText.setText("Permissions GPS manquantes");
                    break;
                case SensorsService.RESOLUTION_REQUIRED:
                default:
                    messageText.setText("Veuillez activer le GPS");
                    break;
            }
            messageBar.setVisibility(View.VISIBLE);
            list.setEnabled(false);
        }

    }

}