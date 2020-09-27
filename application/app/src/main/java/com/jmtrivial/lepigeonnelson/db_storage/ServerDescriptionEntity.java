package com.jmtrivial.lepigeonnelson.db_storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

@Entity(tableName = "server_description")
public class ServerDescriptionEntity {

    @PrimaryKey
    @NonNull
    public String url;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "encoding")
    public String encoding;

    @ColumnInfo(name = "defaultPeriod")
    public Integer defaultPeriod;

    @ColumnInfo(name = "selfDescribed")
    public Boolean selfDescribed;

    public ServerDescriptionEntity(String url,
                                   String name, String description, String encoding,
                                   Integer defaultPeriod, Boolean selfDescribed) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.encoding = encoding;
        this.defaultPeriod = defaultPeriod;
        this.selfDescribed = selfDescribed;
    }



}
