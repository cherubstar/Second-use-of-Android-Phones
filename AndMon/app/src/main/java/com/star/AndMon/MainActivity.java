package com.star.AndMon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // 权限申请的集合
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;

    private RelativeLayout mLocalContainer;
    private SurfaceView mLocalView;

    // Customized logger view
    private LoggerRecyclerView mLogView;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        // 已完成远端视频首帧解码回调
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    // 设置远端视频显示属性
                }
            });
        }

        // 远端用户(通信模式) / 主播(直播模式) 离开当前频道回调
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("User offline, uid: " + (uid & 0xFFFFFFFFL));
                    // 其他用户离开当前频道回调
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        // 检查权限
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            // 执行到此处说明已有权限成功
            initEngineAndJoinChannel();
        }
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);

        mLogView = findViewById(R.id.log_recycler_view);
    }

    /**
     * 检查权限的方法
     * @param permission  权限
     * @param requestCode 请求码
     * @return 是否拥有权限
     */
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            // 发送权限请求
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                // 三个权限有任意的未被允许，弹吐司，退出
                showLongToast("Need permissions " +
                        Manifest.permission.RECORD_AUDIO + "/" +
                        Manifest.permission.CAMERA + "/" +
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }
            // 执行到此处说明用户已经允许权限
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 初始化Agora引擎和连接频道
     */
    private void initEngineAndJoinChannel() {
        initializeEngine(); // 初始化 Agora 引擎
        setupVideoConfig(); // 设置视频信息
        setupLocalVideo();  // 设置本地的视窗
        joinChannel();      // 连接频道
    }

    // Tutorial Step 1
    // 初始化 Agora，创建 RtcEngine 对象
    private void initializeEngine() {
        try {
            // 实例化 Rtc 引擎
            mRtcEngine = RtcEngine.create(
                    getBaseContext(),   // 传入 Content
                    getString(R.string.agora_app_id),   // 传入 APP ID
                    mRtcEventHandler);  // RTC 事件处理器
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO Check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    /**
     * 设置视频信息
     */
    private void setupVideoConfig() {

        // 启用视频
        mRtcEngine.enableVideo();

        mRtcEngine.switchCamera();

        // 视频解码配置
        mRtcEngine.setVideoEncoderConfiguration(
                new VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_640x360,   // 尺寸
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, // 帧率
                        VideoEncoderConfiguration.STANDARD_BITRATE, // 比特率
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    /**
     * 设置本地视频窗
     */
    private void setupLocalVideo() {

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    /**
     * 连接到频道
     */
    private void joinChannel() {

        String token = getString(R.string.agora_access_token).trim();
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token, "syq", "star", 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }
}