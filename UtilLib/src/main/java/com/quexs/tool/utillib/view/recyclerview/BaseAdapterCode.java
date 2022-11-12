package com.quexs.tool.utillib.view.recyclerview;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BaseAdapterCode {
    @IntDef({
            Code.ADD_ITEM,
            Code.ADD_ITEM_LIST,
            Code.REMOVE_ITEM,
            Code.REMOVE_ITEM_LIST,
            Code.CHANGE_ITEM,
            Code.CHANGE_ITEM_LIST,
            Code.REPLACE_ITEM_LIST,
            Code.INSERT_ITEM,
            Code.INSERT_ITEM_LIST
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface Code {
        int ADD_ITEM = 1;
        int ADD_ITEM_LIST = 2;
        int REMOVE_ITEM = 3;
        int REMOVE_ITEM_LIST = 4;
        int CHANGE_ITEM = 5;
        int CHANGE_ITEM_LIST = 6;
        int REPLACE_ITEM_LIST = 7;
        int INSERT_ITEM = 8;
        int INSERT_ITEM_LIST = 9;
    }

}
