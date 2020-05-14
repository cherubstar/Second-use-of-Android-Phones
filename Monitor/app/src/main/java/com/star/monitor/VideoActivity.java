package com.star.monitor;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView = findViewById(R.id.video_view);

        String urlPath = getIntent().getStringExtra("urlPath");
        // 网络视频播放
        videoView.setVideoURI(Uri.parse(urlPath));

        MediaController controller = new MediaController(this);
        videoView.setMediaController(controller);
        videoView.requestFocus();
        videoView.start();
    }
}
