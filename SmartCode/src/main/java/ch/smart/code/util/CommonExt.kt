package ch.smart.code.util

import android.os.Process
import ch.smart.code.R
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
