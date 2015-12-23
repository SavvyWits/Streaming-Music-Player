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
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMusicPlayer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMusicPlayer extends Fragment
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    int duration;
    String title;
    String artist;
    String mediaFile;
    String coverImage;

    MediaPlayer player = null;

    Activity mActivity;
    OnFragmentInteractionListener mListener;

    private Handler repeatUpdateHandler = new Handler();
    public int mValue;           //increment
    private boolean mAutoIncrement = false;          //for fast foward in real time
    private boolean mAutoDecrement = false;         // for rewind in real time

    public FragmentMusicPlayer() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Bundle.
     * @return A new instance of fragment FragmentMusicPlayer.
     */

    public static FragmentMusicPlayer newInstance(Bundle bundle) {
        FragmentMusicPlayer fragment = new FragmentMusicPlayer();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            duration = bundle.getInt("duration");
            title = bundle.getString("name");
            artist = bundle.getString("artist_name");
            mediaFile = bundle.getString("media_file");
            coverImage = bundle.getString("cover_image");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);

        ((SimpleDraweeView)view.findViewById(R.id.cover_image)).setImageURI(Uri.parse(coverImage));
        ((TextView)view.findViewById(R.id.title)).setText(title);
        ((TextView)view.findViewById(R.id.artist)).setText(artist);

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
            mListener.handleNetworkError();
        }

        final ImageView play = (ImageView)view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    play.setImageResource(R.drawable.media_playback_start);
                } else {
                    player.start();
                    play.setImageResource(R.drawable.media_playback_pause);
                }
            }
        });

        view.findViewById(R.id.rewind).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoDecrement = true;
                    repeatUpdateHandler.post(new RptUpdater());
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
                    repeatUpdateHandler.post(new RptUpdater());
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
                player.stop();
                player.release();
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
    public void onPause() {
        super.onPause();
        if(player != null) {
            player.release();
        }
        mListener.handleFinish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mActivity.findViewById(R.id.preparing_progress).setVisibility(View.GONE);
        mActivity.findViewById(R.id.player_controls).setVisibility(View.VISIBLE);
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.stop();
        mp.release();
        mListener.handleNetworkError();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.stop();
        mp.release();
        mListener.handleFinish();
    }

    private class RptUpdater implements Runnable {
        public void run() {
            if(mAutoIncrement) {
                mValue += 100; //change this value to control how much to forward
                if(mValue < duration - player.getCurrentPosition()) {
                    player.seekTo(player.getCurrentPosition() + mValue);
                    repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
                }
            } else if(mAutoDecrement) {
                mValue -= 100; //change this value to control how much to rewind
                if(mValue <= 0)
                    mValue = 0;
                player.seekTo(player.getCurrentPosition() - mValue);
                repeatUpdateHandler.postDelayed( new RptUpdater(), 50 );
            }
        }
    }
}
