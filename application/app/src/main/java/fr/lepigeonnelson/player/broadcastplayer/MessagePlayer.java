package fr.lepigeonnelson.player.broadcastplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.annotation.RequiresApi;

import fr.lepigeonnelson.player.broadcastplayer.messages.BMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class MessagePlayer extends Handler {

    public static final int playMessage = 0;
    public static final int stopMessage = 1;

    private volatile TextToSpeech tts;
    private HashMap<String, String> map;
    private MediaPlayer mPlayer;

    public boolean isPlaying() {
        return isPlaying;
    }

    private boolean isPlaying;

    private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        } // Do nothing

        @SuppressWarnings("deprecation")
        @Override
        public void onError(String utteranceId) {
        } // Do nothing.

        @Override
        public void onDone(String utteranceId) {
            isPlaying = false;
            Log.d("MessagePlayer", "end of TTS");
            messageQueue.sendEmptyMessage(messageQueue.nextMessage);
        }

    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            isPlaying = false;
            Log.d("MessagePlayer", "end of audio play");
            messageQueue.sendEmptyMessage(messageQueue.nextMessage);
        }

    };

    private MessageQueue messageQueue;

    public BMessage getCurrentMessage() {
        if (isPlaying)
            return currentMessage;
        else
            return null;
    }

    private BMessage currentMessage;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MessagePlayer(Context context) {

        this.messageQueue = null;
        this.isPlaying = false;
        Log.d("THREAD", "thread id for message player " + Thread.currentThread().getId());

        // set text-to-speech method with the good language
        tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.FRANCE);
                }
                Log.d("THREAD", "thread id for TTS init " + Thread.currentThread().getId());
                tts.setOnUtteranceProgressListener(mProgressListener);
            }
        });
        map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LePigeonNelson");

        mPlayer = new MediaPlayer();
        mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mPlayer.setOnCompletionListener(onCompletionListener);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == stopMessage) {
            Log.d("MessagePlayer", "stop message");
            stopRendering();
        } else if (msg.what == playMessage) {
            Log.d("MessagePlayer", "play message");
            stopRendering();
            currentMessage = (BMessage) msg.obj;
            renderMessage();
        }
    }

    @SuppressWarnings("deprecation")
    private void renderMessage() {
        if (currentMessage.isText()) {
            Log.d("MessagePlayer", "sending message \"" + currentMessage.getTxt() + "\' to TTS");
            tts.setLanguage(new Locale(currentMessage.getLang()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(currentMessage.getTxt(), TextToSpeech.QUEUE_FLUSH, map);
            } else {
                tts.speak(currentMessage.getTxt(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
            isPlaying = true;
        }
        else if (currentMessage.isAudio()) {
            // play audio file
            try {
                Log.d("MessagePlayer", "send message to MediaPlayer");
                mPlayer.reset();
                mPlayer.setDataSource(currentMessage.getAudioURL());
                mPlayer.prepare();
                mPlayer.start();
                isPlaying = true;
            } catch (IOException e) {
            }
        }

    }

    private void stopRendering() {
        if (isPlaying) {
            isPlaying = false;
            tts.stop();
            mPlayer.reset();
        }
    }


    public void registerQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public void reset() {
        stopRendering();
        tts.shutdown();
    }
}
