package com.jmtrivial.lepigeonnelson;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.jmtrivial.lepigeonnelson.broadcastplayer.SensorsService;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;
import com.jmtrivial.lepigeonnelson.broadcastplayer.BroadcastPlayer;
import com.jmtrivial.lepigeonnelson.broadcastplayer.UIHandler;
import com.jmtrivial.lepigeonnelson.db_storage.AppDatabase;
import com.jmtrivial.lepigeonnelson.ui.EditServerFragment;
import com.jmtrivial.lepigeonnelson.ui.ServerSelectionFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements BroadcastPlayer.BroadcastPlayerListener, AppDatabase.AppDataBaseListener {

    public static final int EDIT_SERVER_FRAGMENT = 1;
    public static final int SERVER_SELECTION_FRAGMENT = 2;
    public static final int SETTINGS_FRAGMENT = 3;
    public static final int LISTEN_BROADCAST_FRAGMENT = 4;

    public ArrayList<ServerDescription> debugServers;
    public ArrayList<ServerDescription> coreServers;
    public ArrayList<ServerDescription> userDefinedServers;
    public ArrayList<ServerDescription> servers;

    private BroadcastPlayer player;
    private AppDatabase db;
    private int activeFragmentType;
    private MenuItem itemSettings;
    private ServerDescription editedServer;
    private Fragment activeFragment;
    private boolean editedServerIsNew;

    public boolean isMainFragment() {
        return activeFragmentType == SERVER_SELECTION_FRAGMENT;
    }

    private final int REQUEST_PERMISSION_COARSE_LOCATION = 1;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 2;
    private boolean showDebugServers;
    private Toolbar toolbar;
    private UIHandler uiHandler;

    // a function to request permissions
    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    // a function to show explanation when asking permission
    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    // show a small message depending on the permission result
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Accès localisation précise accordée.", Toast.LENGTH_SHORT).show();
               } else {
                    Toast.makeText(this, "Accès localisation précise refusée.", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PERMISSION_COARSE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Accès localisation accordée.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Accès localisation refusée.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activeFragmentType = SERVER_SELECTION_FRAGMENT;
        super.onCreate(savedInstanceState);

        editedServer = null;
        activeFragment = null;

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "pigeon-nelson-database").build();
        db.setListener(this);

        // first of all, check permissions for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showExplanation("Accès localisation requise", "Rationale",
                        Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Accès localisation précise requise", "Rationale",
                        Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
            }
        }

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editedServerIsNew = false;
        uiHandler = new UIHandler();


        loadPreferences();

        this.loadServers();

    }

    @Override
    public boolean onSupportNavigateUp() {
        // first close keyboard
        View view = findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // then check if the active fragment is the editor
        if (activeFragmentType == EDIT_SERVER_FRAGMENT) {
            final EditServerFragment editActiveFragment = (EditServerFragment) activeFragment;
            if (editActiveFragment.isModified()) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                editedServer = null;
                                onBackPressed();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Les modifications seront perdues. Voulez-vous revenir à la liste?").setPositiveButton("Oui", dialogClickListener)
                        .setNegativeButton("Annuler", dialogClickListener).show();

            }
            else
                onBackPressed();
        }
        else
            onBackPressed();
        return true;
    }

    private void loadServers() {
        servers = new ArrayList<>();
        coreServers = new ArrayList<>();
        debugServers = new ArrayList<>();
        userDefinedServers = new ArrayList<>();

        player = new BroadcastPlayer(this, 100, uiHandler);
        player.setListener(this);
        player.start();


        createDebugServers();

        createCoreServers();

        // load server descriptions from room
        db.loadAll(userDefinedServers);

        buildServerList();





    }

    private void createCoreServers() {
        // a server to find Museum in neighborhood
        ServerDescription server1 = new ServerDescription("https://lepigeonnelson.jmfavreau.info/protocol2/museums.php");
        server1.setIsEditable(false);

        // a server for orientation
        ServerDescription server2 = new ServerDescription("https://lepigeonnelson.jmfavreau.info/protocol2/compass.php");
        server2.setIsEditable(false);

        coreServers.add(server1);
        coreServers.add(server2);

    }

    private void createDebugServers() {

        // a server to test robustness
        ServerDescription server1 = new ServerDescription("https://http://exemple.fr/");
        server1.setName("Défectueux 1").setDescription("Un serveur injoignable")
                .setPeriod(15).setEncoding("UTF-8").setIsEditable(false);
        debugServers.add(server1);

        // a server to test robustness
        ServerDescription server2 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/jsontests/broken.json");
        server2.setName("Défectueux 2").setDescription("Un json malformé").
                setEncoding("UTF-8").setPeriod(15).setIsEditable(false);
        debugServers.add(server2);

        // a server to test robustness
        ServerDescription server3 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/jsontests/missing-parts.json");
        server3.setName("Défectueux 3").setDescription("Un json avec des champs manquants")
                .setEncoding("UTF-8").setPeriod(15).setIsEditable(false);
        debugServers.add(server3);

        ServerDescription server4 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/helloworld/message.json");
        server4.setIsEditable(false);
        debugServers.add(server4);

        // add an "bonjour le monde" (fr) server
        ServerDescription server5 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/helloworld/message-fr.json");
        server5.setIsEditable(false);
        debugServers.add(server5);

        // add an "bonjour le monde" (fr) server
        ServerDescription server6 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/helloworld/audiomessage-fr.json");
        server6.setIsEditable(false);
        debugServers.add(server6);

        // add a blabla / "bip" server
        ServerDescription server7 = new ServerDescription("https://lepigeonnelson.jmfavreau.info/protocol2/blabla-bip.php");
        server7.setName("Blabla bip").setDescription("Un serveur qui raconte du blabla toutes les 15 secondes, mais qui est coupé par un bip")
                .setEncoding("UTF-8").setPeriod(1).setIsEditable(false);
        debugServers.add(server7);

        // a server to test forgetting constraints
        ServerDescription server8 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/prioritytests/5-messages.json");
        server8.setIsEditable(false);
        debugServers.add(server8);

        // a server to test playable constraints
        ServerDescription server9 = new ServerDescription("https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/protocol-v2/servers/prioritytests/echo.json");
        server9.setIsEditable(false);
        debugServers.add(server9);

    }

    private void loadPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        showDebugServers = preferences.getBoolean("debug_servers", false);

    }

    private void buildServerList() {
        servers.clear();

        if (showDebugServers) {
            servers.addAll(debugServers);
        }
        servers.addAll(coreServers);
        servers.addAll(userDefinedServers);

        // refresh descriptions from server
        for(ServerDescription server: this.servers) {
            if (server.isSelfDescribed() && server.missingDescription()) {
                Log.d("DefaultServers", "load self description for " + server.getUrl());
                player.collectServerDescription(server);
            }
            else
                Log.d("DefaultServers", "not self described " + server.getUrl());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        itemSettings = menu.findItem(R.id.action_settings);

        itemSettings.setVisible(activeFragmentType == SERVER_SELECTION_FRAGMENT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public ServerDescription getActiveServer() {
        return player.getCurrentServer();
    }

    public void setActiveServer(ServerDescription activeServer) {
        player.setCurrentServer(activeServer);
    }

    public void playBroadcast() {
        player.playBroadcast();
    }

    public void stopBroadcast() {
        player.stopBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isMainFragment()) {
            // back to the main fragment
            onBackPressed();
        }
        stopBroadcast();

        // TODO: stop sensor acquisition

    }

    // TODO: add an onResume and start sensor acquisition

    @Override
    protected void onDestroy() {
        player.stopBroadcast();
        player.reset();
        super.onDestroy();
    }

    public BroadcastPlayer getPlayer() {
        return player;
    }

    public void enableDebugServers(boolean showDebugServers) {
        this.showDebugServers = showDebugServers;
        buildServerList();
    }


    @Override  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SensorsService.REQUEST_CHECK_SETTINGS:
                player.checkSensorsSettings();
                break;
            default:
                break;
        }
    }

    public void saveServerDescription(ServerDescription description) {
        Log.d("PigeonNelson", "Save server description " + description.getUrl());
        // save in room this server description
        if (description.isEditable())
            db.add(description);

    }

    @Override
    public void onEndOfBroadcast() {
        if (!isMainFragment()) {
            try {
                onBackPressed();
                stopBroadcast();
            }
            catch (Exception e) {
                // ignore it: the fragment has been deleted before
            }
        }
    }
    @Override
    public void onServerError() {
        Toast.makeText(this, R.string.server_access_error, Toast.LENGTH_SHORT).show();
        if (!isMainFragment())
            onBackPressed();
    }

    @Override
    public void onServerContentError() {
        Toast.makeText(this, R.string.server_content_error, Toast.LENGTH_SHORT).show();
        if (!isMainFragment())
            onBackPressed();
    }

    @Override
    public void onServerGPSError() {
        Toast.makeText(this, R.string.no_GPS_connection, Toast.LENGTH_SHORT).show();
        if (!isMainFragment())
            onBackPressed();
    }

    @Override
    public void onServerDescriptionUpdate(ServerDescription description) {
        Log.d("DefaultServers", "new description for " + description.getUrl());
        for (ServerDescription server : servers) {
            if (description.getUrl().equals(server.getUrl())) {
                server.update(description);
                break;
            }
        }
    }

    @Override
    public void onServerListUpdated() {
        buildServerList();
        if (isMainFragment()) {
            ServerSelectionFragment fragment = (ServerSelectionFragment)activeFragment;
            fragment.notifyDataSetChanged();
        }
    }

    public void updateEditedServer(ServerDescription description) {
        // if this description is a new one, add it to the list
        boolean save = false;
        if (editedServerIsNew) {
            userDefinedServers.add(description);
            save = true;
        }
        else {
            if (editedServer == null) {
                // update the edited server
                editedServer.update(description);
                save = true;
            }
        }
        if (save) {

            saveServerDescription(description);

            if (description.isSelfDescribed()) {
                Log.d("PigeonNelson", "self descripted server, ask for its description");
                player.collectServerDescription(description);
            }

            Log.d("PigeonNelson", "build server list");
            // update view
            buildServerList();
        }

        editedServer = null;

    }

    public void setToolbarTitle(String s) {
        // TODO ???
    }

    public void setActiveFragment(int active, Fragment fragment) {
        switch (active) {
            case EDIT_SERVER_FRAGMENT:
                toolbar.setTitle(R.string.edit_server_fragment);
                toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24);
                break;
            case SERVER_SELECTION_FRAGMENT:

                toolbar.setTitle(R.string.app_name);
                toolbar.setNavigationIcon(R.drawable.ic_baseline_power_off_24);

                break;
            case LISTEN_BROADCAST_FRAGMENT:
                toolbar.setTitle(R.string.app_name);
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);


                break;
            case SETTINGS_FRAGMENT:
                toolbar.setTitle(R.string.settings_fragment);
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
                break;
        }
        if (itemSettings != null)
            itemSettings.setVisible(active == SERVER_SELECTION_FRAGMENT);
        activeFragmentType = active;
        activeFragment = fragment;
    }

    public void setEditNewServer() {
        editedServer = new ServerDescription("");
        editedServerIsNew = true;
    }
    public void setEditServer(ServerDescription server) {
        editedServer = server;
        editedServerIsNew = false;

    }
    public ServerDescription getEditedServer() {
        return editedServer;
    }


    @Override
    public void onServerListUpdatedFromDatabase() {
        uiHandler.sendEmptyMessage(uiHandler.UPDATE_LIST);
    }

    public void deleteSelectedServer() {
        // remove this server from the list of editable servers
        if (editedServer != null) {
            for (Iterator<ServerDescription> iter = userDefinedServers.listIterator(); iter.hasNext(); ) {
                ServerDescription es = iter.next();
                if (es.getUrl().equals(editedServer.getUrl())) {
                    db.delete(es);
                    iter.remove();
                    break;
                }
            }
        }



        // rebuild the server list
        buildServerList();
    }

    public boolean isEditedServerNew() {
        return editedServerIsNew;
    }

    public boolean hasServerWithAddress(String address) {
        for(ServerDescription server: servers) {
            if (address.equals(server.getUrl())) {
                return true;
            }
        }
        return false;

    }
}