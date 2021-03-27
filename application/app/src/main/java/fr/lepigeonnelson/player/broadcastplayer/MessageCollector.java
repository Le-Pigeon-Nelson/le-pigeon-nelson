package fr.lepigeonnelson.player.broadcastplayer;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.JsonReader;
import android.util.Log;

import fr.lepigeonnelson.player.broadcastplayer.messages.BMessage;
import fr.lepigeonnelson.player.broadcastplayer.messages.ConditionFactory;
import fr.lepigeonnelson.player.broadcastplayer.messages.MessageCondition;

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
    public static final int PROTOCOL_VERSION = 2;

    public static final int startCollect = 0;
    public static final int stopCollect = 1;
    public static final int processCollect = 2;
    public static final int getDescription = 3;

    private final SensorsService sensorManager;

    private MessageQueue msgQueue;

    private ConditionFactory cFactory;

    private ArrayList<BMessage> newMessages;

    private ServerDescription currentServer;
    private int serverID;
    private boolean running;
    private String deviceID;


    private UIHandler uiHandler;

    public MessageCollector(MessageQueue msg, Context context, UIHandler uiHandler) {

        sensorManager = SensorsService.getSensorsService(context);
        cFactory = new ConditionFactory();
        this.newMessages = new ArrayList<>();
        this.msgQueue = msg;
        running = false;
        serverID = 0;

        this.uiHandler = uiHandler;

        this.deviceID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == getDescription) {
            ServerDescription description = (ServerDescription) msg.obj;
            collect(description, true);
        }
        else if (msg.what == stopCollect) {
            Log.d("MessageCollector", "stop collect");
            running = false;
        }

        else if (msg.what == startCollect) {
            Log.d("MessageCollector", "start collect");
            this.currentServer = (ServerDescription) msg.obj;
            serverID += 1;
            running = true;

            collectMessages(0);
        }
        else if (msg.what == processCollect) {
            Integer id = (Integer) msg.obj;
            // only run this process if it corresponds to the current server
            if (running && id == serverID) {
                Date d = new Date();
                long releaseTime = d.getTime() + currentServer.getPeriodMilliseconds();
                Log.d("MessageCollector", "get data from server");

                // collect messages from the server
                if (collect(currentServer, false)) {
                    // send them to the message queue
                    Message msgQ = msgQueue.obtainMessage();
                    msgQ.obj = newMessages;
                    msgQ.what = msgQueue.addNewMessages;
                    msgQueue.sendMessage(msgQ);

                    if (currentServer.getPeriodMilliseconds() != 0) {
                        // wait the desired period before collecting again, only if it's asked
                        Date d2 = new Date();
                        long time = releaseTime - d2.getTime();
                        collectMessages(time);
                    }
                }
            }
        }
    }

    void collectMessages(long d) {
        // remove previously delayed collect messages
        removeMessages(processCollect);

        // send new message
        Message msg = obtainMessage();
        msg.obj = serverID;
        msg.what = processCollect;
        if (d <= 0) {
            sendMessage(msg);
        }
        else {
            sendMessageDelayed(msg, d);
        }
    }

    private boolean collect(ServerDescription serverDescription, boolean description) {

        URL url;
        try {
            if (description)
                url = new URL(serverDescription.getUrl() + "?self-description");
            else
                url = new URL(serverDescription.getUrl() + getURLParameters());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            uiHandler.sendEmptyMessage(uiHandler.SERVER_ERROR);
            return false;
        }
        catch (Exception e) {
            uiHandler.sendEmptyMessage(uiHandler.NO_GPS);
            return false;
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

        } catch (IOException e) {
            e.printStackTrace();
            uiHandler.sendEmptyMessage(uiHandler.SERVER_ERROR);
            return false;
        }

        JsonReader reader;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            reader = new JsonReader(new InputStreamReader(in, serverDescription.getEncoding()));
            Log.d("DefaultServers", "read arrays");
            readArray(reader, description, serverDescription);

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            uiHandler.sendEmptyMessage(uiHandler.SERVER_CONTENT_ERROR);
            return false;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            uiHandler.sendEmptyMessage(uiHandler.SERVER_ERROR);
            return false;
        }

        return true;
    }


    private String getURLParameters() throws Exception {
        Location location = sensorManager.getLocation();
        float azimuth = sensorManager.getAzimuth();
        float pitch = sensorManager.getPitch();
        float roll = sensorManager.getRoll();
        if (location != null) {
            URLParamBuilder params = new URLParamBuilder();
            params.addParameter("lat", location.getLatitude());
            params.addParameter("lng", location.getLongitude());
            params.addParameter("loc_accuracy", location.getAccuracy());
            params.addParameter("loc_timestamp", location.getTime());
            params.addParameter("azimuth", azimuth);
            params.addParameter("pitch", pitch);
            params.addParameter("roll", roll);

            params.addParameter("uid", deviceID);

            return params.toString();
        }
        else {
            throw new Exception();
        }
    }

    private void readArray(JsonReader reader,
                           boolean description,
                           ServerDescription serverDescription) throws IOException {
        newMessages.clear();
        Date d = new Date();
        long ctime = d.getTime();
        int i = 0;

        reader.beginArray();
        while (reader.hasNext()) {
            Entry entry = readEntry(reader);
            if (entry.isMessage() && !description) {
                BMessage msg = entry.getMessage();
                if (msg != null) {
                    msg.setCollectedTimestamp(ctime);
                    msg.setLocalID(i);
                    i += 1;
                    newMessages.add(msg);
                } else
                    throw new IOException();
            }
            else if (entry.isDescription() && description) {
                ServerDescription newDescription = entry.getDescription(serverDescription);


                Message msg = uiHandler.obtainMessage();
                msg.obj = newDescription;
                msg.what = uiHandler.NEW_SERVER_DESCRIPTION;
                uiHandler.sendMessage(msg);
            }
        }
        reader.endArray();

    }

    private Entry readEntry(JsonReader reader) throws IOException {
        String txt = null;
        int priority = 10;
        String lang = null;
        String audioURL = null;
        String name = null;
        String description = null;
        String encoding = null;
        Integer defaultPeriod = null;
        Integer period = null;

        ArrayList<MessageCondition> required = new ArrayList<>();
        ArrayList<MessageCondition> forgettingConditions = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String jname = reader.nextName();
            // read elements of a description entry
            if (jname.equals("name")) {
                name = reader.nextString();
            }
            else if (jname.equals("description")) {
                description = reader.nextString();
            }
            else if (jname.equals("encoding")) {
                encoding = reader.nextString();
            }
            else if (jname.equals("defaultPeriod")) {
                defaultPeriod = reader.nextInt();
            }

            // read elements of a message entry
            else if (jname.equals("txt")) {
                txt = reader.nextString();
            }
            else if (jname.equals("lang")) {
                lang = reader.nextString();
            }
            else if (jname.equals("priority")) {
                priority = reader.nextInt();
            }
            else if (jname.equals("audioURL")) {
                audioURL = reader.nextString();
            }
            else if (jname.equals("requiredConditions")) {
                required = readConditions(reader);
            }
            else if (jname.equals("forgettingConditions")) {
                forgettingConditions = readConditions(reader);
            }
            else if (jname.equals("period")) {
                period = reader.nextInt();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Entry(txt, lang, audioURL, priority, period, required, forgettingConditions,
                name, description, encoding, defaultPeriod);

    }

    private ArrayList<MessageCondition> readConditions(JsonReader reader) throws IOException {
        ArrayList<MessageCondition> result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            MessageCondition c = readCondition(reader);
            if (c != null) {
                result.add(c);
            }
            else
                throw new IOException();

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


    public void setCurrentServer(ServerDescription currentServer) {
        this.currentServer = currentServer;
    }

    public void getPublicServers() {
        Log.d("PublicServerCollect", "Start collecting server list");
        URL url;
        try {
            url = new URL("https://Le-Pigeon-Nelson.github.io/le-pigeon-nelson/servers/serverlist.json");
        } catch (Exception e) {
            Log.d("PublicServerCollect", "URL error");
            e.printStackTrace();
            return;
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

        } catch (IOException e) {
            Log.d("PublicServerCollect", "Connection error");
            e.printStackTrace();
            return;
        }

        JsonReader reader;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            // for each server
            reader.beginArray();
            while (reader.hasNext()) {

                String sURL = "";
                int sMinVersion = PROTOCOL_VERSION;
                int sMaxVersion = PROTOCOL_VERSION;
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("url")) {
                        sURL = reader.nextString();
                    }
                    else if (name.equals("min-version")) {
                        sMinVersion = reader.nextInt();
                    }
                    else if (name.equals("max-version")) {
                        sMaxVersion = reader.nextInt();
                    }
                }
                reader.endObject();

                if (isCompatibleVersion(sMinVersion, sMaxVersion) && sURL != "") {
                    Log.d("PublicServerCollect", "Found a new server: " + sURL);
                    Message msg = uiHandler.obtainMessage();
                    msg.obj = sURL;
                    msg.what = uiHandler.NEW_PUBLIC_SERVER;
                    uiHandler.sendMessage(msg);
                }

            }
            reader.endArray();


        } catch (IOException | NumberFormatException e) {
            Log.d("PublicServerCollect", "Format error");
            e.printStackTrace();
            return;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            Log.d("PublicServerCollect", "Cannot close reader");
            e.printStackTrace();
            return;
        }

    }

    private boolean isCompatibleVersion(int sMinVersion, int sMaxVersion) {
        return sMinVersion <= PROTOCOL_VERSION && sMaxVersion >= PROTOCOL_VERSION;
    }


    private class Entry {


        // message fields
        private final String txt;
        private final String lang;
        private final String audioURL;
        private final Integer priority;
        private final Integer period;
        private final ArrayList<MessageCondition> required;
        private final ArrayList<MessageCondition> forgottingConditions;

        // description fields
        private final String name;
        private final String description;
        private final String encoding;
        private final Integer defaultPeriod;

        public Entry(String txt, String lang, String audioURL, Integer priority, Integer period,
                     ArrayList<MessageCondition> required, ArrayList<MessageCondition> forgettingConditions,
                     String name, String description, String encoding, Integer defaultPeriod) {
            this.txt = txt;
            this.lang = lang;
            this.audioURL = audioURL;
            this.priority = priority;
            this.period = period;
            this.required = required;
            this.forgottingConditions = forgettingConditions;

            this.name = name;
            this.description = description;
            this.encoding = encoding;
            this.defaultPeriod = defaultPeriod;

        }


        public boolean isMessage() {
            return (txt != null && lang != null) || audioURL != null;
        }

        public boolean isDescription() {
            return name != null && description != null && encoding != null && defaultPeriod != null;
        }

        public ServerDescription getDescription(ServerDescription serverDescription) {
            ServerDescription newDescription = new ServerDescription(serverDescription.getUrl());
            newDescription.setName(name).setDescription(description)
                .setEncoding(encoding).setPeriod(defaultPeriod)
                .setIsEditable(true).setIsSelfDescribed(true);
            return newDescription;
        }

        public BMessage getMessage() {
            return new BMessage(txt, lang, audioURL, priority, period == null ? BMessage.DEFAULT_PERIOD : period, required, forgottingConditions);
        }

    }

}
