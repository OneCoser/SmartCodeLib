package ch.smart.code.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.SizeUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


/**
 * @author Jack Tony
 * @date 2015/6/2
 */
public class RcvAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    /**
     * view的基本类型，这里只有头/底部/普通，在子类中可以扩展
     */
    public static final int TYPE_HEADER = 99930;
    
    public static final int TYPE_FOOTER = 99931;
    
    public static final int TYPE_EMPTY = 99932;
    
    public static final int TYPE_ERROR = 99933;
    
    public static final int TYPE_LOADING = 99934;
    
    private RecyclerView.LayoutManager layoutManager;
    
    private RecyclerView.Adapter mWrapped;
    
    private boolean hasShownEmptyView = false;
    
    @Nullable
    private RecyclerView emptyViewParent;
    
    @Nullable
    private View headerView = null;
    
    @Nullable
    private View footerView = null;
    
    @Nullable
    private FrameLayout emptyLayout = null;
    
    @Nullable
    private View emptyView;
    
    
    private int emptyType = TYPE_EMPTY;
    
    public RcvAdapterWrapper(@NonNull RecyclerView.Adapter adapter, @NonNull RecyclerView.LayoutManager layoutManager) {
        mWrapped = adapter;
        mWrapped.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
            
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
            }
            
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
                if (hasShownEmptyView && getItemCount() != 0) {
                    notifyItemRemoved(getHeaderCount());
                    hasShownEmptyView = false;
                }
            }
            
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
                /*if (getFooterCount() != 0) {
                    if (positionStart + getFooterCount() + 1 == getItemCount()) { // last one
                        notifyDataSetChanged();
                    }
                }*/
            }
            
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                // FIXME: 2015/11/23 还没支持"多个"item的转移的操作
            }
        });
        this.layoutManager = layoutManager;
        
        if (this.layoutManager instanceof GridLayoutManager) {
            setSpanSizeLookup(this, (GridLayoutManager) this.layoutManager); // 设置头部和尾部都是跨列的
        }
    }
    
    /**
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        int count = mWrapped.getItemCount();
        
        int offset = 0;
        if (headerView != null) {
            offset++;
        }
        if (footerView != null) {
            offset++;
        }
        if (emptyLayout != null) {
            if (count == 0) {
                offset++;
                hasShownEmptyView = true;
            } else {
                hasShownEmptyView = false;
            }
        }
        return offset + count;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (headerView != null && position == 0) {
            return TYPE_HEADER;
        } else if (footerView != null && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else if (emptyLayout != null && mWrapped.getItemCount() == 0 && position == getHeaderCount()) {
            return TYPE_EMPTY;
        } else {
            return mWrapped.getItemViewType(position - getHeaderCount());
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new SimpleViewHolder(headerView);
            case TYPE_FOOTER:
                return new SimpleViewHolder(footerView);
            case TYPE_EMPTY:
                return new SimpleViewHolder(emptyLayout);
            default:
                return mWrapped.onCreateViewHolder(parent, viewType);
        }
    }
    
    /**
     * 载入ViewHolder，这里仅仅处理header和footer视图的逻辑
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        final int type = getItemViewType(position);
        if (type != TYPE_HEADER && type != TYPE_FOOTER && type != TYPE_EMPTY) {
            mWrapped.onBindViewHolder(viewHolder, position - getHeaderCount());
        }
        
        if (type == TYPE_EMPTY && emptyViewParent != null) {
            ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
            int headerHeight = headerView != null ? headerView.getHeight() : 0;
            params.height = Math.max(emptyViewParent.getHeight() - headerHeight, SizeUtils.dp2px(150));
        }
    }
    
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (this.layoutManager == layoutManager) {
            return;
        }
        
        this.layoutManager = layoutManager;
        if (this.layoutManager instanceof GridLayoutManager) {
            setSpanSizeLookup(this, (GridLayoutManager) this.layoutManager); // 设置头部和尾部都是跨列的
        }
        setFullSpan(headerView, layoutManager);
        setFullSpan(footerView, layoutManager);
        setFullSpan(emptyLayout, layoutManager);
    }
    
    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // 添加/移除头部、尾部的操作
    ///////////////////////////////////////////////////////////////////////////
    
    synchronized public void setHeaderView(@Nullable View headerView) {
        if (this.headerView == headerView) {
            return;
        }
        this.headerView = headerView;
        setFullSpan(headerView, layoutManager);
    }
    
    @Nullable
    public View getHeaderView() {
        return headerView;
    }
    
    synchronized public void setFooterView(@Nullable View footerView) {
        if (this.footerView == footerView) {
            return;
        }
        this.footerView = footerView;
        setFullSpan(footerView, layoutManager);
    }
    
    @Nullable
    public View getFooterView() {
        return footerView;
    }
    
    /**
     * 设置空状态的view
     *
     * @param emptyViewParent 如果需要EmptyView的高度占满RecyclerView，则此参数必填；
     *                        传null，则保持EmptyView的自有高度
     */
    synchronized public void setEmptyView(@Nullable View emptyView, @Nullable RecyclerView emptyViewParent) {
        setEmptyView(emptyView, emptyViewParent, TYPE_EMPTY);
    }
    
    /**
     * 设置空状态的view
     *
     * @param emptyViewParent 如果需要EmptyView的高度占满RecyclerView，则此参数必填；
     *                        传null，则保持EmptyView的自有高度
     */
    synchronized public void setEmptyView(@Nullable View emptyView, @Nullable RecyclerView emptyViewParent, int type) {
        if (this.emptyView == emptyView) {
            return;
        }
        this.emptyView = emptyView;
        emptyType = type;
        this.emptyViewParent = emptyViewParent;
        if (emptyView == null) {
            if (emptyLayout != null) {
                emptyLayout.removeAllViews();
            }
        } else {
            ViewParent parent = emptyView.getParent();
            if (parent != null && parent == emptyLayout) {
                return;
            }
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(emptyView);
            }
            if (emptyLayout == null) {
                emptyLayout = new FrameLayout(emptyView.getContext());
                emptyLayout.addView(emptyView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                setFullSpan(emptyLayout, layoutManager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                emptyLayout.removeAllViews();
                emptyLayout.addView(emptyView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    }
    
    
    @Nullable
    public View getEmptyView() {
        return emptyView;
    }
    
    /**
     * 空视图类型
     *
     * @return {@link #TYPE_EMPTY}
     * {@link #TYPE_ERROR}
     * {@link #TYPE_LOADING}
     */
    public int getEmptyType() {
        return emptyType;
    }
    
    /**
     * notifyItemRemoved(0);如果这里需要做头部的删除动画，
     */
    public void removeHeaderView() {
        headerView = null;
        notifyDataSetChanged();
    }
    
    /**
     * 这里因为删除尾部不会影响到前面的pos的改变，所以不用刷新
     */
    public void removeFooterView() {
        footerView = null;
        int footerPos = getItemCount();
        notifyItemRemoved(footerPos);
    }
    
    public RecyclerView.Adapter getWrappedAdapter() {
        return mWrapped;
    }
    
    public int getHeaderCount() {
        return headerView != null ? 1 : 0;
    }
    
    public int getFooterCount() {
        return footerView != null ? 1 : 0;
    }
    
    private void setFullSpan(@Nullable View view, @Nullable RecyclerView.LayoutManager layoutManager) {
        setFullSpan(view, layoutManager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
    
    private void setFullSpan(@Nullable View view, @Nullable RecyclerView.LayoutManager layoutManager, int width, int height) {
        if (view != null) {
            int itemWidth = view.getLayoutParams() != null ?
                view.getLayoutParams().height : ViewGroup.LayoutParams.WRAP_CONTENT;
            int itemHeight = view.getLayoutParams() != null ?
                view.getLayoutParams().height : ViewGroup.LayoutParams.WRAP_CONTENT;
            
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                if (((StaggeredGridLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
                    itemWidth = width;
                } else {
                    itemHeight = height;
                }
                StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(itemWidth, itemHeight);
                layoutParams.setFullSpan(true);
                view.setLayoutParams(layoutParams);
            } else if (layoutManager instanceof LinearLayoutManager) {
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
                    itemWidth = width;
                } else {
                    itemHeight = height;
                }
                view.setLayoutParams(new ViewGroup.LayoutParams(itemWidth, itemHeight));
            }
            notifyDataSetChanged();
        }
    }
    
    /**
     * 设置头和底部的跨列
     */
    private void setSpanSizeLookup(final RecyclerView.Adapter adapter, final GridLayoutManager layoutManager) {
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                final int type = adapter.getItemViewType(position);
                if (type == TYPE_HEADER || type == TYPE_FOOTER || type == TYPE_EMPTY) {
                    return layoutManager.getSpanCount();
                } else {
                    // 如果是普通的，那么就保持原样
                    //return layoutManager.getSpanSizeLookup().getSpanSize(position - adapter.getHeaderCount());
                    return 1;
                }
            }
        });
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!(holder instanceof SimpleViewHolder)) {
            mWrapped.onViewAttachedToWindow(holder);
        }
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (!(holder instanceof SimpleViewHolder)) {
            mWrapped.onViewDetachedFromWindow(holder);
        }
    }
    
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mWrapped.onAttachedToRecyclerView(recyclerView);
    }
    
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mWrapped.onDetachedFromRecyclerView(recyclerView);
    }
    
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (!(holder instanceof SimpleViewHolder)) {
            mWrapped.onViewRecycled(holder);
        }
    }
    
    /**
     * Keep it simple!
     */
    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        
        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
    
}
