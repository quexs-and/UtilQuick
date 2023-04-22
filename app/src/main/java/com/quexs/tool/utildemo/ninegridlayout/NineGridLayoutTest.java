package com.quexs.tool.utildemo.ninegridlayout;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.quexs.tool.utildemo.R;
import com.quexs.tool.utillib.tool.ScaleTool;
import com.quexs.tool.utillib.compat.ScreenCompat;
import com.quexs.tool.utillib.view.ninegridlayout.NineGridLayout;
import com.quexs.tool.utillib.view.ninegridlayout.RatioImageView;

/**
 * 九宫格测试
 */
public class NineGridLayoutTest extends NineGridLayout<NineBean> {

    //图片最大高度
    private int maxImageHeight;
    //视频中间点击按钮最大宽度
    private int gysWidth;

    public NineGridLayoutTest(Context context) {
        this(context,null);
    }

    public NineGridLayoutTest(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NineGridLayoutTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NineGridLayoutTest(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        ScreenCompat screenTool = new ScreenCompat((Activity) context);
        setSpacing(context.getResources().getDimensionPixelOffset(R.dimen.dp_4));
        int screenWidth = screenTool.getWidth();
        maxImageHeight = (int)(screenWidth * 3f / 4);
        gysWidth = context.getResources().getDimensionPixelOffset(R.dimen.dp_40);
    }

    @Override
    protected void lastLayoutImageView(RatioImageView imageView, int left, int top, int right, int bottom, NineBean data) {
        super.lastLayoutImageView(imageView, left, top, right, bottom, data);
        if(data.getType() == 2){
            //视频 需要在图片上面盖一张 视频开关按钮
            ImageView imvGys = new ImageView(getContext());
            imvGys.setImageResource(R.drawable.ic_video_player);
            int imvGysLeft = left + (right - left - gysWidth) / 2;
            int imvGysTop = top + (bottom - top - gysWidth) / 2;
            int imvGysRight = imvGysLeft + gysWidth;
            int imvGysBottom = imvGysTop + gysWidth;
            imvGys.layout(imvGysLeft, imvGysTop, imvGysRight, imvGysBottom);
            addView(imvGys);
        }
    }

    /**
     * 单张图片显示 不定宽高
     * @param data
     * @param parentWidth 父控件宽度
     * @return
     */
    @Override
    protected int[] displayOneImageHeight(NineBean data, int parentWidth) {
        int maxHeight = Math.min(maxImageHeight, parentWidth);
        return ScaleTool.mGetScale2ViewWidth(data.getWidth(), data.getHeight(), parentWidth, maxHeight, 0);
    }

    @Override
    protected void displayImage(RatioImageView imageView, NineBean data) {
        if(data.getType() == 1){
            //图片
            Glide.with(imageView)
                    .asDrawable()
                    .load(data.getThumbnail())
                    .error(Glide.with(imageView).asDrawable().load(data.getUrl()))
                    .into(imageView);
        }else {
            //视频
            Glide.with(imageView)
                    .asDrawable()
                    .load(data.getThumbnail())
                    .into(imageView);

        }


    }

    @Override
    protected void onClickImage(RatioImageView imageView, int position, NineBean data) {
        //点击事件
    }
}
