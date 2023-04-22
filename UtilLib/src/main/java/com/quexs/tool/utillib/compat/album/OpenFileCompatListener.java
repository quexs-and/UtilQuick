package com.quexs.tool.utillib.compat.album;

import android.net.Uri;

import java.util.List;

public interface OpenFileCompatListener {

    void deniedOpen();

    /**
     * 单选 打开文件管理器
     * @param uri
     */
    void radioOpen(Uri uri);

    /**
     * 多选 打开文件管理器
     * @param list
     */
    void multipleOpen(List<Uri> list);
}
