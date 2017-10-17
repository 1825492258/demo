package com.item.socket.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.item.socket.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TestOneActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSocket; // 建立Socket连接按钮
    private Button btnPost; // 发送信息
    private EditText edtText; // 输入的信息
    private TextView tvText; // 展示的信息
    private String textString = ""; //
    private Button btnClean; // 清楚

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_one);
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        btnSocket = (Button) findViewById(R.id.btn_test);
        btnPost = (Button) findViewById(R.id.btn_post);
        edtText = (EditText) findViewById(R.id.edt_text);
        tvText = (TextView) findViewById(R.id.tv_text);
        btnClean = (Button) findViewById(R.id.btn_clean);
        btnSocket.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnClean.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test: // 开启服务
                startService(new Intent(this, MyTextService.class));
                break;
            case R.id.btn_post: // 发送信息
                EventBus.getDefault().post(new Msg(2, edtText.getText().toString()));
                break;
            case R.id.btn_clean: // 清除
                edtText.setText(null);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(new Intent(this, MyTextService.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHelloBus(String message) {
        Log.d("jiejie", "主线程接受信息--------" + message);
        textString = textString + message + "\n";
        tvText.setText(textString);
    }
}
