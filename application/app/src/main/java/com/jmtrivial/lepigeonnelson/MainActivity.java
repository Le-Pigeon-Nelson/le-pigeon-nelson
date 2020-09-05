package com.jmtrivial.lepigeonnelson;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.jmtrivial.lepigeonnelson.broadcastplayer.Server;
import com.jmtrivial.lepigeonnelson.broadcastplayer.BroadcastPlayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Server> servers;

    private BroadcastPlayer player;

    private final int REQUEST_PERMISSION_COARSE_LOCATION = 1;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 2;

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
        super.onCreate(savedInstanceState);

        // first of all, check permissions for location
        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showExplanation("Accès localisation requise", "Rationale",
                        Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
            }        }
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Accès localisation précise requise", "Rationale",
                        Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
            }
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.loadServers();
    }

    private void loadServers() {
        servers = new ArrayList<>();

        player = new BroadcastPlayer(this, 100);

        // add an "hello world" server
        servers.add(new Server("Hello world",
                "One \"hello world\" message every 30 seconds",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/message.json",
                "UTF-8",
                30));

        // add an "bonjour le monde" (fr) server
        servers.add(new Server("Bonjour le monde",
                "Un message \"bonjour le monde\" toutes les 30 secondes",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/message-fr.json",
                "UTF-8",
                30));

        // add an "bonjour le monde" (fr) server
        servers.add(new Server("Bonjour le monde (audio)",
                "Un message \"bonjour le monde\" dit par un humain, toutes les 30 secondes",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/helloworld/audiomessage-fr.json",
                "UTF-8",
                30));

        // add a blabla / "bip" server
        servers.add(new Server("Blabla bip",
                "Un serveur qui raconte du blabla toutes les 15 secondes, mais qui est coupé par un bip",
                "https://lepigeonnelson.jmfavreau.info/blabla-bip.php",
                "UTF-8",
                1));

        // a server to test forgetting constraints
        servers.add(new Server("5 messages ou moins",
                "Un serveur envoie 5 messages mal triés, avec une durée de vie courte",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/prioritytests/5-messages.json",
                "UTF-8",
                15));

        // a server to test playable constraints
        servers.add(new Server("écho",
                "Un serveur envoie des messages joués après quelques temps d'attente",
                "https://raw.githubusercontent.com/jmtrivial/le-pigeon-nelson/master/servers/prioritytests/echo.json",
                "UTF-8",
                15));

        // a server to find Museum in neighborhood
        servers.add(new Server("Musées",
                "Un serveur qui informe de la présence des musées dans le voisinage",
                "https://lepigeonnelson.jmfavreau.info/museums.php",
                "UTF-8",
                60));

        // load servers stored in preferences
        // TODO: load servers stored in preferences

        player.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            // TODO: implement preferences
            return true;
        }
        else if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public Server getActiveServer() {
        return player.getServer();
    }

    public void setActiveServer(Server activeServer) {
        player.setServer(activeServer);
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

}