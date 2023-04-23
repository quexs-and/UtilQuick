package com.quexs.tool.utildemo;

import androidx.multidex.MultiDexApplication;

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MyUtilTool.getInstance().init(this);
    }
}
