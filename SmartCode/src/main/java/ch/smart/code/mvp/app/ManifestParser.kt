package ch.smart.code.mvp.app

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber
import java.util.*

/**
 * 类描述：AndroidManifest中配置的ConfigModule解析
 */
class ManifestParser(private val context: Context) {

    private fun parseModule(className: String): ConfigModule? {
        val clazz: Class<*> = try {
            Class.forName(className)
        } catch (e: Exception) {
            Timber.e(e)
            null
        } ?: return null
        val module = try {
            clazz.newInstance()
        } catch (e: Exception) {
            Timber.e(e)
            null
        } ?: return null
        if (module !is ConfigModule) {
            return null
        }
        return module
    }

    fun parse(): List<ConfigModule> {
        val modules = ArrayList<ConfigModule>()
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            appInfo.metaData?.keySet()?.forEach { key ->
                if (appInfo.metaData[key] == "ConfigModule") {
                    parseModule(key)?.let { config ->
                        modules.add(config)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return modules
    }
}