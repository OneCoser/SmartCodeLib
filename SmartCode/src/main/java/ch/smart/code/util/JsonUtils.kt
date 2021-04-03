package ch.smart.code.util

import com.blankj.utilcode.util.FileUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.victoralbertos.jolyglot.GsonSpeaker
import io.victoralbertos.jolyglot.JolyglotGenerics
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.lang.reflect.Type

/**
 * JSON 解析工具包装对象
 */
val json: JolyglotGenerics by lazy { GsonSpeaker(GsonBuilder().setLenient().create()) }


/**
 * 校验文件是否为正常的 JSON 字符串
 */
fun File.isJsonValid(): Boolean {
    if (FileUtils.getFileLength(this.absolutePath) < 2) return false
    return try {
        this.reader().use {
            JsonParser().parse(it)
            true
        }
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}


/**
 * 校验字符串是否为正常的 JSON 字符串
 */
fun String?.isJsonValid(): Boolean {
    if (isNullOrBlank()) return false
    return try {
        JsonParser().parse(this)
        true
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}

/**
 * 安全的Json转换
 */
fun Any?.toJSONObject(): JSONObject? {
    val data = this ?: return null
    return try {
        when (data) {
            is JSONObject -> data
            is String -> JSONObject(data)
            else -> JSONObject(safeToJson(data) ?: return null)
        }
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

/**
 * 安全的Json转换
 */
fun safeToJson(data: Any?): String? {
    if (data == null) return null
    return try {
        json.toJson(data)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

/**
 * 安全的Json转换
 */
fun <T> safeFromJson(jsonStr: String?, classOfT: Class<T>): T? {
    if (jsonStr.isNullOrBlank()) return null
    return try {
        json.fromJson(jsonStr, classOfT)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

/**
 * 安全的Json转换
 */
fun <T> safeFromJson(jsonStr: String?, type: Type): T? {
    if (jsonStr.isNullOrBlank()) return null
    return try {
        json.fromJson(jsonStr, type)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}