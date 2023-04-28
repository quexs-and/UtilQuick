package com.quexs.tool.utildemo;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.quexs.tool.utildemo.databinding.ActivityMainBinding;
import com.quexs.tool.utillib.compat.album.OpenFileCompat;
import com.quexs.tool.utillib.compat.album.OpenFileCompatListener;
import com.quexs.tool.utillib.compat.UriConvertCompat;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OpenFileCompat openFileCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initOpenAlbum();
        initViewListener();
    }

    private void initOpenAlbum(){
        openFileCompat = new OpenFileCompat(this);
        openFileCompat.setOpenAlbumCompatListener(new OpenFileCompatListener() {

            @Override
            public void openFilePath(List<String> paths) {
                showRadioAlbum(paths);
            }

        });
    }

    private void initViewListener(){
        binding.btnAlbum.setOnClickListener(this::openAlbum);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        openFileCompat.release();
        openFileCompat = null;
    }

    /**
     * 打开相册
     * @param v
     */
    private void openAlbum(View v){
        if(!MyUtilTool.getInstance().getViewConvert().isEffectiveClick(v)) return;
        openFileCompat.open(OpenFileCompat.FileType.ALL, 2);
    }

    /**
     * 显示单选图片
     * @param filePaths
     */
    private void showRadioAlbum(List<String> filePaths){
        String filePath = filePaths.get(0);
        Log.d("file", "filePath=" + filePath);
        File file = new File(filePath);
        Log.d("file", "fileExist=" + file.exists());

//        Glide.with(binding.imvPicture)
//                .load(uri)
//                .into(binding.imvPicture);

    }

}