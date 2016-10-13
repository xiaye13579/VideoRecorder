package com.ao.videorecorder;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.ao.videorecorder.widget.MyVideoView;

/**
 * Created by xiaao on 13/10/2016.
 */

public class PreviewActivity extends AppCompatActivity {

    private String path;
    private MyVideoView videoView;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)) {
            finish();
        }
        videoView = (MyVideoView) findViewById(R.id.videoView);
        button = (Button) findViewById(R.id.button);

        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
            }
        });
    }

}
