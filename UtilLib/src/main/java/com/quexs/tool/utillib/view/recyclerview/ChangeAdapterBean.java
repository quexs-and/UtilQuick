package com.quexs.tool.utillib.view.recyclerview;

public class ChangeAdapterBean<T> {
    private T item;
    private Object key;

    public ChangeAdapterBean(T item, Object key) {
        this.item = item;
        this.key = key;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }
}
