package com.quexs.tool.utillib.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.quexs.tool.utillib.compat.ScreenCompat;

/**
 * 加入了视图宽高改变监听的 ConstraintLayout
 */
public class GlobalConstraintLayout extends ConstraintLayout
        implements ViewTreeObserver.OnGlobalLayoutListener{

    private int mScreenHeight = 0;//屏幕高度
    private GlobalLayoutChangeListener globalLayoutChangeListener;
    private ScreenCompat screenTool;
    private Rect globalLayoutRect;

    public GlobalConstraintLayout(@NonNull Context context) {
        super(context);
        initUI(context);
    }

    public GlobalConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    public GlobalConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI(context);
    }

    public GlobalConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUI(context);
    }


    private void initUI(Context context){
        screenTool = new ScreenCompat((Activity) context);
        globalLayoutRect = new Rect();
    }

    public int getScreenHeight() {
        if (mScreenHeight <= 0) {
            mScreenHeight = screenTool.getHeight();
        }
        return mScreenHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //添加视图变化监听
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //移除视图变化监听
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // 获取当前页面窗口的显示范围
        getWindowVisibleDisplayFrame(globalLayoutRect);
        int screenHeight = getScreenHeight();
        // 输入法的高度
        int keyboardHeight = screenHeight - globalLayoutRect.bottom;
        if(globalLayoutChangeListener != null){
            globalLayoutChangeListener.onGlobalLayoutChange(keyboardHeight,screenHeight);
        }
    }

    /**
     * 视图可视区域变化监听（可用于监听软键盘是否弹出）
     * @param globalLayoutChangeListener
     */
    public void setGlobalLayoutChangeListener(GlobalLayoutChangeListener globalLayoutChangeListener){
        this.globalLayoutChangeListener = globalLayoutChangeListener;
    }

    public interface GlobalLayoutChangeListener{
        /**
         *
         * @param keyboardHeight 软键盘高度
         * @param screenHeight 屏幕高度
         */
        void onGlobalLayoutChange(int keyboardHeight, int screenHeight);
    }

}
