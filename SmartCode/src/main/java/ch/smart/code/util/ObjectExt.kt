package ch.smart.code.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.Utils
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import timber.log.Timber
import java.io.File
import java.math.BigDecimal
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 类描述：对象操作
 */

/**
 * 如果此可为空的字符序列不为“null”或不为空，则返回“true”。
 */
@OptIn(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}


/**
 * 如果此可为空的字符序列不为“null”或空，或者不由空白字符组成，则返回“true”。
 */
@OptIn(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrBlank != null)
    }
    return !this.isNullOrBlank()
}

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

fun Int.drawable(context: Context = Utils.getApp()): Drawable? {
    return ContextCompat.getDrawable(context, this)
}

fun Int.color(context: Context = Utils.getApp()): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.string(vararg formatArgs: Any?): String {
    return Utils.getApp().getString(this, *formatArgs)
}

/**
 * 文件可用空间是否大于传进来的参数
 * @param sizeMb
 * @return true: 空间足够  false：空间不足
 */
@JvmOverloads
fun File?.isAvailableSpace(sizeMb: Int = 50): Boolean {
    if (this == null) return false
    var isHasSpace = false
    if (!this.exists()) {
        return isHasSpace
    }
    val freeSpace = try {
        this.freeSpace
    } catch (e: Exception) {
        Timber.e(e)
        return isHasSpace
    }
    val availableSpare = freeSpace / MemoryConstants.MB
    //    Timber.d("path: %s availableSpare = %s MB sizeMb: %s", this.absolutePath, availableSpare, sizeMb)
    if (availableSpare > sizeMb) {
        isHasSpace = true
    }
    return isHasSpace
}

/**
 * Uri获取参数
 */
fun Uri.getParams(): MutableMap<String, String> {
    val paramMap = mutableMapOf<String, String>()
    try {
        queryParameterNames?.forEach { key ->
            val data = getQueryParameter(key)
            if (data.isNotNullOrBlank()) {
                paramMap[key] = URLDecoder.decode(data, "UTF-8")
            }
        }
    } catch (ignore: Exception) {
        Timber.e(ignore)
    }
    return paramMap
}

/**
 * Uri替换所有参数
 */
fun Uri.replaceParams(params: Map<String, Any>?): Uri {
    val buildUri = this.buildUpon().clearQuery()
    params?.forEach {
        val value = it.value.toString()
        if (it.key.isNotNullOrBlank() && value.isNotNullOrBlank()) {
            buildUri.appendQueryParameter(it.key, URLEncoder.encode(value, "UTF-8"))
        }
    }
    val newUri = buildUri.build()
    Timber.i("替换参数前：%s\n替换参数后：%s", this.toString(), newUri.toString())
    return newUri
}

/**
 * Uri追加参数
 */
fun Uri.addParams(params: Map<String, Any>?): Uri {
    if (params.isNullOrEmpty()) return this
    val allParam = this.getParams()
    params.forEach {
        allParam[it.key] = it.value.toString()
    }
    return this.replaceParams(allParam)
}

/**
 * Uri替换指定参数
 */
fun Uri.replaceKeyValue(key: String, action: (oldValue: String?) -> String?): Uri {
    val params = this.getParams()
    val oldValue = params[key]
    val newValue = action.invoke(oldValue)
    if ((oldValue.isNullOrBlank() && newValue.isNullOrBlank()) || oldValue == newValue) {
        return this
    }
    params[key] = newValue ?: ""
    return this.replaceParams(params)
}


fun Request?.bodyToString(defaultStr: String? = null): String? {
    return try {
        val body = this?.body ?: return defaultStr
        val buffer = Buffer()
        body.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: Exception) {
        Timber.e(e)
        defaultStr
    }
}

fun Response?.bodyToString(defaultStr: String? = null): String? {
    return try {
        val source = this?.body?.source() ?: return defaultStr
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer.clone()
        buffer.readUtf8()
    } catch (e: Exception) {
        Timber.e(e)
        defaultStr
    }
}