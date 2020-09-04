package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.jmtrivial.lepigeonnelson.broadcastplayer.messages.BMessage;

import java.io.IOException;
import java.util.Locale;
import static android.media.AudioManager.STREAM_MUSIC;

public class MessagePlayer extends Handler {
    private BroadcastPlayer bPlayer;

    public static final int playMessage = 0;
    public static final int stopMessage = 1;

    private TextToSpeech tts;
    private MediaPlayer mPlayer;

    private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        } // Do nothing

        @Override
        public void onError(String utteranceId) {
        } // Do nothing.

        @Override
        public void onDone(String utteranceId) {
            messageQueue.sendEmptyMessage(messageQueue.nextMessage);
        }
    };
    private MessageQueue messageQueue;


    public MessagePlayer(Context context) {

        this.messageQueue = null;

        // set text-to-speech method with the good language
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.FRANCE);
                }
                tts.setOnUtteranceProgressListener(mProgressListener);
            }
        });

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(STREAM_MUSIC);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == stopMessage) {
            Log.d("MessagePlayer", "stop message");
            stopRendering();
        } else if (msg.what == playMessage) {
            Log.d("MessagePlayer", "play message");
            renderMessage((BMessage) msg.obj);
        }
    }

    private void renderMessage(BMessage message) {
        if (message.isText()) {
            tts.setLanguage(new Locale(message.getLang()));
            tts.speak(message.getTxt(), TextToSpeech.QUEUE_FLUSH, null);
        }
        else if (message.isAudio()) {
            // play audio file
            try {
                mPlayer.reset();
                mPlayer.setDataSource(message.getAudioURL());
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
            }
        }

    }

    private void stopRendering() {
        tts.stop();
        mPlayer.reset();
    }

    public void registerQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }
}
