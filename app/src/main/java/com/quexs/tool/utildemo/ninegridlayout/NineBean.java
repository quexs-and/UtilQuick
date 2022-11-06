package com.quexs.tool.utildemo.ninegridlayout;

public class NineBean {
    //类型 1-图片 2-视频
    private int type;
    //链接
    private String url;
    //缩略图
    private String thumbnail;

    //图片、视频宽度
    private int width;
    //图片、视频 高度
    private int height;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
