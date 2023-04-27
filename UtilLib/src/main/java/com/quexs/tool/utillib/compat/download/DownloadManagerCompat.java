package com.quexs.tool.utillib.compat.download;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import java.io.File;

/**
 * 下载兼容类
 */
public class DownloadManagerCompat {
    private Context appContext;
    private boolean isReleased;
    private DownloadManagerReceiver downloadManagerReceiver;
    private DownloadManagerListener downloadManagerListener;

    private DownloadManager downloadManager;

    public DownloadManagerCompat(Context context){
        downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        this.appContext = context.getApplicationContext();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentfilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        downloadManagerReceiver = new DownloadManagerReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(downloadManagerReceiver, intentfilter, Context.RECEIVER_EXPORTED);
        }else {
            appContext.registerReceiver(downloadManagerReceiver, intentfilter);
        }
    }

    public void setDownloadManagerListener(DownloadManagerListener downloadManagerListener) {
        this.downloadManagerListener = downloadManagerListener;
    }

    /**
     *
     * @param url 下载链接
     * @param title 下载标题
     * @param description 下载描述
     * @return
     */
    public long download(String url, String title, String description){
        if(isReleased) return -1;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(downloadManagerListener != null){
                downloadManagerListener.permissionDenied();
            }
            return -1;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //设置Notifcaiton的标题和描述
        request.setTitle(title);
        request.setDescription(description);
        //指定在WIFI状态下，执行下载操作。
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //指定在MOBILE状态下，执行下载操作
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //下载情况是否显示在systemUI下拉状态栏中
        request.setVisibleInDownloadsUi(true);
        //设置Notification的显示，和隐藏
        /*
        在下载进行中时显示，在下载完成后就不显示了。可以设置如下三个值：
        VISIBILITY_HIDDEN 下载UI不会显示，也不会显示在通知中，如果设置该值，需要声明android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        VISIBILITY_VISIBLE 当处于下载中状态时，可以在通知栏中显示；当下载完成后，通知栏中不显示
        VISIBILITY_VISIBLE_NOTIFY_COMPLETED 当处于下载中状态和下载完成时状态，均在通知栏中显示
        VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION 只在下载完成时显示在通知栏中。
        * */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();
        //设置下载路径 sdcard/download/
        String fullName = url.substring(url.lastIndexOf(File.separator) + 1);
        int index = fullName.lastIndexOf("?");
        if(index > 0){
            fullName = fullName.substring(0, index);
        }
        //下载道应用内私有目录
        request.setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fullName);
        //加入到下载的队列中。一旦下载管理器准备好执行并且连接可用，下载将自动启动。
        //一个下载任务对应唯一个ID， 此id可以用来去查询下载内容的相关信息
        return downloadManager.enqueue(request);
    }

    /**
     * 获取下载文件的Uri
     * @param downloadId
     * @return
     */
    public Uri getUriForDownloadedFile(long downloadId){
        if(isReleased) return null;
        return downloadManager.getUriForDownloadedFile(downloadId);
    }

    /**
     * 获取下载文件的媒体类型
     * @param downloadId
     * @return
     */
    public String getMimeTypeForDownloadedFile(long downloadId){
        if(isReleased) return null;
        return downloadManager.getMimeTypeForDownloadedFile(downloadId);
    }

    /**
     * 取消任务
     * @param ids
     */
    public void cancel(long... ids){
        if(isReleased) return;
        downloadManager.remove(ids);
    }

    /**
     * 获取下载文件的状态
     * @param downloadId
     * @return
     */
    public int getStatusForDownloadedFile(long downloadId){
        if(isReleased) return -1;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);//筛选下载任务，传入任务ID，可变参数
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if(cursor == null){
                return -1;
            }
            if(cursor.moveToFirst()){
                return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 取消关播接收注册
     */
    public void unregisterReceiver() {
        if(isReleased) return;
        appContext.unregisterReceiver(downloadManagerReceiver);
        downloadManagerReceiver = null;
    }

    public void release(){
        isReleased = true;
        appContext = null;
        downloadManager = null;
    }
}
