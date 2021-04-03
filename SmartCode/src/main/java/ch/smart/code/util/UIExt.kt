package ch.smart.code.util

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.smart.code.R
import ch.smart.code.dialog.LoadingDialog
import ch.smart.code.util.rx.SimpleObserver
import ch.smart.code.util.rx.toIoAndMain
import ch.smart.code.view.ContentEditView
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.qmuiteam.qmui.alpha.QMUIAlphaButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import io.reactivex.Observable
import io.reactivex.functions.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 类描述：UI相关扩展类
 */

/** Toast相关 Start */

@JvmOverloads
fun showToast(msg: String, @DrawableRes iconId: Int? = null) {
    try {
        if (msg.isNotEmpty()) {
            ToastUtils.setGravity(Gravity.CENTER, 0, 0)
            val v: View = ToastUtils.showCustomLong(R.layout.public_layout_toast)
            val vIcon: ImageView = v.findViewById(R.id.public_toast_icon)
            val vTxt: TextView = v.findViewById(R.id.public_toast_txt)
            if (iconId != null) {
                vIcon.visibility = View.VISIBLE
                vIcon.setImageResource(iconId)
            } else {
                vIcon.visibility = View.GONE
            }
            vTxt.text = msg
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

@JvmOverloads
fun showToast(@StringRes msgId: Int, @DrawableRes iconId: Int? = null) {
    showToast(msg = Utils.getApp().getString(msgId), iconId = iconId)
}

fun showSuccessToast(msg: String) {
    showToast(msg = msg, iconId = R.drawable.public_toast_icon_success)
}

fun showSuccessToast(@StringRes msgId: Int) {
    showSuccessToast(Utils.getApp().getString(msgId))
}

fun showErrorToast(msg: String) {
    showToast(msg = msg, iconId = R.drawable.public_toast_icon_error)
}

fun showErrorToast(@StringRes msgId: Int) {
    showErrorToast(Utils.getApp().getString(msgId))
}

/** Loading相关 Start */

private var loadingDialog: LoadingDialog? = null

@JvmOverloads
@Synchronized
fun showLoading(
    context: Context?,
    cancelable: Boolean = false,
    canceledOnTouchOutside: Boolean = false
) {
    dismissLoading()
    try {
        loadingDialog = LoadingDialog(context ?: return).apply {
            setCancelable(cancelable)
            setCanceledOnTouchOutside(canceledOnTouchOutside)
            show()
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

@Synchronized
fun dismissLoading() {
    loadingDialog?.let {
        try {
            if (it.isShowing) {
                it.dismiss()
            }
            it.cancel()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    loadingDialog = null
}


/** View扩展事件 Start */

@JvmOverloads
fun View.clicksThrottle(windowDuration: Long = 1000): Observable<Unit> =
    this.clicks().throttleFirst(windowDuration, TimeUnit.MILLISECONDS)

inline fun View.click(windowDuration: Long = 1000, crossinline action: () -> Unit) {
    this.clicksThrottle(windowDuration = windowDuration).subscribe(object : SimpleObserver<Unit>() {
        override fun onNext(t: Unit) {
            action.invoke()
        }
    })
}

inline fun QMUIAlphaButton.click(
    alphaPress: Boolean = true,
    alphaDisable: Boolean = true,
    duration: Long = 1000,
    crossinline action: () -> Unit
) {
    this.setChangeAlphaWhenPress(alphaPress)
    this.setChangeAlphaWhenDisable(alphaDisable)
    this.click(windowDuration = duration, action = action)
}

fun QMUIAlphaButton.enabled(
    enabled: Boolean,
    @ColorRes trueColorId: Int,
    @ColorRes falseColorId: Int
) {
    this.isEnabled = enabled
    (this.background as? QMUIRoundButtonDrawable)?.setBgData(
        ColorStateList.valueOf(
            if (enabled) {
                trueColorId.color(context = this.context)
            } else {
                falseColorId.color(context = this.context)
            }
        )
    )
}

fun QMUIAlphaButton.color(@ColorRes txtColor: Int, @ColorRes bgColor: Int) {
    this.setTextColor(txtColor.color(context = this.context))
    (this.background as? QMUIRoundButtonDrawable)?.setBgData(
        ColorStateList.valueOf(bgColor.color(context = this.context))
    )
}

fun WebView.initSetting() {
    settings?.apply {
        javaScriptEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        allowFileAccess = true
        layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        setSupportZoom(true)
        builtInZoomControls = true
        useWideViewPort = true
        setSupportMultipleWindows(true)
        // setLoadWithOverviewMode(true);
        setAppCacheEnabled(true)
        // setDatabaseEnabled(true);
        domStorageEnabled = true
        setGeolocationEnabled(true)
        setAppCacheMaxSize(Long.MAX_VALUE)
        // setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        pluginState = WebSettings.PluginState.ON_DEMAND
        // setRenderPriority(WebSettings.RenderPriority.HIGH);
        cacheMode = WebSettings.LOAD_NO_CACHE
    }
}

fun bindTxtChange(vararg views: ContentEditView): Observable<Boolean> {
    if (views.isNullOrEmpty()) return Observable.just(true).toIoAndMain()
    return bindTxtChange(*(views.mapNotNull { it.contentView?.textChanges() }.toTypedArray()))
}

fun bindTxtChange(vararg views: TextView): Observable<Boolean> {
    if (views.isNullOrEmpty()) return Observable.just(true).toIoAndMain()
    return bindTxtChange(*(views.map { it.textChanges() }.toTypedArray()))
}

fun bindTxtChange(
    vararg txts: Observable<CharSequence>,
    excess: Boolean = false
): Observable<Boolean> {
    if (txts.isNullOrEmpty()) return Observable.just(true).toIoAndMain()
    val observable = when (txts.size) {
        1 -> txts[0].map { checkBindTxt(it) }
        2 -> Observable.combineLatest(
            txts[0], txts[1],
            BiFunction { t1, t2 -> checkBindTxt(t1, t2) })
        3 -> Observable.combineLatest(
            txts[0], txts[1], txts[2],
            Function3 { t1, t2, t3 -> checkBindTxt(t1, t2, t3) })
        4 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3],
            Function4 { t1, t2, t3, t4 -> checkBindTxt(t1, t2, t3, t4) })
        5 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3], txts[4],
            Function5 { t1, t2, t3, t4, t5 -> checkBindTxt(t1, t2, t3, t4, t5) })
        6 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3], txts[4], txts[5],
            Function6 { t1, t2, t3, t4, t5, t6 -> checkBindTxt(t1, t2, t3, t4, t5, t6) })
        7 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3], txts[4], txts[5], txts[6],
            Function7 { t1, t2, t3, t4, t5, t6, t7 -> checkBindTxt(t1, t2, t3, t4, t5, t6, t7) })
        8 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3], txts[4], txts[5], txts[6], txts[7],
            Function8 { t1, t2, t3, t4, t5, t6, t7, t8 ->
                checkBindTxt(
                    t1, t2, t3, t4,
                    t5, t6, t7, t8
                )
            })
        9 -> Observable.combineLatest(
            txts[0], txts[1], txts[2], txts[3], txts[4], txts[5], txts[6], txts[7], txts[8],
            Function9 { t1, t2, t3, t4, t5, t6, t7, t8, t9 ->
                checkBindTxt(
                    t1, t2, t3, t4, t5,
                    t6, t7, t8, t9
                )
            })
        else -> {
            val o1 = bindTxtChange(*(txts.take(9).toTypedArray()), excess = true)
            val o2 = bindTxtChange(*(txts.takeLast(txts.size - 9).toTypedArray()), excess = true)
            Observable.combineLatest(o1, o2, BiFunction { t1, t2 -> t1 && t2 })
        }
    }
    return if (excess) {
        observable
    } else {
        observable.throttleLast(500, TimeUnit.MICROSECONDS).toIoAndMain()
    }
}

fun checkBindTxt(vararg txts: CharSequence): Boolean {
    if (txts.isNullOrEmpty()) return false
    txts.forEach { if (it.isEmpty()) return false }
    return true
}

fun checkBindTxt(vararg txts: String?): Boolean {
    if (txts.isNullOrEmpty()) return false
    txts.forEach { if (it.isNullOrEmpty()) return false }
    return true
}