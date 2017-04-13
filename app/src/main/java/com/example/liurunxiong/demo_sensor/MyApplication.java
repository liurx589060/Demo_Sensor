package com.example.liurunxiong.demo_sensor;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by liurunxiong on 2017/4/13.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        /**
         * 获取日志
         */
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
