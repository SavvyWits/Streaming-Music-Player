package com.dudka.rich.streamingmusicplayer;

import android.content.DialogInterface;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String url = "http://streaming.earbits.com/api/v1/track.json?stream_id=5654d7c3c5aa6e00030021aa";
    View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);

        handleVolley();
    }

    private void handleVolley() {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle bundle = new Bundle();
                        try {
                            bundle.putString("name", response.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            bundle.putString("name", getString(R.string.unknown_name));
                        }
                        try {
                            bundle.putString("artist_name", response.getString("artist_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            bundle.putString("artist_name", getString(R.string.unkown_artist));
                        }
                        try {
                            bundle.putString("media_file", response.getString("media_file"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            bundle.putString("cover_image", response.getString("cover_image"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressBar.setVisibility(View.GONE);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.add(R.id.fragment_container, FragmentMusicPlayer.newInstance(bundle));
                        ft.commit();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
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
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
