package com.quexs.tool.utildemo;

import android.app.Application;

import com.quexs.tool.utildemo.util.JsonWrapper;
import com.quexs.tool.utillib.tool.BaseUtilTool;

public class MyUtilTool extends BaseUtilTool {

    private static volatile MyUtilTool instance;

    private JsonWrapper json;


    public static MyUtilTool getInstance() {
        if (instance == null) {
            synchronized (MyUtilTool.class) {
                if (instance == null) {
                    instance = new MyUtilTool();
                }
            }
        }
        return instance;
    }

    public MyUtilTool() {
        super();
    }

    @Override
    public void init(Application application) {
        super.init(application);

    }

    public JsonWrapper getJson() {
        if(json == null){
            json = new JsonWrapper();
        }
        return json;
    }
}
