package ch.smart.code.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import ch.smart.code.R
import io.reactivex.Observable
import online.daoshang.util.rx.clicksThrottle

class UIStatusView : FrameLayout {

    private var loadingView: View
    private var hintView: ListEmptyView

    var status: Int = STATUS_DEF
        private set

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        isClickable = true
        val params =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        hintView = createEmpty(context)
        this.addView(hintView, params)
        loadingView = createLoading(context)
        this.addView(loadingView, params)
        showStatus(STATUS_DEF)
    }

    private fun setVis(v: View?, show: Boolean) {
        v?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showStatus(status: Int) {
        this.status = status
        setVis(loadingView, status == STATUS_LOADING)
        setVis(hintView, status == STATUS_ERROR || status == STATUS_EMPTY)
        setVis(this, status != STATUS_DEF)
    }

    fun showDef() {
        showStatus(STATUS_DEF)
    }

    fun showLoading() {
        showStatus(STATUS_LOADING)
    }

    @JvmOverloads
    fun showError(
        msg: String = context.getString(R.string.public_ui_error),
        @DrawableRes icon: Int = R.drawable.public_ic_error
    ) {
        showStatus(STATUS_ERROR)
        hintView.setEmptyText(msg)
        hintView.setEmptyImage(icon)
    }

    @JvmOverloads
    fun showEmpty(
        msg: String = context.getString(R.string.public_ui_null),
        @DrawableRes icon: Int = R.drawable.public_ic_empty
    ) {
        showStatus(STATUS_EMPTY)
        hintView.setEmptyText(msg)
        hintView.setEmptyImage(icon)
    }

    /**
     * 点击事件（默认响应 STATUS_EMPTY 和 STATUS_ERROR 状态的点击事件）
     * @param windowDuration 多长时间响应一次点击事件
     * @param notFilter 不做状态过滤，响应所有状态下的点击事件
     */
    @JvmOverloads
    fun click(windowDuration: Long = 1000, notFilter: Boolean = false): Observable<Unit> {
        val observable = clicksThrottle(windowDuration)
        return if (notFilter) {
            observable
        } else {
            observable.filter { status == STATUS_EMPTY || status == STATUS_ERROR }
        }
    }

    companion object {

        const val STATUS_DEF = 0
        const val STATUS_LOADING = 1
        const val STATUS_ERROR = 2
        const val STATUS_EMPTY = 3

        fun createLoading(context: Context): View {
            return ListLoadingView(context)
        }

        fun createError(context: Context): ListEmptyView {
            val v = ListEmptyView(context)
            v.setEmptyImage(R.drawable.public_ic_error)
            v.setEmptyText(R.string.public_ui_error)
            return v
        }

        fun createEmpty(context: Context): ListEmptyView {
            val v = ListEmptyView(context)
            v.setEmptyText(R.string.public_ui_null)
            return v
        }
    }

}
