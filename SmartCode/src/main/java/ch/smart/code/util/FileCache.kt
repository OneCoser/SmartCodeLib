package ch.smart.code.util

import android.os.Environment
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils
import io.reactivex.Observable
import online.daoshang.util.isNotNullOrBlank
import online.daoshang.util.rx.SimpleObserver
import online.daoshang.util.rx.toIoAndMain
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

object FileCache {

    /**
     * 获取SD卡上的私有目录，这里的文件会随着App卸载而被删除
     * SD卡写权限：
     * API < 19：需要申请
     * API >= 19：不需要申请
     * 文件目录：
     * Context.getExternalFilesDir() 绝对路径：SDCard/Android/data/应用包名/files/
     * 缓存目录：
     * Context.getExternalCacheDir()  绝对路径：SDCard/Android/data/应用包名/cache/
     *
     * 获取SD卡上的公有目录，APP卸载不会删除文件，需要SD卡写权限
     * Environment.getExternalStoragePublicDirectory()
     */

    // 缓存目录
    private const val CACHE = "cache"

    // MMKV的缓存目录
    private const val MMKV = "mmkv"

    // 图片文件缓存路径
    private const val IMAGE = "image"

    // 下载目录
    private const val DOWNLOAD = "download"

    // 临时缓存文件
    private const val TEMP = "temp"

    // 媒体文件缓存目录
    private const val MEDIA = "media"

    // WebView 缓存目录
    private const val WEB = "web"

    // 日志文件存储目录
    private const val LOG = "log"

    /**
     * 获取 cache 缓存目录
     * @param uniqueName 需要获取的目录名
     */
    private fun getRootDir(uniqueName: String? = null): File? {
        try {
            val app = Utils.getApp()
            val file =
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                    app.externalCacheDir ?: app.cacheDir
                } else {
                    app.cacheDir
                }
            if (uniqueName.isNullOrBlank()) return file
            val uniqueFile = File(file, uniqueName)
            val existsDir = FileUtils.createOrExistsDir(uniqueFile)
            if (!existsDir) {
                Timber.e("创建文件夹失败 path：%s", uniqueName)
                val file = File(Utils.getApp().cacheDir, uniqueName)
                val mkdirs = FileUtils.createOrExistsDir(file)
                return if (mkdirs) file else null
            }
            return uniqueFile
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    fun getCacheDir(uniqueName: String? = null): File? {
        try {
            val file = getRootDir(CACHE)
            if (uniqueName.isNullOrBlank()) return file
            val uniqueFile = File(file, uniqueName)
            val existsDir = FileUtils.createOrExistsDir(uniqueFile)
            if (!existsDir) {
                Timber.e("创建文件夹失败 path：%s", uniqueName)
            }
            return uniqueFile
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    fun getTempDir(): File? {
        return getCacheDir(TEMP)
    }

    fun getLogDir(): File? {
        return getCacheDir(LOG)
    }

    fun getDownloadDir(): File? {
        return getCacheDir(DOWNLOAD)
    }

    fun getImageDir(): File? {
        return getCacheDir(IMAGE)
    }

    fun getMediaDir(): File? {
        return getCacheDir(MEDIA)
    }

    fun getWebDir(): File? {
        return getCacheDir(WEB)
    }

    fun getMMKVDir(): File? {
        return getRootDir(MMKV)
    }

    fun getSuffix(path: String, defSuffix: String? = null): String? {
        try {
            if (path.contains(".")) {
                val index = path.lastIndexOf(".") + 1
                val check1 = path.lastIndexOf("?")
                if (check1 >= 0 && index < check1) {
                    return path.substring(index, check1)
                }
                val check2 = path.lastIndexOf("&")
                if (check2 >= 0 && index < check2) {
                    return path.substring(index, check2)
                }
                return path.substring(index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defSuffix
    }

    fun getUrlFile(
        url: String,
        dir: File? = getDownloadDir(),
        tag: String? = null,
        defSuffix: String? = null
    ): File? {
        return if (dir?.exists() == true) File(
            dir, String.format(
                "%s_%s.%s",
                if (tag.isNotNullOrBlank()) tag else "",
                EncryptUtils.encryptMD5ToString(url).toUpperCase(),
                getSuffix(url, defSuffix = defSuffix)
            )
        ) else null
    }

    @JvmOverloads
    fun clear(uniqueName: String? = null, endAction: (() -> Unit)? = null) {
        val file = getCacheDir(uniqueName = uniqueName)
        if (file == null || !file.exists()) {
            endAction?.invoke()
            return
        }
        Observable.just(file).map {
            FileUtils.delete(file)
            true
        }.delay(1, TimeUnit.SECONDS).toIoAndMain()
            .doOnSubscribe {
                showLoading(ActivityUtils.getTopActivity(), cancelable = false)
            }
            .doFinally {
                dismissLoading()
            }
            .subscribe(object : SimpleObserver<Boolean>() {
                override fun onNext(t: Boolean) {
                    endAction?.invoke()
                }
            })
    }

}
