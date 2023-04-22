package com.quexs.tool.utillib.compat.album;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringDef;
import androidx.fragment.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 打开相册 选取视频图片
 */
public class OpenFileCompat {

    @StringDef({AlbumType.VIDEO, AlbumType.IMAGE, AlbumType.IMAGE_AND_VIDEO, AlbumType.AUDIO, AlbumType.ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlbumType {
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

    private ActivityResultLauncher<Intent> albumLauncher;
    private ActivityResultLauncher<String> writeLauncher;
    private ActivityResultLauncher<String[]> permissionsLauncher;
    private int maxSelectCount;
    private OpenFileCompatListener openFileCompatListener;

    private @AlbumType String openType;

    /**
     * 应当在onCreate中创建
     *
     * @param activity
     */
    public OpenFileCompat(ComponentActivity activity) {
        this.albumLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackAlbum);
        this.writeLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.permissionsLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);
        }
    }

    /**
     * 应当在onCreate中创建
     *
     * @param fragment
     */
    public OpenFileCompat(Fragment fragment) {
        this.albumLauncher = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackAlbum);
        this.writeLauncher = fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.permissionsLauncher = fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);
        }
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
    public void open(@AlbumType String type, int count) {
        this.openType = type;
        this.maxSelectCount = Math.max(count, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (AlbumType.ALL.equals(openType)) {
                    this.permissionsLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_AUDIO});
                } else if (AlbumType.AUDIO.equals(openType)) {
                    this.writeLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
                } else {
                    //使用 Android 13 新特性 相册
                    Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                    if (!TextUtils.equals(type, AlbumType.IMAGE_AND_VIDEO)) {
                        intent.setType(type);
                    }
                    intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
                    this.albumLauncher.launch(intent);
                }
            } else {
                this.writeLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            return;
        }
        lastOpen();
    }

    /**
     * 主动释放
     */
    public void release() {
        writeLauncher = null;
        albumLauncher = null;
        permissionsLauncher = null;
        openFileCompatListener = null;
    }

    private void lastOpen() {
        Intent albumIntent = new Intent();
        albumIntent.setType(openType);
        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
        albumIntent.setAction(Intent.ACTION_GET_CONTENT);
        if (maxSelectCount > 1) {
            //启动多选
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        albumLauncher.launch(albumIntent);
    }

    private void onPermissionResult(boolean result) {
        if (result) {
            lastOpen();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void onPermissionResult(Map<String, Boolean> result) {
        Boolean isImage = result.get(Manifest.permission.READ_MEDIA_IMAGES);
        Boolean isVideo = result.get(Manifest.permission.READ_MEDIA_VIDEO);
        Boolean isAudio = result.get(Manifest.permission.READ_MEDIA_AUDIO);
        if (isImage != null && isImage && isVideo != null && isVideo && isAudio != null && isAudio) {
            lastOpen();
        }
    }

    /**
     * 选取图片视频回调
     *
     * @param result
     */
    private void callbackAlbum(ActivityResult result) {
        Intent intent = result.getData();
        if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
            if(result.getData().getData() != null){
                if (openFileCompatListener != null) {
                    openFileCompatListener.radioOpen(result.getData().getData());
                }
            }else if(result.getData().getClipData() != null){
                ClipData clipData = result.getData().getClipData();
                int lastCount = clipData.getItemCount();
                LinkedHashSet<Uri> resultSet = new LinkedHashSet<>();
                for (int i = 0; i < lastCount; i++) {
                    if (i >= maxSelectCount) {
                        break;
                    }
                    resultSet.add(clipData.getItemAt(i).getUri());
                }
                if (openFileCompatListener != null) {
                    openFileCompatListener.multipleOpen(new ArrayList<>(resultSet));
                }
            }
        }
    }


}
