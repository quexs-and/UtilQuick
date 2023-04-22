package com.quexs.tool.utillib.compat.album;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * 相册兼容工具栏
 * 分享视频到相册
 */
public class ShareVideoCompat {
    private ActivityResultLauncher<String> writeLauncher;
    private final String prefix;
    private Context context;
    private String videoPath;
    private ShareAlbumCompatListener shareAlbumCompatListener;
    private boolean isNotInsertRepeat;

    public ShareVideoCompat(ComponentActivity activity) {
        this(activity, "album_");
    }

    public ShareVideoCompat(Fragment fragment) {
        this(fragment, "album_");
    }

    /**
     * 应当在onCreate中创建
     *
     * @param activity
     * @param prefix   前缀
     */
    public ShareVideoCompat(ComponentActivity activity, String prefix) {
        this.prefix = prefix;
        this.context = activity;
        writeLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onWriteAlbum);
    }

    /**
     * 应当在onCreate中创建
     *
     * @param fragment
     * @param prefix   前缀
     */
    public ShareVideoCompat(Fragment fragment, String prefix) {
        this.prefix = prefix;
        this.context = fragment.getContext();
        writeLauncher = fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onWriteAlbum);
    }

    /**
     * 启用ApplicationContext 避免内存泄露
     */
    public void enableApplicationContext() {
        context = context.getApplicationContext();
    }

    /**
     * 启动不重复插入
     */
    public void enableNotInsertRepeat() {
        this.isNotInsertRepeat = true;
    }

    /**
     * 分享监听
     *
     * @param shareAlbumCompatListener
     */
    public void setShareAlbumCompatListener(ShareAlbumCompatListener shareAlbumCompatListener) {
        this.shareAlbumCompatListener = shareAlbumCompatListener;
    }

    /**
     * 分享到相册
     *
     * @param videoPath
     */
    public void shareToAlbum(String videoPath) {
        this.videoPath = videoPath;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            writeLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else {
            writeLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO);
        }
    }

    /**
     * 主动释放
     */
    public void release() {
        context = null;
        writeLauncher = null;
    }

    /**
     * 写入权限回调
     *
     * @param result
     */
    private void onWriteAlbum(Boolean result) {
        if (result) {
            saveVideoToAlbum(videoPath);
        } else {
            if (shareAlbumCompatListener != null) {
                shareAlbumCompatListener.deniedShare(videoPath);
            }
        }
    }

    /**
     * 保存图片到相册
     *
     * @param videoPath
     */
    private void saveVideoToAlbum(String videoPath) {
        if (shareAlbumCompatListener != null) {
            shareAlbumCompatListener.startShare(videoPath);
        }
        new Thread(new ShareVideoRunnable(videoPath)).start();
    }


    private class ShareVideoRunnable implements Runnable {
        private final String path;

        public ShareVideoRunnable(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            File videoFile = new File(path);
            try {
                if (!videoFile.exists()) throw new Exception("file is null");
                //重复插入判断
                if (isNotInsertRepeat && isExitVideoFileInContentResolver(videoFile)) return;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    saveImageOrVideoToAlbum(videoFile);
                } else {
                    saveVideoToGallery(videoFile);
                }
                if (shareAlbumCompatListener != null) {
                    shareAlbumCompatListener.endShare(null, path);
                }
            } catch (Exception e) {
                if (shareAlbumCompatListener != null) {
                    shareAlbumCompatListener.endShare(e, path);
                }
            }
        }
    }


    /**
     * 保存视频
     *
     * @param videoFile
     * @throws Exception
     */
    private void saveVideoToGallery(File videoFile) throws Exception {
        //保存到图库
        ContentResolver localContentResolver = context.getContentResolver();
        ContentValues localContentValues = getVideoContentValues(videoFile, Calendar.getInstance().getTimeInMillis());
        Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        //Android Q 需要拷贝Uri
        copyFileToContentResolver(context, localUri, videoFile);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
    }

    /**
     * 获取视频 contentValue
     *
     * @param videoFile
     * @param paramLong
     * @return
     */
    public ContentValues getVideoContentValues(File videoFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Video.Media.TITLE, videoFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, prefix + videoFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, paramLong);
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, paramLong);
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, paramLong);
        localContentValues.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, videoFile.length());
        return localContentValues;
    }

    /**
     * 复制文件到共享目录
     *
     * @param context
     * @param localUri
     * @param file
     * @throws Exception
     */
    private void copyFileToContentResolver(Context context, Uri localUri, File file) throws Exception {
        OutputStream out = context.getContentResolver().openOutputStream(localUri);
        int len;
        byte[] buffer = new byte[1024];
        InputStream is = new FileInputStream(file);
        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        is.close();
        out.close();
    }

    /**
     * 保存图片、视频文件到相册
     *
     * @param formFile
     */
    private void saveImageOrVideoToAlbum(File formFile) throws Exception {
        String appDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;
        File appDir = new File(appDirPath);
        if (!appDir.exists() & !appDir.mkdir()) throw new Exception("create app Dir fail");
        String albumFileName = prefix + formFile.getName();
        File albumFile = new File(appDirPath, albumFileName);
        FileInputStream fis = new FileInputStream(formFile);
        FileOutputStream fos = new FileOutputStream(albumFile);
        byte[] buff = new byte[1024];
        int len;
        while ((len = fis.read(buff)) != -1) {
            fos.write(buff, 0, len);
        }
        fis.close();
        fos.flush();
        fos.close();
        // 保存图片、视频后发送广播通知更新数据库
        Uri uri = Uri.fromFile(albumFile);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }


    /**
     * 判断共享文件库中文件是已存在，存在则不插入-防止重发
     *
     * @param file
     * @return
     */
    private boolean isExitVideoFileInContentResolver(File file) {
        String[] projection = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED};
        String selection = (MediaStore.Video.Media.DISPLAY_NAME) + "=?";
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, selection,
                new String[]{prefix + file.getName()}, null);
        if (cursor != null) {
            boolean isExit = cursor.getCount() > 0;
            cursor.close();
            return isExit;
        }
        return false;
    }

}
