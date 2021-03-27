package fr.lepigeonnelson.player.broadcastplayer.messages;

import java.util.ArrayList;

public class BMessage implements Comparable<BMessage> {

    private String txt;
    private int priority;
    private String lang;
    private String audioURL;
    private int period;

    private ArrayList<MessageCondition> required;
    private ArrayList<MessageCondition> forgettingConditions;

    public long getCollectedTime() {
        return collectedTime;
    }

    public static final int DEFAULT_PERIOD = -1;

    private long collectedTime;
    private int localID;

    public BMessage(String txt, String lang,
                    String audioURL,
                    int priority,
                    int period,
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
        this.period = period;
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
    public int getPeriod() { return period; }


    public boolean isPlayable() {
        for(MessageCondition condition: required) {
            if (!condition.satisfied(this))
                return false;
        }
        return true;
    }

    public boolean hasTimeRelatedRequiredConstraint() {
        for(MessageCondition condition: required) {
            if (condition.isTimeConstraint())
                return true;
        }
        return false;
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

    public void addExpiration(int refreshDelay) {
        forgettingConditions.add(new TimeFromReceptionCondition(Maths.Comparison.greaterThan, refreshDelay));
    }

    public long getPeriodMs() {
        return getPeriod() * 1000;
    }
}
