package com.quexs.tool.utillib.tool;

/**
 * 缩放工具
 */
public class ScaleTool {

    /**
     * 等比变更View的宽高
     * @param viewWidth
     * @param viewHeight
     * @param maxWidth
     * @param maxHeight
     * @param offsetWidth
     * @return
     */
    public static int[] mGetScaleViewWidth(int viewWidth, int viewHeight, int maxWidth, int maxHeight, int offsetWidth){
        int lastHeight;
        int lastWidth;
        if (viewHeight > viewWidth) {
            //判断图片高度更长
            if (viewHeight > maxHeight) {
                //图片高度超过最大高度
                lastHeight = maxHeight;
                //宽度等比缩放
                lastWidth = (int) (viewWidth * (1f * maxHeight / viewHeight));
            } else {
                lastHeight = viewHeight;
                lastWidth = viewWidth;
            }
            if (lastWidth > maxWidth) {
                lastHeight = (int) (lastHeight * (1f * maxWidth / lastWidth));
                lastWidth = maxWidth;
            }
        } else {
            //图片宽度更长
            if (viewWidth > maxWidth) {
                lastWidth = maxWidth;
                lastHeight = (int) (viewHeight * (1f * maxWidth / viewWidth));
            } else {
                lastHeight = viewHeight;
                lastWidth = viewWidth;
            }
            if (lastHeight > maxHeight) {
                lastWidth = (int) (lastWidth * (1f * maxHeight / lastHeight));
                lastHeight = maxHeight;
            }
        }
        if(offsetWidth != 0){
            int removeSubBgWidth = lastWidth + offsetWidth;
            lastHeight = (int) (lastHeight * (1f * removeSubBgWidth / lastWidth));
            lastWidth = removeSubBgWidth;
        }

        int[] wh = new int[2];
        wh[0] = lastWidth;
        wh[1] = lastHeight;
        return wh;
    }

    /**
     * 等比变更View的宽高 优先缩放宽高会等于最大宽高
     * @param viewWidth
     * @param viewHeight
     * @param maxHeight
     * @param maxHeight
     * @param offsetWidth
     * @return
     */
    public static int[] mGetScale2ViewWidth(int viewWidth, int viewHeight, int maxWidth, int maxHeight, int offsetWidth){
        int lastHeight;
        int lastWidth;
        if (viewHeight > viewWidth) {
            //判断图片高度更长
            if (viewHeight != maxHeight) {
                //图片高度超过最大高度
                lastHeight = maxHeight;
                //宽度等比缩放
                lastWidth = (int) (viewWidth * (1f * maxHeight / viewHeight));
            } else {
                lastHeight = viewHeight;
                lastWidth = viewWidth;
            }
            if (lastWidth > maxWidth) {
                lastHeight = (int) (lastHeight * (1f * maxWidth / lastWidth));
                lastWidth = maxWidth;
            }
        } else {
            //图片宽度更长
            if (viewWidth != maxWidth) {
                lastWidth = maxWidth;
                lastHeight = (int) (viewHeight * (1f * maxWidth / viewWidth));
            } else {
                lastHeight = viewHeight;
                lastWidth = viewWidth;
            }
            if (lastHeight > maxHeight) {
                lastWidth = (int) (lastWidth * (1f * maxHeight / lastHeight));
                lastHeight = maxHeight;
            }
        }
        if(offsetWidth != 0){
            int removeSubBgWidth = lastWidth + offsetWidth;
            lastHeight = (int) (lastHeight * (1f * removeSubBgWidth / lastWidth));
            lastWidth = removeSubBgWidth;
        }

        int[] wh = new int[2];
        wh[0] = lastWidth;
        wh[1] = lastHeight;
        return wh;
    }

}
