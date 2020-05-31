package com.star.monitor;

import android.content.pm.ActivityInfo;
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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 网络视频播放
        videoView.setVideoURI(Uri.parse("http://39.107.27.182:8080/video/1.mp4"));

        MediaController controller = new MediaController(this);
        videoView.setMediaController(controller);
        videoView.requestFocus();
        videoView.start();
    }
}
