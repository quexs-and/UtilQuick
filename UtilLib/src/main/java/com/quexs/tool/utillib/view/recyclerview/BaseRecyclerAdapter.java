package com.quexs.tool.utillib.view.recyclerview;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建RecyclerViewAdapter 专用于高频次刷新、频繁变动的 RecyclerView 的适配器
 *
 * @param <T>
 * @param <VH>
 */
public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public abstract VH mCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void mBindViewHolder(@NonNull VH holder, int position);

    /**
     * item内容变更
     *
     * @param adapterBean
     * @param t
     */
    public abstract void mChangeItem(BaseAdapterBean<T> adapterBean, T t);

    /**
     * 替换指定位置Item
     *
     * @param position
     * @param t
     */
    public void mReplaceItem(int position, T t) {
        items.set(position, t);
    }

    /**
     * 刷新通知 添加单条新数据源
     *
     * @param originCount
     * @param lastCount
     * @param t
     */
    public void notifyAddItem(int originCount, int lastCount, T t) {
        if (isEnableAddItemReplace) {
            if (lastCount == 0) {
                if (originCount > 0) {
                    notifyItemRangeRemoved(0, originCount);
                    items.clear();
                }
            } else {
                if (originCount == 0) {
                    items.add(t);
                    notifyItemInserted(0);
                } else {
                    if (originCount > 1) {
                        Iterator<T> it = items.iterator();
                        int position = 0;
                        while (it.hasNext()) {
                            it.next();
                            if (position > 0) {
                                it.remove();
                            }
                            position++;
                        }
                        notifyItemRangeRemoved(1, originCount - 1);
                    }
                    notifyItemChanged(0);
                }
            }
        } else {
            if (originCount > 0) {
                notifyItemRangeRemoved(0, originCount);
                items.clear();
            }
            if (lastCount > 0) {
                items.add(t);
                notifyItemInserted(0);
            }
        }
    }

    /**
     * 刷新通知 添加多条新数据源
     *
     * @param originCount
     * @param lastCount
     * @param list
     */
    public void notifyAddItems(int originCount, int lastCount, List<T> list) {
        if (isEnableAddItemReplace) {
            if (lastCount == 0) {
                if (originCount > 0) {
                    notifyItemRangeRemoved(0, originCount);
                    items.clear();
                }
            } else {
                if (originCount == 0) {
                    if (lastCount > 0) {
                        items.addAll(list);
                        notifyItemRangeInserted(0, lastCount);
                    }
                } else {
                    int overCount = lastCount - originCount;
                    if (overCount < 0) {
                        notifyItemRangeRemoved(lastCount, Math.abs(overCount));
                    }
                    int replaceCount = Math.min(originCount, lastCount);
                    if (replaceCount > 0) {
                        notifyItemRangeChanged(0, replaceCount);
                    }
                    if (overCount > 0) {
                        items.addAll(list.subList(originCount, lastCount));
                        notifyItemRangeInserted(originCount, overCount);
                    }
                }
            }
        } else {
            if (originCount > 0) {
                notifyItemRangeRemoved(0, originCount);
                items.clear();
            }
            if (lastCount > 0) {
                items.addAll(list);
                notifyItemRangeInserted(0, lastCount);
            }
        }
    }

    /**
     * 改变单条-通知刷新
     *
     * @param itemCount
     * @param adapterBean
     */
    public void notifyChangeItem(int itemCount, BaseAdapterBean<T> adapterBean) {
        notifyItemChanged(adapterBean.getItemPosition());
    }

    /**
     * 改变多条-通知刷新
     *
     * @param itemCount
     * @param list
     */
    public void notifyChangeItems(int itemCount, List<BaseAdapterBean<T>> list) {
        for (BaseAdapterBean<T> adapterBean : list) {
            notifyItemChanged(adapterBean.getItemPosition());
        }
    }

    /**
     * 移除单条
     *
     * @param originCount
     * @param lastCount
     * @param adapterBean
     */
    public void notifyRemoveItem(int originCount, int lastCount, BaseAdapterBean<T> adapterBean) {
        notifyItemRemoved(adapterBean.getItemPosition());
        items.remove(adapterBean.getItem());
        int changeCount = lastCount - adapterBean.getItemPosition();
        if (changeCount > 0) {
            notifyItemRangeChanged(adapterBean.getItemPosition(), changeCount);
        }
    }

    /**
     * 移除多条
     *
     * @param originCount
     * @param lastCount
     * @param list
     * @param minRemovePosition 最小移除位置
     */
    public void notifyRemoveItems(int originCount, int lastCount, List<BaseAdapterBean<T>> list, int minRemovePosition) {
        for (BaseAdapterBean<T> adapterBean : list) {
            notifyItemRemoved(adapterBean.getItemPosition());
            items.remove(adapterBean.getItem());
        }
        int changeCount = lastCount - minRemovePosition;
        if (changeCount > 0) {
            notifyItemRangeChanged(minRemovePosition, changeCount);
        }
    }

    /**
     * 插入单条-通知刷新
     *
     * @param lastCount
     * @param t
     * @param insertPosition 插入位置
     */
    public void notifyInsertItem(int originCount, int lastCount, T t, int insertPosition) {
        if (insertPosition >= originCount) {
            items.add(t);
            notifyItemInserted(originCount);
        } else {
            items.add(insertPosition, t);
            notifyItemInserted(insertPosition);
            int changeStartPosition = insertPosition + 1;
            notifyItemRangeChanged(changeStartPosition, lastCount - changeStartPosition);
        }
    }

    /**
     * 插入多条-通知刷新
     *
     * @param lastCount
     * @param list
     * @param startInsertPosition 开始插入位置
     */
    public void notifyInsertItems(int originCount, int lastCount, List<T> list, int startInsertPosition) {
        int insertCount = list.size();
        if (startInsertPosition >= originCount) {
            items.addAll(list);
            notifyItemRangeInserted(originCount, insertCount);
        } else {
            items.addAll(startInsertPosition, list);
            notifyItemRangeInserted(startInsertPosition, insertCount);
            int changeStartPosition = startInsertPosition + insertCount;
            notifyItemRangeChanged(changeStartPosition, lastCount - changeStartPosition);
        }
    }


    /**
     * 替换多条-通知刷新 替换指定位置开始的所有数据
     *
     * @param originCount
     * @param lastCount
     * @param list
     * @param replaceStartPosition
     */
    public void notifyReplaceItems(int originCount, int lastCount, List<T> list, int replaceStartPosition) {
        if (lastCount == 0) {
            notifyItemRangeRemoved(0, originCount);
            items.clear();
        } else {
            if (isEnableAddItemReplace) {
                int overCount = lastCount - originCount;
                if (overCount < 0) {
                    notifyItemRangeRemoved(lastCount, Math.abs(overCount));
                }
                int replaceCount = Math.min(originCount, lastCount) - replaceStartPosition;
                if (replaceCount > 0) {
                    notifyItemRangeChanged(replaceStartPosition, replaceCount);
                }
                if (overCount > 0) {
                    int subStartIndex = originCount - replaceStartPosition;
                    if (subStartIndex == 0) {
                        items.addAll(list);
                    } else {
                        items.addAll(list.subList(subStartIndex, list.size()));
                    }
                    notifyItemRangeInserted(originCount, overCount);
                }
            } else {
                if(replaceStartPosition < originCount){
                    notifyItemRangeRemoved(replaceStartPosition, originCount - replaceStartPosition);
                    Iterator<T> it = items.iterator();
                    int position = 0;
                    while (it.hasNext()) {
                        it.next();
                        if (position >= replaceStartPosition) {
                            it.remove();
                        }
                        position++;
                    }
                }
                items.addAll(replaceStartPosition,list);
                notifyItemRangeInserted(replaceStartPosition, list.size());
            }
        }
    }

    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //备份Item列表
    private final ArrayList<BaseAdapterBean<T>> backItems = new ArrayList<>();
    private final ArrayList<T> items = new ArrayList<>();
    //备份item定位Map
    private final ConcurrentHashMap<Object, BaseAdapterBean<T>> localMap = new ConcurrentHashMap<>();
    //备份Item字母定位
    private ConcurrentHashMap<Object, BaseAdapterBean<T>> letterMap;
    //备份Item 同key集合
    private ConcurrentHashMap<Object, List<BaseAdapterBean<T>>> listMap;

    private final ThreadPoolExecutor threadPool;

    //是否关闭item
    private boolean isClosed;
    private final RunHandler<T, VH> runHandler;
    //刷新item数据源时是否启用 位置替换
    private final boolean isEnableAddItemReplace;
    //是否启用定位
    private final boolean isEnableLetterLocalMap;
    //是否启用集合map
    private final boolean isEnableListItemMap;

    //是否正在刷新UI
    private boolean isRefreshingUI;
    //正在执行线程
    private boolean isRunningRunnable;

    public BaseRecyclerAdapter() {
        this(false);
    }

    /**
     * @param isEnableAddItemReplace 是否启用 位置替换
     */
    public BaseRecyclerAdapter(boolean isEnableAddItemReplace) {
        this(isEnableAddItemReplace, false);
    }

    /**
     * @param isEnableAddItemReplace 是否启用 位置替换
     */
    public BaseRecyclerAdapter(boolean isEnableAddItemReplace, boolean isEnableLetterLocalMap) {
        this(isEnableAddItemReplace, isEnableLetterLocalMap, false);
    }

    /**
     * @param isEnableAddItemReplace 是否启用-同位置item自动替换(添加数据源时)
     * @param isEnableLetterLocalMap 是否启用-第二字母定位定位数据位置
     */
    public BaseRecyclerAdapter(boolean isEnableAddItemReplace, boolean isEnableLetterLocalMap, boolean isEnableListItemMap) {
        this.isEnableAddItemReplace = isEnableAddItemReplace;
        this.isEnableLetterLocalMap = isEnableLetterLocalMap;
        this.isEnableListItemMap = isEnableListItemMap;
        this.runHandler = new RunHandler<>(this);
        this.threadPool = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        this.threadPool.allowCoreThreadTimeOut(true);
        if (isEnableLetterLocalMap) {
            this.letterMap = new ConcurrentHashMap<>();
        }
        if (isEnableListItemMap) {
            this.listMap = new ConcurrentHashMap<>();
        }
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        mBindViewHolder(holder, position);
    }

    /**
     * 定位Key
     *
     * @param adapterBean
     * @return
     */
    public Object getKeyObj(BaseAdapterBean<T> adapterBean) {
        return adapterBean.getItemPosition();
    }


    /**
     * 字母定位Key
     *
     * @param adapterBean
     * @return
     */
    public Object getLetterKey(BaseAdapterBean<T> adapterBean, BaseAdapterBean<T> previousAdapterBean) {
        return adapterBean.getItemPosition();
    }

    /**
     * 字母定位Key
     *
     * @param adapterBean
     * @return
     */
    public Object getLetterKey(BaseAdapterBean<T> adapterBean) {
        return adapterBean.getItemPosition();
    }

    /**
     * 集合key
     *
     * @param adapterBean
     * @return
     */
    public Object getListKey(BaseAdapterBean<T> adapterBean) {
        return adapterBean.getItemPosition();
    }

    /**
     * 执行单Item
     *
     * @param t
     */
    public void addRunItem(T t) {
        if (isClosed) return;
        threadPool.execute(new RunItemRunnable(t));
    }

    /**
     * 执行列表
     *
     * @param list
     */
    public void addRunItems(List<T> list) {
        if (isClosed) return;
        threadPool.execute(new RunItemsRunnable(list));
    }

    /**
     * 添加单个Item变更
     *
     * @param changeBean
     */
    public void changeRunItem(ChangeAdapterBean<T> changeBean) {
        if (isClosed) return;
        threadPool.execute(new RunChangeForItemKeyRunnable(changeBean));
    }

    /**
     * 添加单个Item变更
     *
     * @param list
     */
    public void changeRunItems(List<ChangeAdapterBean<T>> list) {
        if (isClosed) return;
        threadPool.execute(new RunChangeForItemListKeyRunnable(list));
    }

    /**
     * 移除Item
     *
     * @param objKey
     */
    public void removeRunItem(Object objKey) {
        if (isClosed) return;
        threadPool.execute(new RunRemoveForItemKeyRunnable(objKey));
    }

    /**
     * 移除Item
     *
     * @param keys
     */
    public void removeRunItems(List<Object> keys) {
        if (isClosed) return;
        threadPool.execute(new RunRemoveForItemListKeyRunnable(keys));
    }

    /**
     * 某位置插入item
     *
     * @param t
     * @param insertPosition
     */
    public void insertRunItem(T t, int insertPosition) {
        if (isClosed) return;
        threadPool.execute(new RunInsertItemRunnable(t, insertPosition));
    }

    /**
     * 某位置插入item
     *
     * @param list
     * @param insertPosition
     */
    public void insertRunItems(List<T> list, int insertPosition) {
        if (isClosed) return;
        threadPool.execute(new RunInsertItemsRunnable(list, insertPosition));
    }


    /**
     * 自定位置开始替换成此列表数据
     *
     * @param list
     * @param replaceStartPosition 从此位置开始替换后面所有数据
     */
    public void replaceRunItems(List<T> list, int replaceStartPosition) {
        if (isClosed) return;
        threadPool.execute(new RunReplaceItemListRunnable(list, replaceStartPosition));
    }

    /**
     * 通过集合key移除合集
     *
     * @param listKey
     */
    public void removeRunListKey(Object listKey) {
        if (isClosed) return;
        threadPool.execute(new RunRemoveKeyListRunnable(listKey));
    }

    /**
     * 通过集合key 改变合集
     *
     * @param t
     * @param listKey
     */
    public void changeRunListKey(T t, Object listKey) {
        if (isClosed) return;
        threadPool.execute(new RunChangeKeyListRunnable(t, listKey));
    }

    /**
     * 获取字母定位的位置
     *
     * @param spell
     * @return
     */
    public Integer getFirstSpellPosition(String spell) {
        if (isEnableLetterLocalMap) {
            BaseAdapterBean<T> adapterBean = letterMap.get(spell);
            if (adapterBean != null) {
                return adapterBean.getItemPosition();
            }
        }
        return null;
    }

    /**
     * 通过Key获取Item位置
     *
     * @param objKey
     * @return
     */
    public int getItemPositionForKey(Object objKey) {
        BaseAdapterBean<T> adapterBean = localMap.get(objKey);
        if (adapterBean != null) {
            return adapterBean.getItemPosition();
        }
        return -1;
    }

    /**
     * 通过key值获取Item
     *
     * @param objKey
     * @return
     */
    public T mGetItemForKey(Object objKey) {
        BaseAdapterBean<T> adapterBean = localMap.get(objKey);
        if (adapterBean != null) {
            return adapterBean.getItem();
        }
        return null;
    }

    /**
     * 关闭
     */
    public void close() {
        isClosed = true;
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }


    /**
     * 刷新Item据源
     */
    private class RunItemRunnable implements Runnable {
        private final T t;

        public RunItemRunnable(T t) {
            this.t = t;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        isRefreshingUI = true;
                        int originCount = backItems.size();
                        backItems.clear();
                        localMap.clear();
                        //有启用字母定位
                        if (isEnableLetterLocalMap) {
                            letterMap.clear();
                        }
                        //有启用集合
                        if (isEnableListItemMap) {
                            listMap.clear();
                        }
                        Message message = Message.obtain();
                        if (t != null) {
                            BaseAdapterBean<T> adapterBean = new BaseAdapterBean<>();
                            adapterBean.setItem(t);
                            adapterBean.setItemPosition(0);
                            backItems.add(adapterBean);
                            localMap.put(getKeyObj(adapterBean), adapterBean);
                            message.obj = t;
                            //有启用字母定位
                            if (isEnableLetterLocalMap) {
                                letterMap.put(getLetterKey(adapterBean), adapterBean);
                            }
                            //有启用集合
                            if (isEnableListItemMap) {
                                Object listKey = getListKey(adapterBean);
                                List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                if (list == null) {
                                    list = new ArrayList<>();
                                    listMap.put(listKey, list);
                                }
                                list.add(adapterBean);
                            }
                        }
                        message.what = BaseAdapterCode.Code.ADD_ITEM;
                        message.arg1 = originCount;
                        message.arg2 = backItems.size();
                        runHandler.sendMessage(message);
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 刷新item数据源
     */
    private class RunItemsRunnable implements Runnable {
        private final List<T> list;

        public RunItemsRunnable(List<T> list) {
            this.list = list;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        isRefreshingUI = true;
                        int originCount = backItems.size();
                        backItems.clear();
                        localMap.clear();
                        //有启用字母定位
                        if (isEnableLetterLocalMap) {
                            letterMap.clear();
                        }
                        //有启用集合
                        if (isEnableListItemMap) {
                            listMap.clear();
                        }
                        Message message = Message.obtain();
                        if (list != null && !list.isEmpty()) {
                            int startPosition = 0;
                            for (T t : list) {
                                BaseAdapterBean<T> adapterBean = new BaseAdapterBean<>();
                                adapterBean.setItem(t);
                                adapterBean.setItemPosition(startPosition);
                                backItems.add(adapterBean);
                                localMap.put(getKeyObj(adapterBean), adapterBean);
                                //有启用同源位置替换
                                if (isEnableAddItemReplace && startPosition < originCount) {
                                    mReplaceItem(startPosition, t);
                                }
                                //有启用字母定位
                                if (isEnableLetterLocalMap) {
                                    if (startPosition == 0) {
                                        letterMap.put(getLetterKey(adapterBean), adapterBean);
                                    } else {
                                        BaseAdapterBean<T> previousAdapterBean = backItems.get(startPosition - 1);
                                        Object letterKey = getLetterKey(adapterBean, previousAdapterBean);
                                        if (letterKey != null) {
                                            letterMap.put(letterKey, adapterBean);
                                        }
                                    }
                                }
                                //有启用集合
                                if (isEnableListItemMap) {
                                    Object listKey = getListKey(adapterBean);
                                    List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                    if (list == null) {
                                        list = new ArrayList<>();
                                        listMap.put(listKey, list);
                                    }
                                    list.add(adapterBean);
                                }
                                startPosition++;
                            }
                            message.obj = list;
                        }
                        message.what = BaseAdapterCode.Code.ADD_ITEM_LIST;
                        message.arg1 = originCount;
                        message.arg2 = backItems.size();
                        runHandler.sendMessage(message);
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 变更 Item
     */
    private class RunChangeForItemKeyRunnable implements Runnable {

        private final ChangeAdapterBean<T> changeBean;

        public RunChangeForItemKeyRunnable(ChangeAdapterBean<T> changeBean) {
            this.changeBean = changeBean;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        BaseAdapterBean<T> adapterBean = localMap.get(changeBean.getKey());
                        if (adapterBean != null) {
                            isRefreshingUI = true;
                            mChangeItem(adapterBean, changeBean.getItem());
                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.CHANGE_ITEM;
                            int originCount = backItems.size();
                            message.arg1 = originCount;
                            message.arg2 = originCount;
                            message.obj = adapterBean;
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 变更Item列表
     */
    private class RunChangeForItemListKeyRunnable implements Runnable {

        private final List<ChangeAdapterBean<T>> list;

        public RunChangeForItemListKeyRunnable(List<ChangeAdapterBean<T>> list) {
            this.list = list;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        List<BaseAdapterBean<T>> changeList = new ArrayList<>();
                        for (ChangeAdapterBean<T> changeAdapterBean : list) {
                            BaseAdapterBean<T> adapterBean = localMap.get(changeAdapterBean.getKey());
                            if (adapterBean != null) {
                                mChangeItem(adapterBean, changeAdapterBean.getItem());
                                changeList.add(adapterBean);
                            }
                        }
                        if (!changeList.isEmpty()) {
                            isRefreshingUI = true;
                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.CHANGE_ITEM_LIST;
                            int originCount = backItems.size();
                            message.arg1 = originCount;
                            message.arg2 = originCount;
                            message.obj = changeList;
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 移除 Item
     */
    private class RunRemoveForItemKeyRunnable implements Runnable {

        private final Object objKey;

        public RunRemoveForItemKeyRunnable(Object objKey) {
            this.objKey = objKey;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        BaseAdapterBean<T> adapterBean = localMap.remove(objKey);
                        if (adapterBean != null) {
                            isRefreshingUI = true;
                            int originCount = backItems.size();
                            backItems.remove(adapterBean);
                            //有启用集合
                            if (isEnableListItemMap) {
                                Object listKey = getListKey(adapterBean);
                                List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                if (list != null && !list.isEmpty()) {
                                    list.remove(adapterBean);
                                    if (list.isEmpty()) {
                                        listMap.remove(listKey);
                                    }
                                }
                            }
                            //刷新位置
                            for (int i = adapterBean.getItemPosition(); i < backItems.size(); i++) {
                                BaseAdapterBean<T> backupAdapterBean = backItems.get(i);
                                backupAdapterBean.setItemPosition(i);
                            }

                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.REMOVE_ITEM;
                            message.arg1 = originCount;
                            message.arg2 = backItems.size();
                            message.obj = adapterBean;
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 移除 Item
     */
    private class RunRemoveForItemListKeyRunnable implements Runnable {

        private final List<Object> keys;

        public RunRemoveForItemListKeyRunnable(List<Object> keys) {
            this.keys = keys;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        int originCount = backItems.size();
                        List<BaseAdapterBean<T>> removeList = new ArrayList<>();
                        Integer minRemovePosition = null;
                        for (Object key : keys) {
                            BaseAdapterBean<T> adapterBean = localMap.remove(key);
                            if (adapterBean != null) {
                                backItems.remove(adapterBean);
                                removeList.add(adapterBean);
                                //最小移除位置
                                minRemovePosition = minRemovePosition == null ? adapterBean.getItemPosition() : Math.min(minRemovePosition, adapterBean.getItemPosition());
                                //有启用集合
                                if (isEnableListItemMap) {
                                    Object listKey = getListKey(adapterBean);
                                    List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                    if (list != null && !list.isEmpty()) {
                                        list.remove(adapterBean);
                                        if (list.isEmpty()) {
                                            listMap.remove(listKey);
                                        }
                                    }
                                }
                            }
                        }
                        //刷新位置
                        if (minRemovePosition != null) {
                            for (int i = minRemovePosition; i < backItems.size(); i++) {
                                BaseAdapterBean<T> backupAdapterBean = backItems.get(i);
                                backupAdapterBean.setItemPosition(i);
                            }
                        }

                        if (!removeList.isEmpty()) {
                            isRefreshingUI = true;
                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.REMOVE_ITEM_LIST;
                            message.arg1 = originCount;
                            message.arg2 = backItems.size();
                            message.obj = removeList;
                            Bundle bundle = new Bundle();
                            bundle.putInt("minRemovePosition", minRemovePosition);
                            message.setData(bundle);
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }


    /**
     * 替换数据列表线程
     */
    private class RunReplaceItemListRunnable implements Runnable {

        private final List<T> list;
        private final int startIndex;

        public RunReplaceItemListRunnable(List<T> list, int startIndex) {
            this.list = list;
            this.startIndex = startIndex;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        isRefreshingUI = true;
                        int originCount = backItems.size();
                        int realStartIndex = Math.min(startIndex, originCount);
                        localMap.clear();
                        if (isEnableLetterLocalMap) {
                            letterMap.clear();
                        }
                        if (isEnableListItemMap) {
                            listMap.clear();
                        }
                        //开始位置保留列表
                        if (realStartIndex > 0) {
                            //保留数据
                            List<BaseAdapterBean<T>> startRetainList = new ArrayList<>();
                            backItems.subList(0, realStartIndex);
                            for (int i = 0; i < realStartIndex; i++) {
                                BaseAdapterBean<T> adapterBean = backItems.get(i);
                                startRetainList.add(adapterBean);
                                localMap.put(getKeyObj(adapterBean), adapterBean);
                                //有启用字母定位
                                if (isEnableLetterLocalMap) {
                                    if (i == 0) {
                                        letterMap.put(getLetterKey(adapterBean), adapterBean);
                                    } else {
                                        BaseAdapterBean<T> previousAdapterBean = startRetainList.get(i - 1);
                                        Object letterKey = getLetterKey(adapterBean, previousAdapterBean);
                                        if (letterKey != null) {
                                            letterMap.put(letterKey, adapterBean);
                                        }
                                    }
                                }
                                //有启用集合
                                if (isEnableListItemMap) {
                                    Object listKey = getListKey(adapterBean);
                                    List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                    if (list == null) {
                                        list = new ArrayList<>();
                                        listMap.put(listKey, list);
                                    }
                                    list.add(adapterBean);
                                }
                            }
                            backItems.clear();
                            backItems.addAll(startRetainList);
                        } else {
                            backItems.clear();
                        }

                        Message message = Message.obtain();
                        if (list != null && !list.isEmpty()) {
                            int startPosition = backItems.size();
                            for (T t : list) {
                                BaseAdapterBean<T> adapterBean = new BaseAdapterBean<>();
                                adapterBean.setItem(t);
                                adapterBean.setItemPosition(startPosition);
                                backItems.add(adapterBean);
                                localMap.put(getKeyObj(adapterBean), adapterBean);
                                //有启用同源位置替换
                                if (isEnableAddItemReplace && startPosition < originCount) {
                                    mReplaceItem(startPosition, t);
                                }
                                //有启用字母定位
                                if (isEnableLetterLocalMap) {
                                    if (startPosition == 0) {
                                        letterMap.put(getLetterKey(adapterBean), adapterBean);
                                    } else {
                                        BaseAdapterBean<T> previousAdapterBean = backItems.get(startPosition - 1);
                                        Object letterKey = getLetterKey(adapterBean, previousAdapterBean);
                                        if (letterKey != null) {
                                            letterMap.put(letterKey, adapterBean);
                                        }
                                    }
                                }
                                //有启用集合
                                if (isEnableListItemMap) {
                                    Object listKey = getListKey(adapterBean);
                                    List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                    if (list == null) {
                                        list = new ArrayList<>();
                                        listMap.put(listKey, list);
                                    }
                                    list.add(adapterBean);
                                }
                                startPosition++;
                            }
                            message.obj = list;
                        }
                        message.what = BaseAdapterCode.Code.REPLACE_ITEM_LIST;
                        message.arg1 = originCount;
                        message.arg2 = backItems.size();
                        Bundle bundle = new Bundle();
                        bundle.putInt("replaceStartPosition", realStartIndex);
                        message.setData(bundle);
                        runHandler.sendMessage(message);
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 指定位置插入Item
     */
    private class RunInsertItemRunnable implements Runnable {

        private final T t;
        //小于0时会自动取0
        private final int insertPosition;

        public RunInsertItemRunnable(T t, int insertPosition) {
            this.t = t;
            this.insertPosition = Math.max(0, insertPosition);
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        isRefreshingUI = true;
                        int originCount = backItems.size();
                        BaseAdapterBean<T> adapterBean = new BaseAdapterBean<>();
                        adapterBean.setItem(t);
                        int lastInsertPosition = Math.min(insertPosition, originCount);
                        if (lastInsertPosition == originCount) {
                            adapterBean.setItemPosition(originCount);
                            backItems.add(adapterBean);
                            localMap.put(getKeyObj(adapterBean), adapterBean);
                        } else {
                            adapterBean.setItemPosition(lastInsertPosition);
                            backItems.add(lastInsertPosition, adapterBean);
                            localMap.put(getKeyObj(adapterBean), adapterBean);
                            //刷新插入item后面的所有item位置
                            for (int i = lastInsertPosition + 1; i < backItems.size(); i++) {
                                BaseAdapterBean<T> backupItem = backItems.get(i);
                                backupItem.setItemPosition(i);
                            }
                        }
                        //有启用集合
                        if (isEnableListItemMap) {
                            Object listKey = getListKey(adapterBean);
                            List<BaseAdapterBean<T>> list = listMap.get(listKey);
                            if (list == null) {
                                list = new ArrayList<>();
                                listMap.put(listKey, list);
                            }
                            list.add(adapterBean);
                        }
                        Message message = Message.obtain();
                        message.what = BaseAdapterCode.Code.INSERT_ITEM;
                        message.arg1 = originCount;
                        message.arg2 = backItems.size();
                        message.obj = t;
                        Bundle bundle = new Bundle();
                        bundle.putInt("insertPosition", lastInsertPosition);
                        message.setData(bundle);
                        runHandler.sendMessage(message);
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 指定位置插入Item
     */
    private class RunInsertItemsRunnable implements Runnable {

        private final List<T> list;
        //小于0时会自动取0
        private final int insertPosition;

        public RunInsertItemsRunnable(List<T> list, int insertPosition) {
            this.list = list;
            this.insertPosition = Math.max(0, insertPosition);
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        isRefreshingUI = true;
                        int originCount = backItems.size();
                        int startInsertPosition = Math.min(insertPosition, originCount);
                        int nextInsertPosition = startInsertPosition;
                        List<BaseAdapterBean<T>> addItems = new ArrayList<>();
                        for (T t : list) {
                            BaseAdapterBean<T> adapterBean = new BaseAdapterBean<>();
                            adapterBean.setItem(t);
                            adapterBean.setItemPosition(nextInsertPosition);
                            addItems.add(adapterBean);
                            localMap.put(getKeyObj(adapterBean), adapterBean);
                            //有启用集合
                            if (isEnableListItemMap) {
                                Object listKey = getListKey(adapterBean);
                                List<BaseAdapterBean<T>> list = listMap.get(listKey);
                                if (list == null) {
                                    list = new ArrayList<>();
                                    listMap.put(listKey, list);
                                }
                                list.add(adapterBean);
                            }
                            nextInsertPosition++;
                        }
                        if (startInsertPosition >= originCount) {
                            backItems.addAll(addItems);
                        } else {
                            backItems.addAll(startInsertPosition, addItems);
                            //刷新插入item后面的所有item位置
                            for (int i = startInsertPosition + addItems.size(); i < backItems.size(); i++) {
                                BaseAdapterBean<T> backupItem = backItems.get(i);
                                backupItem.setItemPosition(i);
                            }
                        }
                        Message message = Message.obtain();
                        message.what = BaseAdapterCode.Code.INSERT_ITEM_LIST;
                        message.arg1 = originCount;
                        message.arg2 = backItems.size();
                        message.obj = list;
                        Bundle bundle = new Bundle();
                        bundle.putInt("startInsertPosition", startInsertPosition);
                        message.setData(bundle);
                        runHandler.sendMessage(message);
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 移除Key集合中的数据
     */
    public class RunRemoveKeyListRunnable implements Runnable {
        private final Object listKey;

        public RunRemoveKeyListRunnable(Object listKey) {
            this.listKey = listKey;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        List<BaseAdapterBean<T>> removeList = listMap.remove(listKey);
                        if (removeList != null && !removeList.isEmpty()) {
                            isRefreshingUI = true;
                            int originCount = backItems.size();
                            int minRemovePosition = Integer.MAX_VALUE;
                            for (BaseAdapterBean<T> adapterBean : removeList) {
                                localMap.remove(getKeyObj(adapterBean));
                                backItems.remove(adapterBean);
                                minRemovePosition = Math.min(minRemovePosition, adapterBean.getItemPosition());
                            }
                            //刷新位置
                            for (int i = minRemovePosition; i < backItems.size(); i++) {
                                BaseAdapterBean<T> backupAdapterBean = backItems.get(i);
                                backupAdapterBean.setItemPosition(i);
                            }
                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.REMOVE_ITEM_LIST;
                            message.arg1 = originCount;
                            message.arg2 = backItems.size();
                            message.obj = removeList;
                            Bundle bundle = new Bundle();
                            bundle.putInt("minRemovePosition", minRemovePosition);
                            message.setData(bundle);
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }
            }
        }
    }

    /**
     * 变更Key集合中的数据
     */
    public class RunChangeKeyListRunnable implements Runnable {
        private final Object listKey;
        private final T t;

        public RunChangeKeyListRunnable(T t, Object listKey) {
            this.listKey = listKey;
            this.t = t;
        }

        @Override
        public void run() {
            if (!isClosed) {
                isRunningRunnable = true;
                while (isRunningRunnable) {
                    if (!isRefreshingUI) {
                        List<BaseAdapterBean<T>> changeList = listMap.get(listKey);
                        if (changeList != null && !changeList.isEmpty()) {
                            isRefreshingUI = true;
                            for (BaseAdapterBean<T> adapterBean : changeList) {
                                mChangeItem(adapterBean, t);
                            }
                            Message message = Message.obtain();
                            message.what = BaseAdapterCode.Code.CHANGE_ITEM_LIST;
                            int originCount = backItems.size();
                            message.arg1 = originCount;
                            message.arg2 = originCount;
                            message.obj = changeList;
                            runHandler.sendMessage(message);
                        }
                        isRunningRunnable = false;
                    }
                }

            }
        }
    }

    private static class RunHandler<T, VH extends RecyclerView.ViewHolder> extends Handler {
        private final WeakReference<BaseRecyclerAdapter<T, VH>> reference;

        public RunHandler(BaseRecyclerAdapter<T, VH> adapter) {
            reference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            BaseRecyclerAdapter<T, VH> adapter = reference.get();
            if (adapter != null && !adapter.isClosed) {
                switch (msg.what) {
                    case BaseAdapterCode.Code.ADD_ITEM:
                        adapter.notifyAddItem(msg.arg1, msg.arg2, (T) msg.obj);
                        break;
                    case BaseAdapterCode.Code.ADD_ITEM_LIST:
                        adapter.notifyAddItems(msg.arg1, msg.arg2, (List<T>) msg.obj);
                        break;
                    case BaseAdapterCode.Code.CHANGE_ITEM:
                        adapter.notifyChangeItem(msg.arg2, (BaseAdapterBean<T>) msg.obj);
                        break;
                    case BaseAdapterCode.Code.CHANGE_ITEM_LIST:
                        adapter.notifyChangeItems(msg.arg2, (List<BaseAdapterBean<T>>) msg.obj);
                        break;
                    case BaseAdapterCode.Code.REMOVE_ITEM:
                        adapter.notifyRemoveItem(msg.arg1, msg.arg2, (BaseAdapterBean<T>) msg.obj);
                        break;
                    case BaseAdapterCode.Code.REMOVE_ITEM_LIST:
                        adapter.notifyRemoveItems(msg.arg1, msg.arg2, (List<BaseAdapterBean<T>>) msg.obj, msg.getData().getInt("minRemovePosition"));
                        break;
                    case BaseAdapterCode.Code.INSERT_ITEM:
                        adapter.notifyInsertItem(msg.arg1, msg.arg2, (T) msg.obj, msg.getData().getInt("insertPosition"));
                        break;
                    case BaseAdapterCode.Code.INSERT_ITEM_LIST:
                        adapter.notifyInsertItems(msg.arg1, msg.arg2, (List<T>) msg.obj, msg.getData().getInt("startInsertPosition"));
                        break;
                    case BaseAdapterCode.Code.REPLACE_ITEM_LIST:
                        adapter.notifyReplaceItems(msg.arg1, msg.arg2, (List<T>) msg.obj, msg.getData().getInt("replaceStartPosition"));
                        break;
                }
                adapter.isRefreshingUI = false;
            }
        }


    }
}
