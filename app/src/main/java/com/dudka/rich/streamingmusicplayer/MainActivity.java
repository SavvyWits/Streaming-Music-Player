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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rich Dudka on 12/22/15.
 */
public class MainActivity extends AppCompatActivity
        implements FragmentMusicPlayerUI.OnFragmentInteractionListener {

    public static final String INTENT_FILTER = "com.dudka.rich.streamingmusicplayer.localbroadcast.activity";
    public static final String ACTIVITY_EVENT_MESSAGE = "activityEventMessage";
    public static final int PLAYER_STARTED = 0x0;
    public static final int PLAYER_COMPLETED = 0x1;
    public static final int PLAYER_ERROR = 0x2;

    private static final String IS_CHANGING_CONFIGURATIONS = "isChangingConfigurations";

    //String url = "YOUR END POINT GOES HERE";
    String url = "http://streaming.earbits.com/api/v1/track.json?stream_id=5654d7c3c5aa6e00030021aa";
    String fragmentTag = "fragmentMusicPlayerUI";

    boolean isPlaying = false;

    JSONObject mResponse;
    View progressBar;

    FragmentMusicPlayerUI musicPlayerUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        progressBar = findViewById(R.id.progress_bar);

        FragmentManager fm = getSupportFragmentManager();
        musicPlayerUI = (FragmentMusicPlayerUI) fm.findFragmentByTag(fragmentTag);

        if (musicPlayerUI == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, FragmentMusicPlayerUI.newInstance(), fragmentTag);
            ft.commit();
            fm.executePendingTransactions();
            musicPlayerUI = (FragmentMusicPlayerUI) fm.findFragmentByTag(fragmentTag);
        }

        boolean changingConfig = false;

        if(savedInstanceState != null)
            changingConfig = savedInstanceState.getBoolean(IS_CHANGING_CONFIGURATIONS);

        if(!changingConfig)
            handleVolley();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(isChangingConfigurations()) {
            outState.putBoolean(IS_CHANGING_CONFIGURATIONS, true);
        } else {
            outState.putBoolean(IS_CHANGING_CONFIGURATIONS, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_FILTER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int event = intent.getIntExtra(ServiceMusicPlayer.SERVICE_EVENT_MESSAGE, 0);
            switch(event) {
                case PLAYER_STARTED:
                    String songName;
                    String artistName;
                    String coverImage = null;
                    try {
                        songName = mResponse.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        songName = getString(R.string.unknown_name);
                    }
                    try {
                        artistName = mResponse.getString("artist_name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        artistName = getString(R.string.unkown_artist);
                    }
                    try {
                        coverImage = mResponse.getString("cover_image");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    musicPlayerUI.notifyUIUpdate(songName, artistName, coverImage);
                    break;
                case PLAYER_COMPLETED:
                    handleVolley();
                    break;
                case PLAYER_ERROR:
                    handleNetworkError();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void handleVolley() {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mResponse = response;
                        String mediaFile = null;
                        int duration = 0;
                        try {
                            mediaFile = response.getString("media_file");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            duration = response.getInt("duration");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(getApplicationContext(), ServiceMusicPlayer.class);
                        intent.putExtra("media_file", mediaFile);
                        intent.putExtra("duration", duration);
                        startService(intent);

                        isPlaying = true;

                        progressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        handleNetworkError();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);
    }

    @Override
    public void handleNetworkError() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(R.string.network_error_title);
        alert.setMessage(R.string.network_error_message);
        alert.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleVolley();
            }
        });
        alert.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();

        isPlaying = false;
    }

    @Override
    public void handleFinish() {
        sendLocalBroadcast(ServiceMusicPlayer.STOP);
        finish();
    }

    @Override
    public void handlePlayButton() {
        if (isPlaying) {
            sendLocalBroadcast(ServiceMusicPlayer.PAUSE);
            musicPlayerUI.setPlayButton();
            isPlaying = false;
        } else {
            sendLocalBroadcast(ServiceMusicPlayer.PLAY);
            musicPlayerUI.setPauseButton();
            isPlaying = true;
        }
    }

    @Override
    public void handleForward() {
        sendLocalBroadcast(ServiceMusicPlayer.FORWARD);
    }

    @Override
    public void handleBack() {
        sendLocalBroadcast(ServiceMusicPlayer.BACK);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendLocalBroadcast(ServiceMusicPlayer.STOP);
    }

    private void sendLocalBroadcast(int msg) {
        Intent intent = new Intent(ServiceMusicPlayer.INTENT_FILTER);
        intent.putExtra(ACTIVITY_EVENT_MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}