package com.star.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.star.qrcode.CustomCaptureActivity;
import com.star.video.MonitorActivity;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private View ll_scan, ll_media, ll_set, ll_about, mBtMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();
    }

    public void initView(){

        mBtMenu = findViewById(R.id.btn_menu);
        mBtMenu.setOnClickListener(this);

        // 获取控件
        ll_scan = findViewById(R.id.ll_scan);  // 扫一扫获取IP摄像头
        ll_media = findViewById(R.id.ll_media);// 媒体库
        ll_set = findViewById(R.id.ll_set);    // 设置
        ll_about = findViewById(R.id.ll_about);// 关于 APP

        // 设置事件
        ll_scan.setOnClickListener(this);
        ll_media.setOnClickListener(this);
        ll_set.setOnClickListener(this);
        ll_about.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_menu:
                new ConfirmDialog(HomeActivity.this).showAtBottom(mBtMenu);
                break;
            case R.id.ll_scan:
                // 创建IntentIntegrator对象
                IntentIntegrator intentIntegrator = new IntentIntegrator(HomeActivity.this);
                // 设置自定义扫描的 Activity
                intentIntegrator.setCaptureActivity(CustomCaptureActivity.class);
                // 设置提示音
                intentIntegrator.setBeepEnabled(true);
                // 设置二维码
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                // 设置提示信息
                intentIntegrator.setPrompt("扫描二维码获取 IP 摄像头");
                // 设置扫描界面的超时时间
                intentIntegrator.setTimeout(5000);
                // 开始扫描
                intentIntegrator.initiateScan();
                break;
            case R.id.ll_media:
                break;
            case R.id.ll_set:
                startActivity(new Intent(HomeActivity.this, FeedBackActivity.class));
                break;
            case R.id.ll_about:
                startActivity(new Intent(HomeActivity.this, AboutAPPActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取解析结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "扫描内容:" + result.getContents(), Toast.LENGTH_LONG).show();
                String channelName = result.getContents().trim();
                Intent intent = new Intent(HomeActivity.this, MonitorActivity.class);
                intent.putExtra("channelName", channelName);
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
