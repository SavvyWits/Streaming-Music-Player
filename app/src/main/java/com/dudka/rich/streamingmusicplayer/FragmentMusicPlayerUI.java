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

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by rich on 12/22/15.
 *
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMusicPlayerUI#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMusicPlayerUI extends Fragment {

    Activity mActivity;
    OnFragmentInteractionListener mListener;

    ImageView playButton;

    private Handler seekHandler = new Handler();
    public int mValue;           //increment
    private boolean mAutoIncrement = false;          //for fast foward in real time
    private boolean mAutoDecrement = false;         // for rewind in real time

    public FragmentMusicPlayerUI() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentMusicPlayer.
     */

    public static FragmentMusicPlayerUI newInstance() {
        FragmentMusicPlayerUI fragment = new FragmentMusicPlayerUI();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        playButton = (ImageView)view.findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.handlePlayButton();
            }
        });

        view.findViewById(R.id.rewind).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoDecrement = true;
                    //seekHandler.post(new SeekUpdater());
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                            && mAutoDecrement) {
                        mAutoDecrement = false;
                    }
                    return false;
                }
                return false;
            }
        });

        view.findViewById(R.id.fast_forward).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoIncrement = true;
                    //seekHandler.post(new SeekUpdater());
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                            && mAutoIncrement) {
                        mAutoIncrement = false;
                    }
                    return false;
                }
                return false;
            }
        });

        view.findViewById(R.id.quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.handleFinish();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mActivity = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    public void notifyUIUpdate(String title, String artist, String coverImage) {
        mActivity.findViewById(R.id.preparing_progress).setVisibility(View.GONE);
        mActivity.findViewById(R.id.player_controls).setVisibility(View.VISIBLE);

        ((TextView)mActivity.findViewById(R.id.title)).setText(title);
        ((TextView)mActivity.findViewById(R.id.artist)).setText(artist);
        ((SimpleDraweeView)mActivity.findViewById(R.id.cover_image)).setImageURI(Uri.parse(coverImage));
    }

    public void setPlayButton() {
        playButton.setImageResource(R.drawable.media_playback_start);
    }

    public void setPauseButton() {
        playButton.setImageResource(R.drawable.media_playback_pause);
    }

    /*private class SeekUpdater implements Runnable {
        public void run() {
            if(mAutoIncrement) {
                mValue += 100; //change this value to control how much to forward
                if(mValue < duration - player.getCurrentPosition()) {
                    player.seekTo(player.getCurrentPosition() + mValue);
                    seekHandler.postDelayed(new SeekUpdater(), 50);
                } else {
                    player.seekTo(duration);
                }
            } else if(mAutoDecrement) {
                mValue -= 100; //change this value to control how much to rewind
                if(mValue > 0) {
                    player.seekTo(player.getCurrentPosition() - mValue);
                    seekHandler.postDelayed(new SeekUpdater(), 50);
                } else {
                    player.seekTo(0);
                }
            }
        }
    }*/

    public interface OnFragmentInteractionListener {
        void handleVolley();
        void handleNetworkError();
        void handleFinish();
        void handlePlayButton();
        void handleForward();
        void handleBack();
    }
}