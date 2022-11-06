package com.quexs.tool.utillib.view.ninegridlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 可设置宽高比的ImageView
 * 并添加了按下时有变暗效果
 */
public class RatioImageView extends AppCompatImageView {

    /**
     * 宽高比例
     */
    private float mRatio = 0f;

    public RatioImageView(@NonNull Context context) {
        this(context,null);
    }

    public RatioImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置ImageView的宽高比
     *
     * @param ratio
     */
    public void setRatio(float ratio) {
        mRatio = ratio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio != 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            float height = width / mRatio;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Drawable drawable = getDrawable();
                if (drawable != null) {
                    drawable.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Drawable drawableUp = getDrawable();
                if (drawableUp != null) {
                    drawableUp.mutate().clearColorFilter();
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
