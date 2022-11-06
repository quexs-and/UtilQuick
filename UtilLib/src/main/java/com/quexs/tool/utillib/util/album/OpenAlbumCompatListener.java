package com.quexs.tool.utillib.util.album;

import android.net.Uri;

import java.util.List;

public interface OpenAlbumCompatListener {
    /**
     * 拒绝被权限
     */
    void deniedOpen();

    /**
     * 单选 打开相册
     * @param uri
     */
    void radioOpen(Uri uri);

    /**
     * 多选 打开相册
     * @param list
     */
    void multipleOpen(List<Uri> list);
}
