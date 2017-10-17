package com.item.socket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.item.socket.one.MyServiceOne;
import com.item.socket.one.MyServiceTwo;
import com.item.socket.test.TestOneActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnOne, btnTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOne = (Button) findViewById(R.id.btn_one);
        btnTwo = (Button) findViewById(R.id.btn_two);
        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_one: // 第一个按钮
                // startService(new Intent(this, MyServiceOne.class));
                startActivity(new Intent(this, TestOneActivity.class));
                break;
            case R.id.btn_two: // 第二个按钮
                startService(new Intent(this, MyServiceTwo.class));
                break;
        }
    }
}
