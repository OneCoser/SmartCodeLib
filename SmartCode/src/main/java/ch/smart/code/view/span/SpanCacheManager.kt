package ch.smart.code.view.span

import android.app.ActivityManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.collection.LruCache
import com.blankj.utilcode.util.Utils
import com.facebook.common.util.ByteConstants
import com.facebook.imageutils.BitmapUtil
import timber.log.Timber
import kotlin.math.min

object SpanCacheManager {
    
    //图标临时缓存，可以避免复用频繁的地方重复创建Bitmap
    private val iconCache: LruCache<String, BitmapDrawable> by lazy {
        var maxSize = 10 * ByteConstants.MB
        try {
            val memoryClass = (Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE)
                    as? ActivityManager)?.memoryClass ?: 1
            val maxMemory = min(memoryClass * ByteConstants.MB, Integer.MAX_VALUE)
            maxSize = when {
                maxMemory < 32 * ByteConstants.MB -> 4 * ByteConstants.MB
                maxMemory < 64 * ByteConstants.MB -> 6 * ByteConstants.MB
                else -> maxMemory / 5
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        object : LruCache<String, BitmapDrawable>(maxSize) {
            override fun sizeOf(key: String, value: BitmapDrawable): Int {
                try {
                    return BitmapUtil.getSizeInBytes(value.bitmap)
                } catch (e: Exception) {
                    Timber.e(e)
                }
                return super.sizeOf(key, value)
            }
        }
    }
    
    fun clear() {
        try {
            Timber.i("Clear：%s，MaxSize：%s", iconCache.size(), iconCache.maxSize())
            iconCache.evictAll()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    
    fun getIcon(key: String): BitmapDrawable? {
        return iconCache[key]
    }
    
    fun putIcon(key: String, icon: BitmapDrawable) {
        iconCache.put(key, icon)
    }
    
}