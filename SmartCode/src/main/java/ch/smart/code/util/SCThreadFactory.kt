package ch.smart.code.util

import timber.log.Timber
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class SCThreadFactory(prefix: String, private val priority: Int) : ThreadFactory {

    private val count = AtomicLong()

    private val namePrefix = prefix + "-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-"

    override fun newThread(r: Runnable): Thread {
        val t = object : Thread(r, namePrefix + count.andIncrement) {
            override fun run() {
                try {
                    super.run()
                } catch (t: Throwable) {
                    Timber.e(t, "Request threw uncaught throwable")
                }
            }
        }
        if (t.isDaemon) {
            t.isDaemon = false
        }
        t.priority = priority
        return t
    }

    companion object {

        @JvmStatic
        private val POOL_NUMBER = AtomicInteger(1)

        @JvmStatic
        fun createExecutorService(): ExecutorService {
            return ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                SCThreadFactory("OkHttp", 8)
            )
        }
    }

}