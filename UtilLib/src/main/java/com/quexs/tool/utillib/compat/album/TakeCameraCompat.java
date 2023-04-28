package com.quexs.tool.utillib.compat.album;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Calendar;

/**
 * 调用系统相机拍照
 */
public class TakeCameraCompat {
    private ActivityResultLauncher<Object> takeCameraLauncher;
    private ActivityResultLauncher<String> cameraLauncher;

    public TakeCameraCompat(ComponentActivity activity,ActivityResultCallback<Uri> callback){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            cameraLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onCameraPermission);
        }
        takeCameraLauncher = activity.registerForActivityResult(new ActivityResultContract<Object, Uri>() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                return onCreateIntent(context,o);
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                return onParseResult(i, intent);
            }
        }, callback);
    }

    public TakeCameraCompat(Fragment fragment, ActivityResultCallback<Uri> callback){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            cameraLauncher = fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onCameraPermission);
        }
        takeCameraLauncher = fragment.registerForActivityResult(new ActivityResultContract<Object, Uri>() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                return onCreateIntent(context,o);
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                return onParseResult(i, intent);
            }
        }, callback);
    }

    public void takeCamera(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            cameraLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        takeCameraLauncher.launch(null);
    }

    public void release(){
        if(takeCameraLauncher != null){
            takeCameraLauncher.unregister();
            takeCameraLauncher = null;
        }
        if(cameraLauncher != null){
            cameraLauncher.unregister();
            cameraLauncher = null;
        }
    }

    private void onCameraPermission(Boolean result){
        if(result){
            takeCameraLauncher.launch(null);
        }
    }

    private Intent onCreateIntent(Context context, Object o){
        String mineType = "image/jepg";
        String fileName = "_" + Calendar.getInstance().getTimeInMillis() + ".jpg";
        Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            uri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }else {
            String authorities = context.getApplicationContext().getPackageName();
            uri = FileProvider.getUriForFile(context,authorities, new File(context.getExternalCacheDir().getAbsoluteFile(), fileName));
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }

    private Uri onParseResult(int i, Intent intent){
        if(i == Activity.RESULT_OK){
            return intent.getData();
        }
        return null;
    }

}
