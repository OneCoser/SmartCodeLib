package ch.smart.code.network

import android.net.ParseException
import ch.smart.code.util.showErrorToast
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DefaultErrorHandle : ResponseErrorHandle {

    override fun errorHandle(throwable: Throwable, showMsg: Boolean): Boolean {
        val msg = if (throwable is HttpException) {
            when (val code = throwable.code()) {
                500 -> "服务器发生错误"
                404 -> "请求地址不存在"
                403 -> "请求被服务器拒绝"
                307 -> "请求被重定向到其他页面"
                else -> "服务器异常($code)"
            }
        } else if (throwable is ApiException) {
            String.format(
                "%s(%s)",
                if (throwable.msg.isNullOrBlank()) "接口异常" else throwable.msg,
                throwable.code
            )
        } else if (throwable is JSONException
            || throwable is JsonIOException
            || throwable is JsonParseException
            || throwable is ParseException
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
        if (showMsg) {
            showErrorToast(msg)
        }
        Timber.e(throwable)
        return true
    }
}
