package com.quexs.tool.utildemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quexs.tool.utildemo.databinding.ActivityMainBinding;
import com.quexs.tool.utillib.compat.album.GetSharedFileCompat;
import com.quexs.tool.utillib.compat.album.GetSharedFileCompatListener;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GetSharedFileCompat getSharedFileCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initOpenAlbum();
        initViewListener();
    }

    private void initOpenAlbum(){
        getSharedFileCompat = new GetSharedFileCompat(this);
        getSharedFileCompat.setOpenAlbumCompatListener(new GetSharedFileCompatListener() {

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
        getSharedFileCompat.release();
        getSharedFileCompat = null;
    }

    /**
     * 打开相册
     * @param v
     */
    private void openAlbum(View v){
        if(!MyUtilTool.getInstance().getViewConvert().isEffectiveClick(v)) return;
        getSharedFileCompat.open(GetSharedFileCompat.FileType.ALL, 2);
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