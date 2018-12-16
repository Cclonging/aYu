package com.example.cc.wechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ChattingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting);
        TextView textView = (TextView) findViewById(R.id.user_info);
        Intent intent = getIntent();
        textView.setText(intent.getStringExtra("name") + " " +
                intent.getStringExtra("header") + " " +
                intent.getStringExtra("desc") );

    }
}
