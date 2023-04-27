package com.quexs.tool.utildemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;

import com.bumptech.glide.load.ImageHeaderParser;
import com.google.common.util.concurrent.ListenableFuture;
import com.quexs.tool.utildemo.databinding.ActivityUserCameraBinding;
import com.quexs.tool.utildemo.util.CameraConfig;
import com.quexs.tool.utildemo.view.CircleProgressButtonView;
import com.quexs.tool.utillib.compat.DirectoryCompat;
import com.quexs.tool.utillib.compat.ScreenCompat;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint({"RestrictedApi"})
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UserCameraActivity extends AppCompatActivity {

    private ActivityUserCameraBinding binding;

    private ActivityResultLauncher<String[]> cameraLauncher;
    private ActivityResultLauncher<Intent> settingLauncher;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private VideoCapture videoCapture;
    private ImageCapture imageCapture;
    private ActivityResultLauncher<Intent> videoLauncher;
    private boolean isFrontCamera;//是否前置摄像头
    private OrientationEventListener orientationEvent;

    private boolean isOnlyVideoCapture;//是否只有相机分析仪

    private int curRotation;

    private CameraConfig cameraConfig;

    private DirectoryCompat directoryCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        directoryCompat = new DirectoryCompat(this);
        cameraConfig = new CameraConfig(this);
        initOrientationListener();
        initRegister();
        initViewListener();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            cameraLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
        }else {
            cameraLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    private void initView(){
        ConstraintLayout.LayoutParams cl = (ConstraintLayout.LayoutParams) binding.imvClose.getLayoutParams();
        cl.topMargin = new ScreenCompat(this).getStatusBarHeight() + getResources().getDimensionPixelOffset(R.dimen.dp_16);
        binding.imvClose.setLayoutParams(cl);
    }

    /**
     * 初始化权限
     */
    private void initRegister() {
        //相机权限回调
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::startCameraForPermission);
        //拍摄回调
        videoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::videoLastResultCallBack);
        //设置界面回调
        settingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::checkPermission);
    }



    /**
     * 初始化监听
     */
    private void initViewListener() {
        binding.imvClose.setOnClickListener(view -> onBackPressed());
        //单击拍照
        binding.recordView.setOnClickListener(this::onClickTakePhoto);
        //长按视频录制
        binding.recordView.setOnLongClickListener(new CircleProgressButtonView.OnLongClickListener() {
            @Override
            public void onLongClick() {
                onLongTakeVideo();
            }

            @Override
            public void onNoMinRecord(int currentTime) {
                //未达到最小录制时长
            }

            @Override
            public void onRecordFinishedListener() {
                if (videoCapture != null) {
                    videoCapture.stopRecording();
                }
            }
        });
        //切换摄像头
        binding.imvSwitchCamera.setOnClickListener(this::onSwitchCamera);
    }

    /**
     * 随时持续更新设备旋转角度
     */
    private void initOrientationListener(){
        orientationEvent = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                if (i == ImageHeaderParser.UNKNOWN_ORIENTATION) {
                    return;
                }
                int rotation;
                if(i >= 45 && i < 135){
                    rotation = Surface.ROTATION_270;
                    curRotation = 90;
                }else if(i >= 135 && i < 225){
                    rotation = Surface.ROTATION_180;
                    curRotation = 180;
                }else if(i >= 225 && i < 315){
                    rotation = Surface.ROTATION_90;
                    curRotation = 270;
                }else {
                    rotation = Surface.ROTATION_0;
                    curRotation = 0;
                }
                if(imageCapture != null){
                    imageCapture.setTargetRotation(rotation);
                }
                if(videoCapture != null){
                    videoCapture.setTargetRotation(rotation);
                }
            }
        };
    }

    /**
     * 开始预览
     */
    private void startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                //1 定义图像预览接口
                Preview preview = new Preview.Builder().build();
                //2 拍照 接口
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//优化捕获速度，可能降低图片质量
                        .setTargetRotation(binding.recordView.getDisplay().getRotation())
                        .build();
                videoCapture = new VideoCapture.Builder()
                        .setTargetRotation(binding.recordView.getDisplay().getRotation())
                        .build();
                //3 图像分析接口;
                //4 绑定声明周期,把我们需要的这三个接口安装到相机管理器的主线路上，实现截取数据的目的
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                        .build();
                if(cameraConfig.isOnlySupportTwoUseCase()){
                    isOnlyVideoCapture = true;
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                }else {
                    try {
                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
                    }catch (Exception e){
                        isOnlyVideoCapture = true;
                        cameraConfig.setOnlySupportTwoUseCase();
                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                    }
                }
                //5 把相机信息高速预览窗口
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }





    @Override
    protected void onStart() {
        super.onStart();
        orientationEvent.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationEvent.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if(directoryCompat != null){
            directoryCompat.release();
        }
    }

    /**
     * 显示拍照图片(此处获取一帧Bitmap 保存的照片)
     * @param imagePath
     */
    private void showTakePhotoImageUIData(String imagePath){

    }

    /**
     * 录像结果回调
     * @param result
     */
    private void videoLastResultCallBack(ActivityResult result){
        if (result.getResultCode() != Activity.RESULT_OK) return;
        setResult(Activity.RESULT_OK, result.getData());
        finish();
    }

    /**
     * 图片结果回调
     * @param filePath
     */
    private void imageLastResultCallBack(String filePath){
        Intent intent = new Intent();
        intent.putExtra("code", 1);
        intent.putExtra("filePath", filePath);
        setResult(Activity.RESULT_OK, intent);
        onBackPressed();
    }


    /**
     * 检测权限
     * @param result
     */
    private void checkPermission(ActivityResult result){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            cameraLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
        }else {
            cameraLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    /**
     * 根据权限打开相机
     * @param result
     */
    private void startCameraForPermission(Map<String, Boolean> result){
        Boolean isCamera = result.get(Manifest.permission.CAMERA);
        Boolean isAudio = result.get(Manifest.permission.RECORD_AUDIO);
        Boolean isStorage = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isCamera != null && isAudio != null && isCamera && isAudio && (isStorage == null || isStorage)) {
            startCameraPreview();
        }else {
            //没有权限
//            Toast.makeText(this,"没有相机权限")
        }
    }

    /**
     * 拍照图片回调
     * @param outputFileResults
     */

    private void onSwitchConvTakePhoto(ImageCapture.OutputFileResults outputFileResults) {
        if(outputFileResults.getSavedUri() != null){

        }
    }

    /**
     * 拍照
     */
    private void onClickTakePhoto() {
        //设置要保存的路径和文件名字
        File photoFile = new File(directoryCompat.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image-" + System.currentTimeMillis() + ".jpg");
        if(isOnlyVideoCapture){
            //只支持视频时，需要通过获取 一帧bitmap 来保存拍照图片
            int rotation = curRotation;
            Bitmap bitmap = binding.viewFinder.getBitmap();
//            viewModel.saveBitmapToLocal(bitmap, rotation);
        }else {
            //2 定义 拍摄imageCapture实例
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(isFrontCamera);
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build();
            imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    onSwitchConvTakePhoto(outputFileResults);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {

                }
            });
        }
    }

    /**
     * 录视频
     */
    @SuppressLint({"MissingPermission"})
    private void onLongTakeVideo() {
        File file = new File(directoryCompat.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video-" + System.currentTimeMillis() + ".mp4");
        videoCapture.startRecording(new VideoCapture.OutputFileOptions.Builder(file).build(), executorService, new VideoCapture.OnVideoSavedCallback() {
            @Override
            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {

            }

            @Override
            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {

            }
        });
    }

    /**
     * 切换摄像头
     * @param view
     */
    private void onSwitchCamera(View view){
        isFrontCamera = !isFrontCamera;
        startCameraPreview();
    }


}