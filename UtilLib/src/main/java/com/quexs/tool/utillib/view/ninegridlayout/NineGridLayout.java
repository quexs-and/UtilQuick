package com.quexs.tool.utillib.view.ninegridlayout;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * 九宫格布局view
 * @param <T>
 */
public abstract class NineGridLayout<T> extends ViewGroup {

    private static final float DEFUALT_SPACING = 3f;
    private static final int MAX_COUNT = 9;
    private float mSpacing = DEFUALT_SPACING;
    private int mColumns;
    private int mRows;
    private int mTotalWidth;

    private boolean mIsShowAll = false;
    private boolean mIsFirst = true;
    private NineGridLayoutListener nineGridLayoutListener;

    private final ArrayList<T> imageList = new ArrayList<>();


    public NineGridLayout(Context context) {
        this(context,null);
    }

    public NineGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NineGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NineGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        mTotalWidth = right - left;
        if (mIsFirst) {
            notifyDataSetChanged();
            mIsFirst = false;
        }
    }

    /**
     * 设置监听
     * @param nineGridLayoutListener
     */
    public void setNineGridLayoutListener(NineGridLayoutListener nineGridLayoutListener) {
        this.nineGridLayoutListener = nineGridLayoutListener;
    }

    /**
     * 设置间隔
     *
     * @param spacing
     */
    public void setSpacing(float spacing) {
        mSpacing = spacing;
    }

    /**
     * 设置是否显示所有图片（超过最大数时）
     *
     * @param isShowAll
     */
    public void setIsShowAll(boolean isShowAll) {
        mIsShowAll = isShowAll;
    }

    public void setImageList(List<T> list) {
        if (getListSize(list) == 0) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        imageList.clear();
        imageList.addAll(list);
        if (!mIsFirst) {
            notifyDataSetChanged();
        }
    }

    public void notifyDataSetChanged() {
        post(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    private void refresh() {
        removeAllViews();
        int size = getListSize(imageList);
        setVisibility(size == 0 ? View.GONE :View.VISIBLE);
        if (size == 1) {
            T data = imageList.get(0);
            RatioImageView imageView = createImageView(0, data);
            int[] imageWh = displayOneImageHeight(data, mTotalWidth);
            //避免在ListView中一张图未加载成功时，布局高度受其他item影响
            setLayoutParamsHeight(imageWh[1],true);
            lastLayoutImageView(imageView, 0, 0, imageWh[0], imageWh[1], data);
            return;
        }
        generateChildrenLayout(size);
        int imageWidth = mColumns == 1 ? mTotalWidth : (int) ((mTotalWidth - (mSpacing * (mColumns - 1))) / mColumns);
        int layoutHeight = (int) (imageWidth * mRows + mSpacing * (mRows - 1));
        setLayoutParamsHeight(layoutHeight,true);
        for (int i = 0; i < size; i++) {
            T data = imageList.get(i);
            RatioImageView imageView;
            if (!mIsShowAll) {
                if (i < MAX_COUNT - 1) {
                    imageView = createImageView(i, data);
                    layoutImageView(imageView, i, imageWidth,data);
                } else { //第9张时
                    if (size <= MAX_COUNT) {//刚好第9张
                        imageView = createImageView(i, data);
                        layoutImageView(imageView, i, imageWidth,data);
                    } else {//超过9张
                        imageView = createImageView(i, data);
                        layoutImageView(imageView, i, imageWidth,data);
                        break;
                    }
                }
            } else {
                imageView = createImageView(i, data);
                layoutImageView(imageView, i, imageWidth, data);
            }
        }
    }

    /**
     * 设置布局高度
     * 为了避免下拉时出现卡顿现象
     * item应当监听并视图创建时生成的视图高度，并在适配器中将记录的高度赋值给视图
     * @param layoutHeight
     */
    public void setLayoutParamsHeight(int layoutHeight, boolean isRecord) {
        if(layoutHeight == 0) return;
        //根据子view数量确定高度
        LayoutParams params = getLayoutParams();
        params.height = layoutHeight;
        setLayoutParams(params);
        if(isRecord && nineGridLayoutListener != null){
            nineGridLayoutListener.onLastGridLayoutHeight(layoutHeight);
        }
    }

    private RatioImageView createImageView(final int i, final T obj) {
        RatioImageView imageView = new RatioImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnClickListener(v -> onClickImage((RatioImageView)v,i, obj));
        return imageView;
    }

    /**
     * @param imageView
     * @param data
     */
    private void layoutImageView(RatioImageView imageView, int i, int imageWidth, T data) {
        int imageHeight = imageWidth;
        int[] position = findPosition(i);
        int left = (int) ((imageWidth + mSpacing) * position[1]);
        int top = (int) ((imageHeight + mSpacing) * position[0]);
        int right = left + imageWidth;
        int bottom = top + imageHeight;
        lastLayoutImageView(imageView, left,top, right,bottom,data);
    }

    protected void lastLayoutImageView(RatioImageView imageView, int left, int top, int right, int bottom, T data){
        imageView.layout(left, top, right, bottom);
        addView(imageView);
        displayImage(imageView, data);
    }

    private int[] findPosition(int childNum) {
        int[] position = new int[2];
        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mColumns; j++) {
                if ((i * mColumns + j) == childNum) {
                    position[0] = i;//行
                    position[1] = j;//列
                    break;
                }
            }
        }
        return position;
    }

    /**
     * 根据图片个数确定行列数量
     *
     * @param length
     */
    private void generateChildrenLayout(int length) {
        if (length <= 3) {
            mRows = 1;
            mColumns = length;
        } else if (length <= 6) {
            mRows = 2;
            mColumns = 3;
            if (length == 4) {
                mColumns = 2;
            }
        } else {
            mColumns = 3;
            if (mIsShowAll) {
                mRows = length / 3;
                int b = length % 3;
                if (b > 0) {
                    mRows++;
                }
            } else {
                mRows = 3;
            }
        }
    }


    private int getListSize(List<T> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * @param parentWidth 父控件宽度
     * @return true 代表按照九宫格默认大小显示，false 代表按照自定义宽高显示
     */
    protected abstract int[] displayOneImageHeight( T data, int parentWidth);

    protected abstract void displayImage(RatioImageView imageView, T data);

    protected abstract void onClickImage(RatioImageView imageView,int position, T data);


    public interface NineGridLayoutListener{
        void onLastGridLayoutHeight(int layoutHeight);
    }

}
