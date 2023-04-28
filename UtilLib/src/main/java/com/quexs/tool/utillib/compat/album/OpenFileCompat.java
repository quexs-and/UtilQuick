package com.quexs.tool.utillib.compat.album;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringDef;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 打开相册 选取视频图片
 */
public class OpenFileCompat {

    @StringDef({FileType.VIDEO,
            FileType.IMAGE,
            FileType.IMAGE_AND_VIDEO,
            FileType.AUDIO,
            FileType.ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FileType {
        /**
         * 视频
         */
        String VIDEO = "video/*";
        /**
         * 图片
         */
        String IMAGE = "image/*";

        /**
         * 音频
         */
        String AUDIO = "audio/*";
        /**
         * 视频图片
         */
        String IMAGE_AND_VIDEO = "image/*;video/*";
        /**
         * 所有文件
         */
        String ALL = "*/*";
    }

    private ActivityResultLauncher<Intent> fileLauncher;
    private OpenFileCompatListener openFileCompatListener;
    private int maxSelectCount;

    private Context mContext;

    /**
     * 应当在onCreate中创建
     *
     * @param activity
     */
    public OpenFileCompat(ComponentActivity activity) {
        this.mContext = activity;
        this.fileLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackFile);
    }

    /**
     * 应当在onCreate中创建
     *
     * @param fragment
     */
    public OpenFileCompat(Fragment fragment) {
        this.mContext = fragment.getContext();
        this.fileLauncher = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackFile);
    }

    /**
     * 相册打开监听
     *
     * @param openFileCompatListener
     */
    public void setOpenAlbumCompatListener(OpenFileCompatListener openFileCompatListener) {
        this.openFileCompatListener = openFileCompatListener;
    }

    /**
     * 打开相册
     *
     * @param count
     */
    public void open(@FileType String type, int count) {
        maxSelectCount = Math.max(count, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //使用 Android 13 新特性 相册 支持指定选择相片最大数量
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            if (!TextUtils.equals(type, FileType.IMAGE_AND_VIDEO)) {
                intent.setType(type);
            }
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
            this.fileLauncher.launch(intent);
            return;
        }
        Intent albumIntent = new Intent();
        if(FileType.IMAGE_AND_VIDEO.equals(type)){
            albumIntent.setType(FileType.ALL);
            albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {FileType.IMAGE, FileType.VIDEO});
        }else {
            albumIntent.setType(type);
        }
        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
        albumIntent.setAction(Intent.ACTION_GET_CONTENT);
        if (maxSelectCount > 1) {
            //启动多选
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        fileLauncher.launch(albumIntent);
    }

    /**
     * 主动释放
     */
    public void release() {
        mContext = null;
        openFileCompatListener = null;
        if(fileLauncher != null){
            fileLauncher.unregister();
            fileLauncher = null;
        }
    }



    /**
     * 选取图片视频回调
     *
     * @param result
     */
    private void callbackFile(ActivityResult result) {
        Intent intent = result.getData();
        if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
            if(mContext != null){
                new Thread(new DataRunnable(mContext,intent));
            }
        }
    }

    private File getFileFromUri(Context appContext, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return new File(uri.getPath());
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            //复制文件到沙盒文件
            ContentResolver contentResolver = appContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                        File file = new File(appContext.getExternalCacheDir(), displayName);
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
                        return file;
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

    private class DataRunnable implements Runnable{
        private final Intent intent;
        private final Context appContext;
        public DataRunnable(Context context, Intent intent){
            this.intent = intent;
            this.appContext = context.getApplicationContext();
        }
        @Override
        public void run() {
            List<String> paths = new ArrayList<>();
            if(intent.getData() != null){
                File file = getFileFromUri(appContext, intent.getData());
                if(file != null){
                    paths.add(file.getAbsolutePath());
                }
                if (openFileCompatListener != null) {
                    openFileCompatListener.openFilePath(paths);
                }
            }else if(intent.getClipData() != null){
                ClipData clipData = intent.getClipData();
                int count = Math.min(maxSelectCount, clipData.getItemCount());
                for (int i = 0; i < count; i++) {
                    File file = getFileFromUri(appContext, clipData.getItemAt(i).getUri());
                    if(file != null){
                        paths.add(file.getAbsolutePath());
                    }
                }
                if(openFileCompatListener != null){
                    openFileCompatListener.openFilePath(paths);
                }
            }
        }
    }

}
