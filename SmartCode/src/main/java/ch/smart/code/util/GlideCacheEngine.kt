package ch.smart.code.util

import android.content.Context
import com.bumptech.glide.Glide
import com.luck.picture.lib.engine.CacheResourcesEngine
import timber.log.Timber

class GlideCacheEngine : CacheResourcesEngine {
    
    override fun onCachePath(context: Context, url: String): String {
        try {
            return Glide.with(context).downloadOnly().load(url).submit().get().absolutePath
        } catch (e: Exception) {
            Timber.e(e)
        }
        return ""
    }
    
    companion object {
        private var instance: GlideCacheEngine? = null
        fun getInstance(): GlideCacheEngine? {
            if (null == instance) {
                synchronized(GlideCacheEngine::class.java) {
                    if (null == instance) {
                        instance = GlideCacheEngine()
                    }
                }
            }
            return instance
        }
    }
}