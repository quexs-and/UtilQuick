package com.quexs.tool.utillib.compat.album;

public interface ShareAlbumCompatListener {
    /**
     * 拒绝被权限
     * @param path
     */
    void deniedShare(String path);

    /**
     * 开始分享
     * @param path
     */
    void startShare(String path);

    /**
     * 结束分享
     * @param path
     */
    void endShare(Exception e,String path);
}
