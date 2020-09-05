package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jmtrivial.lepigeonnelson.broadcastplayer.messages.BMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MessageQueue extends Handler {

    private int refreshDelay;
    private MessagePlayer messagePlayer;

    public static final int stopBroadcast = 0;
    public static final int addNewMessages = 1;
    public static final int nextMessage = 2;
    public static final int checkForPlayableMessage = 3;

    private ArrayList<BMessage> queue;

    public MessageQueue(final MessagePlayer messagePlayer, int refreshDelay) {
        this.messagePlayer = messagePlayer;
        messagePlayer.registerQueue(this);

        queue = new ArrayList<>();
        this.refreshDelay = refreshDelay;

    }

    public void setRefreshDelay(int refreshDelay) {
        this.refreshDelay = refreshDelay;
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == stopBroadcast) {
            Log.d("MessageQueue", "stop broadcast");
            clearQueue();
            messagePlayer.sendEmptyMessage(messagePlayer.stopMessage);
        }
        else if (msg.what == addNewMessages) {
            removeForgettableMessages();
            ArrayList<BMessage> newMessages = (ArrayList<BMessage>) msg.obj;
            queue.addAll(newMessages);
            Log.d("MessageQueue", "add " + newMessages.size() + " new message(s). Queue size: " + queue.size());
            playNextMessage();
        }
        else if (msg.what == nextMessage) {
            Log.d("MessageQueue", "next message?");
            playNextMessage();
        }
        else if (msg.what == checkForPlayableMessage) {
            Log.d("MessageQueue", "next message ready to play?");
            if (!messagePlayer.isPlaying()) {
                playNextMessage();
            }
        }
    }

    private void playNextMessage() {
        removeForgettableMessages();
        Collections.sort(queue);

        if (queue.size() > 0) {

            boolean playing = false;
            BMessage currentMessage = messagePlayer.getCurrentMessage();
            Iterator<BMessage> iterator = queue.iterator();
            while (iterator.hasNext()) {
                BMessage m = iterator.next();
                // find the first playable message
                if (m.isPlayable()) {
                    // play it only if it is a message with higher priority
                    if ((currentMessage == null) ||
                            (currentMessage.getPriority() < m.getPriority())) {
                        Log.d("MessageQueue", "found a next message to play");
                        // ask player to play this message
                        Message msgThread = messagePlayer.obtainMessage();
                        msgThread.obj = m;
                        msgThread.what = messagePlayer.playMessage;
                        messagePlayer.sendMessage(msgThread);
                        playing = true;
                        // remove this message from the queue
                        iterator.remove();
                    }
                    break;
                }
            }
            if (!playing) {
                // if the queue is not empty, but no message is playable, wait before
                // checking again
                sendEmptyMessageAtTime(checkForPlayableMessage, refreshDelay);
            }
        }
    }

    private void removeForgettableMessages() {
        Iterator<BMessage> iterator = queue.iterator();
        while (iterator.hasNext()) {
            BMessage m = iterator.next();
            if (m.isForgettable()) {
                iterator.remove();
            }
        }
    }

    private void clearQueue() {
        queue.clear();
    }

}
