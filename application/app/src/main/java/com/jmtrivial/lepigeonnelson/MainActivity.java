package com.jmtrivial.lepigeonnelson;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.jmtrivial.lepigeonnelson.broadcastplayer.Server;
import com.jmtrivial.lepigeonnelson.broadcastplayer.BroadcastPlayer;
import com.jmtrivial.lepigeonnelson.broadcastplayer.UIHandler;
import com.jmtrivial.lepigeonnelson.ui.ListenBroadcastFragment;
import com.jmtrivial.lepigeonnelson.ui.ServerSelectionFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Server> debugServers;
    public ArrayList<Server> coreServers;
    public ArrayList<Server> userDefinedServers;
    public ArrayList<Server> servers;

    private BroadcastPlayer player;

    private boolean mainFragment;
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
        mainFragment = true;
        super.onCreate(savedInstanceState);

        // first of all, check permissions for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showExplanation("Accès localisation requise", "Rationale",
                        Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            }
        }        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        uiHandler = new UIHandler();


        loadPreferences();

        this.loadServers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadServers() {
        servers = new ArrayList<>();
        coreServers = new ArrayList<>();
        debugServers = new ArrayList<>();
        userDefinedServers = new ArrayList<>();

        player = new BroadcastPlayer(this, 100, uiHandler);

        // a server to test robustness
        debugServers.add(new Server("Défectueux 1",
                "Un serveur injoignable",
                "https://http://exemple.fr/",
                "UTF-8",
                15));

        // a server to test robustness
        debugServers.add(new Server("Défectueux 2",
                "Un json malformé",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/jsontests/broken.json",
                "UTF-8",
                15));

        // a server to test robustness
        debugServers.add(new Server("Défectueux 3",
                "Un json avec des champs manquants",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/jsontests/missing-parts.json",
                "UTF-8",
                15));

        // add an "hello world" server
        debugServers.add(new Server("Hello world",
                "One \"hello world\" message every 30 seconds",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/message.json",
                "UTF-8",
                30));

        // add an "bonjour le monde" (fr) server
        debugServers.add(new Server("Bonjour le monde",
                "Un message \"bonjour le monde\" toutes les 30 secondes",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/message-fr.json",
                "UTF-8",
                30));

        // add an "bonjour le monde" (fr) server
        debugServers.add(new Server("Bonjour le monde (audio)",
                "Un message \"bonjour le monde\" dit par un humain, toutes les 30 secondes",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/audiomessage-fr.json",
                "UTF-8",
                30));

        // add a blabla / "bip" server
        debugServers.add(new Server("Blabla bip",
                "Un serveur qui raconte du blabla toutes les 15 secondes, mais qui est coupé par un bip",
                "https://lepigeonnelson.jmfavreau.info/blabla-bip.php",
                "UTF-8",
                1));

        // a server to test forgetting constraints
        debugServers.add(new Server("5 messages ou moins",
                "Un serveur envoie 5 messages mal triés, avec une durée de vie courte",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/prioritytests/5-messages.json",
                "UTF-8",
                15));

        // a server to test playable constraints
        debugServers.add(new Server("écho",
                "Un serveur envoie des messages joués après quelques temps d'attente",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/prioritytests/echo.json",
                "UTF-8",
                15));

        // a server to find Museum in neighborhood
        coreServers.add(new Server("Musées",
                "Connaître les musées dans son voisinage",
                "https://lepigeonnelson.jmfavreau.info/museums.php",
                "UTF-8",
                0));

        // a server to find Museum in neighborhood
        coreServers.add(new Server("Rose des vents",
                "Connaître la direction vers laquelle on s'oriente",
                "https://lepigeonnelson.jmfavreau.info/compass.php",
                "UTF-8",
                0));

        // TODO: load servers stored in preferences

        buildServerList();

        player.start();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem itemSettings = menu.findItem(R.id.action_settings);
        itemSettings.setVisible(mainFragment);

        if (mainFragment)
            toolbar.setNavigationIcon(R.drawable.ic_baseline_power_off_24);
        else
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);


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

    public Server getActiveServer() {
        return player.getServer();
    }

    public void setActiveServer(Server activeServer) {
        player.setServer(activeServer);
    }

    public void playBroadcast() {
        player.playBroadcast();
    }

    public void stopBroadcast() {
        player.stopBroadcast();
    }

    @Override
    protected void onDestroy() {
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

    public void setMainFragment(boolean b) {
        mainFragment = b;
    }
}