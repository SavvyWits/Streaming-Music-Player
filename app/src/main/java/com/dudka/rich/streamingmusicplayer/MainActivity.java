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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
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

    public static final String INTENT_FILTER = "com.dudka.rich.streamingmusicplayer.localbroadcast";
    public static final String PLAYER_EVENT_MESSAGE = "playerEventMessage";
    public static final int PLAYER_COMPLETION = 0x0;
    public static final int PLAYER_ERROR = 0x1;

    private static final String IS_CHANGING_CONFIGURATIONS = "isChangingConfigurations";

    //String url = "YOUR END POINT GOES HERE";
    String url = "http://streaming.earbits.com/api/v1/track.json?stream_id=5654d7c3c5aa6e00030021aa";
    String fragmentTag = "fragmentMusicPlayerUI";

    View progressBar;

    FragmentMusicPlayerUI musicPlayerUI;

    Messenger mService = null;
    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        progressBar = findViewById(R.id.progress_bar);

        FragmentManager fm = getSupportFragmentManager();
        musicPlayerUI = (FragmentMusicPlayerUI) fm.findFragmentByTag(fragmentTag);

        if (musicPlayerUI == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, FragmentMusicPlayerUI.newInstance(), fragmentTag);
            ft.commit();
            musicPlayerUI = (FragmentMusicPlayerUI) fm.findFragmentByTag(fragmentTag);
        }

        boolean changingConfig = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(IS_CHANGING_CONFIGURATIONS, false);

        Log.d("MediaPlayer", "" + changingConfig);

        if(!changingConfig)
            handleVolley();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_FILTER));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int event = intent.getIntExtra(PLAYER_EVENT_MESSAGE, 0);
            switch(event) {
                case PLAYER_COMPLETION:
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

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void handleVolley() {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Volley Response", response.toString());
                        String songName;
                        String artistName;
                        String coverImage = null;
                        String mediaFile = null;
                        int duration = 0;
                        try {
                            songName = response.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            songName = getString(R.string.unknown_name);
                        }
                        try {
                            artistName = response.getString("artist_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            artistName = getString(R.string.unkown_artist);
                        }
                        try {
                            coverImage = response.getString("cover_image");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

                        //musicPlayerUI.notifyUIUpdate(songName, artistName, coverImage);

                        Intent intent = new Intent(getApplicationContext(), ServiceMusicPlayer.class);
                        intent.putExtra("media_file", mediaFile);
                        intent.putExtra("duration", duration);
                        startService(intent);//, mConnection, Context.BIND_AUTO_CREATE);

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
    }

    @Override
    public void handleFinish() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.STOP);
        try {
            mService.send(msg);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
        finish();
    }

    @Override
    public void handlePlay() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.PLAY);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    @Override
    public void handlePause() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.PAUSE);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    @Override
    public void handleStop() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.STOP);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    @Override
    public void handleForward() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.FORWARD);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    @Override
    public void handleBack() {
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.BACK);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(IS_CHANGING_CONFIGURATIONS, isChangingConfigurations()).apply();
        doBindService();
        Message msg = Message.obtain(null, ServiceMusicPlayer.STOP);
        try {
            mService.send(msg);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
    }

    void doBindService() {
        bindService(new Intent(MainActivity.this,
                ServiceMusicPlayer.class), mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    void doUnbindService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mBound = false;
        }
    }
}