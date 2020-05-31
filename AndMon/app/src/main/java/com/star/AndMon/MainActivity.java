package com.star.AndMon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // ==============================================================//
    private FrameLayout mFrameLayout;
    private ImageButton mVideoOnOff;
    private ImageButton mVideoUpload;

    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private CameraPreview mPreview;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private boolean isRecording = false;

    private String fileUri;
    private String file;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            String result = msg.obj.toString();
            System.out.println(result);
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    private ImageView mImageView;
    // ==============================================================//

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
        mFrameLayout = findViewById(R.id.video_frame_layout);
        mVideoOnOff = findViewById(R.id.video_on_off);
        mVideoUpload = findViewById(R.id.video_upload);

        mImageView = findViewById(R.id.agora_image_view);

        mLocalContainer = findViewById(R.id.local_video_view_container);
        mLogView = findViewById(R.id.log_recycler_view);

        mVideoOnOff.setOnClickListener(this);
        mVideoUpload.setOnClickListener(this);

        initCameraPreview();
    }

    private void initCameraPreview() {

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        mFrameLayout.addView(mPreview);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_on_off:
                if (isRecording) {
                    Toast.makeText(MainActivity.this, "停止录屏", Toast.LENGTH_SHORT).show();
                    // 使用监控功能
                    initEngineAndJoinChannel();
//                    new uploadVideo().start();
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                    // inform the user that recording has stopped
                    isRecording = false;
                    mVideoOnOff.setBackgroundResource(R.drawable.video_off);
                } else {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        Toast.makeText(MainActivity.this, "开始录屏", Toast.LENGTH_SHORT).show();
                        // 设置图片隐藏
                        mImageView.setVisibility(View.INVISIBLE);
                        // 使用录像功能
                        onDestroy();
                        removeLocalVideo();
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mediaRecorder.start();

                        // inform the user that recording has started
                        isRecording = true;
                        mVideoOnOff.setBackgroundResource(R.drawable.video_on);
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                }
                break;
            case R.id.video_upload:
                // 上传视频
                Toast.makeText(this, "已上传", Toast.LENGTH_SHORT).show();
                multiUploadVideo();
                break;
        }
    }

    private void multiUploadVideo() {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .build();

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video/1.mp4");
        Log.d(TAG, "file -> " + file.toString());

        MediaType fileType = MediaType.parse("video/x-msvideo");
        RequestBody fileBody = RequestBody.create(file, fileType);
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        String url = "http://39.107.27.182:8080/monitor/upload";
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Call task = client.newCall(request);
        task.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure -> " + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                Log.d(TAG, "code -> " + code);
                if(code == HttpURLConnection.HTTP_OK){
                    ResponseBody body = response.body();
                    if(body != null){
                        Message message = new Message();
                        message.obj = body.string();
                        handler.sendMessage(message);
                        Log.d(TAG, "success");
                    }
                }
            }
        });
    }

    /**
     * 上传录制的视频
     */
    public class uploadVideo extends Thread{
        @Override
        public void run() {
            if(fileUri.contains(file)){
                FileInputStream fis = null;
                Socket socket = null;
                try {
                    // 1.创建一个本地字节输入流FileInputStream对象,构造方法中绑定要读取的数据源
                    fis = new FileInputStream(fileUri);
                    // 2.创建一个客户端Socket对象,构造方法中绑定服务器的IP地址和端口号
                    socket = new Socket("39.107.27.182", 8888);
                    // 3.使用Socket中的方法getOutputStream,获取网络字节输出流OutputStream对象
                    OutputStream os = socket.getOutputStream();
                    // 4.使用本地字节输入流FileInputStream对象中的方法read,读取本地文件
                    int len = 0;
                    byte[] bytes = new byte[1024];
                    while ((len = fis.read(bytes)) != -1) {
                        // 5.使用网络字节输出流OutputStream对象中的方法write,把读取到的文件上传到服务器
                        os.write(bytes, 0, len);
                    }
                    /*
                     * 解决:上传完文件,给服务器写一个结束标记 void shutdownOutput() 禁用此套接字的输出流。 对于 TCP
                     * 套接字，任何以前写入的数据都将被发送，并且后跟 TCP 的正常连接终止序列。
                     */
                    socket.shutdownOutput();

                    // 6.使用Socket中的方法getInputStream,获取网络字节输入流InputStream对象
                    InputStream is = socket.getInputStream();

                    // 7.使用网络字节输入流InputStream对象中的方法read读取服务回写的数据
                    StringBuilder stringBuilder = new StringBuilder("");
                    while ((len = is.read(bytes)) != -1) {
                        stringBuilder.append(new String(bytes, 0, len));
                    }

                    Message message = new Message();
                    message.obj = stringBuilder.toString();

                    handler.sendMessage(message);

                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    // 8.释放资源(FileInputStream,Socket)
                    if(fis != null){
                        try {
                            fis.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
     * 移除本地视窗
     */
    public void removeLocalVideo(){
        mLocalContainer.removeViewInLayout(mLocalView);
    }

    /**
     * 连接到频道
     */
    private void joinChannel() {

        String token = getString(R.string.agora_access_token).trim();
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token, "monitor", "star", 0);
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

    // ================================================================================================//
    // ================================================================================================//
    // ============================================视频录制=============================================//
    // ================================================================================================//
    // ================================================================================================//

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video");
        /*File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "video");*/
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("video", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            /*mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
            file = "VID_"+ timeStamp + ".mp4";*/
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "1.mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        fileUri = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(fileUri);
//        mediaRecorder.setOutputFile(getOutputMediaFileUri(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
}