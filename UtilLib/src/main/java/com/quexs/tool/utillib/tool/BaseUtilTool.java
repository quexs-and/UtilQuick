package com.quexs.tool.utillib.tool;

import android.app.Application;

import com.quexs.tool.utillib.util.ViewConvert;
import com.quexs.tool.utillib.util.stack.AppManager;

/**
 * 实用程序工具基类
 */
public class BaseUtilTool {

    //Activity 堆栈管理器
    private AppManager appManager;
    private ViewConvert viewConvert;

    public BaseUtilTool() {

    }

    public void init(Application application){
        appManager = new AppManager(application);
    }

    /**
     * 获取 Activity 堆栈管理器
     *
     * @return
     */
    public AppManager getAppManager() {
        return appManager;
    }

    /**
     * 获取View转换工具
     * @return
     */
    public ViewConvert getViewConvert() {
        if(viewConvert == null){
            viewConvert = new ViewConvert();
        }
        return viewConvert;
    }
}

