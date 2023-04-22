package com.quexs.tool.utillib.compat;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.RequiresApi;

import com.quexs.tool.utillib.util.FilePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Uri 转 文件路径
 */
public class UriConvertCompat {
    private Context appContext;
    private boolean isEnableCacheForQ;
    private boolean isEnableCopyForR;

    public UriConvertCompat(Context appContext){
        this.appContext = appContext;
    }

    /**
     * 设置Android Q以上 复制的 文件是否存储在缓存中
     * @param enableCacheForQ
     */
    public void setEnableCacheForQ(boolean enableCacheForQ) {
        isEnableCacheForQ = enableCacheForQ;
    }

    /**
     * 设置Android R以上 是否复制缓存文件来获取文件
     * @param enableCopyForR
     */
    public void setEnableCopyForR(boolean enableCopyForR) {
        isEnableCopyForR = enableCopyForR;
    }

    /**
     * 获取文件路径
     * @param uri
     * @return
     */
    public String getAbsolutePath(Uri uri){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if(isEnableCopyForR){
                    return getAbsolutePathFromApiQ(uri);
                }else {
                    return getAbsolutePathDoesNotApiQ(uri);
                }
            }
            return getAbsolutePathFromApiQ(uri);
        }
        return getAbsolutePathDoesNotApiQ(uri);
    }

    /**
     * 获取Uri文件路径 不包括 Android Q
     * @param uri
     * @return
     */
    public String getAbsolutePathDoesNotApiQ(Uri uri){
        //Android Q 单独处理方案
        if (DocumentsContract.isDocumentUri(appContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                // 外部存储空间
                String docId = DocumentsContract.getDocumentId(uri);
                String[] divide = docId.split(":");
                if ("primary".equalsIgnoreCase(divide[0])) return Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                return "/storage/".concat(divide[0]).concat("/").concat(divide[1]);
            } else if (isDownloadDocument(uri)) {
                // 下載目錄
                String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    return docId.replaceFirst("raw:", "");
                }else {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                    return queryAbsolutePath(contentUri, null, null);
                }
            } else if (isMediaDocument(uri)) {
                // 图片、影音、档案
                String docId = DocumentsContract.getDocumentId(uri);
                String[] divide = docId.split(":");
                String type = divide[0];
                Uri mediaUri = null;
                if ("image".equalsIgnoreCase(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                return queryAbsolutePath(mediaUri, "_id=?", new String[]{divide[1]});
            }
        } else {
            // 如果是一般的URI
            if ("content".equalsIgnoreCase(uri.getScheme())){
                if(isGooglePhotosUri(uri)){
                    return uri.getLastPathSegment();
                }else {
                    // 內容URI
                    return queryAbsolutePath(uri, null, null);
                }
            }else if ("file".equalsIgnoreCase(uri.getScheme())){
                return uri.getPath();
            }else {
                return queryAbsolutePath(uri, null, null);
            }
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public void release(){
        appContext = null;
    }

    public String queryAbsolutePath(Uri uri, String selection, String[] selectionArgs){
        String[] projection = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = appContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if(cursor != null){
            try {
                if (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Android Q 单独获取文件路径
     *
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private String getAbsolutePathFromApiQ(Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            return file.getAbsolutePath();
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            //复制文件到沙盒文件
            ContentResolver contentResolver = appContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        String displayName = cursor.getString(columnIndex);
                        InputStream is = contentResolver.openInputStream(uri);
                        File file = new File(isEnableCacheForQ ?FilePath.getCachePath(appContext, "convert", true)  : FilePath.getPath(appContext, "convert", true), Calendar.getInstance().getTimeInMillis() + "_" + displayName);
                        FileOutputStream fos = new FileOutputStream(file);
                        FileUtils.copy(is, fos);
                        fos.close();
                        is.close();
                        return file.getAbsolutePath();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }
}
