package ch.smart.code.network

import ch.smart.code.util.isProduction
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.net.Proxy
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object OkHttpFactory {

    /**
     * 所有的网络请求使用本线程池
     * 线程池大小设置参考：https://www.jianshu.com/p/ad4ae37c7d4b
     */
    val executor: ExecutorService by lazy {
        ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            SynchronousQueue<Runnable>(),
            HttpThreadFactory("OkHttp", 8)
        )
    }

    /**
     * 公共OkHttpClient，提供给三方图片库或下载库使用
     */
    val commonHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .dispatcher(Dispatcher(executor))
        if (isProduction()) {
            builder.proxy(Proxy.NO_PROXY)
        }
        builder.build()
    }

    /**
     * 复用公共OkHttpClient的一些配置
     */
    fun copyCommonHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        if (isProduction()) {
            builder.proxy(Proxy.NO_PROXY)
        }
        return builder.dispatcher(commonHttpClient.dispatcher())
            .connectionPool(commonHttpClient.connectionPool())
    }
}