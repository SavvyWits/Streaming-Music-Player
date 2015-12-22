package com.dudka.rich.streamingmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMusicPlayer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMusicPlayer extends Fragment {

    String title;
    String artist;
    String mediaFile;
    String coverImage;

    Activity mActivity;
    Context mContext;

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

        //((ImageView)view.findViewById(R.id.cover_image)).setImageURI(Uri.parse(coverImage));
        ((TextView)view.findViewById(R.id.title)).setText(title);
        ((TextView)view.findViewById(R.id.artist)).setText(artist);

        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mediaFile);
            player.prepare();
            player.start();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.finish();
    }
}
