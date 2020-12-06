package ch.smart.code.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.smart.code.R
import ch.smart.code.SmartCodeApp
import ch.smart.code.dialog.LoadingDialog
import ch.smart.code.dialog.MsgAlert
import ch.smart.code.view.ContentEditView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.jakewharton.rxbinding3.widget.textChanges
import com.jess.arms.integration.lifecycle.ActivityLifecycleable
import com.jess.arms.integration.lifecycle.FragmentLifecycleable
import com.jess.arms.integration.lifecycle.Lifecycleable
import com.jess.arms.mvp.IView
import com.jess.arms.utils.ArmsUtils
import com.jess.arms.utils.RxLifecycleUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.*
import okhttp3.OkHttpClient
import ch.smart.code.util.rx.SimpleObserver
import ch.smart.code.util.rx.toIoAndMain
import com.jakewharton.rxbinding3.view.clicks
import com.qmuiteam.qmui.alpha.QMUIAlphaButton
import timber.log.Timber
import java.math.BigDecimal
import java.net.Proxy
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@JvmOverloads
fun exitApp(status: Int = 0) {
    //从最上面的 Activity 开始删除
    ActivityUtils.getActivityList()?.reversed()?.forEach {
        it.finish()
    }
    Process.killProcess(Process.myPid())
    exitProcess(status)
}

fun getOkHttpBuilder(): OkHttpClient.Builder {
    val client = ArmsUtils.obtainAppComponentFromContext(Utils.getApp()).okHttpClient()
    val builder = OkHttpClient.Builder()
    if (!SmartCodeApp.DEBUG) {
        builder.proxy(Proxy.NO_PROXY)
    }
    return builder.dispatcher(client.dispatcher())
        .connectionPool(client.connectionPool())
}

/** 绑定生命周期相关 Start */

fun <T> Observable<T>.applySchedulers(view: IView?): Observable<T> {
    if (view == null) return Observable.empty()
    return this.toIoAndMain()
        .doOnSubscribe {
            view.showLoading() //显示进度条
        }
        .doFinally {
            view.hideLoading() //隐藏进度条
        }.bindObservableToDestroyV(view)
}

fun <T> Observable<T>.bindObservableToDestroyV(view: IView?): Observable<T> {
    if (view == null) return Observable.empty()
    return this.compose(bindViewLifecycleToDestroy(view))
}

fun <T> Observable<T>.bindObservableToDestroyL(life: Lifecycleable<*>): Observable<T> {
    return this.compose(bindLifecycleableToDestroy(life))
}

fun <T> Single<T>.bindSingleToDestroyV(view: IView): Single<T> {
    return this.compose(bindViewLifecycleToDestroy(view))
}

fun <T> Single<T>.bindSingleToDestroyL(life: Lifecycleable<*>): Single<T> {
    return this.compose(bindLifecycleableToDestroy(life))
}

fun <T> bindViewLifecycleToDestroy(view: IView): LifecycleTransformer<T>? {
    return when (view) {
        is FragmentLifecycleable -> {
            RxLifecycleUtils.bindUntilEvent(
                view as FragmentLifecycleable,
                FragmentEvent.DESTROY_VIEW
            )
        }
        is ActivityLifecycleable -> {
            RxLifecycleUtils.bindUntilEvent(view as ActivityLifecycleable, ActivityEvent.DESTROY)
        }
        else -> throw IllegalArgumentException("Unknown IView")
    }
}

fun <T> bindLifecycleableToDestroy(life: Lifecycleable<*>): LifecycleTransformer<T>? {
    return when (life) {
        is ActivityLifecycleable -> {
            RxLifecycleUtils.bindUntilEvent(life, ActivityEvent.DESTROY)
        }
        is FragmentLifecycleable -> {
            RxLifecycleUtils.bindUntilEvent(life, FragmentEvent.DESTROY_VIEW)
        }
        else -> throw IllegalArgumentException("Unknown Lifecycleable")
    }
}

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
    context: Context,
    cancelable: Boolean = false,
    canceledOnTouchOutside: Boolean = false
) {
    dismissLoading()
    try {
        loadingDialog = LoadingDialog(context).apply {
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

/** 权限请求相关 Start */

fun sendPermissionResult(list: List<String>, result: Boolean) {
    Permission.transformText(Utils.getApp(), list).forEach { name ->
        Timber.i("sendPermissionResult：%s_%s", name, result)
    }
}

fun hasPermission(context: Context, vararg permissions: String): Boolean {
    return AndPermission.hasPermissions(context, permissions)
}

fun hasPermission(context: Context, vararg groups: Array<String>): Boolean {
    return AndPermission.hasPermissions(context, *groups)
}

//如果没有权限会弹提示框
fun hasPermissionAlert(context: Context, vararg groups: Array<String>): Boolean {
    return hasAlert(context, *groups)
}

private fun hasAlert(context: Context, vararg permissions: Array<String>): Boolean {
    val isFlag = hasPermission(context, *permissions)
    if (!isFlag) {
        val list = mutableListOf<String>()
        permissions.forEach {
            list.addAll(it)
        }
        requestDeniedAlert(context, list)
    }
    return isFlag
}

const val REQ_CODE_PERMISSION: Int = 1010

//请求权限被拒绝后的弹框提示
fun requestDeniedAlert(context: Context, list: List<String>) {
    if (list.isEmpty()) {
        return
    }
    try {
        val names = Permission.transformText(context, list).toString()
        MsgAlert(context).setMsg(context.getString(R.string.public_permission_denied_alert, names))
            .setLeftButton(R.string.public_cancel).setRightButton(
                R.string.public_permission_setting,
                listener = object : MsgAlert.MsgAlertClickListener {
                    override fun onClick(alert: MsgAlert) {
                        alert.cancel()
                        AndPermission.with(context).runtime().setting().start(REQ_CODE_PERMISSION)
                    }
                }).setCanceledOnTouchOutsideS(false).setCancelableS(true).show()
    } catch (e: Exception) {
        Timber.e(e)
    }
}

/**
 * 过滤已被允许与未被允许的权限
 * @param context:Context
 * @param permissions:请求的权限，可多个
 * @param grantedList:已被允许的权限
 * @param deniedList:未被允许的权限
 */
fun filterPermission(
    context: Context,
    vararg permissions: String,
    grantedList: ArrayList<String>?,
    deniedList: ArrayList<String>?
) {
    permissions.forEach {
        if (hasPermission(context, it)) {
            grantedList?.add(it)
        } else {
            deniedList?.add(it)
        }
    }
}

/**
 * 请求权限
 * @param context:Context
 * @param permissions:请求的权限，可多个
 * @param deniedAlert:权限被拒绝后是否弹框提示
 * @param grantedAction:权限允许回调
 * @param deniedAction:权限拒绝回调
 */
fun startRequestPermission(
    context: Context,
    permissions: Array<String>,
    deniedAlert: Boolean,
    grantedAction: ((t: List<String>) -> Unit)?,
    deniedAction: ((t: List<String>) -> Unit)?
) {
    AndPermission.with(context).runtime().permission(permissions)
        .onGranted {
            sendPermissionResult(it, true)
            grantedAction?.invoke(it)
        }
        .onDenied {
            sendPermissionResult(it, false)
            if (deniedAlert) {
                requestDeniedAlert(context, it)
            }
            deniedAction?.invoke(it)
        }
        .start()
}

/**
 * 请求权限
 * @param context:Context
 * @param permissions:请求的权限，可多个
 * @param grantedAllAction:是否当全部请求权限都是被允许状态才回调granted
 * @param granted:权限允许回调
 * @param deniedAlert:权限被拒绝后是否弹框提示
 * @param denied:权限拒绝回调
 */
inline fun requestPermission(
    context: Context,
    vararg permissions: String,
    grantedAllAction: Boolean,
    crossinline granted: ((t: List<String>) -> Unit),
    deniedAlert: Boolean,
    crossinline denied: ((t: List<String>) -> Unit)
) {
    val grantedList = arrayListOf<String>()
    val deniedList = arrayListOf<String>()
    filterPermission(
        context = context,
        permissions = *permissions,
        grantedList = grantedList,
        deniedList = deniedList
    )
    if (deniedList.isNullOrEmpty()) {
        // 当未被允许的权限为空时代表需要请求的权限之前都被允许过了，这时候直接回调granted
        granted(grantedList)
    } else {
        // deniedList是本次真正需要请求的权限，如需要请求3个权限，其中允许了2个拒绝了1个，那onGranted与onDenied都会回调，如果都被允许就只会回调onGranted，反之只会回调onDenied
        // 所以grantedAllAction参数用于控制是否只有全部被允许的时候才回调granted
        val checkSize = if (grantedAllAction) deniedList.size else -1
        startRequestPermission(context, deniedList.toTypedArray(), deniedAlert, {
            if (checkSize < 0 || it.size == checkSize) {
                grantedList.addAll(it)
                granted(grantedList)
            }
        }, {
            denied(it)
        })
    }
}

/**
 * 请求权限
 * @param context:Context
 * @param permissions:请求的权限，可多个
 * @param grantedAllAction:是否当全部请求权限都是被允许状态才回调granted
 * @param granted:权限允许回调
 * @param deniedAlert:权限被拒绝后是否弹框提示
 */
inline fun requestPermission(
    context: Context,
    vararg permissions: String,
    grantedAllAction: Boolean,
    crossinline granted: ((t: List<String>) -> Unit),
    deniedAlert: Boolean
) {
    val grantedList = arrayListOf<String>()
    val deniedList = arrayListOf<String>()
    filterPermission(
        context = context,
        permissions = *permissions,
        grantedList = grantedList,
        deniedList = deniedList
    )
    if (deniedList.isNullOrEmpty()) {
        // 当未被允许的权限为空时代表需要请求的权限之前都被允许过了，这时候直接回调granted
        granted(grantedList)
    } else {
        // deniedList是本次真正需要请求的权限，如需要请求3个权限，其中允许了2个拒绝了1个，那onGranted与onDenied都会回调，如果都被允许就只会回调onGranted，反之只会回调onDenied
        // 所以grantedAllAction参数用于控制是否只有全部被允许的时候才回调granted
        val checkSize = if (grantedAllAction) deniedList.size else -1
        startRequestPermission(context, deniedList.toTypedArray(), deniedAlert, {
            if (checkSize < 0 || it.size == checkSize) {
                grantedList.addAll(it)
                granted(grantedList)
            }
        }, null)
    }
}

/**
 * 请求权限
 * @param context:Context
 * @param permissions:请求的权限，可多个
 * @param deniedAlert:权限被拒绝后是否弹框提示
 */
inline fun requestPermission(
    context: Context,
    vararg permissions: String,
    deniedAlert: Boolean
) {
    val deniedList = arrayListOf<String>()
    filterPermission(
        context = context,
        permissions = *permissions,
        grantedList = null,
        deniedList = deniedList
    )
    if (deniedList.isNotEmpty()) {
        // deniedList是本次真正需要请求的权限，如需要请求3个权限，其中允许了2个拒绝了1个，那onGranted与onDenied都会回调，如果都被允许就只会回调onGranted，反之只会回调onDenied
        startRequestPermission(context, deniedList.toTypedArray(), deniedAlert, null, null)
    }
}

/**
 * 请求权限
 * @param context:Context
 * @param groups:请求的权限组，可多个
 * @param grantedAllAction:是否当全部请求权限都是被允许状态才回调granted
 * @param granted:权限允许回调
 * @param deniedAlert:权限被拒绝后是否弹框提示
 * @param denied:权限拒绝回调
 */
inline fun requestPermission(
    context: Context,
    vararg groups: Array<String>,
    grantedAllAction: Boolean,
    crossinline granted: ((t: List<String>) -> Unit),
    deniedAlert: Boolean,
    crossinline denied: ((t: List<String>) -> Unit)
) = requestPermission(
    context,
    *(arrayListOf<String>().apply { groups.forEach { this@apply.addAll(it) } }.toTypedArray()),
    grantedAllAction = grantedAllAction,
    granted = granted,
    deniedAlert = deniedAlert,
    denied = denied
)

/**
 * 请求权限
 * @param context:Context
 * @param groups:请求的权限组，可多个
 * @param grantedAllAction:是否当全部请求权限都是被允许状态才回调granted
 * @param granted:权限允许回调
 * @param deniedAlert:权限被拒绝后是否弹框提示
 */
inline fun requestPermission(
    context: Context,
    vararg groups: Array<String>,
    grantedAllAction: Boolean,
    crossinline granted: ((t: List<String>) -> Unit),
    deniedAlert: Boolean
) = requestPermission(
    context,
    *(arrayListOf<String>().apply { groups.forEach { this@apply.addAll(it) } }.toTypedArray()),
    grantedAllAction = grantedAllAction,
    granted = granted,
    deniedAlert = deniedAlert
)

/**
 * 请求权限
 * @param context:Context
 * @param groups:请求的权限组，可多个
 * @param deniedAlert:权限被拒绝后是否弹框提示
 */
inline fun requestPermission(
    context: Context,
    vararg groups: Array<String>,
    deniedAlert: Boolean
) = requestPermission(
    context,
    *(arrayListOf<String>().apply { groups.forEach { this@apply.addAll(it) } }.toTypedArray()),
    deniedAlert = deniedAlert
)

//启动&首页时需要请求的权限
inline fun requestForStartup(context: Context, crossinline action: ((t: List<String>) -> Unit)) =
    requestPermission(
        context,
        Permission.READ_PHONE_STATE,
        Permission.WRITE_EXTERNAL_STORAGE,
        Permission.READ_EXTERNAL_STORAGE,
        Permission.ACCESS_COARSE_LOCATION,
        Permission.ACCESS_FINE_LOCATION,
        grantedAllAction = true,
        granted = action,
        deniedAlert = false,
        denied = action
    )

//请求相机&内存卡读取权限
inline fun requestCameraAndStorage(
    context: Context,
    crossinline granted: ((t: List<String>) -> Unit)
) = requestPermission(
    context,
    Permission.Group.CAMERA,
    Permission.Group.STORAGE,
    grantedAllAction = true,
    granted = granted,
    deniedAlert = true
)

//请求内存卡读取权限
inline fun requestStorage(context: Context, crossinline granted: ((t: List<String>) -> Unit)) =
    requestPermission(
        context,
        Permission.Group.STORAGE,
        grantedAllAction = true,
        granted = granted,
        deniedAlert = true
    )

fun canInstallAPK(): Boolean {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Utils.getApp().packageManager.canRequestPackageInstalls()
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
    return true
}

fun openInstallSetting() {
    try {
        val activity = ActivityUtils.getTopActivity()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isFinishing == false) {
            activity.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.packageName)
                )
            )
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun checkInstallAlert(activity: Activity): Boolean {
    if (!canInstallAPK()) {
        MsgAlert(activity)
            .setMsg("请打开'未知来源'应用安装权限,否则无法获取自动更新")
            .setLeftButton(
                R.string.public_cancel,
                listener = object : MsgAlert.MsgAlertClickListener {
                    override fun onClick(alert: MsgAlert) {
                        alert.cancel()
                    }
                })
            .setRightButton("去打开", txtColorId = R.color.public_color_0F7FD6,
                listener = object : MsgAlert.MsgAlertClickListener {
                    override fun onClick(alert: MsgAlert) {
                        alert.cancel()
                        openInstallSetting()
                    }
                }).setCanceledOnTouchOutsideS(false).setCancelableS(false).show()
        return false
    }
    return true
}

/** 资源选择 Start */

@JvmOverloads
fun openImageSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1,
    needCrop: Boolean = false
) {
    PictureSelector.create(activity).openGallery(PictureMimeType.ofImage())
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style).isWeChatStyle(true).isUseCustomCamera(true)
        .isPageStrategy(true).isMaxSelectEnabledMask(true).imageSpanCount(4)
        .isReturnEmpty(false).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .selectionMode(if (maxCount > 1) PictureConfig.MULTIPLE else PictureConfig.SINGLE)
        .isSingleDirectReturn(true)
        .isPreviewImage(true).isCamera(true).isEnableCrop(needCrop).isCompress(true)
        .isOriginalImageControl(true).isGif(false).maxSelectNum(maxCount).minSelectNum(minCount)
        .forResult(listener)
}

fun checkGetPath(media: LocalMedia?): String? {
    if (media?.compressPath.isNotNullOrBlank()) {
        return media?.compressPath
    }
    if (media?.androidQToPath.isNotNullOrBlank()) {
        return media?.androidQToPath
    }
    if (media?.realPath.isNotNullOrBlank()) {
        return media?.realPath
    }
    return null
}

@JvmOverloads
fun openVideoSelect(
    activity: Activity,
    listener: OnResultCallbackListener<LocalMedia>,
    maxCount: Int = 9,
    minCount: Int = 1
) {
    PictureSelector.create(activity).openGallery(PictureMimeType.ofVideo())
        .imageEngine(GlideImageEngine.getInstance())
        .theme(R.style.picture_default_style).isWeChatStyle(true).isUseCustomCamera(true)
        .isPageStrategy(true).isMaxSelectEnabledMask(true).imageSpanCount(4)
        .isReturnEmpty(false).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        .selectionMode(if (maxCount > 1) PictureConfig.MULTIPLE else PictureConfig.SINGLE)
        .isSingleDirectReturn(true)
        .isPreviewVideo(true).isCamera(true).maxVideoSelectNum(maxCount).minVideoSelectNum(minCount)
        .forResult(listener)
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

/** 安全转换相关 Start */

fun String?.safeDouble(default: Double = 0.0): Double {
    try {
        return this?.toDoubleOrNull() ?: default
    } catch (e: Exception) {
        Timber.e(e)
    }
    return default
}

fun String?.safeInt(default: Int = 0): Int {
    try {
        return this?.toIntOrNull() ?: default
    } catch (e: Exception) {
        Timber.e(e)
    }
    return default
}

fun Any?.safeSub(start: Int, end: Int? = null): String? {
    val str = this?.toString() ?: return null
    try {
        if (end == null || end <= start || end > str.length) {
            return str.substring(start)
        }
        return str.substring(start, end)
    } catch (e: Exception) {
        Timber.e(e)
    }
    return null
}

fun String?.safeScale(scale: Int = 4, default: String = "0.0"): BigDecimal {
    return BigDecimal(this ?: default).setScale(scale, BigDecimal.ROUND_DOWN)
}

fun Double?.safeScale(scale: Int = 4, default: Double = 0.0, useNF: Boolean = false): BigDecimal {
    val data = this ?: default
    if (useNF) {
        //去掉科学计数法显示
        try {
            val nf = NumberFormat.getInstance()
            nf.isGroupingUsed = false
            val str = nf.format(data)
            val index = str.indexOf(".") + scale + 1
            return str.safeSub(0, index).safeScale(scale = scale)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    return data.toString().safeScale(scale = scale)
}

fun Long?.safeTimeStr(formatterType: String): String? {
    val time = this ?: return null
    try {
        return SimpleDateFormat(formatterType).format(Date(time))
    } catch (e: Exception) {
        Timber.e(e)
    }
    return time.toString()
}

fun String?.safeTimeMillis(formatterType: String, default: Long = 0): Long {
    if (this.isNullOrEmpty()) return default
    try {
        return SimpleDateFormat(formatterType).parse(this)?.time ?: default
    } catch (e: Exception) {
        Timber.e(e)
    }
    return default
}

fun Long?.safeDurationCN(ismMillis: Boolean = true): String {
    try {
        val time = this ?: 0
        val totalSeconds = if (ismMillis) time / 1000 else time
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        if (hours > 0 && minutes > 0) {
            return String.format("%s小时%s分钟", hours, minutes)
        }
        if (hours > 0) {
            return String.format("%s小时", hours)
        }
        return String.format("%s秒", totalSeconds)
    } catch (e: Exception) {
        Timber.e(e)
    }
    return ""
}

fun Long?.safeDuration(ismMillis: Boolean = true, keepHour: Boolean = true): String {
    try {
        val time = this ?: 0
        val totalSeconds = if (ismMillis) time / 1000 else time
        if (totalSeconds <= 0) {
            return if (keepHour) "00:00:00" else "00:00"
        }
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val stringBuilder = StringBuilder()
        val formatter = Formatter(stringBuilder, Locale.getDefault())
        return when {
            hours > 0 -> formatter.format("%02d:%02d:%02d", hours, minutes, seconds)
            keepHour -> formatter.format("00:%02d:%02d", minutes, seconds)
            else -> formatter.format("%02d:%02d", minutes, seconds)
        }.toString()
    } catch (e: Exception) {
        Timber.e(e)
    }
    return ""
}