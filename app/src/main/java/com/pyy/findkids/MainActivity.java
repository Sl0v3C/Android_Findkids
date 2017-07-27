package com.pyy.findkids;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 去掉界面显示
        //setContentView(R.layout.activity_main);
        finish();
    }
}
