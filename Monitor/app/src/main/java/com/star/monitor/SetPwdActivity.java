package com.star.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SetPwdActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {

    private EditText mEtSetPwd;
    private Button mBtnSetPwdSubmit;
    private ImageView mIvSetPwdDel;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            String result = message.obj.toString();

            switch (result){
                case "1001":
                    Toast.makeText(SetPwdActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetPwdActivity.this, MainActivity.class));
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_set_pwd);

        initView();
    }

    private void initView(){

        mEtSetPwd = findViewById(R.id.et_set_pwd);
        mBtnSetPwdSubmit = findViewById(R.id.bt_set_pwd_submit);
        // 获取控件
        mIvSetPwdDel = findViewById(R.id.iv_set_pwd_del);

        mBtnSetPwdSubmit.setOnClickListener(this);
        mIvSetPwdDel.setOnClickListener(this);

        findViewById(R.id.ib_navigation_back).setOnClickListener(this);

        // 注册其他事件
        mEtSetPwd.setOnFocusChangeListener(this);
        mEtSetPwd.addTextChangedListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ib_navigation_back:
                finish();
                break;
            case R.id.bt_set_pwd_submit:
                registerRequest();
                break;
            case R.id.iv_set_pwd_del:
                mEtSetPwd.setText(null);
        }
    }

    /**
     * 注册
     */
    public void registerRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String username = getIntent().getStringExtra("phoneNumber");
                String password = mEtSetPwd.getText().toString().trim();

                String urlPath = "http://39.107.27.182:8080/monitor/signUp?account=" + username + "&password=" + password;
                String result = UrlRequestUtil.readParse(urlPath);

                Message message = new Message();
                message.obj = Integer.parseInt(result);

                handler.sendMessage(message);
            }
        }).start();
    }

    //用户名密码焦点改变
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();

        if (id == R.id.et_set_pwd) {
            if (hasFocus) {
                mEtSetPwd.setActivated(true);
            }
        } else {
            if (hasFocus) {
                mEtSetPwd.setActivated(false);
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    //用户名密码输入事件
    @Override
    public void afterTextChanged(Editable s) {
        String pwd = mEtSetPwd.getText().toString().trim();

        //是否显示清除按钮
        if (pwd.length() > 0) {
            mIvSetPwdDel.setVisibility(View.VISIBLE);
        } else {
            mIvSetPwdDel.setVisibility(View.INVISIBLE);
        }

        //完成按钮是否可用
        if (!TextUtils.isEmpty(pwd)) {
            mBtnSetPwdSubmit.setEnabled(true);
            mBtnSetPwdSubmit.setBackgroundResource(R.drawable.bg_login_submit);
            mBtnSetPwdSubmit.setTextColor(getResources().getColor(R.color.white));
        } else {
            mBtnSetPwdSubmit.setEnabled(false);
            mBtnSetPwdSubmit.setBackgroundResource(R.drawable.bg_login_submit_lock);
            mBtnSetPwdSubmit.setTextColor(getResources().getColor(R.color.account_lock_font_color));
        }
    }
}
