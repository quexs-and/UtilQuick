package com.quexs.tool.utillib.base;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BasisFragment extends Fragment {

    protected BasisActivityListener basisActivityListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BasisActivityListener) {
            basisActivityListener = (BasisActivityListener) context;
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
