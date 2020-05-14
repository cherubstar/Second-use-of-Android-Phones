package com.star.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class AboutAPPActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton mIbNavigationBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        mIbNavigationBack = findViewById(R.id.ib_navigation_back);
        mIbNavigationBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_navigation_back:
                //返回
                finish();
                break;
        }
    }
}
