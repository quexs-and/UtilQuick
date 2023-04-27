package com.quexs.tool.utillib.compat.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadManagerReceiver extends BroadcastReceiver {

    private DownloadManagerListener downloadManagerListener;

    public void setDownloadManagerListener(DownloadManagerListener downloadManagerListener) {
        this.downloadManagerListener = downloadManagerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if(downloadId != 0){
            switch (action){
                case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                    //下载完成
                    if(downloadManagerListener != null){
                        downloadManagerListener.downloadComplete(downloadId);
                    }
                    break;
                case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                    //点击通知栏
                    if(downloadManagerListener != null){
                        downloadManagerListener.notificationClicked(downloadId);
                    }
                    break;
            }
        }

    }
}
