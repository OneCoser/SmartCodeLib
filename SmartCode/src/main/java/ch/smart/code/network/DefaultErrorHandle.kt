package ch.smart.code.network

import android.net.ParseException
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 类描述：网络请求出错默认处理
 */
class DefaultErrorHandle : ResponseErrorHandle {

    override fun errorHandle(throwable: Throwable, showMsg: Boolean): Boolean {
        val msg = if (throwable is HttpException) {
            convertStatusCode(throwable)
        } else if (throwable is ApiException) {
            if (throwable.msg.isNullOrBlank()) {
                String.format("接口异常(%s)", throwable.code)
            } else {
                String.format("%s(%s)", throwable.msg, throwable.code)
            }
        } else if (throwable is JSONException ||
            throwable is JsonIOException ||
            throwable is JsonParseException ||
            throwable is ParseException
        ) {
            "数据解析出错"
        } else if (throwable is ConnectException) {
            "网络连接失败"
        } else if (throwable is SocketTimeoutException) {
            "网络连接超时"
        } else if (throwable is UnknownHostException) {
            "网络不可用"
        } else {
            "请求失败"
        }
        if (showMsg && msg.isNotEmpty()) {
            ToastUtils.setGravity(Gravity.CENTER, 0, 0)
            ToastUtils.showShort(msg)
        }
        Timber.e(throwable)
        return true
    }

    private fun convertStatusCode(httpException: HttpException): String {
        return when (val code = httpException.code()) {
            500 -> "服务器发生错误"
            404 -> "请求地址不存在"
            403 -> "请求被服务器拒绝"
            307 -> "请求被重定向到其他页面"
            else -> "服务器异常($code)"
        }
    }
}
