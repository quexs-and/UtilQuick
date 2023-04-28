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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Uri 转 文件路径
 */
public class UriConvertCompat {
    private Context appContext;
    private boolean isEnableCache;
    private boolean isEnableCopyForR;
    private DirectoryCompat directoryCompat;

    public UriConvertCompat(Context context) {
        this.appContext = context.getApplicationContext();
        this.directoryCompat = new DirectoryCompat(context);
    }

    /**
     * 如果是复制文件，则复制到缓存中
     *
     * @param isEnableCache
     */
    public UriConvertCompat seEnableCache(boolean isEnableCache) {
        this.isEnableCache = isEnableCache;
        return this;
    }

    /**
     * 设置Android R以上 是否复制缓存文件来获取文件
     *
     * @param enableCopyForR
     */
    public UriConvertCompat setEnableCopyForR(boolean enableCopyForR) {
        isEnableCopyForR = enableCopyForR;
        return this;
    }

    /**
     * 获取文件路径
     *
     * @param uri
     * @return
     */
    public String getAbsolutePath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isEnableCopyForR) {
                    return getAbsolutePathFromGetContentUri(uri);
                } else {
                    return getAbsolutePathDoesNotApiQ(uri);
                }
            }
            return getAbsolutePathFromGetContentUri(uri);
        }
        return getAbsolutePathDoesNotApiQ(uri);
    }

    /**
     * 获取Uri文件路径 不包括 Android Q
     *
     * @param uri
     * @return
     */
    public String getAbsolutePathDoesNotApiQ(Uri uri) {
        //Android Q 单独处理方案
        if (DocumentsContract.isDocumentUri(appContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                // 外部存储空间
                String docId = DocumentsContract.getDocumentId(uri);
                String[] divide = docId.split(":");
                if ("primary".equalsIgnoreCase(divide[0]))
                    return Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                return "/storage/".concat(divide[0]).concat("/").concat(divide[1]);
            } else if (isDownloadDocument(uri)) {
                // 下載目錄
                String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    return docId.replaceFirst("raw:", "");
                } else {
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
                if(mediaUri != null){
                    mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                    return queryAbsolutePath(mediaUri, "_id=?", new String[]{divide[1]});
                }
                return getAbsolutePathFromGetContentUri(uri);
            }
        } else {
            // 如果是一般的URI
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                } else {
                    // 內容URI
                    return queryAbsolutePath(uri, null, null);
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            } else {
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

    public void release() {
        appContext = null;
        if(directoryCompat != null){
            directoryCompat.release();
            directoryCompat = null;
        }
    }

    public String queryAbsolutePath(Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = appContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     *
     * @param uri
     * @return
     */
    public String getAbsolutePathFromGetContentUri(Uri uri) {
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
                        File file = new File(isEnableCache ? directoryCompat.getCacheDir("Convert") : directoryCompat.getExternalFilesDir("Convert"), displayName);
                        if (!file.exists()) {
                            InputStream is = contentResolver.openInputStream(uri);
                            FileOutputStream fos = new FileOutputStream(file);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                FileUtils.copy(is, fos);
                            } else {
                                byte[] bt = new byte[1024];
                                int l;
                                while ((l = is.read(bt)) > 0) {
                                    fos.write(bt, 0, l);
                                }
                                fos.flush();
                            }
                            fos.close();
                            is.close();
                        }
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
