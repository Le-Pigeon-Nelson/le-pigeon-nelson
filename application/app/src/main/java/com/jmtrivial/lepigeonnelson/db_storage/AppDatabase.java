package com.jmtrivial.lepigeonnelson.db_storage;

import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.jmtrivial.lepigeonnelson.MainActivity;
import com.jmtrivial.lepigeonnelson.broadcastplayer.ServerDescription;

import java.util.ArrayList;
import java.util.List;

@Database(entities = {ServerDescriptionEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private AppDataBaseListener listener = null;

    public void setListener(AppDataBaseListener listener) {
        this.listener = listener;
    }

    abstract ServerDescriptionDao getDao();

    private ServerDescriptionEntity convert(ServerDescription desc) {
        return new ServerDescriptionEntity(desc.getUrl(), desc.getName(),
        desc.getDescription(), desc.getEncoding(), desc.getPeriod(), desc.isSelfDescribed());
    }

    private ServerDescription convert(ServerDescriptionEntity entity) {
        ServerDescription result = new ServerDescription(entity.url);
        result.setName(entity.name).setDescription(entity.description).setUrl(entity.url)
                .setEncoding(entity.encoding).setPeriod(entity.defaultPeriod);
        result.setIsSelfDescribed(entity.selfDescribed);
        // a server description stored in preferences is editable
        result.setIsEditable(true);
        return result;
    }

    // this method add or update the given description
    public void add(final ServerDescription serverDescription) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getDao().insertDescription(convert(serverDescription));
            }
        });
    }

    public void delete(final ServerDescription serverDescription) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getDao().delete(convert(serverDescription));
            }
        });
    }

    public void deleteByURL(final String url) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getDao().deleteByURL(url);
            }
        });
    }

    public ServerDescription findServerByURL(ArrayList<ServerDescription> servers, String url) {
        for(ServerDescription server: servers) {
            if (server.getUrl().equals(url))
                return server;
        }
        return null;
    }

    public  void loadAll(final ArrayList<ServerDescription> servers) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<ServerDescriptionEntity> list = getDao().getAll();
                Log.d("AppDatabase", "found " + list.size() + " elements in db");

                for (ServerDescriptionEntity entity : list) {
                    // find if entity.url is in servers
                    ServerDescription desc = findServerByURL(servers, entity.url);
                    if (desc != null) {
                        desc.update(convert(entity));
                    } else {
                        // add a new entry in the server list
                        servers.add(convert(entity));
                        Log.d("AppDatabase", "add new server " + entity.url);
                    }
                }
                if (listener != null) {
                    listener.onServerListUpdatedFromDatabase();
                }
                else {
                    Log.d("AppDatabase", "listener not yet ready");
                }
            }
        });
    }

    public interface AppDataBaseListener {
        void onServerListUpdatedFromDatabase();
    }
}