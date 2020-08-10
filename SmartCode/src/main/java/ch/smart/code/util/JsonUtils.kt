package ch.smart.code.util

import com.blankj.utilcode.util.FileUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.victoralbertos.jolyglot.GsonSpeaker
import io.victoralbertos.jolyglot.JolyglotGenerics
import timber.log.Timber
import java.io.File

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