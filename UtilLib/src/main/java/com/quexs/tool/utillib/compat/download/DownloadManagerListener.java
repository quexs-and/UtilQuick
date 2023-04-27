package com.quexs.tool.utillib.compat.download;

public interface DownloadManagerListener {

    void permissionDenied();
    void downloadComplete(long id);
    void notificationClicked(long id);

}
