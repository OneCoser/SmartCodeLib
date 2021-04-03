package ch.smart.code.util

import android.os.Process
import ch.smart.code.R
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ProcessUtils
import kotlin.system.exitProcess


/**
 * 是否为主进程
 */
fun isMainProcess(): Boolean {
    return ProcessUtils.isMainProcess()
}

/**
 * 双击返回退出程序
 */
private var backExitPressedTime: Long = 0

@JvmOverloads
fun backExitApp(status: Int = 0) {
    val nowTime = System.currentTimeMillis()
    if (nowTime - backExitPressedTime > 2000) {
        showToast(R.string.public_back_pressed_exit)
        backExitPressedTime = nowTime
    } else {
        exitApp(status = status)
    }
}

/**
 * 退出程序
 */
@JvmOverloads
fun exitApp(status: Int = 0) {
    //从最上面的 Activity 开始删除
    ActivityUtils.getActivityList()?.reversed()?.forEach {
        it.finish()
    }
    Process.killProcess(Process.myPid())
    exitProcess(status)
}

/**
 * 生成路由
 */
fun String?.router(): Postcard? {
    if (this.isNullOrBlank()) return null
    return ARouter.getInstance().build(this)
}

/**
 * 发起路由
 */
fun String?.navigation(): Boolean {
    val router = this.router() ?: return false
    router.navigation()
    return true
}
