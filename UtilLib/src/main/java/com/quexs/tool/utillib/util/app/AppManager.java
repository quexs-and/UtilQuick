package com.quexs.tool.utillib.util.app;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * App Activity 堆栈管理
 */
public class AppManager {

    //Activity 堆栈 弱引用
    private final Stack<StackWeakReference> actStack = new Stack<>();

    //activity 总数
    private int activityCount = 0;

    public AppManager(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                putActivity(activity, savedInstanceState != null);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                activityCount++;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                activityCount--;
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                actStack.remove(new StackWeakReference(activity));
            }
        });
    }

    /**
     * app是否处于前台
     *
     * @return
     */
    public boolean isInForeground() {
        return activityCount > 0;
    }

    /**
     * 关闭指定类名的Activity
     *
     * @param cls
     */
    public void finishActivity(Class<?> cls) {
        List<Activity> list = getActivity(cls);
        for (Activity activity : list) {
            if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 获取指定类名的Activity
     *
     * @param cls
     * @return
     */
    public List<Activity> getActivity(Class<?> cls) {
        Iterator<StackWeakReference> it = actStack.iterator();
        List<Activity> list = new ArrayList<>();
        while (it.hasNext()) {
            StackWeakReference reference = it.next();
            Activity activity = reference.get();
            if (activity == null) {
                it.remove();
                continue;
            }
            if (!TextUtils.equals(activity.getClass().getName(), cls.getName())) continue;
            list.add(activity);
        }
        return list;
    }

    /**
     * Activity 入栈
     *
     * @param activity
     * @param isMemoryRestart
     */
    private void putActivity(Activity activity, boolean isMemoryRestart) {
        if (isMemoryRestart) {
            //内存重启,代表前面有一个Activity对象被回收是空值,需要移除
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actStack.removeIf(new Predicate<StackWeakReference>() {
                    @Override
                    public boolean test(StackWeakReference stackWeakReference) {
                        Activity removeActivity = stackWeakReference.get();
                        return removeActivity == null || removeActivity.isDestroyed();
                    }
                });
            } else {
                Iterator<StackWeakReference> it = actStack.iterator();
                while (it.hasNext()) {
                    StackWeakReference reference = it.next();
                    Activity removeActivity = reference.get();
                    if (removeActivity == null || removeActivity.isDestroyed()) {
                        it.remove();
                    }
                }
            }
        }
        actStack.add(new StackWeakReference(activity));
    }


}
