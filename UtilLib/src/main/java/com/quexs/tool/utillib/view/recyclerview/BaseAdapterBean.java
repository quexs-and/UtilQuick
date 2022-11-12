package com.quexs.tool.utillib.view.recyclerview;


public class BaseAdapterBean<T> {

    private T item;
    private int itemPosition;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }
}
