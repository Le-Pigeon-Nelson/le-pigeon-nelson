package com.jmtrivial.lepigeonnelson;

public class Server {

    private String name;
    private String url;
    private String protocol;
    private Integer port;
    private String description;
    private Integer period;

    public Server(String name, String protocol, String url, Integer port, String description, Integer period) {
        this.name = name;
        this.protocol = protocol;
        this.url = url;
        this.port = port;
        this.description = description;
        this.period = period;
    };

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }
}
