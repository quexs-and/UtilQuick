package com.quexs.tool.utillib.compat;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * 目录兼容类
 */
public class DirectoryCompat {
    private Context appContext;
    public DirectoryCompat(Context context){
        this.appContext = context.getApplicationContext();
    }

    public File getExternalFilesDir(String type){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File file = appContext.getExternalFilesDir(type);
            if(!file.exists()){
                file.mkdirs();
            }
            return file;
        }
        return null;
    }

    public File getExternalCacheDir(String type){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File cacheFile = appContext.getExternalCacheDir();
            if(!TextUtils.isEmpty(type)){
                File file = new File(cacheFile, type);
                if(!file.exists()){
                    file.mkdirs();
                }
                return file;
            }
            return cacheFile;
        }
        return null;
    }

    public File getFilesDir(String type){
        if(TextUtils.isEmpty(type)){
            return appContext.getFilesDir();
        }
        File file = new File(appContext.getFilesDir(),type);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public File getCacheDir(String type){
        if(TextUtils.isEmpty(type)){
            return appContext.getCacheDir();
        }
        File file = new File(appContext.getCacheDir(),type);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public void release(){
        appContext = null;
    }

}
