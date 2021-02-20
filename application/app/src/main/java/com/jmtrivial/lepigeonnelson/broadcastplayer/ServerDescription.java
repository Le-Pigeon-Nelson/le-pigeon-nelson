package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import kotlin.experimental.ExperimentalTypeInference;

public class ServerDescription {


    private String encoding;
    private String name;
    private String url;
    private String description;
    private Integer period;

    private boolean selfDescribed;
    private boolean hasDescription;
    private ServerDescriptionListener listener;
    private boolean editable;


    public ServerDescription(String url) {
        this.selfDescribed = true;
        this.hasDescription = false;
        this.url = url;
        this.name = "...";
        this.description = "...";

        // set default values
        this.period = 0;
        this.encoding = "UTF-8";
        selfDescribed = true;
        this.editable = true;

        this.listener = null;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isSelfDescribed() {
        return selfDescribed;
    }

    public void setListener(ServerDescriptionListener listener) {
        this.listener = listener;
    }


    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() { return this.url; }

    public String getEncoding() { return this.encoding; }

    public long getPeriodMilliseconds() {
        return period * 1000;
    }
    public int getPeriod() {
        return period;
    }

    public void update(ServerDescription sd) {
        this.url = sd.url;
        this.name = sd.name;
        this.description = sd.description;
        this.encoding = sd.encoding;
        this.period = sd.period;
        this.hasDescription = true;
        Log.d("ServerDescription", "update");

        if (listener != null && editable)
            listener.onUpdatedDescription(this);
    }

    public void setIsSelfDescribed(Boolean selfDescribed) {
        this.selfDescribed = selfDescribed;
    }

    public ServerDescription setPeriod(int i) {
        period = i;
        return this;
    }

    public ServerDescription setEncoding(String encoding) {
        this.encoding = encoding;
        this.hasDescription =true;
        return this;
    }

    public ServerDescription setName(String name) {
        this.name = name;
        this.hasDescription =true;
        return this;
    }

    public ServerDescription setUrl(String url) {
        this.url = url;
        return this;
    }

    public ServerDescription setDescription(String description) {
        this.description = description;
        this.hasDescription =true;
        return this;
    }

    public ServerDescription setIsEditable(boolean b) {
        this.editable = b;
        return this;
    }

    public boolean missingDescription() {
        return !hasDescription;
    }


    public interface ServerDescriptionListener {
        void onUpdatedDescription(ServerDescription description);
    };
}
