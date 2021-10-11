package ch.smart.code.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import ch.smart.code.R
import ch.smart.code.dialog.MsgAlert
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import timber.log.Timber
import java.util.ArrayList

/**
 * 类描述：权限请求相关操作
 */

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
    Permission.CAMERA,
    Permission.WRITE_EXTERNAL_STORAGE,
    Permission.READ_EXTERNAL_STORAGE,
    grantedAllAction = true,
    granted = granted,
    deniedAlert = true
)

//请求内存卡读取权限
inline fun requestStorage(context: Context, crossinline granted: ((t: List<String>) -> Unit)) =
    requestPermission(
        context,
        Permission.WRITE_EXTERNAL_STORAGE,
        Permission.READ_EXTERNAL_STORAGE,
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
