package com.quexs.tool.utillib.util.album;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringDef;
import androidx.fragment.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * 打开相册 选取视频图片
 */
public class OpenAlbumCompat {

    @StringDef({AlbumType.VIDEO, AlbumType.IMAGE, AlbumType.ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlbumType {
        String VIDEO = "video/*";
        String IMAGE = "image/*";
        String ALL = "*/*";
    }

    private ActivityResultLauncher<Intent> albumLauncher;
    private int maxSelectCount;
    private OpenAlbumCompatListener openAlbumCompatListener;

    /**
     * 应当在onCreate中创建
     *
     * @param activity
     */
    public OpenAlbumCompat(ComponentActivity activity) {
        this.albumLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackAlbum);
    }

    /**
     * 应当在onCreate中创建
     *
     * @param fragment
     */
    public OpenAlbumCompat(Fragment fragment) {
        this.albumLauncher = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::callbackAlbum);
    }

    /**
     * 打开相册
     *
     * @param count
     */
    public void open(@AlbumType String type, int count) {
        this.maxSelectCount = Math.max(count, 1);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android 13 新特性
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            if (!TextUtils.equals(type, AlbumType.ALL)) {
                intent.setType(type);
            }
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
            albumLauncher.launch(intent);
            return;
        }
        Intent albumIntent = new Intent();
        albumIntent.addCategory(Intent.CATEGORY_OPENABLE);
        switch (type) {
            case AlbumType.IMAGE:
                albumIntent.setType("image/*");
                break;
            case AlbumType.VIDEO:
                albumIntent.setType("video/*");
                break;
            case AlbumType.ALL:
                albumIntent.setType("*/*");
                albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                break;
        }
        albumIntent.setAction(Intent.ACTION_GET_CONTENT);
        if (maxSelectCount > 1) {
            //启动多选
            albumIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        albumLauncher.launch(albumIntent);
    }

    /**
     * 相册打开监听
     *
     * @param openAlbumCompatListener
     */
    public void setOpenAlbumCompatListener(OpenAlbumCompatListener openAlbumCompatListener) {
        this.openAlbumCompatListener = openAlbumCompatListener;
    }

    /**
     * 主动释放
     */
    public void release() {
        albumLauncher = null;
        openAlbumCompatListener = null;
    }

    /**
     * 选取图片视频回调
     *
     * @param result
     */
    private void callbackAlbum(ActivityResult result) {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            if (maxSelectCount > 1) {
                ClipData clipData = result.getData().getClipData();
                int lastCount = clipData.getItemCount();
                LinkedHashSet<Uri> resultSet = new LinkedHashSet<>();
                for (int i = 0; i < lastCount; i++) {
                    if (i >= maxSelectCount) {
                        break;
                    }
                    resultSet.add(clipData.getItemAt(i).getUri());
                }
                if (openAlbumCompatListener != null) {
                    openAlbumCompatListener.multipleOpen(new ArrayList<>(resultSet));
                }
            } else {
                Uri uri = result.getData().getData();
                if (openAlbumCompatListener != null) {
                    openAlbumCompatListener.radioOpen(uri);
                }
            }
        }
    }


}
