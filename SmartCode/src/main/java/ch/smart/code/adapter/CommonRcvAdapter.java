package ch.smart.code.adapter;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import ch.smart.code.adapter.item.AdapterItem;
import ch.smart.code.adapter.item.RcvAdapterItem;
import ch.smart.code.adapter.util.IAdapter;
import ch.smart.code.adapter.util.ItemTypeUtil;

import static com.jess.arms.utils.Preconditions.checkNotNull;

/**
 * @author Jack Tony
 * @date 2015/5/17
 */
public abstract class CommonRcvAdapter<T> extends RecyclerView.Adapter<RcvAdapterItem<T>> implements IAdapter<T> {
    
    private List<T> mDataList;
    
    private Object mType;
    
    private ItemTypeUtil mUtil;
    
    private int currentPos;
    
    private LifecycleOwner lifecycleOwner;
    
    private boolean lifecycleEnable = true;
    
    public CommonRcvAdapter(@Nullable List<T> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        mDataList = data;
        mUtil = new ItemTypeUtil();
    }
    
    /**
     * 配合RecyclerView的pool来设置TypePool
     */
    public void setTypePool(HashMap<Object, Integer> typePool) {
        mUtil.setTypePool(typePool);
    }
    
    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
    
    @Override
    public void setData(@NonNull List<T> data) {
        mDataList = data;
        notifyDataSetChanged();
    }
    
    @Override
    public List<T> getData() {
        return mDataList;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    /**
     * instead by{@link #getItemType(T)}
     * <p>
     * 通过数据得到obj的类型的type
     * 然后，通过{@link ItemTypeUtil}来转换位int类型的type
     */
    @Deprecated
    @Override
    public int getItemViewType(int position) {
        this.currentPos = position;
        mType = getItemType(getItem(position));
        return mUtil.getIntType(mType);
    }
    
    @NonNull
    @Override
    public Object getItemType(@NonNull T t) {
        return -1;
    }
    
    public T getItem(int position) {
        if (mDataList != null && position >= 0 && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }
    
    @NonNull
    @Override
    public RcvAdapterItem<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RcvAdapterItem<>(parent.getContext(), parent, createItem(mType));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RcvAdapterItem<T> holder, int position) {
        attachLifecycleOwnerIfNeed(holder.getItem());
        holder.getItem().handleData(getItem(position), position);
    }
    
    /**
     * Attaches the holder to lifecycle if need.
     *
     * @param holder The holder we are going to bind.
     */
    private void attachLifecycleOwnerIfNeed(final AdapterItem<T> holder) {
        if (lifecycleEnable && lifecycleOwner != null && holder instanceof LifecycleObserver) {
            lifecycleOwner.getLifecycle().addObserver((LifecycleObserver) holder);
        }
    }
    
    /**
     * Detachs the holder to lifecycle if need.
     *
     * @param holder The holder we are going to unBind.
     */
    private void detachLifecycleOwnerIfNeed(final AdapterItem<T> holder) {
        if (lifecycleEnable && lifecycleOwner != null && holder instanceof LifecycleObserver) {
            lifecycleOwner.getLifecycle().removeObserver((LifecycleObserver) holder);
        }
    }
    
    @Override
    public int getCurrentPosition() {
        return currentPos;
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull RcvAdapterItem<T> holder) {
        super.onViewAttachedToWindow(holder);
        holder.getItem().onAttach();
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull RcvAdapterItem<T> holder) {
        super.onViewDetachedFromWindow(holder);
        holder.getItem().onDetach();
        detachLifecycleOwnerIfNeed(holder.getItem());
    }
    
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (recyclerView.getContext() instanceof LifecycleOwner && lifecycleOwner == null) {
            setLifecycleOwner((LifecycleOwner) recyclerView.getContext());
        }
    }
    
    public CommonRcvAdapter<T> setLifecycleOwner(@NonNull final LifecycleOwner lifecycleOwner) {
        checkNotNull(lifecycleOwner, "lifecycleOwner can't be null here.");
        this.lifecycleOwner = lifecycleOwner;
        return this;
    }
    
    public CommonRcvAdapter<T> setLifecycleEnable(boolean lifecycleEnable) {
        this.lifecycleEnable = lifecycleEnable;
        return this;
    }
}
