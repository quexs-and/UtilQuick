package com.quexs.tool.utillib.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import com.quexs.tool.utillib.R;

import java.util.Calendar;

/**
 * View 转换 工具
 */
public class ViewConvert {

    public ViewConvert(){

    }

    /**
     * 防重复点击 默认1000毫秒
     * @param view
     * @return
     */
    public boolean isEffectiveClick(View view){
        return isEffectiveClick(view,1000);
    }

    /**
     * 是否有效点击(防误触)
     * @param view
     * @param intervalMillisecond 最小间隔时间-毫秒
     * @return
     */
    public boolean isEffectiveClick(View view, long intervalMillisecond){
        long curTimeMillis = Calendar.getInstance().getTimeInMillis();
        Object delayTag = view.getTag(R.string.view_key_enable_delay);
        if(delayTag == null){
            view.setTag(R.string.view_key_enable_delay, curTimeMillis);
            return true;
        }
        long upTimeMills = (long) delayTag;
        if(curTimeMillis - upTimeMills >= intervalMillisecond){
            view.setTag(R.string.view_key_enable_delay, curTimeMillis);
            return true;
        }
        return false;
    }

    /**
     * View隐藏时获取View的宽高
     * @param view
     * @return
     */
    public int[] mGetViewWh(View view) {
        int[] wh = new int[2];
        int width = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        wh[0] = view.getMeasuredWidth();
        wh[1] = view.getMeasuredHeight();
        return wh;
    }

    /**
     * View转Bitmap
     * @param view
     * @return
     */
    public Bitmap getBitmap(View view){
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        return bmp;
    }

    /**
     * View转Bitmap
     * @param view
     * @param scaleRatio 缩放比例
     * @return
     */
    public Bitmap getBitmap(View view, float scaleRatio){
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        if(scaleRatio != 1){
            Bitmap scaleBitmap = Bitmap.createScaledBitmap(bmp, (int) (view.getWidth() * scaleRatio), (int) (view.getHeight() * scaleRatio), true);
            bmp.recycle();
            return scaleBitmap;
        }
        return bmp;
    }

    /**
     * View转Drawable
     * @param view
     * @return
     */
    public BitmapDrawable getDrawable(View view){
        return new BitmapDrawable(view.getResources(), getBitmap(view));
    }

    /**
     *
     * @param view
     * @param radio
     * @return
     */
    /**
     * View转Drawable 并高斯模糊
     * @param view
     * @param scaleRatio 缩放 通过缩放可以得到更好的毛玻璃效果
     * @param radio 模糊度 0 < radio <= 25
     * @return
     */
    public BitmapDrawable getBlurDrawable(View view, float scaleRatio, float radio){
        Bitmap bmp = getBitmap(view, scaleRatio);
        //创建RenderScript内核对象
        RenderScript renderScript = RenderScript.create(view.getContext());
        //创建Allocation
        Allocation input = Allocation.createFromBitmap(renderScript, bmp,  Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(renderScript, input.getType());
        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        //设置blurScript对象的输入内存
        intrinsicBlur.setInput(input);
        //设置渲染的模糊程度, 25f是最大模糊度
        intrinsicBlur.setRadius(radio);
        //将输出数据保存到输出内存中
        intrinsicBlur.forEach(output);
        //将数据填充到Allocation中
        output.copyTo(bmp);
        renderScript.destroy();
        return new BitmapDrawable(view.getResources(), bmp);
    }




}
