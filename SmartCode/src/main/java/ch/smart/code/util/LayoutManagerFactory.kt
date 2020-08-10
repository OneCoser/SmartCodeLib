package ch.smart.code.util

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

object LayoutManagerFactory {

    /**
     * 获取LinearLayoutManager
     *
     * @param isVertical    是否为垂直模式
     * @param reverseLayout 是否逆向布局（聊天界面数据从下往上就是逆向布局）
     */
    @JvmOverloads
    @JvmStatic
    fun getLinearLayoutManager(
        context: Context,
        isVertical: Boolean = true,
        reverseLayout: Boolean = false
    ): LinearLayoutManager {
        return if (isVertical) {
            SCLinearLayoutManager(context, RecyclerView.VERTICAL, reverseLayout)
        } else {
            SCLinearLayoutManager(context, RecyclerView.HORIZONTAL, reverseLayout)
        }
    }


    /**
     * 获取GridLayoutManager
     *
     * @param spanCount     如果是垂直方向,为列数。如果方向是水平的,为行数。
     * @param isVertical    是否为垂直模式
     * @param reverseLayout 是否逆向布局（聊天界面数据从下往上就是逆向布局）
     */
    @JvmOverloads
    @JvmStatic
    fun getGridLayoutManager(
        context: Context,
        spanCount: Int,
        isVertical: Boolean = true,
        reverseLayout: Boolean = false
    ): GridLayoutManager {
        return if (isVertical) {
            SCGridLayoutManager(context, spanCount, RecyclerView.VERTICAL, reverseLayout)
        } else {
            SCGridLayoutManager(context, spanCount, RecyclerView.HORIZONTAL, reverseLayout)
        }
    }


    /**
     * 获取StaggeredGridLayoutManager(瀑布流)
     *
     * @param spanCount  如果是垂直方向,为列数。如果方向是水平的,为行数。
     * @param isVertical 是否为垂直模式
     */
    @JvmOverloads
    @JvmStatic
    fun getStaggeredGridLayoutManager(
        spanCount: Int,
        isVertical: Boolean = true
    ): StaggeredGridLayoutManager {
        return if (isVertical) {
            StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
        } else {
            StaggeredGridLayoutManager(spanCount, RecyclerView.HORIZONTAL)
        }
    }
}
