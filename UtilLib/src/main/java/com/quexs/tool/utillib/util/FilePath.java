package com.quexs.tool.utillib.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class FilePath {
    /**
     * 存放下载文件的目录-私有目录路径（默认外部存储）
     * @param context
     * @return
     */
    public static String getDownloadPath(Context context, boolean isAbsolute){
        return getPath(context, Environment.DIRECTORY_DOWNLOADS, isAbsolute);
    }

    public static String getCacheDownloadPath(Context context,boolean isAbsolute){
        return getCachePath(context, Environment.DIRECTORY_DOWNLOADS, isAbsolute);
    }

    /**
     * 存放图片文件的目录-私有目录路径（默认外部存储）
     * @param context
     * @return
     */
    public static String getPicturePath(Context context, boolean isAbsolute){
        return getPath(context,Environment.DIRECTORY_PICTURES,isAbsolute);
    }

    public static String getCachePicturePath(Context context,boolean isAbsolute){
        return getCachePath(context, Environment.DIRECTORY_PICTURES, isAbsolute);
    }


    /**
     * 存放音频文件的目录-私有目录路径（默认外部存储）
     * @param context
     * @return
     */
    public static String getMusicPath(Context context, boolean isAbsolute){
        return getPath(context,Environment.DIRECTORY_MUSIC, isAbsolute);
    }

    public static String getCacheMusicPath(Context context,boolean isAbsolute){
        return getCachePath(context, Environment.DIRECTORY_MUSIC, isAbsolute);
    }

    /**
     * 存放视频文件的目录-私有目录路径（默认外部存储）
     * @param context
     * @return
     */
    public static String getMoviesPath(Context context, boolean isAbsolute){
        return getPath(context,Environment.DIRECTORY_MOVIES, isAbsolute);
    }

    public static String getCacheMoviesPath(Context context,boolean isAbsolute){
        return getCachePath(context, Environment.DIRECTORY_MOVIES, isAbsolute);
    }

    /**
     * 获取路径-私有目录路径（默认优先外部存储）
     * @param context
     * @param type
     * @param isAbsolute 是否绝对路径
     * @return
     * 注：此处返回的是路径名
     */
    public static String getPath(Context context, String type, boolean isAbsolute){
        File file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //Android 10开始支持内置分区存储
            // 有挂载SD 卡 则 使用内置-外部存储
            // 没有挂载SD 卡 则 使用内置-内部存储
            file = isSDCardMounted() ? context.getExternalFilesDir(type) : new File(context.getFilesDir(), type);
        }else {
            //Android 10 以前 直接使用应用内部存储
            file = new File(context.getFilesDir(), type);
        }
        if(file.exists() || file.mkdirs()) return isAbsolute ? file.getAbsolutePath() : file.getPath();
        return "";
    }

    /**
     * 获取缓存文件路径
     * @param context
     * @param isAbsolute 是否绝对路径
     * @return
     */
    public static String getCachePath(Context context, String type, boolean isAbsolute){
        File file;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //Android 10开始支持内置分区存储
            // 有挂载SD 卡 则 使用内置-外部存储
            // 没有挂载SD 卡 则 使用内置-内部存储
            file = new File(isSDCardMounted() ? context.getExternalCacheDir() : context.getCacheDir(), type);
        }else {
            //Android 10 以前 直接使用应用内部存储
            file = new File(context.getCacheDir(), type);
        }
        if(file.exists() || file.mkdirs()) return isAbsolute ? file.getAbsolutePath() : file.getPath();
        return "";
    }

    /**
     * 判断SD卡是否挂载
     * @return
     */
    public static boolean isSDCardMounted(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SD卡的根目录
     * @return
     */
    public static String getSDCardBaseDir(){
        if(isSDCardMounted()) return Environment.getExternalStorageDirectory().getAbsolutePath();
        return null;
    }

    /**
     * 获取SD卡的完整空间大小，返回MB
     * @return
     */
    public static long getSDCardSize(){
        if(isSDCardMounted()){
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getBlockCountLong();
            long size = fs.getBlockSizeLong();
            return count * size / 1024 / 1024;
        }
        return 0;
    }

    /**
     * 获取SD卡的可用空间大小
     * @return
     */
    public static long getSDCardAvailableSize(){
        if(isSDCardMounted()){
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getAvailableBlocksLong();
            long size = fs.getBlockSizeLong();
            return count * size / 1024 / 1024;
        }
        return 0;
    }
}
