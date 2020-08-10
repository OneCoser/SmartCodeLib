package ch.smart.code.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ch.smart.code.util.click
import ch.smart.code.view.UIStatusView
import com.blankj.utilcode.util.Utils
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import online.daoshang.util.LayoutManagerFactory

interface SCListView : SCBaseView {

    fun getRecyclerView(): RecyclerView

    @JvmDefault
    fun getLayoutManager(): RecyclerView.LayoutManager {
        return LayoutManagerFactory.getLinearLayoutManager(getCurContext() ?: Utils.getApp())
    }

    @JvmDefault
    fun getRefreshLayout(): SmartRefreshLayout? {
        return null
    }

    @JvmDefault
    fun getLoadingView(): View? {
        val context = getCurContext()
        return if (context != null) UIStatusView.createLoading(context) else null
    }

    @JvmDefault
    fun getEmptyView(): View? {
        val context = getCurContext()
        return if (context != null) {
            UIStatusView.createEmpty(context).apply {
                this.click{
                    onEmptyClick()
                }
            }
        } else {
            null
        }
    }

    @JvmDefault
    fun getErrorView(): View? {
        val context = getCurContext()
        return if (context != null) {
            UIStatusView.createError(context).apply {
                this.click{
                    onErrorClick()
                }
            }
        } else {
            null
        }
    }

    @JvmDefault
    fun getHeaderView(): View? {
        return null
    }

    @JvmDefault
    fun getFooterView(): View? {
        return null
    }

    @JvmDefault
    fun onErrorClick() {
        getRefreshLayout()?.autoRefresh()
    }

    @JvmDefault
    fun onEmptyClick() {
    }
}
