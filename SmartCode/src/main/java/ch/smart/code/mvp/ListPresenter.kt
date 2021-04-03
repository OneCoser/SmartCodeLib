package ch.smart.code.mvp

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import ch.smart.code.adapter.CommonRcvAdapter
import ch.smart.code.adapter.RcvAdapterWrapper
import ch.smart.code.adapter.item.RcvAdapterItem
import ch.smart.code.adapter.util.OnRcvScrollListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import timber.log.Timber

/**
 * 类描述：加载列表Presenter基类
 */
abstract class ListPresenter<M : IModel, V : IListView, T>(model: M, rootView: V) :
    BasePresenter<M, V>(model, rootView) {

    @JvmField
    protected var adapterWrapper: RcvAdapterWrapper? = null

    protected val dataList = mutableListOf<T>()

    // 占位数据,不加入翻页计算
    protected open var placeholderSize = 0

    protected var currentPage = 1

    protected open var pageLimit = 20

    // 是否能够加载下一页
    protected var hasMore = true

    // 当前是否正在加载中
    protected var isLoading = false

    // 是否清空列表
    protected var isClearList = false

    // 在 Fragment.destroyView 时是否清除数据源
    protected open var destroyViewClearData = true

    protected open var autoLoad = true

    // 如果可以确定每个 item 的高度是固定的，设置这个选项可以提高性能，如果 item 高度不固定，请设置为 false
    protected open var hasFixedSize = true

    protected var emptyView: View? = null
        get() {
            if (field == null) {
                field = rootView?.getEmptyView()
            }
            return field
        }

    protected var loadingView: View? = null
        get() {
            if (field == null) {
                field = rootView?.getLoadingView()
            }
            return field
        }

    protected var errorView: View? = null
        get() {
            if (field == null) {
                field = rootView?.getErrorView()
            }
            return field
        }

    /**
     * 使用 2017 Google IO 发布的 Architecture Components 中的 Lifecycles 的新特性 (此特性已被加入 Support library)
     * 使 {@code Presenter} 可以与 {@link SupportActivity} 和 {@link Fragment} 的部分生命周期绑定
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onInitAdapter() {
        val view = rootView ?: return
        val adapterIsInitialized = adapterWrapper != null
        if (adapterIsInitialized) {
            val recyclerView = view.getRecyclerView()
            if (recyclerView.adapter == adapterWrapper) {
                return
            }
        }
        val destroyClear = if (adapterIsInitialized) destroyViewClearData else true
        initRecyclerView()
        isLoading = false
        if (autoLoad && destroyClear) {
            loadData(true)
        }
    }

    protected open fun initRecyclerView() {
        val view = rootView ?: return
        val layoutManager = view.getLayoutManager()
        val recyclerView = view.getRecyclerView()
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(hasFixedSize)
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapterWrapper = RcvAdapterWrapper(createAdapter(), layoutManager)
        adapterWrapper?.headerView = view.getHeaderView()
        adapterWrapper?.footerView = view.getFooterView()
        recyclerView.adapter = adapterWrapper
        val refreshLayout = view.getRefreshLayout()
        if (refreshLayout != null) {
            refreshLayout.setEnableAutoLoadMore(true) // 是否启用列表惯性滑动到底部时自动加载更多
            refreshLayout.setEnableScrollContentWhenLoaded(true) // 是否在加载完成时滚动列表显示新的内容
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false) // 设置在内容不满一页的时候，是否可以上拉加载更多
            //            refreshLayout.setDisableContentWhenLoading(true) //设置是否开启在加载时候禁止操作内容视图
            //            refreshLayout.setDisableContentWhenRefresh(true) //设置是否开启在加载时候禁止操作内容视图
            //            refreshLayout.setEnableFooterFollowWhenLoadFinished(true) //是否在全部加载结束之后Footer跟随内容
            //            refreshLayout.setEnableLoadMore(false) //设置是否启用上拉加载更多
            refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (rootView == null) return
                    if (hasMore) {
                        loadData(false)
                    } else {
                        refreshLayout.setNoMoreData(!hasMore)
                    }
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    if (rootView == null) return
                    loadData(true)
                }
            })
        } else {
            recyclerView.addOnScrollListener(object : OnRcvScrollListener(3) {
                override fun onBottom() {
                    if (rootView == null) return
                    if (hasMore) {
                        loadData(false)
                    }
                }
            })
        }
    }

    @Synchronized
    open fun loadData(pullToRefresh: Boolean) {
        if (rootView == null) return
        if (isLoading) {
            rootView?.getRefreshLayout()?.let {
                if (pullToRefresh) {
                    it.finishRefresh(0, true, !hasMore)
                } else {
                    it.finishLoadMore(0, true, !hasMore)
                }
            }
            return
        }
        var dataSize = dataList.size - placeholderSize
        if (dataSize < 0) {
            dataSize = 0
        }
        val no = when {
            pullToRefresh -> 0
            dataSize % pageLimit != 0 -> dataSize / pageLimit + 1
            else -> dataSize / pageLimit
        }
        currentPage = no + 1
        isLoading = true
        isClearList = pullToRefresh
        if (currentPage == 1 && dataList.isEmpty()) {
            showLoading()
        }
        requestData(pullToRefresh)
    }

    /**
     * requestData方法请求数据成功后将数据集传入本方法进行刷新列表
     */
    @JvmOverloads
    @Synchronized
    protected open fun onDataSuccess(newData: List<T>?, isError: Boolean = false) {
        val view = rootView ?: return
        val adapter = adapterWrapper ?: return
        if (!isError) {
            hasMore = checkLoadMore(newData)
        }
        isLoading = false
        val refreshLayout = view.getRefreshLayout()
        if (isClearList) {
            dataList.clear()
            adapter.notifyDataSetChanged()
            refreshLayout?.finishRefresh(!isError)
        } else {
            refreshLayout?.finishLoadMore(!isError)
        }
        refreshLayout?.setNoMoreData(!hasMore)
        if (null != newData && newData.isNotEmpty()) {
            val positionStart = dataList.size
            dataList.addAll(newData)
            adapter.notifyItemRangeInserted(positionStart + adapter.headerCount, newData.size)
        }
        if (dataList.isEmpty()) {
            showEmpty()
        }
    }

    protected open fun showEmpty() {
        if (adapterWrapper == null || rootView?.getRecyclerView() == null) return
        if (adapterWrapper?.emptyView != null && adapterWrapper?.emptyView == emptyView) return
        adapterWrapper?.setEmptyView(emptyView, rootView?.getRecyclerView())
    }

    fun showError() {
        onDataSuccess(null, true)
        if (adapterWrapper == null || rootView?.getRecyclerView() == null) return
        if (adapterWrapper?.emptyView != null && adapterWrapper?.emptyView == errorView) return
        if (errorView == null) {
            showEmpty()
        } else {
            adapterWrapper?.setEmptyView(
                errorView,
                rootView?.getRecyclerView(),
                RcvAdapterWrapper.TYPE_ERROR
            )
        }
    }

    protected fun showLoading() {
        if (adapterWrapper == null || rootView?.getRecyclerView() == null) return
        if (adapterWrapper?.emptyView != null && adapterWrapper?.emptyView == loadingView) return
        adapterWrapper?.setEmptyView(
            loadingView,
            rootView?.getRecyclerView(),
            RcvAdapterWrapper.TYPE_LOADING
        )
    }

    /**
     * 判断是否能够加载下一页
     *
     * @return true：有下一页  false：没有下一页
     */
    protected open fun checkLoadMore(newData: List<T>?): Boolean {
        val limit = if (isClearList) {
            pageLimit + placeholderSize
        } else {
            pageLimit
        }
        return newData != null && newData.size >= limit
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoading = false
        dataList.clear()
        adapterWrapper?.let {
            try {
                it.notifyDataSetChanged()
                it.removeFooterView()
                it.removeHeaderView()
                it.setEmptyView(null, null)
                it.layoutManager = null
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        adapterWrapper = null
        emptyView = null
        loadingView = null
        errorView = null
    }

    /**
     * 在 Fragment 中使用时绑定本生命周期，防止在 ViewPager 中使用导致内存泄露
     */
    open fun onDestroyView() {
        isLoading = false
        rootView?.getRecyclerView()?.let {
            it.adapter = null
        }
        if (destroyViewClearData) {
            dataList.clear()
        }
        adapterWrapper?.let {
            try {
                if (destroyViewClearData) {
                    it.notifyDataSetChanged()
                } else {
                    (it.wrappedAdapter as? CommonRcvAdapter<*>)?.apply {
                        data = null
                        notifyDataSetChanged()
                    }
                }
                it.removeFooterView()
                it.removeHeaderView()
                it.setEmptyView(null, null)
                it.layoutManager = null
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected abstract fun createAdapter(): RecyclerView.Adapter<RcvAdapterItem<T>>

    /**
     * 请求列表数据
     * @param pullToRefresh 是否下拉刷新
     */
    protected abstract fun requestData(pullToRefresh: Boolean)
}
