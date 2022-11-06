package com.quexs.tool.utillib.base;


import android.content.Context;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BasisDialog extends DialogFragment {

    protected BasisActivityListener basisActivityListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BasisActivityListener) {
            basisActivityListener = (BasisActivityListener) context;
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (isShowing()) return;
        if (isAdded()) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.show(this);
            ft.commit();
            return;
        }
        super.show(manager, tag);
    }

    /**
     * 是否正在显示 dialog
     *
     * @return
     */
    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            onStart(window);
        }
    }

    public void onStart(Window window) {

    }

    @Override
    public void dismiss() {
        if (isResumed()) {
            super.dismiss();
        } else {
            dismissAllowingStateLoss();
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideInputMethod() {
        if (basisActivityListener != null) {
            basisActivityListener.hideInputMethod();
        }
    }

    /**
     * 显示软键盘
     *
     * @param editText
     */
    public void showInputMethod(EditText editText) {
        if (basisActivityListener != null) {
            basisActivityListener.showInputMethod(editText);
        }
    }

}
