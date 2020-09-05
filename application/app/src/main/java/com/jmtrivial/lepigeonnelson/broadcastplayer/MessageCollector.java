package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;

import com.jmtrivial.lepigeonnelson.broadcastplayer.messages.BMessage;
import com.jmtrivial.lepigeonnelson.broadcastplayer.messages.ConditionFactory;
import com.jmtrivial.lepigeonnelson.broadcastplayer.messages.MessageCondition;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


public class MessageCollector extends Handler {
    public static final int startCollect = 0;
    public static final int stopCollect = 1;

    private MessageQueue msgQueue;

    private ConditionFactory cFactory;

    private ArrayList<BMessage> newMessages;

    private Server server;
    private boolean running;

    private Runnable collectMessages;


    public MessageCollector(MessageQueue msg) {

        cFactory = new ConditionFactory();
        this.newMessages = new ArrayList<>();
        this.msgQueue = msg;
        running = false;

        collectMessages = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    Date d = new Date();
                    long releaseTime = d.getTime() + server.getPeriodMilliseconds();
                    Log.d("MessageCollector", "get data from server");

                    // collect messages from the server
                    if (collect()) {
                        // send them to the message queue
                        Message msg = msgQueue.obtainMessage();
                        msg.obj = newMessages;
                        msg.what = msgQueue.addNewMessages;
                        msgQueue.sendMessage(msg);
                    }

                    // wait the desired period before collecting again
                    Date d2 = new Date();
                    long time = releaseTime - d2.getTime();
                    if (time > 0) {
                        postDelayed(collectMessages, time);
                    }
                    else {
                        post(collectMessages);
                    }

                }
            }
        };
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == stopCollect) {
            Log.d("MessageCollector", "stop collect");
            running = false;
        }
        else if (msg.what == startCollect) {
            Log.d("MessageCollector", "start collect");
            this.server = (Server) msg.obj;
            running = true;

            post(collectMessages);
        }
    }

    private boolean collect() {

        URL url = null;
        try {
            url = new URL(server.getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JsonReader reader = new JsonReader(new InputStreamReader(in, server.getEncoding()));

            try {
                readMessagesArray(reader);
            } finally {
                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return true;
    }

    private void readMessagesArray(JsonReader reader) throws IOException {
        newMessages.clear();
        Date d = new Date();
        long ctime = d.getTime();
        int i = 0;

        reader.beginArray();
        while (reader.hasNext()) {
            BMessage msg = readMessage(reader);
            if (msg != null) {
                msg.setCollectedTimestamp(ctime);
                msg.setLocalID(i);
                i += 1;
                newMessages.add(msg);
            }
        }
        reader.endArray();

    }

    private BMessage readMessage(JsonReader reader) throws IOException {
        String txt = null;
        int priority = 10;
        String lang = null;
        String audioURL = null;

        ArrayList<MessageCondition> required = new ArrayList<>();
        ArrayList<MessageCondition> forgettingConditions = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("txt")) {
                txt = reader.nextString();
            }
            else if (name.equals("lang")) {
                lang = reader.nextString();
            }
            else if (name.equals("priority")) {
                priority = reader.nextInt();
            }
            else if (name.equals("audioURL")) {
                audioURL = reader.nextString();
            }
            else if (name.equals("requiredConditions")) {
                required = readConditions(reader);
            }
            else if (name.equals("forgettingConditions")) {
                forgettingConditions = readConditions(reader);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new BMessage(txt, lang, audioURL, priority, required, forgettingConditions);

    }

    private ArrayList<MessageCondition> readConditions(JsonReader reader) throws IOException {
        ArrayList<MessageCondition> result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            MessageCondition c = readCondition(reader);
            if (c != null) {
                result.add(c);
            }
        }
        reader.endArray();
        return result;
    }

    private MessageCondition readCondition(JsonReader reader) throws IOException {
        String ref = null;
        String comparison = null;
        String parameter = null;
        boolean reverse = false;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("reference")) {
                if (parameter != null)
                    reverse = true;
                ref = reader.nextString();
            }
            else if (name.equals("comparison")) {
                comparison = reader.nextString();
            }
            else if (name.equals("parameter")) {
                parameter = reader.nextString();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return cFactory.getCondition(ref, comparison, parameter, reverse);
    }


    public void setServer(Server server) {
        this.server = server;
    }
}
