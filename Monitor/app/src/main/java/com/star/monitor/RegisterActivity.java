package com.star.monitor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {

    private LinearLayout mLlRegisterPhone;
    private EditText mEtRegisterUsername;
    private LinearLayout mLlRegisterSmsCode;
    private EditText mEtRegisterAuthCode;
    private TextView mTvRegisterSmsCall;
    private Button mBtRegisterSubmit;
    private ImageView mIvRegisterUsernameDel;

    private static String phoneNumber;         // 手机号码
    private String verificationCode;    // 验证码

    private boolean flag;   // 操作是否成功
    private boolean isExist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register_step_one);

        initView();

        final Context context = RegisterActivity.this;
        final String appKey = "2eb379ee978fa";
        final String appSecret = "f1733328f3cb0772088b6879afefbc67";

        SMSSDK.initSDK(context, appKey, appSecret);
        // 操作回调
        EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };

        // 注册回调接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    // 初始化视图
    private void initView(){

        mLlRegisterPhone = findViewById(R.id.ll_register_phone);
        mEtRegisterUsername = findViewById(R.id.et_register_username);
        mLlRegisterSmsCode = findViewById(R.id.ll_register_sms_code);
        mEtRegisterAuthCode = findViewById(R.id.et_register_auth_code);
        mTvRegisterSmsCall = findViewById(R.id.tv_register_sms_call);
        mBtRegisterSubmit = findViewById(R.id.bt_register_submit);
        mIvRegisterUsernameDel = findViewById(R.id.iv_register_username_del);

        //注册点击事件
        mEtRegisterUsername.setOnClickListener(this);
        mEtRegisterAuthCode.setOnClickListener(this);
        mTvRegisterSmsCall.setOnClickListener(this);
        mBtRegisterSubmit.setOnClickListener(this);
        mIvRegisterUsernameDel.setOnClickListener(this);

        findViewById(R.id.ib_navigation_back).setOnClickListener(this);

        // 注册其他事件
        mEtRegisterUsername.setOnFocusChangeListener(this);
        mEtRegisterUsername.addTextChangedListener(this);
        mEtRegisterAuthCode.setOnFocusChangeListener(this);
        mEtRegisterAuthCode.addTextChangedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_navigation_back:
                finish();
                break;
            case R.id.et_register_username:
                mEtRegisterAuthCode.clearFocus();
                mEtRegisterUsername.setFocusableInTouchMode(true);
                mEtRegisterUsername.requestFocus();
                break;
            case R.id.et_register_auth_code:
                mEtRegisterUsername.clearFocus();
                mEtRegisterAuthCode.setFocusableInTouchMode(true);
                mEtRegisterAuthCode.requestFocus();
                break;
            case R.id.tv_register_sms_call:
                if (!TextUtils.isEmpty(mEtRegisterUsername.getText())) {
                    if (mEtRegisterUsername.getText().length() == 11) {
                        phoneNumber = mEtRegisterUsername.getText().toString();
                        // 判断号码是否存在
                        try {
                            judgePhoneIsExist(phoneNumber);
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!isExist){
                            Toast.makeText(this, "已点击，请勿重复点击", Toast.LENGTH_SHORT).show();
                            SMSSDK.getVerificationCode("86", phoneNumber); // 发送验证码给号码的 phoneNumber 的手机
                            mEtRegisterAuthCode.requestFocus();
                        }else{
                            Toast.makeText(this, "输入的电话号码已存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "请输入完整的电话号码", Toast.LENGTH_SHORT).show();
                        mEtRegisterUsername.requestFocus();
                    }
                } else {
                    Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
                    mEtRegisterUsername.requestFocus();
                }
                break;
            case R.id.bt_register_submit:
                if (!TextUtils.isEmpty(mEtRegisterAuthCode.getText())) {
                    if (mEtRegisterAuthCode.getText().length() == 6) {
                        verificationCode = mEtRegisterAuthCode.getText().toString();
                        SMSSDK.submitVerificationCode("86", phoneNumber, verificationCode);
                        flag = false;
                    } else {
                        Toast.makeText(this, "请输入完整的验证码", Toast.LENGTH_SHORT).show();
                        mEtRegisterAuthCode.requestFocus();
                    }
                } else {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    mEtRegisterAuthCode.requestFocus();
                }
                break;
            case R.id.iv_register_username_del:
                mEtRegisterUsername.setText(null);
                break;
        }
    }

    // 号码验证码焦点改变
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();

        if (id == R.id.et_register_username) {
            if (hasFocus) {
                mLlRegisterPhone.setActivated(true);
                mLlRegisterSmsCode.setActivated(false);
            }
        } else {
            if (hasFocus) {
                mLlRegisterSmsCode.setActivated(true);
                mLlRegisterPhone.setActivated(false);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;

            if (result == SMSSDK.RESULT_COMPLETE) {
                // 如果操作成功
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    // 校验验证码，返回校验的手机和国家代码
                    Toast.makeText(RegisterActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, SetPwdActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    // 获取验证码成功，true为智能验证，false为普通下发短信
                    Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    // 返回支持发送验证码的国家列表
                }
            } else {
                // 如果操作失败
                if (flag) {
                    Toast.makeText(RegisterActivity.this, "验证码获取失败，请重新获取", Toast.LENGTH_SHORT).show();
                    mEtRegisterUsername.requestFocus();
                } else {
                    ((Throwable) data).printStackTrace();
                    Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    /**
     * 判断号码是否存在
     */
    public void judgePhoneIsExist(final String phoneNumber){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlPath = "http://39.107.27.182:8080/monitor/judge?account=" +phoneNumber;
                String result = UrlRequestUtil.readParse(urlPath);

                if("1007".equals(result)){
                    isExist = false;
                }
            }
        }).start();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    // 手机号验证码输入事件
    @Override
    public void afterTextChanged(Editable s) {
        String username = mEtRegisterUsername.getText().toString().trim();
        String authCode = mEtRegisterAuthCode.getText().toString().trim();

        //是否显示清除按钮
        if (username.length() > 0) {
            mIvRegisterUsernameDel.setVisibility(View.VISIBLE);
        } else {
            mIvRegisterUsernameDel.setVisibility(View.INVISIBLE);
        }

        //注册按钮是否可用
        if (!TextUtils.isEmpty(authCode) && !TextUtils.isEmpty(username)) {
            mBtRegisterSubmit.setEnabled(true);
            mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_register_submit);
            mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.white));
        } else {
            mBtRegisterSubmit.setEnabled(false);
            mBtRegisterSubmit.setBackgroundResource(R.drawable.bg_register_submit_lock);
            mBtRegisterSubmit.setTextColor(getResources().getColor(R.color.account_lock_font_color));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();  // 注销回调接口
    }
}