package com.dudka.rich.streamingmusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ServiceMusicPlayer extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final int PLAY = 0x0;
    public static final int PAUSE = 0x1;
    public static final int STOP = 0x2;
    public static final int FORWARD = 0x3;
    public static final int BACK = 0x4;

    MediaPlayer player;

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        String mediaFile = intent.getStringExtra("media_file");
        Log.d("MediaPlayer", "Media File = " + mediaFile);

        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mediaFile);
            player.setOnPreparedListener(this);
            player.prepareAsync();
            player.setOnErrorListener(this);
            player.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            //mListener.handleNetworkError();
        }
        return START_STICKY;
    }

    public ServiceMusicPlayer() {
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("MediaPlayer", "onPrepared");
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.stop();
        mp.release();
        Intent intent = new Intent(MainActivity.INTENT_FILTER);
        intent.putExtra(MainActivity.PLAYER_EVENT_MESSAGE, MainActivity.PLAYER_COMPLETION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.stop();
        mp.release();
        Intent intent = new Intent(MainActivity.INTENT_FILTER);
        intent.putExtra(MainActivity.PLAYER_EVENT_MESSAGE, MainActivity.PLAYER_ERROR);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        return false;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAY:
                    break;
                case PAUSE:
                    break;
                case STOP:
                    break;
                case FORWARD:
                    break;
                case BACK:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
