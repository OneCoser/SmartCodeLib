package ch.smart.code.imageloader;

import android.app.ActivityManager;
import android.os.Build;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;

public class SCBitmapMemoryCacheSupplier implements Supplier<MemoryCacheParams> {
    private static final int MAX_CACHE_ENTRIES = 256;
    private static final int MAX_CACHE_ENTRIES_LOLLIPOP = 128;
    private static final int MAX_EVICTION_QUEUE_SIZE = Integer.MAX_VALUE;
    private static final int MAX_EVICTION_QUEUE_ENTRIES = Integer.MAX_VALUE;
    private static final int MAX_CACHE_ENTRY_SIZE = Integer.MAX_VALUE;
    
    private final ActivityManager mActivityManager;
    
    
    public SCBitmapMemoryCacheSupplier(ActivityManager activityManager) {
        mActivityManager = activityManager;
    }
    
    
    @Override
    public MemoryCacheParams get() {
        int maxCacheSize = getMaxCacheSize();
        int maxCacheEntries = MAX_CACHE_ENTRIES;
        int maxEvictionQueueEntries = MAX_EVICTION_QUEUE_ENTRIES;
        int maxEvictionQueueSize = MAX_EVICTION_QUEUE_SIZE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            maxCacheEntries = MAX_CACHE_ENTRIES_LOLLIPOP;
            maxEvictionQueueEntries = 128;
            maxEvictionQueueSize = maxCacheSize / 4;
        }
        
        return new MemoryCacheParams(
            maxCacheSize, //内存缓存的最大Size,
            maxCacheEntries,//缓存的最大条目，应该就是缓存图片的最大数目
            maxEvictionQueueSize,//驱逐队列的Size
            maxEvictionQueueEntries,//驱逐队列的数目
            MAX_CACHE_ENTRY_SIZE//单个缓存条目的最大大小
        );
    }
    
    
    private int getMaxCacheSize() {
        final int maxMemory = Math.min(mActivityManager.getMemoryClass() *
            ByteConstants.MB, Integer.MAX_VALUE);
        if (maxMemory < 32 * ByteConstants.MB) {
            return 4 * ByteConstants.MB;
        } else if (maxMemory < 64 * ByteConstants.MB) {
            return 6 * ByteConstants.MB;
        } else {
            return maxMemory / 5;
        }
    }
}
