package com.quexs.tool.utillib.base;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity 基类
 */
public class BasisActivity extends AppCompatActivity implements BasisActivityListener {

    //是否释放资源
    private boolean isReleased;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isReleased", isReleased);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isReleased = savedInstanceState.getBoolean("isReleased", false);
    }

    @Override
    public void finish() {
        release();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            release();
        }
    }

    /**
     * 真实数据回收
     */
    private void release() {
        if (isReleased) {
            return;
        }
        isReleased = true;
        onRelease();
    }

    /**
     * 释放资源
     */
    public void onRelease() {

    }

    @Override
    public void hideInputMethod() {
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public void showInputMethod(EditText editText) {
        //显示软键盘
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

}
