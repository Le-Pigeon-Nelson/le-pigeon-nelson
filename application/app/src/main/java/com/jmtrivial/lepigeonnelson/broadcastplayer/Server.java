package com.jmtrivial.lepigeonnelson.broadcastplayer;

public class Server {

    private String encoding;
    private String name;
    private String url;
    private String description;
    private Integer period;

    public Server(String name, String description, String url, String encoding, Integer period) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.period = period;
        this.encoding = encoding;
    };

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
}
