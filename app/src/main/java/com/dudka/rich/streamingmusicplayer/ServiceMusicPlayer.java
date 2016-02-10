/*
 * Copyright (C) 2015 Rich Dudka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dudka.rich.streamingmusicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class ServiceMusicPlayer extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String INTENT_FILTER = "com.dudka.rich.streamingmusicplayer.localbroadcast.service";
    public static final String SERVICE_EVENT_MESSAGE = "serviceEventMessage";
    public static final int PLAY = 0x0;
    public static final int PAUSE = 0x1;
    public static final int STOP = 0x2;
    public static final int START_SEEK = 0x3;
    public static final int STOP_SEEK_FORWARD = 0x4;
    public static final int STOP_SEEK_BACK = 0x5;

    private int duration;
    private long seekStartTime = 0;

    private MediaPlayer player;

    @Override
    public void onCreate() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_FILTER));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        duration = intent.getIntExtra("duration", 0);
        String mediaFile = intent.getStringExtra("media_file");
        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mediaFile);
            player.setOnPreparedListener(this);
            player.prepareAsync();
            player.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            sendLocalBroadcast(MainActivity.PLAYER_ERROR);
        }
        return START_STICKY;
    }

    public ServiceMusicPlayer() {}

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long seekStopTime;
            long totalSeekTime;
            int adjustedSeek;
            int seekSpeed = 10;

            int event = intent.getIntExtra(MainActivity.ACTIVITY_EVENT_MESSAGE, 0);
            switch(event) {
                case PLAY:
                    if(player != null) {
                        player.start();
                    }
                    break;
                case PAUSE:
                    if(player !=null) {
                        player.pause();
                    }
                    break;
                case STOP:
                    if(player != null) {
                        player.stop();
                        player.release();
                    }
                    stopSelf();
                    break;
                case START_SEEK:
                    player.pause();
                    seekStartTime = System.currentTimeMillis();
                    break;
                case STOP_SEEK_FORWARD:
                    seekStopTime = System.currentTimeMillis();
                    totalSeekTime = seekStopTime - seekStartTime;
                    adjustedSeek = seekSpeed * (int)totalSeekTime;
                    if(adjustedSeek < duration - player.getCurrentPosition()) {
                        player.seekTo(player.getCurrentPosition() + adjustedSeek);
                    } else {
                        player.seekTo(duration);
                    }
                    player.start();
                    break;
                case STOP_SEEK_BACK:
                    seekStopTime = System.currentTimeMillis();
                    totalSeekTime = seekStopTime - seekStartTime;
                    adjustedSeek = seekSpeed * (int)totalSeekTime;
                    if(player.getCurrentPosition() - adjustedSeek > 0) {
                        player.seekTo(player.getCurrentPosition() - adjustedSeek);
                    } else {
                        player.seekTo(0);
                    }
                    player.start();
                    break;
                default:
                    if(player != null) {
                        player.stop();
                        player.release();
                    }
                    stopSelf();
            }
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mp.setOnCompletionListener(this);
        sendLocalBroadcast(MainActivity.PLAYER_STARTED);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.stop();
        mp.release();
        sendLocalBroadcast(MainActivity.PLAYER_COMPLETED);
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        mp.stop();
        mp.release();
        sendLocalBroadcast(MainActivity.PLAYER_ERROR);
        stopSelf();
        return false;
    }

    private void sendLocalBroadcast(int msg) {
        Intent intent = new Intent(MainActivity.INTENT_FILTER);
        intent.putExtra(SERVICE_EVENT_MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player != null)
            player.release();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
