package com.jmtrivial.lepigeonnelson;

import android.os.Bundle;

import com.jmtrivial.lepigeonnelson.broadcastplayer.Server;
import com.jmtrivial.lepigeonnelson.broadcastplayer.BroadcastPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Server> servers;

    private BroadcastPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.loadServers();
    }

    private void loadServers() {
        servers = new ArrayList<>();

        player = new BroadcastPlayer(this);

        // add an "hello world" server
        servers.add(new Server("Hello world",
                "One \"hello world\" message every 30 seconds",
                "https://jmtrivial.github.io/le-pigeon-nelson/servers/helloworld/message.json",
                "UTF-8",
                30));

        // add an "bonjour le monde" (fr) server
        servers.add(new Server("Bonjour le monde",
                "Un message \"bonjour le monde\" toutes les 30 secondes",
                "https://jmtrivial.github.io/le-pigeon-nelson/servers/helloworld/message-fr.json",
                "UTF-8",
                30));

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

        return super.onOptionsItemSelected(item);
    }

    public void setActiveServer(Server activeServer) {
        player.setServer(activeServer);
        player.playBroadcast();
    }

    public void stopBroadcast() {
        player.stopBroadcast();
    }
}