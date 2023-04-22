package com.quexs.tool.utillib.compat;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowMetrics;
import androidx.annotation.RequiresApi;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 屏幕工具
 */
public class ScreenCompat {
    private final Activity activity;

    public ScreenCompat(Activity activity){
        this.activity = activity;
    }

    /**
     * 获取显示的屏幕宽高
     * @return
     */
    public int[] getWh() {
        int[] wh = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android 12 强制通过WindowMetrics类获取屏幕宽高
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            wh[0] = windowMetrics.getBounds().width();
            wh[1] = windowMetrics.getBounds().height() - getRealNavigationBarHeight();
        } else {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            wh[0] = displayMetrics.widthPixels;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                wh[1] = displayMetrics.heightPixels - getRealNavigationBarHeight();
            }else {
                wh[1] = displayMetrics.heightPixels;
            }
        }
        return wh;
    }

    /**
     * 获取显示的屏幕高
     *
     * @return
     */
    public int getHeight() {
        return getWh()[1];
    }

    /**
     * 获取显示的屏幕高（不包含虚拟按键部分的高）
     *
     * @return
     */
    public int getWidth() {
        return getWh()[0];
    }

    /**
     * 非全面屏下-获取虚拟按键的高度
     *
     * @param context
     * @return
     */
    public int getNavigationBarHeight(Context context) {
        int result = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取真实导航栏高度
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int getRealNavigationBarHeight(){
        View view = activity.findViewById(android.R.id.navigationBarBackground);
        if(view == null || view.getVisibility() != View.VISIBLE) return 0;
        return getNavigationBarHeight(activity);
    }

    /**
     * 获取状态栏高度
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 屏幕尺寸size
     *
     * @return
     */
    public double getSize() {
        DisplayMetrics dm = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        display.getRealMetrics(dm);
        int realWidth = point.x;//真实屏幕宽度
        int realHeight = point.y;//真实屏幕高度
        double size = Math.sqrt((realWidth / dm.xdpi) * (realWidth / dm.xdpi) + (realHeight / dm.ydpi) * (realHeight / dm.ydpi));
        return new BigDecimal(size).setScale(2, RoundingMode.DOWN).stripTrailingZeros().doubleValue();
    }

}
