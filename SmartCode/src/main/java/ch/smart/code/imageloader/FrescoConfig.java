package ch.smart.code.imageloader;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.File;

import info.liujun.image.LJCacheKeyFactory;
import okhttp3.OkHttpClient;

public class FrescoConfig {
    
    //小图所放路径的文件夹名
    private static final String IMAGE_PIPELINE_SMALL_CACHE_DIR = "image_cache_small";
    //默认图所放路径的文件夹名
    private static final String IMAGE_PIPELINE_CACHE_DIR = "image_cache";
    
    //默认图极低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 8 * ByteConstants.MB;
    //默认图低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_LOW_SIZE = 20 * ByteConstants.MB;
    //默认图磁盘缓存的最大值
    private static final int MAX_DISK_CACHE_SIZE = 200 * ByteConstants.MB;
    
    
    private static SCMemoryTrimmableRegistry trimmableRegistry;
    
    private FrescoConfig() {
    }
    
    public static ImagePipelineConfig.Builder getConfigBuilder(Context context, File baseDirectoryPath, OkHttpClient okHttpClient) {
        DiskCacheConfig smallDiskCacheConfig = DiskCacheConfig
            .newBuilder(context).setBaseDirectoryPath(baseDirectoryPath)
            .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)
            .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
            .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
            .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)
            .build();
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
            //缓存图片基路径
            .setBaseDirectoryPath(baseDirectoryPath)
            //文件夹名
            .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
            //默认缓存的最大大小。
            .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
            //缓存的最大大小,当设备磁盘空间低时。
            .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
            //缓存的最大大小,当设备磁盘空间极低时。
            .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)
            .build();
        OkHttpNetworkFetcher networkFetcher = new OkHttpNetworkFetcher(okHttpClient);
        trimmableRegistry = new SCMemoryTrimmableRegistry();
        
        return ImagePipelineConfig.newBuilder(context)
            .setDownsampleEnabled(true)
            .setMemoryTrimmableRegistry(trimmableRegistry)
            //自定的网络层配置：如OkHttp，Volley
            .setNetworkFetcher(networkFetcher)
            //缓存Key工厂
            .setCacheKeyFactory(LJCacheKeyFactory.getInstance())
            //内存缓存配置（一级缓存，已解码的图片）
            .setBitmapMemoryCacheParamsSupplier(new SCBitmapMemoryCacheSupplier((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)))
            // 磁盘缓存配置（总，三级缓存）
            .setMainDiskCacheConfig(diskCacheConfig)
            //磁盘缓存配置（小图片，可选～三级缓存的小图优化缓存）
            .setSmallImageDiskCacheConfig(smallDiskCacheConfig);
    }
    
    public static void onTrimMemory(int level) {
        if (trimmableRegistry != null) {
            switch (level) {
                //内存不足，并且该进程的UI已经不可见了。
                case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                    //内存不足，并且该进程是后台进程。
                case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                    trimmableRegistry.onTrimMemory(MemoryTrimType.OnCloseToDalvikHeapLimit);
                    break;
                //内存不足，并且该进程在后台进程列表的中部。
                case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                    //内存不足，并且该进程在后台进程列表最后一个，马上就要被清理
                case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                    trimmableRegistry.onTrimMemory(MemoryTrimType.OnAppBackgrounded);
                    onLowMemory();
                    break;
                //内存不足(后台进程不足3个)，并且该进程优先级比较高，需要清理内存
                case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                    //内存不足(后台进程不足5个)，并且该进程优先级比较高，需要清理内存
                case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                    //内存不足(后台进程超过5个)，并且该进程优先级比较高，需要清理内存
                case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                    trimmableRegistry
                        .onTrimMemory(MemoryTrimType.OnSystemLowMemoryWhileAppInForeground);
                    break;
                default:
                    break;
            }
        }
    }
    
    public static void onLowMemory() {
        if (Fresco.hasBeenInitialized()) {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.clearMemoryCaches();
        }
    }
}
