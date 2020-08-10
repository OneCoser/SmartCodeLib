package ch.smart.code.adapter.util;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedList;
import ch.smart.code.adapter.item.AdapterItem;

public interface ISortedAdapter<T> {

    /**
     * @param data 设置数据源
     */
    void setData(@NonNull SortedList<T> data);

    SortedList<T> getData();

    /**
     * @param t list中的一条数据
     * @return 强烈建议返回string, int, bool类似的基础对象做type，不要返回data中的某个对象
     */
    Object getItemType(T t);

    /**
     * 当缓存中无法得到所需item时才会调用
     *
     * @param type 通过{@link #getItemType(T)}得到的type
     * @return 任意类型的 AdapterItem
     */
    @Keep
    @NonNull
    AdapterItem<T> createItem(Object type);


    /**
     * 通知adapter更新当前页面的所有数据
     */
    void notifyDataSetChanged();

    /**
     * 得到当前要渲染的最后一个item的position
     */
    int getCurrentPosition();
}
