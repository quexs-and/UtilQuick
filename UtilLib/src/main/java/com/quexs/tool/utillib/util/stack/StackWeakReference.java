package com.quexs.tool.utillib.util.stack;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class StackWeakReference extends WeakReference<Activity> {

    public StackWeakReference(Activity referent) {
        super(referent);
    }

    public StackWeakReference(Activity referent, ReferenceQueue<? super Activity> q) {
        super(referent, q);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (null == obj || getClass() != obj.getClass()) return false;
        WeakReference<Activity> wAct = (WeakReference<Activity>) obj;
        return get() == wAct.get();
    }
}
