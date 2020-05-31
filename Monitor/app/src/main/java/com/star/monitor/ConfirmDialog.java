package com.star.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.star.qrcode.CustomCaptureActivity;

public class ConfirmDialog extends PopupWindow implements View.OnClickListener {

    private Context context;
    private View ll_scan, ll_media, ll_set, ll_about;

    public ConfirmDialog(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirm_dialog, null);

        // 获取控件
        ll_scan = view.findViewById(R.id.ll_scan);  // 扫一扫获取IP摄像头
        ll_media = view.findViewById(R.id.ll_media);// 媒体库
        ll_set = view.findViewById(R.id.ll_set);    // 设置
        ll_about = view.findViewById(R.id.ll_about);// 关于 APP

        // 设置事件
        ll_scan.setOnClickListener(this);
        ll_media.setOnClickListener(this);
        ll_set.setOnClickListener(this);
        ll_about.setOnClickListener(this);

        setContentView(view);
        initWindow();
    }

    private void initWindow() {
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        this.setWidth((int) (d.widthPixels * 0.35));
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        backgroundAlpha((Activity) context, 0.8f);//0.0-1.0
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha((Activity) context, 1f);
            }
        });
    }

    //设置添加屏幕的背景透明度
    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    public void showAtBottom(View view) {
        //弹窗位置设置
        showAsDropDown(view, Math.abs((view.getWidth() - getWidth()) / 2), 10);
        //showAtLocation(view, Gravity.TOP | Gravity.RIGHT, 10, 110);//有偏差
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_scan:
                Toast.makeText(context, "扫描二维码获取 IP 摄像头", Toast.LENGTH_SHORT).show();
                // 创建IntentIntegrator对象
                IntentIntegrator intentIntegrator = new IntentIntegrator((Activity) context);
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
                Toast.makeText(context, "查看媒体库", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ll_set:
                Toast.makeText(context, "设置", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ll_about:
                Toast.makeText(context, "关于APP", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
    }
}
