package ch.smart.code.adapter.util;

import java.util.List;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import ch.smart.code.adapter.item.AdapterItem;

/**
 * @author Jack Tony
 * @date 2015/11/29
 * 通用的adapter必须实现的接口，作为方法名统一的一个规范
 */
public interface IAdapter<T> {

    /**
     * @param data 设置数据源
     */
    void setData(@NonNull List<T> data);

    List<T> getData();

    /**
     * @param t list中的一条数据
     * @return 强烈建议返回string, int, bool类似的基础对象做type，不要返回data中的某个对象
     */
    @NonNull
    Object getItemType(T t);

    /**
     * 当缓存中无法得到所需item时才会调用
     *
     * @param type 通过{@link #getItemType(T)}得到的type
     * @return 任意类型的 AdapterItem
     */
    @Keep
    @NonNull
    AdapterItem<T> createItem(@NonNull Object type);

    /**
     * 得到当前要渲染的最后一个item的position
     */
    int getCurrentPosition();
}
