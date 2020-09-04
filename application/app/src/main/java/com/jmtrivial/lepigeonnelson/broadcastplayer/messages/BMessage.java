package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

import android.util.Log;

import java.util.ArrayList;

public class BMessage implements Comparable<BMessage> {

    private String txt;
    private int priority;
    private String lang;
    private String audioURL;

    private ArrayList<MessageCondition> required;
    private ArrayList<MessageCondition> forgettingConditions;

    public long getCollectedTime() {
        return collectedTime;
    }

    private long collectedTime;
    private int localID;

    public BMessage(String txt, String lang,
                    String audioURL,
                    int priority,
                    ArrayList<MessageCondition> required,
                    ArrayList<MessageCondition> forgettingConditions) {
        this.txt = txt;
        this.lang = lang;
        // set a default language
        if (txt != null && lang == null)
            this.lang = "en";
        this.audioURL = audioURL;
        this.priority = priority;
        this.required = required;
        this.forgettingConditions = forgettingConditions;

        this.collectedTime = 0;
        this.localID = 0;

    }



    public String getTxt() {
        return txt;
    }
    public String getLang() {
        return lang;
    }
    public String getAudioURL() {
        return audioURL;
    }


    public boolean isPlayable() {
        for(MessageCondition condition: required) {
            if (!condition.satisfied(this))
                return false;
        }
        return true;
    }

    public boolean isForgettable() {
        for(MessageCondition condition: forgettingConditions) {
            if (condition.satisfied(this))
                return true;
        }
        return false;

    }

    public boolean isText() {
        return txt != null;
    }


    public void setCollectedTimestamp(long ctime) {
        this.collectedTime = ctime;
    }

    public void setLocalID(int lid) {
        this.localID = lid;
    }

    @Override
    public int compareTo(BMessage msg) {
        // first use priority
        if (priority > msg.priority)
            return -1;
        else if (priority < msg.priority)
            return 1;
        else {
            // then collect time
            if (collectedTime < msg.collectedTime)
                return -1;
            else if (collectedTime > msg.collectedTime)
                return 1;
            else {
                // then order from the collected list
                if (localID < msg.localID)
                    return -1;
                else if (localID > msg.localID)
                    return 1;
                else
                    return 0;
            }
        }
    }

    public boolean isAudio() {
        return audioURL != null;
    }

    public int getPriority() {
        return priority;
    }
}
